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
 * An simple implementation of ShardingTool to deal "RANGE" type sharding
 * strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingRangeTool implements ShardingTool {

	@Override
	public String[] handleShardTable(SqlBoxContext ctx, Object entityOrClass, Object... shardkey) {// NOSONAR
		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, entityOrClass);
		ColumnModel col = t.getShardTableColumn();
		if (col == null)
			throw new SqlBoxException("Not found sharding setting for entity '" + entityOrClass + "'");
		if (!"RANGE".equalsIgnoreCase(col.getShardTable()[0]))
			return null;// NOSONAR
		String tableSize = col.getShardTable()[1];

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

 
		if (shardkey2 != null) {
			return calculateTableNames(t.getTableName(), shardKey1, shardkey2, tableSize);
		} else {
			Set<String> set = new HashSet<String>();
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calculateTableName(t.getTableName(), key, tableSize));
				return set.toArray(new String[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calculateTableName(t.getTableName(), key, tableSize));
				return set.toArray(new String[set.size()]);
			} else {
				set.add(calculateTableName(t.getTableName(), shardKey1, tableSize));
				return set.toArray(new String[set.size()]);
			}
		}
	}

	@Override
	public SqlBoxContext[] handleShardDatabase(SqlBoxContext ctx, Object entityOrClass, Object... shardkey) {// NOSONAR

		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, entityOrClass);
		ColumnModel col = t.getShardDatabaseColumn();
		if (col == null)
			throw new SqlBoxException("Not found sharding setting for entity '" + entityOrClass + "'");
		if (!"RANGE".equalsIgnoreCase(col.getShardDatabase()[0]))
			return null;// NOSONAR
		String tableSize = col.getShardDatabase()[1];
		
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

 
		if (shardkey2 != null) {
			return calculateDatabases(ctx, shardKey1, shardkey2, tableSize);
		} else {
			Set<SqlBoxContext> set = new HashSet<SqlBoxContext>();
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calculateDatabase(ctx, key, tableSize));
				return set.toArray(new SqlBoxContext[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calculateDatabase(ctx, key, tableSize));
				return set.toArray(new SqlBoxContext[set.size()]);
			} else
				return new SqlBoxContext[] { calculateDatabase(ctx, shardKey1, tableSize) };
		}

	}

	/**
	 * Give tableName, keyValue, tableSize, calculate a tableName_x String
	 */
	private static String calculateTableName(String tableName, Object keyValue, String tableSize) {
		long shardKeyValue = Long.parseLong(String.valueOf(keyValue));
		long size = Long.parseLong(tableSize);
		return new StringBuffer(tableName).append("_").append(shardKeyValue / size).toString();
	}

	/**
	 * Give tableName, firstKey, secondKey, tableSize, return a tableName_x String
	 * Array
	 */
	private static String[] calculateTableNames(String tableName, Object firstKey, Object secondKey, String tableSize) {
		long from = Long.parseLong(String.valueOf(firstKey));
		long last = Long.parseLong(String.valueOf(secondKey));
		long size = Long.parseLong(String.valueOf(tableSize));
		int firstTable = (int) (from / size);
		int lastTable = (int) (last / size);
		if (lastTable < firstTable)
			return new String[] {};
		String[] result = new String[lastTable - firstTable + 1];
		for (int i = firstTable; i <= lastTable; i++)
			result[i - firstTable] = new StringBuffer(tableName).append("_").append(i).toString();
		return result;
	}

	private static SqlBoxContext calculateDatabase(SqlBoxContext ctx, Object keyValue, String tableSize) {
		long shardKeyValue = Long.parseLong(String.valueOf(keyValue));
		long size = Long.parseLong(String.valueOf(tableSize));
		return (SqlBoxContext) ctx.getMasters()[(int) (shardKeyValue / size)];
	}

	private static SqlBoxContext[] calculateDatabases(SqlBoxContext ctx, Object firstKey, Object secondKey,
			String tableSize) {
		long from = Long.parseLong(String.valueOf(firstKey));
		long last = Long.parseLong(String.valueOf(secondKey));
		long size = Long.parseLong(String.valueOf(tableSize));
		int firstTable = (int) (from / size);
		int lastTable = (int) (last / size);
		if (lastTable < firstTable)
			return new SqlBoxContext[] {};
		SqlBoxContext[] result = new SqlBoxContext[lastTable - firstTable + 1];
		for (int i = firstTable; i <= lastTable; i++)
			result[i - firstTable] = (SqlBoxContext) ctx.getMasters()[i];
		return result;
	}

}
