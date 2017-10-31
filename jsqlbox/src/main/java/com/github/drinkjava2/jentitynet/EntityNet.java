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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNet is an entity net, it's a memory graph structure, simlar concept can
 * see Neo4j,
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {
	public static final String KeySeparator = "_";

	/**
	 * configModels has entityClass, it means it know which entity be mapped,
	 * but the shortage is it often have no alias name be set
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the net */
	private Map<String, EntityNode> body = new LinkedHashMap<String, EntityNode>();
 
	private Map<String, Map<Class<?>, Set<String>>> childs; 
	
	public EntityNet() {
	}

	public EntityNet(List<Map<String, Object>> listMap, TableModel... models) {
		joinList(listMap, models);
	}

	/**
	 * Join another LisMap to current EntityNet, modelConfigs parameter is
	 * optional
	 */
	public EntityNet joinList(List<Map<String, Object>> listMap, TableModel... modelConfigs) {
		if (listMap == null)
			throw new EntityNetException("Can not join null listMap");
		TableModel[] bindeds = EntityNetUtils.getBindedTableModel(listMap);
		EntityNetUtils.removeBindedTableModel(listMap);
		if (bindeds == null || bindeds.length == 0)
			bindeds = new TableModel[0];
		TableModel[] configs = EntityNetUtils.jointConfigModels(bindeds, modelConfigs);
		EntityNetUtils.checkModelHasEntityClassAndAlias(configs);
		if (configs != null && configs.length > 0)// Join models
			for (TableModel tb : configs) {
				if (tb.getEntityClass() == null) {
					if (StrUtils.isEmpty(tb.getAlias()))
						throw new EntityNetException(
								"TableModel of '" + tb.getTableName() + "' entityClass and alias are not set");
				} else {
					this.configModels.put(tb.getEntityClass(), tb);
				}
			}
		for (Map<String, Object> map : listMap) {// join map list
			EntityNetUtils.assemblyOneRowToEntities(this, map);
		}
		return this;
	}

	/**
	 * Add or join an node into EntityNet body, if node ID already exist, fill
	 * not-null values of entity and add parentEntityIDs
	 */
	protected void addOrJoinOneNode(TableModel model, EntityNode node) {
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
	 * Add an entity to EntityNet, if already have same PKEY entity exist, if
	 * old entity field is null, will new new entity's value fill in
	 */
	protected void addEntityAsNode(EntityNet net, TableModel model, Object entity) {
		String id = EntityNetUtils.transferPKeyToNodeID(model, entity);
		Set<String> parentIDs = EntityNetUtils.transferFKeysToParentIDs(model, entity);
		EntityNode node = new EntityNode(id, entity, parentIDs);
		addOrJoinOneNode(model, node);
	}

	/** Return EntityNode list in EntityNet which type is entityClass */
	public List<EntityNode> getNodeList(Class<?> entityClass) {
		List<EntityNode> result = new ArrayList<EntityNode>();
		for (EntityNode node : body.values())
			if (node.getClass() != null && node.getClass().equals(entityClass))
				result.add(node);
		return result;
	}

	/** Return entity list in EntityNet which type is entityClass */
	public <T> List<T> getEntityList(Class<T> entityClass) {
		return EntityNetUtils.nodeList2EntityList(getNodeList(entityClass));
	}

	/**
	 * In EntityNet, find target node list by given source nodeList and search
	 * path
	 */
	public List<EntityNode> findNodeList(List<EntityNode> nodeList, SearchPath path) {
		return EntityNetUtils.findRelated(this, nodeList, path);
	}

	/**
	 * In EntityNet, find target entity list by given source entityList and
	 * search path
	 */
	public <T> List<T> findEntityList(List<Object> entityList, SearchPath path) {
		List<EntityNode> nodeList = EntityNetUtils.entityList2NodeList(this, entityList);
		return EntityNetUtils.nodeList2EntityList(findNodeList(nodeList, path));
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
