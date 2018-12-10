/*
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

/**
 * SqlHandler is the Interceptor to do some intercept operation instead of
 * direct access database
 * 
 * @since 1.7.0.2
 */
public interface SqlHandler {// NOSONAR

	/**
	 * @return The order number, usually when add a SqlHandler into a existed
	 *         SqlHandlers list, will sort according this order number
	 */
	public Integer getOrder();

	/**
	 * @param order
	 *            Usually when add a SqlHandler into a existed SqlHandlers list,
	 *            will sort according this order number
	 */
	public void setOrder(Integer order);

	/**
	 * handle method is an intercept method, subClass should implement this method
	 * 
	 * @param runner
	 *            The ImprovedQueryRunner instance
	 * @param ps
	 *            The PreparedSQL instance
	 * @return The result Object
	 */
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps);

	/**
	 * The method may be called by hander method, to modify PreparedSQL setting, but
	 * not always
	 */
	public void beforeExecute(ImprovedQueryRunner runner, PreparedSQL ps);

	/**
	 * The method may be called by hander method, modify result to a new result
	 * object, but not always
	 */
	public Object afterExecute(ImprovedQueryRunner runner, PreparedSQL ps, Object result);
}
