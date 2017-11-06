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

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityNet;
import com.github.drinkjava2.jsqlbox.EntityNetBuilder;
import com.github.drinkjava2.jsqlbox.EntityNetSqlExplainer;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * This is a EntityNetBuilder implementation to let jSqlBox know how to build
 * TinyNet
 */
@SuppressWarnings("unchecked")
public class TinyEntityNetBuilder implements EntityNetBuilder {
	public static final TinyEntityNetBuilder instance = new TinyEntityNetBuilder();

	@Override
	public <T> T createEntityNet() {
		return (T) (new TinyNet());
	}

	@Override
	public <T> T createEntityNet(List<Map<String, Object>> listMap, TableModel[] configs) {
		return (T) (new TinyNet(listMap, configs));
	}

	@Override
	public <T> T createEntityNet(SqlBoxContext ctx, boolean loadKeyOnly, Object... configObjects) {
		if (configObjects == null || configObjects.length == 0)
			throw new TinyNetException("LoadNet() does not support empty netConfigs parameter");
		TableModel[] models = EntityNetSqlExplainer.objectConfigsToModels(ctx, configObjects);
		EntityNet net = new TinyNet();
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
			net.addMapList(mapList, t);
		}
		return (T) net;
	}

}
