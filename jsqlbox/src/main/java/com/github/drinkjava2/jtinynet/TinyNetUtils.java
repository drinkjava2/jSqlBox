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
import java.util.List;

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
	public static String joinPKeyValues(TableModel model, Object entity) {
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

	/** Convert a Node list to Entity List */
	public static <T> List<T> nodeList2EntityList(List<Node> nodeList) {
		List<T> result = new ArrayList<T>();
		for (Node node : nodeList)
			result.add((T) node.getEntity());
		return result;
	}

}
