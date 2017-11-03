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

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityNet;

/**
 * jTinyNet project is the default implementation of EntityNet in jSqlBox, it's
 * a memory based entity net, can be called "graph database". Not like Neo4j can
 * write to disk file, jTinyNet is only a pure and simple memory graph database,
 * it's 1:1 mapping to relationship database's tables, the relationship in Neoj4
 * is call "Edge", but in jTinyNet it's called relationship, just exactly use
 * the existed database's foreign key constraints. If want create relationship
 * between nodes but do not want make real FKey constraint in database, can
 * build a fake FKeyModel by setting "dll=false" (see jDialects project).
 * 
 * There are some benefits to use a graph/Net type database:<br/>
 * 1) Much quicker browse speed between connected nodes than database. <br/>
 * 2) Works in memory, can be used as query cache. <br/>
 * 3) No need write SQLs. In jTinyNet, do complicated search still use pure Java
 * language, jTinyNet does not support Cypher language because I think it's a
 * little complicated.<br/>
 * 
 * TinyNet class is not thread safe. If want use it as a global Cache,
 * programmer need use synchronised method to serialise access it, like use a
 * HashMap in multiple thread program.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class TinyNet implements EntityNet {
	/** Used to combine compound key column names into a single String */
	public static final String COMPOUND_COLUMNNAME_SEPARATOR = "_CmPdIdSpr_";

	/** Used to combine compound key column values into a single String */
	public static final String COMPOUND_VALUE_SEPARATOR = "_CmPdValSpr_";

	/** ConfigModels is virtual meta data of database and store O-R mapping info */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the EntityNet */
	private Map<Class<?>, LinkedHashMap<String, Node>> body = new HashMap<Class<?>, LinkedHashMap<String, Node>>();

	/**
	 * childCache cache ChildRelations. To improve loading speed, ChildCache will
	 * not be filled until do a search and parent nodes are just in search path,
	 * this is called "delay cached". Write to EntityNet may cause partial or whole
	 * childCache be cleared
	 */
	private Map<String, List<ChildRelation>> childCache;

	public TinyNet() {
	}

	public TinyNet(List<Map<String, Object>> listMap, TableModel... models) {
		addMapList(listMap, models);
	}

	/**
	 * Transfer List<Map<String, Object>> instance to entities and add to current
	 * Net, modelConfigs parameter is optional
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
		if (oldEntity != null) {// will not update fields, but joint parents
			// TODO here
			// Set<String> oldParents = oldEntity.getParentIDs();
			// if (oldParents != null)
			// oldParents.addAll(node.getParentIDs());
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
		List<Object[]> parentIds = TinyNetUtils.transferFKeysToParentIDs(model, entity);
		// TODO here
		// Node node = new Node(id, entity, parentIds);
		// addOrJoinOneNode(model, node);
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
	 * In TinyNet, find target node list by given source nodeList and search path
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
