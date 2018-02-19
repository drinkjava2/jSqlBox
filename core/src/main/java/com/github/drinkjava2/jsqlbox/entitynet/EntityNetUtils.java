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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;

/**
 * Store static methods about EntityNet
 * 
 * @author Yong Zhu 
 * @since 1.0.0
 */
public class EntityNetUtils {

 

	public static final ThreadLocal<WeakHashMap<Object, Object>> netConfigBindToListCache = new ThreadLocal<WeakHashMap<Object, Object>>() {
		@Override
		protected WeakHashMap<Object, Object> initialValue() {
			return new WeakHashMap<Object, Object>();
		}
	};

	public static void removeBindedTableModel(List<?> listMap) {
		netConfigBindToListCache.get().remove(listMap);
	}

	public static TableModel[] getBindedTableModel(List<?> listMap) {
		return (TableModel[]) netConfigBindToListCache.get().get(listMap);
	}

	public static void bindTableModel(Object listMap, TableModel[] tableModels) {
		netConfigBindToListCache.get().put(listMap, tableModels);
	}
	
	/**
	 * After a query, listMap may binded a threadLocal type TableModel[] netConfigs,
	 * this method used to join the binded tableModels with given configObjects,
	 * return a new TableModel[],
	 */
	public static TableModel[] joinConfigsModels(SqlBoxContext ctx, List<Map<String, Object>> listMap,
			Object... configObjects) {
		// bindeds: tableModels entityClass and alias may be empty
		// given: tableModels should have entityClass, alias may be null
		TableModel[] bindeds = getBindedTableModel(listMap);
		removeBindedTableModel(listMap);
		if (bindeds == null || bindeds.length == 0)
			bindeds = new TableModel[0];

		TableModel[] givens;
		if (configObjects != null && configObjects.length > 0)
			givens = objectConfigsToModels(ctx, configObjects);
		else
			givens = new TableModel[0];

		return EntityNetUtils.jointConfigModels(bindeds, givens);
	}

	public static TableModel[] jointConfigModels(TableModel[] bindeds, TableModel[] givens) {
		// check setted to avoid user set empty value to TableModel
		Map<String, TableModel> uses = new HashMap<String, TableModel>();
		for (TableModel tb : givens) {
			SqlBoxException.assureNotNull(tb.getEntityClass(),
					"EntityClass setting can not be null for '" + tb.getTableName() + "'");
			SqlBoxException.assureNotEmpty(tb.getTableName(),
					"TableName setting can not be empty for '" + tb.getTableName() + "'");
			uses.put(tb.getTableName().toLowerCase(), tb);
		}

		for (TableModel tb : bindeds) {
			SqlBoxException.assureNotEmpty(tb.getTableName(),
					"TableName setting can not be empty for '" + tb.getTableName() + "'");
			TableModel exist = uses.get(tb.getTableName().toLowerCase());
			if (tb.getEntityClass() != null) {// it's binded by has entityClass
				if (exist == null)
					uses.put(tb.getTableName().toLowerCase(), tb);
				else // exist and current tb both can use, duplicated
					throw new SqlBoxException("Duplicated entityClass setting for '" + tb.getTableName() + "'");
			}
		}

		for (TableModel tb : bindeds) { // use alias to fill
			TableModel exist = uses.get(tb.getTableName().toLowerCase());
			if (exist != null && tb.getEntityClass() == null) {// it's binded by
																// has
																// entityClass
				String alias = tb.getAlias();
				if (!StrUtils.isEmpty(alias) && StrUtils.isEmpty(exist.getAlias()))
					exist.setAlias(alias);
			}
		}
		TableModel[] result = new TableModel[uses.size()];
		int i = 0;
		for (Entry<String, TableModel> entry : uses.entrySet()) {
			result[i++] = entry.getValue();
		}
		return result;
	}

