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

/**
 * This interface defines some Sharding methods
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */

public interface ShardingTool {// NOSONAR
	/**
	 * Dealing a sharding method
	 * 
	 * @param methodName
	 *            A sharding method name, like shardEqual, shardIn, shardBetween...
	 * @param entityOrClass
	 *            A entity or Entity class or TableModel which has a @Sharding
	 *            annotation or setting
	 * @param firstValue
	 *            The first value of sharding key, may be a object or collection,
	 *            depends on different method
	 * @param secondValue
	 *            The second value of sharding keys, usually is a start-end pair
	 *            object to mark a range
	 * @return null if this @sharding annotation strategy is not fit to current
	 *         implementation, otherwise return a String array of all founded table
	 *         names, if not found return a empty String[]
	 */
	public String[] doSharding(String methodName, Object entityOrClass, Object firstValue, Object secondValue);

}
