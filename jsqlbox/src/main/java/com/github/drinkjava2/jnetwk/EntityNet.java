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
package com.github.drinkjava2.jnetwk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNet is a entity net
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {
	public static final String KeySeparator = "_";

	/**
	 * configModels has entityClass, it means it know which entity be mapped, but
	 * the shortage is it often have no alias name be set
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the net */
	private Map<String, EntityNode> body = new LinkedHashMap<String, EntityNode>();

	public EntityNet() {
	}

	public EntityNet(List<Map<String, Object>> listMap, TableModel... models) {
		joinList(listMap, models);
	}

	/** Join another LisMap to current EntityNet, modelConfigs is optional */
	public EntityNet joinList(List<Map<String, Object>> listMap, TableModel... modelConfigs) {
		TableModel[] models = modelConfigs;
		EntityNetUtils.checkModelHasEntityClassAndAlias(models);
		if (listMap == null)
			throw new EntityNetException("Can not join null listMap");
		if (models != null && models.length > 0)// Join models
			for (TableModel tb : models) {
				if (tb.getEntityClass() == null) {
					if (StrUtils.isEmpty(tb.getAlias()))
						throw new EntityNetException("TableModel bot entityClass and alias are not set");
				} else {
					this.configModels.put(tb.getEntityClass(), tb);
				}
			}
		for (Map<String, Object> map : listMap) {// join map list
			assemblyOneRowToEntities(map);
		}
		return this;
	}

	/**
	 * Assembly Map List data to Entities, according current configModels
	 */
	private Object[] assemblyOneRowToEntities(Map<String, Object> oneRow) {
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
							entity = ClassCacheUtils.createNewEntity(model.getEntityClass());
						EntityNetException.assureNotEmpty(col.getEntityField(),
								"EntityField not found for column '" + col.getColumnName() + "'");
						ClassCacheUtils.writeValueToBeanField(entity, col.getEntityField(), row.getValue());
					}
				}
			}
			if (entity != null)
				addEntityAsNode(model, entity);
		}
		return resultList.toArray(new Object[resultList.size()]);
	}

	/**
	 * Add or join an entity into EntityNet body, if entity already exist, fill
	 * not-null values and add parentEntityIDs
	 */
	public void addOrJoinOneNode(TableModel model, EntityNode node) {
		EntityNode oldEntity = body.get(node.getId());
		if (oldEntity != null) {// fill non-null values
			for (ColumnModel newCol : model.getColumns()) {
				EntityNetException.assureNotEmpty(newCol.getEntityField(),
						"EntityField not found for new Entity column '" + newCol.getColumnName() + "'");
				Object newValue = ClassCacheUtils.readValueFromBeanField(node.getEntity(), newCol.getEntityField());
				if (newValue != null) {// fill new values from new Entity
					String oldEntityField = model.getColumn(newCol.getColumnName()).getEntityField();
					if (!newCol.getEntityField().equals(oldEntityField))
						throw new EntityNetException(
								"Old entity and new entity has same entitID but has different field name");
					ClassCacheUtils.writeValueToBeanField(oldEntity.getEntity(), newCol.getEntityField(), newValue);
				}
			}
			Set<String> oldParents = oldEntity.getParentIDs();
			if (oldParents != null)
				oldParents.addAll(node.getParentIDs());
		} else {
			body.put(node.getId(), node);
		}
	}

	/**
	 * Add an entity to EntityNet, if already have same PKEY entity exist, if old
	 * entity field is null, will new new entity's value fill in
	 */
	private void addEntityAsNode(TableModel model, Object entity) {
		String id = EntityNetUtils.transferPKeyToNodeID(model, entity);
		Set<String> parentIDs = EntityNetUtils.transferFKeysToParentIDs(model, entity);
		EntityNode node = new EntityNode(id, entity, parentIDs);
		addOrJoinOneNode(model, node);
	}

	public List<EntityNode> getEntityNodeList(Class<?> entityClass) {
		List<EntityNode> result = new ArrayList<EntityNode>();
		for (Entry<String, EntityNode> entry : body.entrySet()) {
			Object entity = entry.getValue().getEntity();
			if (entityClass.isInstance(entity))
				result.add(entry.getValue());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getEntityList(Class<T> entityClass) {
		List<T> result = new ArrayList<T>();
		for (Entry<String, EntityNode> entry : body.entrySet()) {
			Object entity = entry.getValue().getEntity();
			if (entityClass.isInstance(entity))
				result.add((T) entity);
		}
		return result;
	}

	/**
	 * Find target entities related for given sourceEntities, no need give path,
	 * computer will guess the path, if more than 1 path exist, computer just use
	 * the first found path, if want manually assign a path, should give a
	 * EntitySearchPath parameter.
	 */
	public <T> T[] findRelated(Object[] sourceEntities, Class<T> targetEntityClass) {
		return findRelated(sourceEntities, targetEntityClass);
	}

	/**
	 * In EntityNet, find target entities by given source entities and target class
	 * and a full search path EntityNetPath
	 */
	public <T> T[] findRelated(Object[] sourceEntity, Class<T> targetEntityClass, EntitySearchPath path) {
		return EntityNetUtils.findRelated(this, sourceEntity, targetEntityClass, path);
	}

	// ======getter & setter =======
	public Map<Class<?>, TableModel> getConfigModels() {
		return configModels;
	}

	public void setConfigModels(Map<Class<?>, TableModel> configModels) {
		this.configModels = configModels;
	}

	public Map<String, EntityNode> getBody() {
		return body;
	}

	public void setBody(Map<String, EntityNode> body) {
		this.body = body;
	}

}