	/**
	 * If nodeValidator is object, return it, otherwise it is a NodeValidator class,
	 * build a new instance as return
	 */
	@SuppressWarnings("unchecked")
	public static NodeValidator getOrBuildValidator(Object nodeValidator) {
		if (nodeValidator == null)
			return null;
		if (nodeValidator instanceof NodeValidator)
			return (NodeValidator) nodeValidator;
		Class<NodeValidator> c = (Class<NodeValidator>) nodeValidator;
		try {
			return c.newInstance();
		} catch (Exception e) {
			throw new EntityNetException("Can not create instance of checker class " + nodeValidator);
		}
	}

	/**
	 * Join fkey column names into one String, used for reference ID
	 */
	public static String buildJoinedColumns(String... columns) {
		StringBuilder sb = new StringBuilder();
		for (String columnName : columns) {
			if (sb.length() > 0)
				sb.append(EntityNet.COMPOUND_COLUMNNAME_SEPARATOR);
			sb.append(columnName);
		}
		return sb.toString();
	}

	/**
	 * Check if each TableModel has entityClass and Alias, if no, throw exception
	 */
	public static void checkModelHasEntityClassAndAlias(TableModel... models) {
		if (models != null && models.length > 0)// Join models
			for (TableModel tb : models) {
				if (tb.getEntityClass() == null)
					throw new EntityNetException(
							"TableModel entityClass not set for table '" + tb.getTableName() + "'");
				if (StrUtils.isEmpty(tb.getAlias()))
					throw new EntityNetException("TableModel alias not set for table '" + tb.getTableName() + "'");
			}
	}

	/**
	 * Join PKey values into one String, used for node ID
	 */
	public static String buildNodeId(EntityNet net, Object entity) {
		if (entity == null)
			return null;
		if (net.getConfigModels() == null)
			return null;
		return buildNodeId(net.getConfigModels().get(entity.getClass()), entity);
	}

	/**
	 * Join PKey values into one String, used for node ID
	 */
	public static String buildNodeId(TableModel model, Object entity) {
		if (model == null || entity == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : model.getColumns()) {
			if (col.getPkey() && !col.getTransientable()) {
				EntityNetException.assureNotEmpty(col.getEntityField(),
						"EntityField not found for FKey column '" + col.getColumnName() + "'");
				if (sb.length() > 0)
					sb.append(EntityNet.COMPOUND_COLUMNNAME_SEPARATOR);
				sb.append(ClassCacheUtils.readValueFromBeanField(entity, col.getEntityField()));
			}
		}
		if (sb.length() == 0)
			throw new EntityNetException("Table '" + model.getTableName() + "' no Prime Key columns set");
		return sb.toString();
	}

	/**
	 * Transfer FKey values to ParentRelation list
	 */
	public static List<ParentRelation> transferFKeysToParentRelations(TableModel model, Object entity) {
		List<ParentRelation> resultList = null;
		for (FKeyModel fkey : model.getFkeyConstraints()) {
			String refTable = fkey.getRefTableAndColumns()[0];
			String fkeyValues = "";
			String fkeyColumns = "";
			for (String colNames : fkey.getColumnNames()) {
				String entityField = model.getColumn(colNames).getEntityField();
				Object fKeyValue = ClassCacheUtils.readValueFromBeanField(entity, entityField);
				if (StrUtils.isEmpty(fKeyValue)) {
					fkeyValues = null;
					break;
				}
				if (fkeyValues.length() > 0)
					fkeyValues += EntityNet.COMPOUND_VALUE_SEPARATOR;
				fkeyValues += fKeyValue;// NOSONAR

				if (fkeyColumns.length() > 0)
					fkeyColumns += EntityNet.COMPOUND_COLUMNNAME_SEPARATOR;
				fkeyColumns += colNames;// NOSONAR
			}
			if (!StrUtils.isEmpty(fkeyColumns) && !StrUtils.isEmpty(fkeyValues) && !StrUtils.isEmpty(refTable)) {
				if (resultList == null)
					resultList = new ArrayList<ParentRelation>();
				resultList.add(new ParentRelation(fkeyColumns, fkeyValues, refTable));
			}
		}
		return resultList;
	}

