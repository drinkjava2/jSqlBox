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

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This interface defines some Sharding methods
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public interface ShardingTool {// NOSONAR
	/**
	 * Dealing a shardKey array based on current SqlBoxContext and TableModel,
	 * return table name array, in this jSqlBox version, only support return 1 table
	 * name
	 * 
	 * @param tableModel
	 *            The tableModel of an entity need do sharding
	 * @param shardKey
	 *            The shard key or keys
	 * @return A Integer[] stored table names or null if the sharding strategy is not
	 *         fit for current implementation
	 */
	public Integer[] handleShardTable(TableModel tableModel, Object... shardKey);

	/**
	 * Dealing a shardKey array based on current SqlBoxContext and TableModel,
	 * return SqlBoxContext array, in this jSqlBox version, only support return 1
	 * SqlBoxContext
	 * 
	 * @param tableModel
	 *            The tableModel of an entity need do sharding
	 * @param shardKey
	 *            The shard key or keys
	 * @return A Integer[] stored SqlBoxContext name order, start from 0, or null if the
	 *         sharding strategy is not fit for current implementation
	 */
	public Integer[] handleShardDatabase(TableModel tableModel, Object... shardKey);

}
