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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * TinyNetUtils is utility class store public static methods about TinyNet
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class TinyNetUtils {

	/**
	 * Check if each TableModel has entityClass and Alias, if no, throw exception
	 */
	public static void checkModelHasEntityClassAndAlias(TableModel... models) {
		if (models != null && models.length > 0)// Join models
			for (TableModel tb : models) {
				if (tb.getEntityClass() == null)
					throw new TinyNetException("TableModel entityClass not set for table '" + tb.getTableName() + "'");
				if (StrUtils.isEmpty(tb.getAlias()))
					throw new TinyNetException("TableModel alias not set for table '" + tb.getTableName() + "'");
			}
	}

	/**
	 * Join PKey values into one String, used for node ID
	 */
	public static String buildNodeId(TinyNet net, Object entity) {
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
				TinyNetException.assureNotEmpty(col.getEntityField(),
						"EntityField not found for FKey column '" + col.getColumnName() + "'");
				if (sb.length() > 0)
					sb.append(TinyNet.COMPOUND_COLUMNNAME_SEPARATOR);
				sb.append(ClassCacheUtils.readValueFromBeanField(entity, col.getEntityField()));
			}
		}
		if (sb.length() == 0)
			throw new TinyNetException("Table '" + model.getTableName() + "' no Prime Key columns set");
		return sb.toString();
	}

	/**
	 * Join fkey column names into one String, used for reference ID
	 */
	public static String buildJoinedColumns(String... columns) {
		StringBuilder sb = new StringBuilder();
		for (String columnName : columns) {
			if (sb.length() > 0)
				sb.append(TinyNet.COMPOUND_COLUMNNAME_SEPARATOR);
			sb.append(columnName);
		}
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
					fkeyValues += TinyNet.COMPOUND_VALUE_SEPARATOR;
				fkeyValues += fKeyValue;// NOSONAR

				if (fkeyColumns.length() > 0)
					fkeyColumns += TinyNet.COMPOUND_COLUMNNAME_SEPARATOR;
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

	public static NodeValidator getOrBuildChecker(Object checker) {
		if (checker == null)
			return null;
		if (checker instanceof NodeValidator)
			return (NodeValidator) checker;
		Class<NodeValidator> c = (Class<NodeValidator>) checker;
		try {
			return c.newInstance();
		} catch (Exception e) {
			throw new TinyNetException("Can not create instance of checker class " + checker);
		}
	}

	/** Convert a Entity list to Node List */
	public static Node entity2Node(TinyNet net, Object entity) {
		if (entity == null)
			return null;
		Map<String, Node> map = net.getBody().get(entity.getClass());
		if (map == null)
			return null;
		return map.get(buildNodeId(net, entity));
	}

	/** Convert entity array to Node set */
	public static Set<Node> entityArray2NodeSet(TinyNet net, Object... entities) {
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
	public static Set<Node> entityCollection2NodeSet(TinyNet net, Collection<Object> entities) {
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
	public static <T> List<T> nodeCollection2EntityList(Collection<Node> nodes) {
		List<T> result = new ArrayList<T>();
		if (nodes == null)
			return result;
		for (Node node : nodes)
			result.add((T) node.getEntity());
		return result;
	}

}
