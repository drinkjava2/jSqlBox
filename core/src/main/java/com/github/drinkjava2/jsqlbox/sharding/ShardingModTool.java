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
 * An simple implementation of ShardingTool to deal "MOD" type sharding strategy
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public class ShardingModTool implements ShardingTool {

	@Override
	public String[] doSharding(SqlBoxContext ctx, String methodName, Object entityOrClass, Object firstValue,
			Object secondValue) {// NOSONAR
		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, entityOrClass);
		ColumnModel col = t.getShardingColumn();
		if (col == null || col.getSharding() == null || col.getSharding().length == 0)
			throw new SqlBoxException("Not found sharding setting for entity '" + entityOrClass + "'");

		// return null if is not "MOD" sharding strategy
		if (!"MOD".equalsIgnoreCase(col.getSharding()[0]))
			return null;// NOSONAR
		String modNumber = col.getSharding()[1];
		if (firstValue == null)
			throw new SqlBoxException("Shardkey value can not be null");

		if ("shardEqual".equalsIgnoreCase(methodName)) {
			return new String[] { calculateTableName(t.getTableName(), firstValue, modNumber) };
		} else if ("shardIn".equalsIgnoreCase(methodName)) {
			Set<String> set = new HashSet<String>();
			if (firstValue.getClass().isArray()) {
				for (Object key : (Object[]) firstValue)
					set.add(calculateTableName(t.getTableName(), key, modNumber));
			} else if (firstValue instanceof Collection) {
				for (Object key : (Collection<?>) firstValue)
					set.add(calculateTableName(t.getTableName(), key, modNumber));
			} else
				set.add(calculateTableName(t.getTableName(), firstValue, modNumber));
			return set.toArray(new String[set.size()]);
		} else if ("shardBetween".equalsIgnoreCase(methodName)) {
			throw new SqlBoxException("ShardingModTool does not support shardBetween method");
		} else
			throw new SqlBoxException("ShardingModTool does support unknow method '" + methodName + "' ");
	}

	/**
	 * Give tableName, keyValue, mod number, calculate a tableName_x format String
	 */
	private static String calculateTableName(String tableName, Object keyValue, String modNumber) {
		long shardKeyValue = Long.parseLong(String.valueOf(keyValue));
		int mod = Integer.parseInt(modNumber);
		return new StringBuffer(tableName).append("_").append(shardKeyValue % mod).toString();
	}

}
