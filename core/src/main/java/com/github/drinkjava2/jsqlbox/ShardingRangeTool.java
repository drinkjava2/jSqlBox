/**
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.ShardingTool;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * An simple implementation of ShardingTool to deal "RANGE" type sharding
 * strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingRangeTool implements ShardingTool {
	public static final ShardingRangeTool instance = new ShardingRangeTool();

	@Override
	public String getStrategyName() {
		return "RANGE";
	}

	// shardingSetting for ShardingRangeTool should like below:
	// "RANGE", 20000000

	// SqlItem.getParameters should like one of below:
	// "EQUAL", entityOrClass, shardKey
	// "IN", entityOrClass, shardKeys
	// "BETWEEN", entityOrClass, shardKey1, shardKey2

	@Override
	public String[] doSharding(DbPro dbPro, SqlItem sqlItem, String[] shardingSetting) {
		Object[] params = sqlItem.getParameters();

		SqlBoxContext ctx = (SqlBoxContext) dbPro;
		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, params[1]);
		ColumnModel col = t.getShardingColumn();
		if (col == null)
			throw new SqlBoxException("Not found sharding setting for entity '" + params[1] + "'");

		Object firstKey = params[2];
		if (firstKey == null)
			throw new SqlBoxException("ShardKey value can not be null");
		Object secondKey = null;
		if (params.length >= 4)
			secondKey = params[3];
		String tableSize = shardingSetting[1];

		String method = (String) params[0];
		if ("EQUAL".equalsIgnoreCase(method)) {
			return new String[] { calculateTableName(t.getTableName(), firstKey, tableSize) };
		} else if ("IN".equalsIgnoreCase(method)) {
			Set<String> set = new HashSet<String>();
			if (firstKey.getClass().isArray()) {
				for (Object key : (Object[]) firstKey)
					set.add(calculateTableName(t.getTableName(), key, tableSize));
			} else if (firstKey instanceof Collection) {
				for (Object key : (Collection<?>) firstKey)
					set.add(calculateTableName(t.getTableName(), key, tableSize));
			} else
				set.add(calculateTableName(t.getTableName(), firstKey, tableSize));
			return set.toArray(new String[set.size()]);
		} else if ("BETWEEN".equalsIgnoreCase(method)) {
			return calculateTableNames(t.getTableName(), firstKey, secondKey, tableSize);
		} else
			throw new SqlBoxException("ShardingRangeTool does support sharding method '" + method + "' ");
	}

	/**
	 * Give tableName, keyValue, tableSize, calculate a tableName_x String
	 */
	private static String calculateTableName(String tableName, Object keyValue, String tableSize) {
		long shardKeyValue = Long.parseLong((String) keyValue);
		long size = Long.parseLong(tableSize);
		return new StringBuffer(tableName).append("_").append(shardKeyValue / size).toString();
	}

	/**
	 * Give tableName, firstKey, secondKey, tableSize, return a tableName_x String
	 * Array
	 */
	private static String[] calculateTableNames(String tableName, Object firstKey, Object secondKey, String tableSize) {
		long from = Long.parseLong((String) firstKey);
		long last = Long.parseLong((String) secondKey);
		long size = Long.parseLong(tableSize);
		int firstTable = (int) (from / size);
		int lastTable = (int) (last / size);
		if (lastTable < firstTable)
			return new String[] {};
		String[] result = new String[lastTable - firstTable + 1];
		for (int i = firstTable; i <= lastTable; i++)
			result[i - firstTable] = new StringBuffer(tableName).append("_").append(i).toString();
		return result;
	}
}
