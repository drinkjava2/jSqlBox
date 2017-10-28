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
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * EntityNet is a entity net
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {

	private Boolean weaved = false;

	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/* A backup copy of map list */
	private List<Map<String, Object>> listMaps = new ArrayList<Map<String, Object>>();

	/** The body of the net */
	private Map<Class<?>, List<Object>> body = new HashMap<Class<?>, List<Object>>();

	public EntityNet(List<Map<String, Object>> listMap, TableModel... models) {
		joinList(listMap, models);
	}

	public EntityNet joinList(List<Map<String, Object>> listMap, TableModel... models) {
		weaved = false;
		if (listMap == null)
			throw new EntityNetException("Can not join null listMap");
		if (models != null && models.length > 0) {
			for (TableModel tableModel : models) {
				if (tableModel.getEntityClass() == null)
					throw new EntityNetException("Can not join tableModel with null entityClass");
				this.configModels.put(tableModel.getEntityClass(), tableModel);
			}
		}
		for (Map<String, Object> map : listMap) {
			listMaps.add(map);
			addOneRow(map);
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
	public Object[] assemblyMapListToEntities(Map<String, Object> oneRow) {
		List<Object> resultList = new ArrayList<Object>();

		for (TableModel model : configModels.values()) {
			Object obj = null;
			for (String alaisColumname : oneRow.keySet()) { // u_userName
				String alias = model.getAlias();
				if (StrUtils.isEmpty(alias))
					alias = model.getTableName();
				for (ColumnModel col : model.getColumns()) {
					if (alaisColumname.equalsIgnoreCase(alias + "_" + col.getColumnName())) {
						if (obj == null)
							obj = createNewEntity(model.getEntityClass());
						//TODO here
					}
				}
			}
		}

		// if (models != null && models.length > 0) {
		// for (TableModel tb : models) {
		// if (tableName.equalsIgnoreCase(tb.getTableName())) {
		// for (ColumnModel col : tb.getColumns()) {
		// if (!col.getTransientable())
		// sb.append(alias).append(".").append(col.getColumnName()).append(" as
		// ").append(alias)
		// .append("_").append(col.getColumnName()).append(", ");
		// }
		// break;
		// }
		// }
		// }
		return resultList.toArray(new Object[resultList.size()]);
	}

	/**
	 * Add one Map<String, Object> row to EntityNet, this method will analyse
	 * and transfer row to Entity Objects, then add these objects into EntityNet
	 * body
	 */
	public void addOneRow(Map<String, Object> oneRow) {
		Object[] entities = assemblyMapListToEntities(oneRow);
		for (Object entity : entities) {
			addEntity(entity);
		}
	}

	/**
	 * Add an entity to EntityNet, if already have same PKEY entity exist, use
	 * new added one replace, usually they should be same entity but some times
	 * not.
	 */
	public void addEntity(Object entity) {
		// TODO
	}

	/**
	 * In EntityNet, find target entities by given source entities and target
	 * class
	 */
	public Object[] find(Object[] source, Class<?> targetEntityClass) {
		// TODO work at here
		return null;
	}

	/**
	 * In EntityNet, find target entities by given source entities and target
	 * class and a full path format like: "P", Email.class,"C",Role.class...,
	 * here "P" means parent node, "C" means child node
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

	public List<Map<String, Object>> getListMaps() {
		return listMaps;
	}

	public void setListMaps(List<Map<String, Object>> listMaps) {
		this.listMaps = listMaps;
	}

	public Map<Class<?>, List<Object>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, List<Object>> body) {
		this.body = body;
	}

}
