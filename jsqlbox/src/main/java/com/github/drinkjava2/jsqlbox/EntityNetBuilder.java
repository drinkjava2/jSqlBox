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

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jentitynet.EntityNet;
import com.github.drinkjava2.jentitynet.EntityNetUtils;

/**
 * This is a helper class store public static methods concern to build EntityNet
 */
public class EntityNetBuilder {

	/**
	 * After a query, listMap may binded a threadLocal type TableModel[]
	 * netConfigs, this method used to join the binded tableModels with given
	 * configObjects, return a new TableModel[],
	 */
	public static TableModel[] joinConfigsModels(SqlBoxContext ctx, List<Map<String, Object>> listMap,
			Object... configObjects) {
		// bindeds: tableModels entityClass and alias may be empty
		// given: tableModels should have entityClass, alias may be null
		TableModel[] bindeds = EntityNetUtils.getBindedTableModel(listMap);
		EntityNetUtils.removeBindedTableModel(listMap);
		if (bindeds == null || bindeds.length == 0)
			bindeds = new TableModel[0];

		TableModel[] givens;
		if (configObjects != null && configObjects.length > 0)
			givens = EntityNetSqlExplainer.objectConfigsToModels(ctx, configObjects);
		else
			givens = new TableModel[0];

		TableModel[] result = EntityNetUtils.jointConfigModels(bindeds, givens);
		return result;
	}

	/**
	 * Load from database buy given netConfigs array, loadKeyOnly determine if
	 * only load PKey/FKey columns
	 * 
	 * @param ctx
	 *            Current SqlBoxContext
	 * @param loadKeyOnly
	 *            If true will only load PKey and FKeys field
	 * @param configObjects
	 *            netConfigs array, can be entity class, entity, SqlBox or
	 *            TableModel
	 * @return The EntityNet
	 */
	public static EntityNet loadKeyOrAllColumnsNet(SqlBoxContext ctx, boolean loadKeyOnly, Object... configObjects) {
		if (configObjects == null || configObjects.length == 0)
			throw new SqlBoxException("LoadNet() does not support empty netConfigs parameter");
		TableModel[] models = EntityNetSqlExplainer.objectConfigsToModels(ctx, configObjects);
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
				EntityNetUtils.removeBindedTableModel(mapList);
			}
			net.joinList(mapList, t);
		}
		return net;
	}
}
