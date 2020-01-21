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
 * An simple implementation of ShardingTool to deal "MOD" type sharding strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingModTool implements ShardingTool {

	@Override
	public Integer[] handleShardTable(TableModel model, Object... shardkey) {// NOSONAR
		ColumnModel col = model.getShardTableColumn();
		if (col == null)
			return null;
		if (col.getShardTable() == null || col.getShardTable().length == 0)
			throw new DbException("Not found ShardTable setting for table '" + model.getTableName() + "'");

		// return null if is not "MOD" shardTable strategy
		if (!"MOD".equalsIgnoreCase(col.getShardTable()[0]))
			return null;// NOSONAR
		String modNumber = col.getShardTable()[1];

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

		Set<Integer> set = new HashSet<Integer>();

		if (shardkey2 != null) {
			throw new DbException("ShardingModTool does not support shardBetween type method");
		} else {
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calculateMod(key, modNumber));
				return set.toArray(new Integer[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calculateMod(key, modNumber));
				return set.toArray(new Integer[set.size()]);
			} else
				return new Integer[] { calculateMod(shardKey1, modNumber) };
		}
	}

	@Override
	public Integer[] handleShardDatabase(TableModel model, Object... shardkey) {// NOSONAR

		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null)
			return null;
		if (col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new DbException("Not found ShardDatabase setting for table '" + model.getTableName() + "'");

		// return null if is not "MOD" shardTable strategy
		if (!"MOD".equalsIgnoreCase(col.getShardDatabase()[0]))
			return null;// NOSONAR
		String modNumber = col.getShardDatabase()[1];

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
			throw new DbException("ShardDatabase key value can not be null, on table:"+col.getTableModel().getTableName()+", column:"+col.getColumnName());

		Set<Integer> set = new HashSet<Integer>();

		if (shardkey2 != null)
			throw new DbException("ShardingModTool does not support shardBetween type method");
		else {
			if (shardKey1 instanceof Collection<?>) {
				for (Object key : (Collection<?>) shardKey1)
					set.add(calculateMod(key, modNumber));
				return set.toArray(new Integer[set.size()]);
			} else if (shardKey1.getClass().isArray()) {
				for (Object key : (Object[]) shardKey1)
					set.add(calculateMod(key, modNumber));
				return set.toArray(new Integer[set.size()]);
			} else
				return new Integer[] { calculateMod(shardKey1, modNumber) };
		}
	}

	private static int calculateMod(Object keyValue, String modNumber) {
		long shardKeyValue = Long.parseLong(String.valueOf(keyValue));
		int mod = Integer.parseInt(modNumber);
		return (int) (shardKeyValue % mod);
	}

}
