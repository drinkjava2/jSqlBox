/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jentitynet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ClassCacheUtils;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;

/**
 * EntityNet is a entity net
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {
	public static final String KeySep = "_";

	private Boolean weaved = false;

	/**
	 * configModels has entityClass, it means it know which entity be mapped, but
	 * the shortage is it often have no alias name be set
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the net */
	private Map<String, Object> body = new HashMap<String, Object>();

	public EntityNet() {
	}

	public EntityNet(List<Map<String, Object>> listMap, TableModel... models) {
		joinList(listMap, models);
	}

	private void checkModelHasEntityClassAndAlias(TableModel... models) {
		if (models != null && models.length > 0)// Join models
			for (TableModel tb : models) {
				if (tb.getEntityClass() == null)
					throw new SqlBoxException("TableModel entityClass not set for table '" + tb.getTableName() + "'");
				if (StrUtils.isEmpty(tb.getAlias()))
					throw new SqlBoxException("TableModel alias not set for table '" + tb.getTableName() + "'");
			}

	}

	public EntityNet joinList(List<Map<String, Object>> listMap, TableModel... models) {
		checkModelHasEntityClassAndAlias(models);
		weaved = false;
		if (listMap == null)
			throw new EntityNetException("Can not join null listMap");
		if (models != null && models.length > 0)// Join models
			for (TableModel tb : models) {
				if (tb.getEntityClass() == null) {
					if (StrUtils.isEmpty(tb.getAlias()))
						throw new SqlBoxException("TableModel bot entityClass and alias are not set");
				} else {
					this.configModels.put(tb.getEntityClass(), tb);
				}
			}
		for (Map<String, Object> map : listMap) {// join map list
			assemblyOneRowToEntities(map);
		}
		return this;
	}

	private Object createNewEntity(Class<?> entityClass) {
		try {
			return entityClass.newInstance();
		} catch (Exception e) {
			throw new EntityNetException(e);
		}
	}

	/**
	 * Assembly Map List data to Entities, according current configModels
	 */
	public Object[] assemblyOneRowToEntities(Map<String, Object> oneRow) {
		List<Object> resultList = new ArrayList<Object>();
		for (TableModel model : configModels.values()) { 
			Object entity = null;
			String alias = model.getAlias();
			if (StrUtils.isEmpty(alias))
				throw new EntityNetException("No alias found for table '" + model.getTableName() + "'");

			for (Entry<String, Object> row : oneRow.entrySet()) { // u_userName
				for (ColumnModel col : model.getColumns()) {
					if (row.getKey().equalsIgnoreCase(alias + "_" + col.getColumnName())) {
						if (entity == null)
							entity = createNewEntity(model.getEntityClass());
						SqlBoxException.assureNotEmpty(col.getEntityField(),
								"EntityField not found for column '" + col.getColumnName() + "'");
						ClassCacheUtils.writeValueToBeanField(entity, col.getEntityField(), row.getValue());
					}
				}
			}
			if (entity != null)
				addEntity(entity);
		}
		return resultList.toArray(new Object[resultList.size()]);
	}

	/**
	 * Transfer PKey values to a entityID String, format:
	 * tablename_id1value_id2value
	 */
	public static String transferPKeyToString(SqlBox box, Object entity) {
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : box.getTableModel().getColumns()) {
			if (col.getPkey()) {
				SqlBoxException.assureNotEmpty(col.getEntityField(),
						"EntityField not found for FKey column '" + col.getColumnName() + "'");
				sb.append(KeySep).append(ClassCacheUtils.readValueFromBeanField(entity, col.getEntityField()));
			}
		}
		if (sb.length() == 0)
			throw new EntityNetException("Table '" + box.table() + "' no Prime Key columns set");
		return box.table() + sb.toString();
	}

	/**
	 * Transfer FKey values to String set, format: table1_id1value_id2value,
	 * table2_id1_id2... <br/>
	 */
	public static Set<String> transferFKeysToString(SqlBox box, Object entity) {
		Set<String> result = new HashSet<String>();
		TableModel tb = box.getTableModel();
		for (FKeyModel fkey : tb.getFkeyConstraints()) {
			String fTable = fkey.getRefTableAndColumns()[0];
			String fkeyEntitID = fTable;
			for (String colNames : fkey.getColumnNames()) {
				String entityField = tb.getColumn(colNames).getEntityField();
				Object fKeyValue = ClassCacheUtils.readValueFromBeanField(entity, entityField);
				if (StrUtils.isEmpty(fKeyValue)) {
					fkeyEntitID = null;
					break;
				}
				fkeyEntitID += KeySep + fKeyValue;
			}
			if (!StrUtils.isEmpty(fkeyEntitID))
				result.add(fkeyEntitID);
		}
		return result;
	}

	/**
	 * Add or join an entity into EntityNet body, if entity already exist, fill
	 * not-null values and add parentEntityIDs
	 */
	public void addOrJoinOneEntity(SqlBox box, Object entity, String entityID, Set<String> parentEntityIDs) {
		Object oldEntity = body.get(entityID);
		TableModel newTable = box.getTableModel();
		if (oldEntity != null) {// fill non-null values
			SqlBox oldBox = SqlBoxUtils.findBox(oldEntity);
			for (ColumnModel newCol : newTable.getColumns()) {
				SqlBoxException.assureNotEmpty(newCol.getEntityField(),
						"EntityField not found for new Entity column '" + newCol.getColumnName() + "'");
				Object newValue = ClassCacheUtils.readValueFromBeanField(entity, newCol.getEntityField());
				if (newValue != null) {// fill new values from new Entity
					String oldEntityField = oldBox.getTableModel().getColumn(newCol.getColumnName()).getEntityField();
					if (!newCol.getEntityField().equals(oldEntityField))
						throw new SqlBoxException(
								"Old entity and new entity has same entitID but has different field name");
					ClassCacheUtils.writeValueToBeanField(oldEntity, newCol.getEntityField(), newValue);
				}
			}
			Set<String> oldParents = oldBox.getParentEntityIDs();
			if (oldParents != null)
				oldParents.addAll(parentEntityIDs);
		} else {
			box.setEntityID(entityID);
			box.setParentEntityIDs(parentEntityIDs);
			body.put(entityID, entity);
		}

	}

	/**
	 * Add an entity to EntityNet, if already have same PKEY entity exist, use new
	 * added one replace, usually they should be same entity have same fields but
	 * some times not
	 */
	public void addEntity(Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(null, entity);
		String entityID = transferPKeyToString(box, entity);
		Set<String> parentEntityIDs = transferFKeysToString(box, entity);
		addOrJoinOneEntity(box, entity, entityID, parentEntityIDs);
	}

	/**
	 * In EntityNet, find target entities by given source entities and target class
	 */
	public Object[] find(Object[] source, Class<?> targetEntityClass) {
		// TODO work at here
		return null;
	}

	/**
	 * In EntityNet, find target entities by given source entities and target class
	 * and a full path format like: "P", Email.class,"C",Role.class..., here "P"
	 * means parent node, "C" means child node
	 */
	public Object[] findWithPath(Object[] source, Class<?> targetEntityClass) {
		// TODO work at here
		return null;
	}

	// ======getter & setter =======
	public Boolean getWeaved() {
		return weaved;
	}

	public void setWeaved(Boolean weaved) {
		this.weaved = weaved;
	}

	public Map<Class<?>, TableModel> getConfigModels() {
		return configModels;
	}

	public void setConfigModels(Map<Class<?>, TableModel> configModels) {
		this.configModels = configModels;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}
}
