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
package com.github.drinkjava2.jnetwk;

import java.util.HashSet;
import java.util.Set;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNetUtils is utility class store public static methods about EntityNet
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNetUtils {

	/**
	 * Search in EntityNet, return qualified entities
	 * 
	 * @param net The EntityNet
	 * @param sourceEntity The source entities
	 * @param targetEntityClass The target entity class
	 * @param path The EntitySearchPath
	 * @return qualified entities
	 */
	public static <T> T[] findRelated(EntityNet net, Object[] sourceEntity, Class<T> targetEntityClass,
			EntitySearchPath path) {
		return null;// TODO here
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
	 * Transfer PKey values to a entityID String, format:
	 * tablename_id1value_id2value
	 */
	public static String transferPKeyToNodeID(TableModel model, Object entity) {
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : model.getColumns()) {
			if (col.getPkey()) {
				EntityNetException.assureNotEmpty(col.getEntityField(),
						"EntityField not found for FKey column '" + col.getColumnName() + "'");
				sb.append(EntityNet.KeySeparator)
						.append(ClassCacheUtils.readValueFromBeanField(entity, col.getEntityField()));
			}
		}
		if (sb.length() == 0)
			throw new EntityNetException("Table '" + model.getTableName() + "' no Prime Key columns set");
		return model.getTableName() + sb.toString();
	}

	/**
	 * Transfer FKey values to String set, format: table1_id1value_id2value,
	 * table2_id1_id2... <br/>
	 */
	public static Set<String> transferFKeysToParentIDs(TableModel model, Object entity) {
		Set<String> result = new HashSet<String>();
		for (FKeyModel fkey : model.getFkeyConstraints()) {
			String fTable = fkey.getRefTableAndColumns()[0];
			String fkeyEntitID = fTable;
			for (String colNames : fkey.getColumnNames()) {
				String entityField = model.getColumn(colNames).getEntityField();
				Object fKeyValue = ClassCacheUtils.readValueFromBeanField(entity, entityField);
				if (StrUtils.isEmpty(fKeyValue)) {
					fkeyEntitID = null;
					break;
				}
				fkeyEntitID += EntityNet.KeySeparator + fKeyValue;
			}
			if (!StrUtils.isEmpty(fkeyEntitID))
				result.add(fkeyEntitID);
		}
		return result;
	}

}
