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
	public String[] doSharding(SqlBoxContext ctx, String methodName, Object entityOrClass, Object firstValue,
			Object secondValue) {// NOSONAR
		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, entityOrClass);
		ColumnModel col = t.getShardingColumn();
		if (col == null)
			throw new SqlBoxException("Not found sharding setting for entity '" + entityOrClass + "'");

		// return null if is not "RANGE" sharding strategy
		if (!"RANGE".equalsIgnoreCase(col.getSharding()[0]))
			return null;// NOSONAR

		if (firstValue == null)
			throw new SqlBoxException("ShardKey value can not be null");

		String tableSize = col.getSharding()[1];
		if ("EQUAL".equalsIgnoreCase(methodName)) {
			return new String[] { calculateTableName(t.getTableName(), firstValue, tableSize) };
		} else if ("IN".equalsIgnoreCase(methodName)) {
			Set<String> set = new HashSet<String>();
			if (firstValue.getClass().isArray()) {
				for (Object key : (Object[]) firstValue)
					set.add(calculateTableName(t.getTableName(), key, tableSize));
			} else if (firstValue instanceof Collection) {
				for (Object key : (Collection<?>) firstValue)
					set.add(calculateTableName(t.getTableName(), key, tableSize));
			} else
				set.add(calculateTableName(t.getTableName(), firstValue, tableSize));
			return set.toArray(new String[set.size()]);
		} else if ("BETWEEN".equalsIgnoreCase(methodName)) {
			return calculateTableNames(t.getTableName(), firstValue, secondValue, tableSize);
		} else
			throw new SqlBoxException("ShardingRangeTool does support sharding method '" + methodName + "' ");
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
}
