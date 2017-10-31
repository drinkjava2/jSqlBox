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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Store static methods about EntityNet
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNetUtils {

	/**
	 * After a query, listMap may binded a threadLocal type TableModel[] netConfigs,
	 * this method used to join the binded tableModels with given configObjects,
	 * return a new TableModel[],
	 */
	public static TableModel[] joinConfigsModels(SqlBoxContext ctx, List<Map<String, Object>> listMap,
			Object... configObjects) {
		// bindeds: tableModels entityClass and alias may be empty
		// given: tableModels should have entityClass, alias may be null
		TableModel[] bindeds = EntityNetSqlExplainer.getBindedTableModel(listMap);
		EntityNetSqlExplainer.removeBindedTableModel(listMap);
		if (bindeds == null || bindeds.length == 0)
			bindeds = new TableModel[0];

		TableModel[] givens;
		if (configObjects != null && configObjects.length > 0)
			givens = EntityNetSqlExplainer.objectConfigsToModels(ctx, configObjects);
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
}
