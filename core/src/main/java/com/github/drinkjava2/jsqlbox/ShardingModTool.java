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
 * An simple implementation of ShardingTool to deal "MOD" type sharding strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingModTool implements ShardingTool {

	public static final ShardingModTool instance = new ShardingModTool();

	@Override
	public String getStrategyName() {
		return "MOD";
	}

	// shardingSetting should like below:
	// @Sharding("MOD", 8)

	// SqlItem should like one of below:
	// new SqlItem(SqlItemType.SHARD, "EQUAL", entityOrClass, shardKey)
	// new SqlItem(SqlItemType.SHARD, "IN", entityOrClass, shardKey)

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
		String modNumber = shardingSetting[1];

		String method = (String) params[0];
		if ("EQUAL".equalsIgnoreCase(method)) {
			return new String[] { calculateTableName(t.getTableName(), firstKey, modNumber) };
		} else if ("IN".equalsIgnoreCase(method)) {
			Set<String> set = new HashSet<String>();
			if (firstKey.getClass().isArray()) {
				for (Object key : (Object[]) firstKey)
					set.add(calculateTableName(t.getTableName(), key, modNumber));
			} else if (firstKey instanceof Collection) {
				for (Object key : (Collection<?>) firstKey)
					set.add(calculateTableName(t.getTableName(), key, modNumber));
			} else
				set.add(calculateTableName(t.getTableName(), firstKey, modNumber));
			return set.toArray(new String[set.size()]);
		} else if ("BETWEEN".equalsIgnoreCase(method)) {
			throw new SqlBoxException("ShardingModTool does not support shardBetween sharding method");
		} else
			throw new SqlBoxException("ShardingModTool does support sharding method '" + method + "' ");

	}

	/**
	 * Give tableName, keyValue, mod number, calculate a tableName_x format String
	 */
	private static String calculateTableName(String tableName, Object keyValue, String modNumber) {
		long shardKeyValue = Long.parseLong((String) keyValue);
		int mod = Integer.parseInt(modNumber);
		return new StringBuffer(tableName).append("_").append(shardKeyValue % mod).toString();
	}
}