	/** Convert a Entity list to Node List */
	public static Node entity2Node(EntityNet net, Object entity) {
		if (entity == null)
			return null;
		Map<String, Node> map = net.getBody().get(entity.getClass());
		if (map == null)
			return null;
		return map.get(EntityNetUtils.buildNodeId(net, entity));
	}

	/** Convert entity array to Node set */
	public static Set<Node> entityArray2NodeSet(EntityNet net, Object... entities) {
		Set<Node> result = new LinkedHashSet<Node>();
		if (entities == null)
			return result;
		for (Object entity : entities) {
			Node node = entity2Node(net, entity);
			if (node != null)
				result.add(node);
		}
		return result;
	}

	/** Convert entity array to Node set */
	public static Set<Node> entityCollection2NodeSet(EntityNet net, Collection<Object> entities) {
		Set<Node> result = new LinkedHashSet<Node>();
		if (entities == null)
			return result;
		for (Object entity : entities) {
			Node node = entity2Node(net, entity);
			if (node != null)
				result.add(node);
		}
		return result;
	}

	/** Convert a node collection to entity set */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> nodeCollection2EntitySet(Collection<Node> nodes) {
		Set<T> result = new LinkedHashSet<T>();
		if (nodes == null)
			return result;
		for (Node node : nodes)
			result.add((T) node.getEntity());
		return result;
	}

	/** Convert a Node Set Map to an entity set map */
	public static Map<Class<?>, Set<Object>> nodeSetMapToEntitySetMap(Map<Class<?>, Set<Node>> nodeMap) {
		Map<Class<?>, Set<Object>> resultMap = new HashMap<Class<?>, Set<Object>>();
		for (Entry<Class<?>, Set<Node>> entry : nodeMap.entrySet()) {
			Set<Object> set = new LinkedHashSet<Object>();
			for (Object obj : entry.getValue()) {
				set.add(obj);
			}
			resultMap.put(entry.getKey(), set);
		}
		return resultMap;
	}

	/** Convert a node collection to entity list */
	@SuppressWarnings("unchecked")
	public static <T> List<T> nodeCollection2EntityList(Collection<Node> nodes) {
		List<T> result = new ArrayList<T>();
		if (nodes == null)
			return result;
		for (Node node : nodes)
			result.add((T) node.getEntity());
		return result;
	}

	/**
	 * Transfer Object[] to TableModel[], object can be SqlBox instance, entityClass
	 * or entity Bean
	 * 
	 * <pre>
	 * 1. TableModel instance, will use it
	 * 2. SqlBox instance, will use its tableModel
	 * 3. Class, will call ctx.createSqlBox() to create a SqlBox instance and use its tableModel
	 * 4. Object, will call SqlBoxUtils.findAndBindSqlBox() to create a SqlBox instance
	 * </pre>
	 */
	public static TableModel[] objectConfigsToModels(SqlBoxContext ctx, Object[] netConfigs) {
		if (netConfigs == null || netConfigs.length == 0)
			return new TableModel[0];
		TableModel[] result = new TableModel[netConfigs.length];
		for (int i = 0; i < netConfigs.length; i++) {
			Object obj = netConfigs[i];
			if (obj == null)
				throw new SqlBoxException("Can not convert null to SqlBox instance");
			if (obj instanceof TableModel)
				result[i] = (TableModel) obj;
			else if (obj instanceof SqlBox)
				result[i] = ((SqlBox) obj).getTableModel();
			else if (obj instanceof Class)
				result[i] = SqlBoxUtils.createSqlBox(ctx, (Class<?>) obj).getTableModel();
			else {
				result[i] = SqlBoxUtils.findAndBindSqlBox(ctx, obj).getTableModel();
			}
		}
		return result;
	}
}
