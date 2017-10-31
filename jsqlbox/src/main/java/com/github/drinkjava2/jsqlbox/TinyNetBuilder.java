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
import com.github.drinkjava2.jtinynet.TinyNet;

/**
 * This is a helper class store public static methods concern to build EntityNet
 */
public class TinyNetBuilder implements EntityNetBuilder {
	public static final TinyNetBuilder instance = new TinyNetBuilder();

	@Override
	public EntityNet createEntityNet() {
		return new TinyNet();
	}

	@Override
	public EntityNet createEntityNet(List<Map<String, Object>> listMap, TableModel[] configs) {
		return new TinyNet(listMap, configs);
	}

	@Override
	public EntityNet createEntityNet(SqlBoxContext ctx, boolean loadKeyOnly, Object... configObjects) {
		if (configObjects == null || configObjects.length == 0)
			throw new SqlBoxException("LoadNet() does not support empty netConfigs parameter");
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
		return net;
	} 
	
}
