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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityNet;

/**
 * jTinyNet project is the default implementation of EntityNet in jSqlBox, it's
 * a memory based Object net, can kind of be called "Graph Database". Not like
 * Neo4j can write to disk file, jTinyNet is only a memory graph 1:1 mapping to
 * relational database's tables, the relationship in Neo4j is call "Edge", but
 * in jTinyNet is still called relationship, just exactly use the existed
 * relational database's foreign key constraints. If want use jTinyNet but don't
 * want output FKey constraint in DDL, can build fake FKeyModels by setting
 * "ddl=false" (see jDialects project).
 * 
 * Some benefits to use a graph database than relational database:<br/>
 * 1) No need write complicated join SQLs. <br/>
 * 2) Working in memory, much quicker browse speed between connected nodes than
 * traditional database. <br/>
 * 3) Can use pure Java language do query operation, detail see "Path" query.
 * 
 * jTinyNet does not support Cypher language because Cypher is designed for huge
 * graph structure data, but jTinyNet is designed for regular relational
 * database with usually no more than hundreds tables.
 * 
 * TinyNet class is not thread safe. If want use it as a global cache,
 * programmer need use synchronized method to serialize access it, like use a
 * HashMap in multiple thread environment.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class TinyNet implements EntityNet {
	/** Used to combine compound key column names into a single String */
	public static final String COMPOUND_COLUMNNAME_SEPARATOR = "_CmPdIdSpr_";

	/** Used to combine compound key column values into a single String */
	public static final String COMPOUND_VALUE_SEPARATOR = "_CmPdValSpr_";

	/**
	 * ConfigModels is virtual meta data of EntityNet, and also store O-R
	 * mapping info related to database
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the EntityNet */
	private Map<Class<?>, LinkedHashMap<String, Node>> body = new HashMap<Class<?>, LinkedHashMap<String, Node>>();

	/** Enable query cache */
	private Boolean cacheable = true;

	/**
	 * queryCache cache search result after do a path serach <br/>
	 * 
	 * QueryCache will be filled when all of these conditions meet: <br/>
	 * 1)TinyNet's cacheable is true (default) <br/>
	 * 2)Path's cacheable is true (default)<br/>
	 * 3)Path's checker should be null or a Checker class <br/>
	 * 4)For Path Chain, all child Paths should cacheable.
	 * 
	 * Write to EntityNet may cause partial or whole queryCache be cleared.
	 */
	// NodeId , PathId, ChildNodeIDs
	private Map<String, Map<Integer, String>> queryCache = new HashMap<String, Map<Integer, String>>();

	/**
	 * Use a Integer to replace String as path id can save memory
	 */
	private int currentPathId = 1;

	private Map<String, Integer> pathIdCache = new HashMap<String, Integer>();

	public TinyNet() {
	}

	public TinyNet(List<Map<String, Object>> listMap, TableModel... models) {
		addMapList(listMap, models);
	}

	/**
	 * Transfer List<Map<String, Object>> instance to entities and add to
	 * current Net, modelConfigs parameter is optional
	 */
	@Override
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
			addOneRowMapList(map);
		}
		return this;
	}

	/**
	 * Assembly one row of Map List to Entities, according net's configModels
	 */
	protected void addOneRowMapList(Map<String, Object> oneRow) {
		for (TableModel model : this.getConfigModels().values()) {
			Object entity = null;
			String alias = model.getAlias();
			if (StrUtils.isEmpty(alias))
				throw new TinyNetException("No alias found for table '" + model.getTableName() + "'");
			Set<String> loadedFields = new HashSet<String>();
			for (Entry<String, Object> row : oneRow.entrySet()) { // u_userName
				for (ColumnModel col : model.getColumns()) {
					if (row.getKey().equalsIgnoreCase(alias + "_" + col.getColumnName())) {
						if (entity == null)
							entity = ClassCacheUtils.createNewEntity(model.getEntityClass());
						TinyNetException.assureNotEmpty(col.getEntityField(),
								"EntityField not found for column '" + col.getColumnName() + "'");
						ClassCacheUtils.writeValueToBeanField(entity, col.getEntityField(), row.getValue());
						loadedFields.add(col.getEntityField());
					}
				}
			}
			if (entity != null)
				this.addOrJoinEntity(model, entity, loadedFields);
		}
	}

	/**
	 * Add an entity to TinyNet, if already have same PKEY entity exist, if old
	 * entity field is never loaded from database, will put new entity's value
	 */
	protected void addOrJoinEntity(TableModel model, Object entity, Set<String> loadedFields) {
		String id = TinyNetUtils.buildNodeId(model, entity);
		List<ParentRelation> parentRelations = TinyNetUtils.transferFKeysToParentRelations(model, entity);
		Node node = new Node();
		node.setEntity(entity);
		node.setParentRelations(parentRelations);
		node.setId(id);
		node.setLoadedFields(loadedFields);
		addOrJoinOneNodeToBody(node);
	}

	/**
	 * Add or join an node into TinyNet body, if old node with same ID already
	 * exist, join loaded fields and ParentRelation
	 */
	protected void addOrJoinOneNodeToBody(Node node) {
		Node oldNode = findIfNodeAlreadyExist(node);
		if (oldNode == null)
			this.addNode(node);
		else {
			// join loaded fields
			Set<String> newFields = node.getLoadedFields();
			if (newFields == null || newFields.isEmpty())
				return;
			Set<String> oldFields = oldNode.getLoadedFields();
			if (oldFields == null) {
				oldFields = new HashSet<String>();
				oldNode.setLoadedFields(oldFields);
			}
			for (String newField : newFields)
				if (!oldFields.contains(newField)) {
					oldFields.add(newField);
					Object newValue = ClassCacheUtils.readValueFromBeanField(node.getEntity(), newField);
					ClassCacheUtils.writeValueToBeanField(oldNode.getEntity(), newField, newValue);
				}

			// join parentRelations
			List<ParentRelation> newParentRelations = node.getParentRelations();
			if (newParentRelations == null || newParentRelations.isEmpty())
				return;
			List<ParentRelation> oldParentRelations = oldNode.getParentRelations();

			if (oldParentRelations == null) {
				oldParentRelations = new ArrayList<ParentRelation>();
				oldNode.setParentRelations(oldParentRelations);
			}
			for (ParentRelation newP : newParentRelations) {
				for (ParentRelation oldP : oldParentRelations) {
					if (newP.equals(oldP))
						break;
				}
				oldParentRelations.add(newP);
			}
		}
	}

	/** Add one node into EntityNet body */
	protected void addNode(Node node) {
		TinyNetException.assureNotNull(node, "Can not add null node");
		TinyNetException.assureNotNull(node.getEntity(), "Can not add node with null entity");
		Class<?> entityClass = node.getEntity().getClass();
		LinkedHashMap<String, Node> nodeMap = body.get(entityClass);
		if (nodeMap == null) {
			nodeMap = new LinkedHashMap<String, Node>();
			body.put(entityClass, nodeMap);
		}
		nodeMap.put(node.getId(), node);
	}

	// ===============MISC methods============
	private Node findIfNodeAlreadyExist(Node node) {
		if (node == null || node.getEntity() == null)
			return null;
		LinkedHashMap<String, Node> nodes = body.get(node.getEntity().getClass());
		if (nodes == null)
			return null;
		return nodes.get(node.getId());
	}

	/** Return total how many nodes */
	public int size() {
		int size = 0;
		for (LinkedHashMap<String, Node> map : body.values()) {
			size += map.size();
		}
		return size;
	}

	/** Return EntityNode list in TinyNet which type is entityClass */
	public Collection<Node> getAllNodeCollection(Class<?> entityClass) {
		LinkedHashMap<String, Node> nodesMap = body.get(entityClass);
		if (nodesMap == null || nodesMap.isEmpty())
			return new LinkedHashSet<Node>();
		return (Collection<Node>) nodesMap.values();
	}

	/** Return entity list in TinyNet which type is entityClass */
	public <T> List<T> getAllEntityList(Class<T> entityClass) {
		return TinyNetUtils.nodes2EntityList(getAllNodeCollection(entityClass));
	}

	/** Return entity set in TinyNet which type is entityClass */
	public <T> Set<T> getAllEntitySet(Class<T> entityClass) {
		return TinyNetUtils.nodes2EntitySet(getAllNodeCollection(entityClass));
	}

	// ============= Find methods=============================
	public Set<Node> findNodeSet(Object entity, Path path) {
		if (entity == null)
			return new LinkedHashSet<Node>();
		Node node = TinyNetUtils.entity2Node(this, entity);
		return findNodeSet(new LinkedHashSet<Node>(Arrays.asList(node)), path);
	}

	public Set<Node> findEntityNodeSet(Collection<Object> entities, Path path) {
		Set<Node> nodeSrc = new LinkedHashSet<Node>();
		for (Object entity : entities) {
			Node node = TinyNetUtils.entity2Node(this, entity);
			if (node != null)
				nodeSrc.add(node);
		}
		return findNodeSet(nodeSrc, path);
	}

	/**
	 * According given nodeList and Search path, find related nodes
	 * 
	 * @param src
	 *            The source input list
	 * @param paths
	 *            The result node set
	 * @return The target node set
	 */
	public Set<Node> findNodeSet(Collection<Node> nodeSrc, Path path) {
		return null;
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

	public Map<String, Map<Integer, String>> getChildCache() {
		return queryCache;
	}

	public void setChildCache(Map<String, Map<Integer, String>> childCache) {
		this.queryCache = childCache;
	}

}
