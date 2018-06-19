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
package com.github.drinkjava2.jsqlbox.sharding;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * An simple implementation of ShardingTool to deal "MOD" type sharding strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingModTool implements ShardingTool {

	@Override
	public String[] handleShardTable(SqlBoxContext ctx, Object entityOrClass, Object... shardkey) {// NOSONAR
		TableModel t = SqlBoxContextUtils.configToModel(entityOrClass);
		ColumnModel col = t.getShardTableColumn();
		if (col == null || col.getShardTable() == null || col.getShardTable().length == 0)
			throw new SqlBoxException("Not found ShardTable setting for table '" + t.getTableName() + "'");

		// return null if is not "MOD" shardTable strategy
		if (!"MOD".equalsIgnoreCase(col.getShardTable()[0]))
			return null;// NOSONAR
		String modNumber = col.getShardTable()[1];

		Object shardKey1 = null;
		Object shardkey2 = null;
		if (shardkey == null || shardkey.length == 0) {
			if (entityOrClass instanceof Class)
				throw new SqlBoxException("entityOrClass need ShardTable key value");
			shardKey1 = ClassCacheUtils.readValueFromBeanField(entityOrClass, col.getColumnName());
		} else if (shardkey.length == 1) {
			shardKey1 = shardkey[0];
		} else {
			shardKey1 = shardkey[0];
			shardkey2 = shardkey[1];
		}
		if (shardKey1 == null)
			throw new SqlBoxException("ShardTable key value can not be null");

		Set<String> set = new HashSet<String>();

		if (shardkey2 != null)
			throw new SqlBoxException("ShardingModTool does not support shardBetween type method");
		else {
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calculateTableName(t.getTableName(), key, modNumber));
				return set.toArray(new String[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calculateTableName(t.getTableName(), key, modNumber));
				return set.toArray(new String[set.size()]);
			} else
				return new String[] { calculateTableName(t.getTableName(), shardKey1, modNumber) };
		}
	}

	@Override
	public SqlBoxContext[] handleShardDatabase(SqlBoxContext ctx, Object entityOrClass, Object... shardkey) {// NOSONAR
		TableModel t = SqlBoxContextUtils.configToModel( entityOrClass);

		ColumnModel col = t.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new SqlBoxException("Not found ShardDatabase setting for table '" + t.getTableName() + "'");

		// return null if is not "MOD" shardTable strategy
		if (!"MOD".equalsIgnoreCase(col.getShardDatabase()[0]))
			return null;// NOSONAR
		String modNumber = col.getShardDatabase()[1];

		Object shardKey1 = null;
		Object shardkey2 = null;
		if (shardkey == null || shardkey.length == 0) {
			if (entityOrClass instanceof Class)
				throw new SqlBoxException("entityOrClass need ShardDatabase key value");
			shardKey1 = ClassCacheUtils.readValueFromBeanField(entityOrClass, col.getColumnName());
		} else if (shardkey.length == 1) {
			shardKey1 = shardkey[0];
		} else {
			shardKey1 = shardkey[0];
			shardkey2 = shardkey[1];
		}
		if (shardKey1 == null)
			throw new SqlBoxException("ShardDatabase key value can not be null");

		Set<SqlBoxContext> set = new HashSet<SqlBoxContext>();

		if (shardkey2 != null)
			throw new SqlBoxException("ShardingModTool does not support shardBetween type method");
		else {
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calculateMaster(ctx, key, modNumber));
				return set.toArray(new SqlBoxContext[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calculateMaster(ctx, key, modNumber));
				return set.toArray(new SqlBoxContext[set.size()]);
			} else
				return new SqlBoxContext[] { calculateMaster(ctx, shardKey1, modNumber) };
		}
	}

	private static String calculateTableName(String tableName, Object keyValue, String modNumber) {
		return new StringBuilder(tableName).append("_").append(calculateMod(keyValue, modNumber)).toString();
	}

	private static SqlBoxContext calculateMaster(SqlBoxContext ctx, Object keyValue, String modNumber) {
		return (SqlBoxContext) (ctx.getMasters()[calculateMod(keyValue, modNumber)]);
	}

	private static int calculateMod(Object keyValue, String modNumber) {
		long shardKeyValue = Long.parseLong(String.valueOf(keyValue));
		int mod = Integer.parseInt(modNumber);
		return (int) (shardKeyValue % mod);
	}

}
