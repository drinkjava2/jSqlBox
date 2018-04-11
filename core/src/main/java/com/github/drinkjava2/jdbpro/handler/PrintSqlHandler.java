/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdbpro.handler;

import java.util.Arrays;

import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlHandler;

/**
 * PaginHandler is the AroundSqlHandler used to translate SQL to paginated SQL
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
public class PrintSqlHandler implements SqlHandler {

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		System.out.println("SQL: " + ps.getSql());// NOSONAR
		System.out.println("Param:" + Arrays.deepToString(ps.getParamArray()));// NOSONAR
		return runner.runPreparedSQL(ps);
	}
}
