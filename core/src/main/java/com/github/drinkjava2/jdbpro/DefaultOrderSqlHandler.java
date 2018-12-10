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
public abstract class DefaultOrderSqlHandler implements SqlHandler {// NOSONAR
	public static final int DEFAULT_ORDER = 100;
	protected Integer order = DEFAULT_ORDER;

	@Override
	public Integer getOrder() {
		return order;
	}

	@Override
	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		beforeExecute(runner, ps);
		Object result = runner.runPreparedSQL(ps);
		return afterExecute(runner, ps, result);
	}

	@Override
	public void beforeExecute(ImprovedQueryRunner runner, PreparedSQL ps) {
	}

	@Override
	public Object afterExecute(ImprovedQueryRunner runner, PreparedSQL ps, Object result) {
		return result;
	}

}
