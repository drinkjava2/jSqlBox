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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	 * Search in EntityNet, return qualified entities
	 * 
	 * @param net The EntityNet
	 * @param sourceEntity The source entities
	 * @param targetEntityClass The target entity class
	 * @param path The EntitySearchPath
	 * @return qualified entities
	 */
	public static List<Node> findRelated(TinyNet net, List<Node> nodeList, SearchPath path) {
		return null;// TODO here
	}

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
	 * Transfer PKey values to a entityID String, format:
	 * tablename_id1value_id2value
	 */
	public static String transferPKeysToID(TableModel model, Object entity) {
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
	 * Transfer FKey values to String set, format: table1_id1value_id2value,
	 * table2_id1_id2... <br/>
	 */
	public static List<Object[]> transferFKeysToParentIDs(TableModel model, Object entity) {
		List<Object[]> resultList = null;

		Set<String> result = new HashSet<String>();
		for (FKeyModel fkey : model.getFkeyConstraints()) {
			String fTable = fkey.getRefTableAndColumns()[0];
			String fkeyEntitID = "";
			String fkeyCol = "";
			for (String colNames : fkey.getColumnNames()) {
				String entityField = model.getColumn(colNames).getEntityField();
				Object fKeyValue = ClassCacheUtils.readValueFromBeanField(entity, entityField);
				if (StrUtils.isEmpty(fKeyValue)) {
					fkeyEntitID = null;
					break;
				}
				if (fkeyEntitID.length() > 0)
					fkeyEntitID += TinyNet.COMPOUND_VALUE_SEPARATOR;
				fkeyEntitID += fKeyValue;// NOSONAR
			}
			if (!StrUtils.isEmpty(fkeyEntitID))
				resultList.add(new Object[] {    });//TODO: work at here
		}
		return resultList;
	} 

	/** Convert a Node list to Entity List */
	public static <T> List<T> nodeList2EntityList(List<Node> nodeList) {
		List<T> result = new ArrayList<T>();
		for (Node node : nodeList)
			result.add((T) node.getEntity());
		return result;
	}

}
