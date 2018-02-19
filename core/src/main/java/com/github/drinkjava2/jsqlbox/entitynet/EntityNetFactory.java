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

import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.handler.EntitySqlMapListHandler;

/**
 * This is a EntityNetFactory implementation to create a TinyEntityNet
 */
public abstract class EntityNetFactory {// NOSONAR

	/** Create a EntityNet instance */
	public static EntityNet createEntityNet() {
		return new EntityNet();
	}

	/** Create a EntityNet instance by given listMap and configs */
	public static EntityNet createEntityNet(List<Map<String, Object>> listMap, TableModel[] configs) {
		return new EntityNet(listMap, configs);
	}

	/**
	 * Create a EntityNet instance, load data from database buy given loadKeyOnly
	 * and configObjects parameters
	 * 
	 * @param ctx
	 *            A SqlBoxContext instance
	 * @param loadKeyOnly
	 *            If true will only load PKey and FKeys field, otherwise load all
	 *            columns
	 * @param configObjects
	 *            netConfigs array, can be entity class, entity, SqlBox or
	 *            TableModel instance
	 * @return The EntityNet
	 */
	public static EntityNet createEntityNet(SqlBoxContext ctx, boolean loadKeyOnly, Object... configObjects) {
		if (configObjects == null || configObjects.length == 0)
			throw new EntityNetException("LoadNet() does not support empty netConfigs parameter");
		TableModel[] models = EntityNetUtils.objectConfigsToModels(ctx, configObjects);
		EntityNet net = new EntityNet();
		String starOrSharp = loadKeyOnly ? ".##" : ".**";
		for (TableModel t : models) {
			List<Map<String, Object>> mapList = null;
			String alias = t.getAlias();
			if (StrUtils.isEmpty(alias))
				alias = t.getTableName();
			try {
				mapList = ctx.nQuery(new EntitySqlMapListHandler(t),
						"select " + alias + starOrSharp + " from " + t.getTableName() + " as " + alias);
			} finally {
				EntityNetUtils.removeBindedTableModel(mapList);
			}
			net.addMapList(mapList, t);
		}
		return net;
	}

}
