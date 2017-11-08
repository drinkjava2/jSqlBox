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
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * a memory based entity net, can be called "graph database". Not like Neo4j can
 * write to disk file, jTinyNet is only a pure memory graph database, it's 1:1
 * mapping to relationship database's tables, the relationship in Neoj4 is call
 * "Edge", but in jTinyNet is still called relationship, just exactly use the
 * existed relationship database's foreign key constraints. If want create
 * relationship between nodes but do not want output FKey constraint DDL, can
 * build a fake FKeyModel by setting "ddl=false" (see jDialects project).
 * 
 * There are some benefits to use a graph database:<br/>
 * 1) Much quicker browse speed between connected nodes than database. <br/>
 * 2) Works in memory, can be used as query cache. <br/>
 * 3) No need write SQLs. <br/>
 * 
 * In jTinyNet, do complicated search still use pure Java language, jTinyNet
 * does not support Cypher language because it's too complicated and too hard to
 * me, another reason is cypher does not support refactoring.
 * 
 * TinyNet class is not thread safe. If want use it as a global Cache,
 * programmer need use synchronized method to serialize access it, like use a
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

	/**
	 * ConfigModels is virtual meta data of EntityNet, and also store O-R
	 * mapping info related to database
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The body of the EntityNet */
	private Map<Class<?>, LinkedHashMap<String, Node>> body = new HashMap<Class<?>, LinkedHashMap<String, Node>>();

	/**
	 * childCache cache Child nodes, Child determined by 4 facts: selfID,
	 * childClass, child's fkeyColumns, child's ID is the target need to cache
	 * 
	 * For example, a User "Sam" many have below 4 ChildRelationShips to another
	 * user Tom, it means Sam is Tom's teacher, father, driver and boss:<br/>
	 * 
	 * <Sam, <student_tb, <teacher_fisrtName+lastName, Tom_li> <br/>
	 * <Sam, <student_tb, <father_fisrtName+lastName, Tom_li> <br/>
	 * <Sam, <user_tb, <driver_fisrtName+lastName, Tom_li> <br/>
	 * <Sam, <employee_tb, <boss_fisrtName+lastName, Tom_li> <br/>
	 * 
	 * To improve EntityNet initialisation speed, ChildCache will not be filled
	 * until do a entity search and also parent nodes need exact in search path,
	 * this is called "Delay Cache". Write to EntityNet may cause partial or
	 * whole cache be cleared.
	 */
	private Map<String, Map<String, Map<String, String>>> childCache = new HashMap<String, Map<String, Map<String, String>>>();
	
	//           id , pathName, childIDs       
	private Map<String, Map<String, Map<String, String>>> pathCaches = new HashMap<String, Map<String, Map<String, String>>>();

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
		String id = TinyNetUtils.joinPKeyValues(model, entity);
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

	// ============= Find methods=============================
	public <T> List<T> findEntityList(Object entity, Path... conditions) {
		List<Node> srcNodeList = TinyNetUtils.entity2NodeList(this, entity);
		List<Node> resultNodeList = findNodes(srcNodeList, conditions);
		return TinyNetUtils.nodeList2EntityList(resultNodeList);
	}

	public <T> List<T> findEntityList(List<T> srcEntityList, Path... conditions) {
		List<Node> srcNodeList = TinyNetUtils.entityList2NodeList(this, srcEntityList);
		List<Node> resultNodeList = findNodes(srcNodeList, conditions);
		return TinyNetUtils.nodeList2EntityList(resultNodeList);
	}

	/**
	 * According given nodeList and Search condition, find related nodes,
	 * multiple conditions will execute multiple path searches, result list will
	 * be the union of each search
	 * 
	 * @param inputList
	 *            The source node list
	 * @param conditions
	 *            The search condition
	 * @return The target node list
	 */
	public List<Node> findNodes(List<Node> inputList, Path... conditions) {
		if (conditions == null || conditions.length == 0)
			throw new TinyNetException("Search condition can not be null");
		if (inputList == null || inputList.size() == 0)
			return new ArrayList<Node>();
		if (conditions.length == 1) {
			List<Node> resultList = new ArrayList<Node>();
			TinyNetUtils.findNodes(this, inputList, resultList, conditions[0]);
			return resultList;
		}
		List<Node> summaryResultList = new ArrayList<Node>();
		for (Path condition : conditions) {
			List<Node> oneSearchResult = findNodes(inputList, condition);
			summaryResultList.addAll(oneSearchResult);
		}
		return summaryResultList;
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

	public Map<String, Map<String, Map<String, String>>> getChildCache() {
		return childCache;
	}

	public void setChildCache(Map<String, Map<String, Map<String, String>>> childCache) {
		this.childCache = childCache;
	}

}
