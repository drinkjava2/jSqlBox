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
import com.github.drinkjava2.jsqlbox.DbException;

/**
 * An simple implementation of ShardingTool to deal "RANGE" type sharding
 * strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingRangeTool implements ShardingTool {

	@Override
	public Integer[] handleShardTable(TableModel model, Object... shardkey) {// NOSONAR
		ColumnModel col = model.getShardTableColumn();
		if (col == null)
			return null;
		if (!"RANGE".equalsIgnoreCase(col.getShardTable()[0]))
			return null;// NOSONAR
		String rangeSize = col.getShardTable()[1];

		Object shardKey1 = null;
		Object shardkey2 = null;
		Class<?> entityOrClass = model.getEntityClass();
		DbException.assureNotNull(entityOrClass);
		if (shardkey == null || shardkey.length == 0)
			throw new DbException("ShardTable key parameter needed");
		else if (shardkey.length == 1) {
			shardKey1 = shardkey[0];
		} else {
			shardKey1 = shardkey[0];
			shardkey2 = shardkey[1];
		}
		if (shardKey1 == null)
			throw new DbException("ShardTable key value can not be null");
		return doCalculate(rangeSize, shardKey1, shardkey2);
	}

	@Override
	public Integer[] handleShardDatabase(TableModel model, Object... shardkey) {// NOSONAR
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null)
			return null;
		if (!"RANGE".equalsIgnoreCase(col.getShardDatabase()[0]))
			return null;// NOSONAR
		String rangeSize = col.getShardDatabase()[1];

		Object shardKey1 = null;
		Object shardkey2 = null;
		Class<?> entityOrClass = model.getEntityClass();
		DbException.assureNotNull(entityOrClass);
		if (shardkey == null || shardkey.length == 0)
			throw new DbException("ShardDatabase key parameter needed");
		else if (shardkey.length == 1) {
			shardKey1 = shardkey[0];
		} else {
			shardKey1 = shardkey[0];
			shardkey2 = shardkey[1];
		}
		if (shardKey1 == null)
			throw new DbException("ShardDatabase key value can not be null");
		return doCalculate(rangeSize, shardKey1, shardkey2);
	}

	private Integer[] doCalculate(String rangeSizz, Object shardKey1, Object shardkey2) {
		if (shardkey2 != null) {
			return calculateRanges(shardKey1, shardkey2, rangeSizz);
		} else {
			Set<Integer> set = new HashSet<Integer>();
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calcuteByOneValue(key, rangeSizz));
				return set.toArray(new Integer[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calcuteByOneValue(key, rangeSizz));
				return set.toArray(new Integer[set.size()]);
			} else
				return new Integer[] { calcuteByOneValue(shardKey1, rangeSizz) };
		}
	}

	/**
	 * Give rangeSize, firstKey, secondKey, tableSize, return a tableName_x String
	 * Array
	 */
	private static Integer[] calculateRanges(Object firstKey, Object secondKey, String rangeSize) {
		long from = Long.parseLong(String.valueOf(firstKey));
		long last = Long.parseLong(String.valueOf(secondKey));
		long size = Long.parseLong(String.valueOf(rangeSize));
		int firstTable = (int) (from / size);
		int lastTable = (int) (last / size);
		if (lastTable < firstTable)
			return new Integer[] {};
		Integer[] result = new Integer[lastTable - firstTable + 1];
		for (int i = firstTable; i <= lastTable; i++)
			result[i - firstTable] = i;
		return result;
	}

	private static Integer calcuteByOneValue(Object keyValue, String rangeSize) {
		long shardKeyValue = Long.parseLong(String.valueOf(keyValue));
		long size = Long.parseLong(String.valueOf(rangeSize));
		return (int) (shardKeyValue / size);
	}

}
