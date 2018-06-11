/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.entitynet;

import java.lang.reflect.Method;
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
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * EntityNet is a child project to build entity net. it's a memory based Object
 * net, can kind of be called "Graph Database" or "NoSQL Database". Not like
 * Neo4j can write to disk file, EntityNet is only a memory based graph 1:1
 * mapping to relational database's tables, the relationship in Neo4j is call
 * "Edge", but in EntityNet is still called relationship, just exactly use the
 * existed relational database's foreign key constraints. If want use EntityNet
 * but don't want output foreign key constraint in DDL, can build fake
 * FKeyModels by setting "ddl=false" (see jDialects project).
 * 
 * Some benefits to use a graph database than traditional database:<br/>
 * 1) No need write complicated join SQLs. <br/>
 * 2) Working in memory, much quicker browse speed between nodes than access
 * database records stored on hard disk. <br/>
 * 3) Can use pure Java language do browsing search.
 * 
 * EntityNet class is not thread safe. If want use it as a global cache,
 * programmer need use synchronized method to serialize modify it, like use a
 * HashMap in multiple threads environment.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class EntityNet {

	/** Used to combine compound key column names into a single String */
	public static final String COMPOUND_COLUMNNAME_SEPARATOR = "_CpdIdSpr_";

	/** Used to combine compound key column values into a single String */
	public static final String COMPOUND_VALUE_SEPARATOR = "_CpdValSpr_";

	/** SqlBoxContext instance */
	SqlBoxContext sqlBoxContext;

	/**
	 * ConfigModels is virtual meta data of EntityNet, and also store O-R mapping
	 * info related to database
	 */
	private Map<Class<?>, TableModel> configModels = new HashMap<Class<?>, TableModel>();

	/** The row Data loaded from database */
	private List<Map<String, Object>> rowData = new ArrayList<Map<String, Object>>();

	/** The body of the EntityNet */
	// entityClass, nodeID, node
	private Map<Class<?>, LinkedHashMap<Object, Node>> body = new HashMap<Class<?>, LinkedHashMap<Object, Node>>();

	protected void constructors__________________________() {// NOSONAR
	}

	public EntityNet(SqlBoxContext sqlBoxContext) {
		this.sqlBoxContext = sqlBoxContext;
	}

	/** Create a EntityNet by given configurations, load all columns */
	public EntityNet loadAll(Object... configObjects) {
		return loadAllOrSketch(false, configObjects);
	}

	/** Create a EntityNet instance but only load PKey and FKeys columns */
	public EntityNet loadSketch(Object... configObjects) {
		return loadAllOrSketch(true, configObjects);
	}

	/**
	 * Create a EntityNet instance, load data from database buy given loadKeyOnly
	 * and configObjects parameters
	 * 
	 * @param ctx
	 *            A SqlBoxContext instance
	 * @param loadSketchOnly
	 *            If true will only load PKey and FKeys field only
	 * @param configObjects
	 *            netConfigs array, can be entity class, entity, SqlBox or
	 *            TableModel instance
	 * @return The EntityNet
	 */
	private EntityNet loadAllOrSketch(boolean loadSketchOnly, Object... configObjects) {
		if (configObjects == null || configObjects.length == 0)
			throw new EntityNetException("LoadNet() does not support empty netConfigs parameter");
		TableModel[] models = SqlBoxContextUtils.objectConfigsToModels(sqlBoxContext, configObjects);
		EntityNet net = new EntityNet(sqlBoxContext);
		String starOrSharp = loadSketchOnly ? ".##" : ".**";
		for (TableModel t : models) {
			List<Map<String, Object>> mapList = null;
			String alias = t.getAlias();
			if (StrUtils.isEmpty(alias))
				alias = t.getTableName();
			mapList = sqlBoxContext.iQuery(new SSMapListHandler(t),
					"select " + alias + starOrSharp + " from " + t.getTableName() + " as " + alias);
			net.add(mapList, t);
		}
		return net;
	}

	protected void addAndRemove__________________________() {// NOSONAR
	}

	/**
	 * Add a List<Map<String, Object>> list to entity net
	 *
	 * @param listMap
	 * @param modelConfigs
	 * @return EntityNet it self
	 */
	public EntityNet add(List<Map<String, Object>> listMap, Object... configObjects) {
		if (listMap == null)
			throw new EntityNetException("Can not join null listMap");

		TableModel[] configs = SqlBoxContextUtils.objectConfigsToModels(sqlBoxContext, configObjects);

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
			addOneRowMapList(map);
		}
		return this;
	}

	/** Join MapListWrap to existed EntityNet */
	public EntityNet add(MapListWrap mapListWrap) {
		return add(mapListWrap.getMapList(), mapListWrap.getConfig());
	}

	/**
	 * Assembly one row of Map List to Entities, according net's configModels
	 */
	protected void addOneRowMapList(Map<String, Object> oneRow) {
		for (TableModel model : this.configModels.values()) {
			Object entity = null;
			String alias = model.getAlias();
			if (StrUtils.isEmpty(alias))
				throw new EntityNetException("No alias found for table '" + model.getTableName() + "'");
			Set<String> loadedFields = new HashSet<String>();

			// create new Entity
			for (Entry<String, Object> row : oneRow.entrySet()) { // u_userName
				for (ColumnModel col : model.getColumns()) {
					if (row.getKey().equalsIgnoreCase(alias + "_" + col.getColumnName())) {
						if (entity == null)
							entity = ClassCacheUtils.createNewEntity(model.getEntityClass());
						EntityNetException.assureNotEmpty(col.getEntityField(),
								"EntityField not found for column '" + col.getColumnName() + "'");
						ClassCacheUtils.writeValueToBeanField(entity, col.getEntityField(), row.getValue());
						loadedFields.add(col.getEntityField());
					}
				}
			}
			if (entity != null)
				this.addOrJoinEntity(entity, loadedFields);
		}
	}

	/**
	 * Add a Entity into entity net
	 */
	public void addEntity(Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(sqlBoxContext, entity);
		TableModel tableModel = box.getTableModel();
		Set<String> loadedFields = new HashSet<String>();
		for (ColumnModel col : tableModel.getColumns())
			loadedFields.add(col.getEntityField());
		addOrJoinEntity(entity, loadedFields);
	}

	/** Remove entity from current entityNet */
	public void removeEntity(Object entity) {
		Object id = EntityNetUtils.buildNodeIdFromEntity(this, entity);
		body.get(entity.getClass()).remove(id);
	}

	/** Update entity in current entityNet */
	public void updateEntity(Object entity) {
		removeEntity(entity);
		addEntity(entity);
	}

	/**
	 * Add an entity to EntityNet, if already have same PKEY entity exist, if old
	 * entity field is never loaded from database, will put new entity's value
	 */
	protected void addOrJoinEntity(Object entity, Set<String> loadedFields) {
		Object id = EntityNetUtils.buildNodeIdFromEntity(this, entity);
		if (id == null)
			return;
		List<ParentRelation> parentRelations = EntityNetUtils.transferFKeysToParentRelations(this, entity);
		Node node = new Node();
		node.setEntity(entity);
		node.setParentRelations(parentRelations);
		node.setId(id);
		node.setLoadedFields(loadedFields);
		addOrJoinOneNode(node);
	}

	/**
	 * Add or join an node into EntityNet body, if old node with same ID already
	 * exist, join loaded fields and ParentRelation
	 */
	protected void addOrJoinOneNode(Node node) {
		Node oldNode = findNode(node);
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
		EntityNetException.assureNotNull(node, "Can not add null node");
		EntityNetException.assureNotNull(node.getEntity(), "Can not add node with null entity");
		Class<?> entityClass = node.getEntity().getClass();
		LinkedHashMap<Object, Node> nodeMap = body.get(entityClass);
		if (nodeMap == null) {
			nodeMap = new LinkedHashMap<Object, Node>();
			body.put(entityClass, nodeMap);
		}
		nodeMap.put(node.getId(), node);
	}

	/** Add one node into EntityNet body */
	protected void addNodes(Collection<Node> nodes) {
		for (Node node : nodes)
			addNode(node);
	}

	protected void core__________________________() {// NOSONAR
	}

	/** Find a Node by give node's id */
	private Node findNode(Node node) {
		if (node == null || node.getEntity() == null)
			return null;
		LinkedHashMap<Object, Node> nodes = body.get(node.getEntity().getClass());
		if (nodes == null)
			return null;
		return nodes.get(node.getId());
	}

	/** Return total how many nodes */
	public int size() {
		int size = 0;
		for (LinkedHashMap<Object, Node> map : body.values()) {
			size += map.size();
		}
		return size;
	}

	/** Return true if nodes size is 0 */
	public boolean isEmpty() {
		return 0 == size();
	}

	// ============= Find methods=============================

	/**
	 * Run a Path, return a new created EntityNet according the path
	 * 
	 * @param path
	 *            The path instance
	 * @param input
	 *            Optional input entities
	 * @return A new EntityNet instance
	 */
	public EntityNet runPath(Path path, Object... input) {// NOSONAR
		Map<Class<?>, Set<Node>> output = new HashMap<Class<?>, Set<Node>>();
		List<Node> inputNodes = null;
		if (input != null) {
			inputNodes = new ArrayList<Node>();
			for (Object obj : input)
				if (obj != null) {
					if (obj instanceof Node)
						inputNodes.add((Node) obj);
					else {
						Node node = EntityNetUtils.entity2Node(this, obj);
						if (node != null)
							inputNodes.add(node);
					}
				}
		}
		Path topPath = path.getTopPath();
		topPath.initializePath(this);
		realRunPath(0, topPath, inputNodes, output);
		return EntityNetUtils.nodeSetMapToEntityNet(this, output);
	}

	/**
	 * According given path and input Node Set, find related node set
	 * 
	 * @param level
	 *            search level, start from 0
	 * @param path
	 *            The Path
	 * @param input
	 *            The input node collection
	 * @param output
	 *            The output node collection
	 * @return Related node set
	 */
	private void realRunPath(Integer level, Path path, Collection<Node> input, // NOSONAR
			Map<Class<?>, Set<Node>> result) {
		if (level > 1000)
			throw new EntityNetException(
					"Search level beyond 1000, this may caused by a circular reference path chain.");
		TableModel model = path.getTargetModel();
		String type0 = path.getType().substring(0, 1);
		String type1 = path.getType().substring(1, 2);
		Class<?> targetClass = model.getEntityClass();

		Set<Node> selected = new LinkedHashSet<Node>();
		// Start
		if ("S".equalsIgnoreCase(type0)) {
			if (level != 0)
				throw new EntityNetException("'S' type can only be used on path start");

			Collection<Node> nodesToCheck = pickNodeSet(targetClass);
			validateSelected(level, path, selected, nodesToCheck);
		} else
		// Child
		if ("C".equalsIgnoreCase(type0) && input != null && !input.isEmpty()) {
			for (Node inputNode : input) {

				// Find childNodes meat class/columns/id condition
				Set<Node> nodesToCheck = new LinkedHashSet<Node>();
				for (Entry<Object, Node> cNode : body.get(targetClass).entrySet()) {
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
			}
		} else
		// Parent
		if ("P".equalsIgnoreCase(type0) && input != null && !input.isEmpty()) {
			String targetTableName = model.getTableName();
			EntityNetException.assureNotEmpty(targetTableName, "targetTableName can not be null");
			for (Node inputNode : input) {
				// Find parent nodes meat tableName/refColumns/nodeId condition
				Set<Node> nodesToCheck = new LinkedHashSet<Node>();
				List<ParentRelation> prs = inputNode.getParentRelations();
				if (prs != null)
					for (ParentRelation pr : prs) {
						if (targetTableName.equalsIgnoreCase(pr.getParentTable())
								&& path.getRefColumns().equalsIgnoreCase(pr.getRefColumns())) {
							Node node = this.pickOneNode(targetClass, pr.getParentId());
							if (node != null)
								nodesToCheck.add(node);
						}
					}
				validateSelected(level, path, selected, nodesToCheck);
			}
		}
		Set<Node> nodes = result.get(targetClass);
		if (nodes == null) {
			nodes = new LinkedHashSet<Node>();
			result.put(targetClass, nodes);
		}

		if ("+".equals(type1) || "*".equals(type1))
			nodes.addAll(selected);

		if (level > 10000)
			throw new EntityNetException("Search depth >10000, this may caused by careless programming.");

		if ("*".equals(type1) && !selected.isEmpty())
			realRunPath(level + 1, path, selected, result);

		if (path.getNextPath() != null) {
			realRunPath(level + 1, path.getNextPath(), selected, result);
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

	protected void giveAndPick__________________________() {// NOSONAR
		//TODO: work at give methods
	}

	/**
	 * Give a's value to b's field "a" or "aList" or "aSet"
	 */
	public EntityNet give(Class<?> a, Class<?> b) {
		return give(a, b, a.getSimpleName());
	}

	/**
	 * Give a's value to b's field "bFieldName"
	 */
	public EntityNet give(Class<?> a, Class<?> b, String fd) {
		String upFieldname = StrUtils.toUpperCaseFirstOne(fd);
		String bSetMethodName = "get" + upFieldname;
		Method bSetMethod = ClassCacheUtils.checkMethodExist(b, bSetMethodName);
		if (bSetMethod != null)
			doGive(a, b, bSetMethodName, bSetMethod);
		else {
			bSetMethodName = bSetMethodName + "List";
			bSetMethod = ClassCacheUtils.checkMethodExist(b, bSetMethodName);
			if (bSetMethod != null)
				doGive(a, b, bSetMethodName, bSetMethod);
			else {
				bSetMethodName = "get" + bSetMethodName + "Set";
				bSetMethod = ClassCacheUtils.checkMethodExist(b, bSetMethodName);
				if (bSetMethod != null)
					doGive(a, b, bSetMethodName, bSetMethod);
			}
		}
		return this;
	}

	/**
	 * Give a's value to b's field "a" or "aList" or "aSet" <br/>
	 * And Give b's value to a's field "b" or "bList" or "bSet"
	 */
	public EntityNet giveBoth(Class a, Class b) {
		give(a, b, a.getSimpleName());
		give(b, a, b.getSimpleName());
		return this;
	}

	private void doGive(Class a, Class b, String bFieldName, Method bMethod) {

	}

	/** Pick a Node by given entityClass and nodeId */
	public Node pickOneNode(Class<?> entityClass, Object nodeId) {
		LinkedHashMap<Object, Node> nodesMap = body.get(entityClass);
		if (nodesMap == null)
			return null;
		return nodesMap.get(nodeId);
	}

	/** Pick Node set in EntityNet which type is entityClass */
	public Set<Node> pickNodeSet(Class<?> entityClass) {
		Set<Node> result = new LinkedHashSet<Node>();
		LinkedHashMap<Object, Node> nodesMap = body.get(entityClass);
		if (nodesMap == null || nodesMap.isEmpty())
			return result;
		result.addAll(nodesMap.values());
		return result;
	}

	/**
	 * Pick a entity by given entityClass and entityId or Id HashMap<String,Object>
	 */
	@SuppressWarnings("unchecked")
	public <T> T pickOneEntity(Class<?> entityClass, Object entityIdOrIdMap) {
		Node node = pickOneNode(entityClass, EntityNetUtils.buildNodeIdFromUnknow(entityIdOrIdMap));
		if (node == null)
			return null;
		return (T) node.getEntity();
	}

	/** Pick entity set in EntityNet which type is entityClass */
	public <T> Set<T> pickEntitySet(Class<T> entityClass) {
		return EntityNetUtils.nodeCollection2EntitySet(pickNodeSet(entityClass));
	}

	/** Pick entity List in EntityNet which type is entityClass */
	public <T> List<T> pickEntityList(Class<T> entityClass) {
		return EntityNetUtils.nodeCollection2EntityList(pickNodeSet(entityClass));
	}

	/** Pick entity set in EntityNet which type is entityClass */
	@SuppressWarnings("unchecked")
	public Map<Class<?>, Set<Object>> pickEntitySetMap() {
		Map<Class<?>, Set<Object>> result = new HashMap<Class<?>, Set<Object>>();
		for (Class<?> claz : body.keySet())
			result.put(claz, this.pickEntitySet((Class<Object>) claz));
		return result;
	}

	// ============= Find methods=============================

	protected void getteSetters__________________________() {// NOSONAR
	}

	public Map<Class<?>, TableModel> getConfigModels() {
		return configModels;
	}

	public void setConfigModels(Map<Class<?>, TableModel> configModels) {
		this.configModels = configModels;
	}

	public Map<Class<?>, LinkedHashMap<Object, Node>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, LinkedHashMap<Object, Node>> body) {
		this.body = body;
	}

	public SqlBoxContext getSqlBoxContext() {
		return sqlBoxContext;
	}

	public void setSqlBoxContext(SqlBoxContext sqlBoxContext) {
		this.sqlBoxContext = sqlBoxContext;
	}

	public List<Map<String, Object>> getRowData() {
		return rowData;
	}

	public void setRowData(List<Map<String, Object>> rowData) {
		this.rowData = rowData;
	}

}
