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
package com.github.drinkjava2.jtinynet;

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
import com.github.drinkjava2.jsqlbox.EntityNet;

/**
 * TinyNet is an entity net, it's a memory based graph structure
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class TinyNet implements EntityNet {
	public static final String KeySeparator = "_";

	/**
	 * configModels has entityClass, it means it know which entity be mapped,
	 * but the shortage is it often have no alias name be set
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	private Map<Class<?>, LinkedHashMap<String, Node>> body = new HashMap<Class<?>, LinkedHashMap<String, Node>>();

	/**
	 * Parents of node, 4 dimensions determine a parent: selfNodeID,
	 * fkeyColumnNames, parentClass, parentNodeID, for example: <br/>
	 * user1ID, teacherColumnName, TeacherClass, teacher5ID <br/>
	 * user1ID, teacherColumnName, SuperTeacherClass, superTeacher9ID <br/>
	 */
	private Map<String, Map<String, Map<Class<?>, String>>> parents;

	/**
	 * Children of node, 4 dimensions determine a child: selfNodeID, childClass,
	 * fkeyColumnNames, childNodeId like: <br/>
	 * tercher5ID, User.class, teacherColumnName, user1ID <br/>
	 * user1ID, teacherColumnName, SuperTeacherClass, superTeacher9ID <br/>
	 */
	private Map<String, Map<String, Map<Class<?>, String>>> childs;

	public TinyNet() {
	}

	public TinyNet(List<Map<String, Object>> listMap, TableModel... models) {
		addMapList(listMap, models);
	}

	/**
	 * Transfer List<Map<String, Object>> instance to entities and add to
	 * current Net, modelConfigs parameter is optional
	 */
	public TinyNet addMapList(List<Map<String, Object>> listMap, TableModel... configs) {
		if (listMap == null)
			throw new TinyNetException("Can not join null listMap");
		TinyNetUtils.checkModelHasEntityClassAndAlias(configs);
		if (configs != null && configs.length > 0)// Join models
			for (TableModel tb : configs) {
				if (tb.getEntityClass() == null) {
					if (StrUtils.isEmpty(tb.getAlias()))
						throw new TinyNetException(
								"TableModel of '" + tb.getTableName() + "' entityClass and alias are not set");
				} else {
					this.configModels.put(tb.getEntityClass(), tb);
				}
			}
		for (Map<String, Object> map : listMap) {// join map list
			TinyNetUtils.assemblyOneRowToEntities(this, map);
		}
		return this;
	}

	protected Node getExistedNode(Node node) {
		if (node == null || node.getEntity() == null)
			return null;
		LinkedHashMap<String, Node> nodes = body.get(node.getEntity().getClass());
		if (nodes == null)
			return null;
		return nodes.get(node.getId());
	}

	protected void addNode(Node node) {
		TinyNetException.assureNotNull(node, "Can not add null node");
		TinyNetException.assureNotNull(node.getEntity(), "Can not add node with null entity");
		Class<?> entityClass = node.getEntity().getClass();
		LinkedHashMap<String, Node> nodes = body.get(entityClass);
		if (nodes == null) {
			nodes = new LinkedHashMap<String, Node>();
			body.put(entityClass, nodes);
		}
		nodes.put(node.getId(), node);
	}

	/**
	 * Add or join an node into TinyNet body, if node ID already exist, fill
	 * not-null values of entity and add parentEntityIDs
	 */
	protected void addOrJoinOneNode(TableModel model, Node node) {
		Node oldEntity = getExistedNode(node);
		if (oldEntity != null) {// fill non-null values
			for (ColumnModel newCol : model.getColumns()) {
				TinyNetException.assureNotEmpty(newCol.getEntityField(),
						"EntityField not found for new Entity column '" + newCol.getColumnName() + "'");
				Object newValue = ClassCacheUtils.readValueFromBeanField(node.getEntity(), newCol.getEntityField());
				if (newValue != null) {// fill new values from new Entity
					String oldEntityField = model.getColumn(newCol.getColumnName()).getEntityField();
					if (!newCol.getEntityField().equals(oldEntityField))
						throw new TinyNetException(
								"Old entity and new entity has same entitID but has different field name");
					ClassCacheUtils.writeValueToBeanField(oldEntity.getEntity(), newCol.getEntityField(), newValue);
				}
			}
			//TODO
//			Set<String> oldParents = oldEntity.getParentIDs();
//			if (oldParents != null)
//				oldParents.addAll(node.getParentIDs());
		} else {
			this.addNode(node);
		}
	}

	/**
	 * Add an entity to TinyNet, if already have same PKEY entity exist, if old
	 * entity field is null, will new new entity's value fill in
	 */
	public void addEntityAsNode(TableModel model, Object entity) {
		String id = TinyNetUtils.transferPKeyToNodeID(model, entity);
		Set<String> parentIDs = TinyNetUtils.transferFKeysToParentIDs(model, entity);
		Node node = new Node(id, entity, parentIDs);
		addOrJoinOneNode(model, node);
	}

	// ============== query methods ================================

	/** Return total how many nodes */
	public int size() {
		int size = 0;
		for (LinkedHashMap<String, Node> map : body.values()) {
			size += map.size();
		}
		return size;
	}

	/** Return EntityNode list in TinyNet which type is entityClass */
	public List<Node> getNodeList(Class<?> entityClass) {
		LinkedHashMap<String, Node> nodesMap = body.get(entityClass);
		if (nodesMap == null || nodesMap.isEmpty())
			return new ArrayList<Node>();
		return new ArrayList<Node>(nodesMap.values());
	}

	/** Return entity list in TinyNet which type is entityClass */
	public <T> List<T> getEntityList(Class<T> entityClass) {
		return TinyNetUtils.nodeList2EntityList(getNodeList(entityClass));
	}

	/**
	 * In TinyNet, find target node list by given source nodeList and search
	 * path
	 */
	public List<Node> findNodeList(List<Node> nodeList, SearchPath path) {
		return TinyNetUtils.findRelated(this, nodeList, path);
	}

	// ======getter & setter =======
	public Map<Class<?>, TableModel> getConfigModels() {
		return configModels;
	}

	public void setConfigModels(Map<Class<?>, TableModel> configModels) {
		this.configModels = configModels;
	}

	public Map<Class<?>, LinkedHashMap<String, Node>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, LinkedHashMap<String, Node>> body) {
		this.body = body;
	}

}
