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
package com.github.drinkjava2.jdbpro;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.SqlItem;

/**
 * This interface defines some Sharding methods
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public interface ShardingTool {// NOSONAR

	/**
	 * @return Current ShardingTool's Strategy name, like "MOD", "RANGE"... , a
	 *         Shard-SqlItem only be treated by ShardingTool has same Strategy name
	 *         (Ignore Case)
	 */
	public String getStrategyName();

	/**
	 * Dealing a SqlItemType.SHARD type SqlItem with same strategy name, return
	 * table name String[]
	 * 
	 * @param dbPro
	 * @param item
	 *            The SqlItem, an example: new SqlItem(SqlItemType.SHARD,
	 *            "shardBetween", entityOrClass, shardKey1, shardKey2)
	 * @param shardingSetting
	 *            The sharding setting String[], example: {"MOD","8"} or
	 *            {"RANGE","2000000"}
	 * @return
	 */
	public String[] doSharding(DbPro dbPro, SqlItem sqlItem, String[] shardingSetting);

}
