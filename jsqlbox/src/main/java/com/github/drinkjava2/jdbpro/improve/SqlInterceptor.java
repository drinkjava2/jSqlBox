/*
 * Copyright (C) 2016 Yong Zhu.
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
package com.github.drinkjava2.jdbpro.improve;

/**
 * SqlInterceptor should have a explain method to explain SQL and a
 * explainResult method to explainResult
 * 
 * @since 1.7.0.1
 */
@SuppressWarnings({ "all" })
public interface SqlInterceptor {
	public static final int NO_PARAM = 0;
	public static final int SINGLE_PARAM = 1;
	public static final int ARRAY_PARAM = 2;

	/**
	 * handleSql method used to change SQL string before execute to add extra
	 * features like pagination, logging...
	 * 
	 * @param query The ImprovedQueryRunner
	 * @param Sql The original SQL
	 * @param paramType Indicate parameter, can be NO_PARAM or SINGLE_PARAM or
	 *            ARRAY_PARAM
	 * @param param The parameter
	 * @return Changed SQL String
	 */
	public String handleSql(ImprovedQueryRunner query, String originalSQL, int paramType, Object param);

	/**
	 * After a SQL be executed, handleResult method be called to change the result 
	 * 
	 * @param result The original result Object
	 * @return The changed result Object
	 */
	public Object handleResult(Object originalResult);
}
