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
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;

/**
 * Here put public static methods related to sharding
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */

public abstract class ShardingUtils {// NOSONAR

	/**
	 * Return sharding Db and Sharding TB values put in a Integer array, for example
	 * Integer[]{0,5}, if no sharding, return null, for example:
	 * Integer[]{null,null};
	 * 
	 * @param entity
	 * @return Integer[] stored DB's order and Table's order
	 */
	public static Integer[] getShardDbTbResult(SqlBoxContext ctx, TableModel model, Object entity) {
		Integer shardedDBCode = SqlBoxContextUtils.getShardedDBCode(ctx, model, entity.getClass());
		Integer shardedTBCode = SqlBoxContextUtils.getShardedTBCode(ctx, model, entity.getClass());
		return new Integer[] { shardedDBCode, shardedTBCode };
	}
}
