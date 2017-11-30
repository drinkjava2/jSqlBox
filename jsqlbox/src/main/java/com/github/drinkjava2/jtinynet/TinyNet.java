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
 * a memory based Object net, can kind of be called "Graph Database" or "NoSQL
 * Database". Not like Neo4j can write to disk file, jTinyNet is only a memory
 * based graph 1:1 mapping to relational database's tables, the relationship in
 * Neo4j is call "Edge", but in jTinyNet is still called relationship, just
 * exactly use the existed relational database's foreign key constraints. If
 * want use jTinyNet but don't want output FKey constraint in DDL, can build
 * fake FKeyModels by setting "ddl=false" (see jDialects project).
 * 
 * Some benefits to use a graph database than relational database:<br/>
 * 1) No need write complicated join SQLs. <br/>
 * 2) Working in memory, much quicker browse speed between connected nodes than
 * traditional database. <br/>
 * 3) Can use pure Java language do query operation, detail see "Path" query.
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
	 * ConfigModels is virtual meta data of EntityNet, and also store O-R mapping
	 * info related to database
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the EntityNet */
	// entityClass, nodeID, node
	private Map<Class<?>, LinkedHashMap<String, Node>> body = new HashMap<Class<?>, LinkedHashMap<String, Node>>();

	/** Enable child query cache */
	private Boolean allowQueryCache = true;

	/**
	 * queryCache cache search result after do a path serach <br/>
	 * 
	 * QueryCache will be filled when all of these conditions meet: <br/>
	 * 1)TinyNet's cacheable is true (default) <br/>
	 * 2)Path's cacheable is true (default)<br/>
	 * 3)Path's checker should be null or a Checker class <br/>
	 * 4)For Path Chain, all child Paths should cacheable
	 * 
	 * Any writing to EntityNet will cause all queryCache be cleared.
	 */
	// NodeId , PathId, ChildNodeIDs
	private Map<String, Map<Integer, Set<Node>>> queryCache = new HashMap<String, Map<Integer, Set<Node>>>();

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
	 * Transfer List<Map<String, Object>> instance to entities and add to current
	 * Net, modelConfigs parameter is optional
	 */
	@Override
	public TinyNet addMapList(List<Map<String, Object>> listMap, TableModel... configs) {
		if (listMap == null)
			throw new TinyNetException("Can not join null listMap");
		// clean query caches
		cleanAllQueryCaches();

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

	private void cleanAllQueryCaches() {
		pathIdCache = new HashMap<String, Integer>();
		queryCache = new HashMap<String, Map<Integer, Set<Node>>>();
	}

	/**
	 * Assembly one row of Map List to Entities, according net's configModels
	 */
	protected void addOneRowMapList(Map<String, Object> oneRow) {
		for (TableModel model : this.configModels.values()) {
			Object entity = null;
			String alias = model.getAlias();
			if (StrUtils.isEmpty(alias))
				throw new TinyNetException("No alias found for table '" + model.getTableName() + "'");
			Set<String> loadedFields = new HashSet<String>();

			// create new Entity
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
				this.addOrJoinEntity(entity, model, loadedFields);
		}
	}

	@Override
	public void addEntity(Object entity, TableModel tableModel) {
		cleanAllQueryCaches();
		Set<String> loadedFields = new HashSet<String>();
		for (ColumnModel col : tableModel.getColumns())
			loadedFields.add(col.getEntityField());
		addOrJoinEntity(entity, tableModel, loadedFields);
	}

	@Override
	public void removeEntity(Object entity, TableModel tableModel) {
		cleanAllQueryCaches();
		String id = TinyNetUtils.buildNodeId(tableModel, entity);
		body.get(entity.getClass()).remove(id);
	}

	@Override
	public void updateEntity(Object entity, TableModel tableModel) {
		removeEntity(entity, tableModel);
		addEntity(entity, tableModel);
	}

	/**
	 * Add an entity to TinyNet, if already have same PKEY entity exist, if old
	 * entity field is never loaded from database, will put new entity's value
	 */
	protected void addOrJoinEntity(Object entity, TableModel tableModel, Set<String> loadedFields) {
		String id = TinyNetUtils.buildNodeId(tableModel, entity);
		List<ParentRelation> parentRelations = TinyNetUtils.transferFKeysToParentRelations(tableModel, entity);
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

	/** Return a Node by given entityClass and nodeId */
	public Node getOneNode(Class<?> entityClass, String nodeId) {
		LinkedHashMap<String, Node> nodesMap = body.get(entityClass);
		if (nodesMap == null)
			return null;
		return nodesMap.get(nodeId);
	}

	/** Return a entity by given entityClass and nodeId */
	@SuppressWarnings("unchecked")
	public <T> T getOneEntity(Class<?> entityClass, String nodeId) {
		Node node = getOneNode(entityClass, nodeId);
		if (node == null)
			return null;
		return (T) node.getEntity();
	}

	/** Return EntityNode list in TinyNet which type is entityClass */
	public Set<Node> getAllNodeSet(Class<?> entityClass) {
		Set<Node> result = new LinkedHashSet<Node>();
		LinkedHashMap<String, Node> nodesMap = body.get(entityClass);
		if (nodesMap == null || nodesMap.isEmpty())
			return result;
		result.addAll(nodesMap.values());
		return result;
	}

	/** Return entity set in TinyNet which type is entityClass */
	public <T> Set<T> getAllEntitySet(Class<T> entityClass) {
		return TinyNetUtils.nodeCollection2EntitySet(getAllNodeSet(entityClass));
	}

	/** Return entity set in TinyNet which type is entityClass */
	public <T> List<T> getAllEntityList(Class<T> entityClass) {
		return TinyNetUtils.nodeCollection2EntityList(getAllNodeSet(entityClass));
	}

	// ============= Find methods=============================
	/**
	 * Find entity set in TinyNet by given path and entity input array
	 */
	public <T> Set<T> findEntitySet(Class<T> targetEntityClass, Path path, Object... entities) {
		Set<Node> input = TinyNetUtils.entityArray2NodeSet(this, entities);
		Map<Class<?>, Set<Node>> nodeMapSet = findNodeMapByNodeCollection(path, input);
		Set<Node> nodeSet = nodeMapSet.get(targetEntityClass);
		return TinyNetUtils.nodeCollection2EntitySet(nodeSet);
	}

	/**
	 * Find entity set in TinyNet by given path and entity input collection
	 */
	public <T> Set<T> findEntitySet(Class<T> targetEntityClass, Path path, Collection<Object> entityCollection) {
		Set<Node> input = new LinkedHashSet<Node>();
		for (Object entity : entityCollection) {
			Node node = TinyNetUtils.entity2Node(this, entity);
			if (node != null)
				input.add(node);
		}
		Map<Class<?>, Set<Node>> nodeMapSet = findNodeMapByNodeCollection(path, input);
		Set<Node> nodeSet = nodeMapSet.get(targetEntityClass);
		return TinyNetUtils.nodeCollection2EntitySet(nodeSet);
	}

	/**
	 * Find entity set map in TinyNet by given path and entity input array
	 */
	public Map<Class<?>, Set<Object>> findEntityMap(Path path, Object... entities) {
		Map<Class<?>, Set<Node>> nodeMap = findNodeMapByEntities(path, entities);
		return TinyNetUtils.nodeSetMapToEntitySetMap(nodeMap);
	}

	/**
	 * Find node set in TinyNet by given path and entity input array
	 */
	public Map<Class<?>, Set<Node>> findNodeMapByEntities(Path path, Object... entities) {
		Set<Node> input = TinyNetUtils.entityArray2NodeSet(this, entities);
		return findNodeMapByNodeCollection(path, input);
	}

	/**
	 * Find node set in TinyNet by given path and entity input collection
	 */
	public Map<Class<?>, Set<Node>> findNodeMapByEntityCollection(Path path, Collection<Object> entityCollection) {
		Set<Node> input = new LinkedHashSet<Node>();
		for (Object entity : entityCollection) {
			Node node = TinyNetUtils.entity2Node(this, entity);
			if (node != null)
				input.add(node);
		}
		return findNodeMapByNodeCollection(path, input);
	}

	public Map<Class<?>, Set<Node>> findNodeMapByNodeCollection(Path path, Collection<Node> input) {
		Map<Class<?>, Set<Node>> result = new HashMap<Class<?>, Set<Node>>();
		Path topPath = path.getTopPath();
		topPath.initializePath(this);
		findNodeSetforNodes(0, topPath, input, result);
		return result;
	}

	/**
	 * According given path and input Node Set, find related node set
	 * 
	 * @param level search level, start from 0
	 * @param path The Path
	 * @param input The input node collection
	 * @param output The output node collection
	 * @return Related node set
	 */
	private void findNodeSetforNodes(Integer level, Path path, Collection<Node> input,
			Map<Class<?>, Set<Node>> result) {
		if (level > 1000)
			throw new TinyNetException("Search level beyond 1000, this may caused by a circular reference path chain.");
		TableModel model = path.getTargetModel();
		String type0 = path.getType().substring(0, 1);
		String type1 = path.getType().substring(1, 2);
		Class<?> targetClass = model.getEntityClass();

		Set<Node> selected = new LinkedHashSet<Node>();
		String pathUniqueString = path.getUniqueIdString();
		Integer pathId = pathIdCache.get(pathUniqueString);

		// Start
		if ("S".equalsIgnoreCase(type0)) {
			if (level != 0)
				throw new TinyNetException("'S' type can only be used on path start");
			// Check if cached
			Map<Integer, Set<Node>> rootCache = queryCache.get("ROOT");
			if (this.allowQueryCache && path.getCacheable() && pathId != null && (rootCache != null)) {
				Set<Node> cachedNodes = rootCache.get(pathId);
				if (cachedNodes != null)
					selected = cachedNodes;
			} else {// check all targetClass
				Collection<Node> nodesToCheck = getAllNodeSet(targetClass);
				validateSelected(level, path, selected, nodesToCheck);
				// cache it if allow cache
				if (this.allowQueryCache && path.getCacheable() && !StrUtils.isEmpty(pathUniqueString)) {
					cacheSelected("ROOT", pathUniqueString, selected);
				}
			}
		} else
		// Child
		if ("C".equalsIgnoreCase(type0) && input != null && !input.isEmpty()) {
			for (Node inputNode : input) {
				Map<Integer, Set<Node>> childCache = queryCache.get(inputNode.getId());
				if (this.allowQueryCache && path.getCacheable() && pathId != null && childCache != null) {
					Set<Node> cachedNodes = childCache.get(pathId);
					if (cachedNodes != null)
						selected.addAll(cachedNodes);
				} else {
					// Find childNodes meat class/columns/id condition
					Set<Node> nodesToCheck = new LinkedHashSet<Node>();
					for (Entry<String, Node> cNode : body.get(targetClass).entrySet()) {
						List<ParentRelation> prs = cNode.getValue().getParentRelations();
						if (prs != null)
							for (ParentRelation pr : prs) {
								if (inputNode.getId().equals(pr.getParentId())
										&& pr.getRefColumns().equalsIgnoreCase(path.getRefColumns())) {
									nodesToCheck.add(cNode.getValue());
									break;
								}
							}
					}

					validateSelected(level, path, selected, nodesToCheck);

					// now cached childNodes on parentNode
					if (this.allowQueryCache && path.getCacheable() && !StrUtils.isEmpty(pathUniqueString)) {
						cacheSelected(inputNode.getId(), pathUniqueString, selected);
					}
				}
			}
		} else
		// Parent
		if ("P".equalsIgnoreCase(type0) && input != null && !input.isEmpty()) {
			String targetTableName = model.getTableName();
			TinyNetException.assureNotEmpty(targetTableName, "targetTableName can not be null");
			for (Node inputNode : input) {
				// Find parent nodes meat tableName/refColumns/nodeId condition
				Set<Node> nodesToCheck = new LinkedHashSet<Node>();
				List<ParentRelation> prs = inputNode.getParentRelations();
				if (prs != null)
					for (ParentRelation pr : prs) {
						if (targetTableName.equalsIgnoreCase(pr.getParentTable())
								&& path.getRefColumns().equalsIgnoreCase(pr.getRefColumns())) {
							Node node = this.getOneNode(targetClass, pr.getParentId());
							if (node != null)
								nodesToCheck.add(node);
						}
					}
				validateSelected(level, path, selected, nodesToCheck);
				// now cached childNodes on parentNode
				if (this.allowQueryCache && path.getCacheable() && !StrUtils.isEmpty(pathUniqueString)) {
					cacheSelected(inputNode.getId(), pathUniqueString, selected);
				}
			}
		}
		Set<Node> nodes = result.get(targetClass);
		if (nodes == null) {
			nodes = new LinkedHashSet<Node>();
			result.put(targetClass, nodes);
		}

		if ("+".equals(type1) || "*".equals(type1))
			nodes.addAll(selected);

		if (!(path.getCacheable() && StrUtils.isEmpty(path.getUniqueIdString()))) {
			if (selected.size() > 100000)
				throw new TinyNetException(
						"Query result return more than 100000 records to cache in memory, this may caused by careless programming.");
		}

		if (level > 10000)
			throw new TinyNetException("Search depth >10000, this may caused by careless programming.");

		if ("*".equals(type1) && !selected.isEmpty())
			findNodeSetforNodes(level + 1, path, selected, result);

		if (path.getNextPath() != null) {
			findNodeSetforNodes(level + 1, path.getNextPath(), selected, result);
		}
	}

	private void validateSelected(Integer level, Path path, Set<Node> selected, Collection<Node> nodesToCheck) {
		NodeValidator checker = path.getNodeValidator();
		if (checker == null)
			checker = DefaultNodeValidator.instance;
		for (Node node : nodesToCheck)
			if (checker.validateNode(node, level, selected.size(), path))
				selected.add(node);
	}

	private void cacheSelected(String nodeId, String pathUniqueString, Set<Node> selected) {
		currentPathId++;
		Integer pathId = currentPathId;
		pathIdCache.put(pathUniqueString, pathId);
		Map<Integer, Set<Node>> rootCache = queryCache.get(nodeId);
		if (rootCache == null) {
			rootCache = new HashMap<Integer, Set<Node>>();
			queryCache.put(nodeId, rootCache);
		}
		rootCache.put(pathId, selected);
	}

	// getter & setter=======
	protected Map<Class<?>, TableModel> getConfigModels() {
		return configModels;
	}

	protected Map<Class<?>, LinkedHashMap<String, Node>> getBody() {
		return body;
	}

	public Boolean getAllowQueryCache() {
		return allowQueryCache;
	}

	public void setAllowQueryCache(Boolean allowQueryCache) {
		this.cleanAllQueryCaches();
		this.allowQueryCache = allowQueryCache;
	}

}
