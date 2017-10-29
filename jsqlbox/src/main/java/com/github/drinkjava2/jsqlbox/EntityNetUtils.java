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
package com.github.drinkjava2.jsqlbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapListHandler;

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
	 * After a query, listMap may binded a threadLocal type TableModel[] netConfigs,
	 * this method used to join the binded tableModels with given netConfigs, return
	 * a new TableModel[],
	 */
	public static TableModel[] joinConfigsIntoModels(SqlBoxContext ctx, List<Map<String, Object>> listMap,
			Object... netConfigs) {
		// bindeds: tableModels entityClass and alias may be empty
		// given: tableModels should have entityClass, alias may be null
		TableModel[] bindeds = EntityNetSqlExplainer.getBindedTableModel(listMap);
		if (bindeds == null || bindeds.length == 0)
			bindeds = new TableModel[0];

		TableModel[] given;
		if (netConfigs != null && netConfigs.length > 0)
			given = EntityNetSqlExplainer.objectConfigsToModels(ctx, netConfigs);
		else
			given = new TableModel[0];

		// check setted to avoid user set empty value to TableModel
		Map<String, TableModel> uses = new HashMap<String, TableModel>();
		for (TableModel tb : given) {
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
			if (exist != null && tb.getEntityClass() == null) {// it's binded by has entityClass
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
	 * Load from database buy given netConfigs array, loadKeyOnly determine if only
	 * load PKey/FKey columns
	 * 
	 * @param ctx Current SqlBoxContext
	 * @param loadKeyOnly If true will only loadKey
	 * @param netConfigs netConfigs array, can be entity class, entity, SqlBox or
	 *            TableModel
	 * @return The EntityNet
	 */
	public static EntityNet loadKeyOrFullNet(SqlBoxContext ctx, boolean loadKeyOnly, Object... netConfigs) {
		if (netConfigs == null || netConfigs.length == 0)
			throw new SqlBoxException("LoadNet() does not support empty netConfigs parameter");
		TableModel[] models = EntityNetSqlExplainer.objectConfigsToModels(ctx, netConfigs);
		EntityNet net = new EntityNet();
		String starOrSharp = loadKeyOnly ? ".##" : ".**";
		for (TableModel t : models) {
			List<Map<String, Object>> mapList = null;
			String alias = t.getAlias();
			if (StrUtils.isEmpty(alias))
				alias = t.getTableName();
			try {
				mapList = ctx.nQuery(new MapListHandler(SqlBoxContext.netProcessor(t)),
						"select " + alias + starOrSharp + " from " + t.getTableName() + " as " + alias);
			} finally {
				EntityNetSqlExplainer.removeBindedTableModel(mapList);
			}
			net.joinList(mapList, t);
		}
		return net;
	}

	/**
	 * Check if each TableModel has entityClass and Alias, if no, throw exception
	 */
	public static void checkModelHasEntityClassAndAlias(TableModel... models) {
		if (models != null && models.length > 0)// Join models
			for (TableModel tb : models) {
				if (tb.getEntityClass() == null)
					throw new SqlBoxException("TableModel entityClass not set for table '" + tb.getTableName() + "'");
				if (StrUtils.isEmpty(tb.getAlias()))
					throw new SqlBoxException("TableModel alias not set for table '" + tb.getTableName() + "'");
			}

	}

	/**
	 * Transfer PKey values to a entityID String, format:
	 * tablename_id1value_id2value
	 */
	public static String transferPKeyToString(SqlBox box, Object entity) {
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : box.getTableModel().getColumns()) {
			if (col.getPkey()) {
				SqlBoxException.assureNotEmpty(col.getEntityField(),
						"EntityField not found for FKey column '" + col.getColumnName() + "'");
				sb.append(EntityNet.KeySep)
						.append(ClassCacheUtils.readValueFromBeanField(entity, col.getEntityField()));
			}
		}
		if (sb.length() == 0)
			throw new SqlBoxException("Table '" + box.table() + "' no Prime Key columns set");
		return box.table() + sb.toString();
	}

	/**
	 * Transfer FKey values to String set, format: table1_id1value_id2value,
	 * table2_id1_id2... <br/>
	 */
	public static Set<String> transferFKeysToString(SqlBox box, Object entity) {
		Set<String> result = new HashSet<String>();
		TableModel tb = box.getTableModel();
		for (FKeyModel fkey : tb.getFkeyConstraints()) {
			String fTable = fkey.getRefTableAndColumns()[0];
			String fkeyEntitID = fTable;
			for (String colNames : fkey.getColumnNames()) {
				String entityField = tb.getColumn(colNames).getEntityField();
				Object fKeyValue = ClassCacheUtils.readValueFromBeanField(entity, entityField);
				if (StrUtils.isEmpty(fKeyValue)) {
					fkeyEntitID = null;
					break;
				}
				fkeyEntitID += EntityNet.KeySep + fKeyValue;
			}
			if (!StrUtils.isEmpty(fkeyEntitID))
				result.add(fkeyEntitID);
		}
		return result;
	}

}
