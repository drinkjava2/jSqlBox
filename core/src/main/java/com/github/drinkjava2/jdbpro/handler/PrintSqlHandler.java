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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * PaginHandler is the AroundSqlHandler used to translate SQL to paginated SQL
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
@SuppressWarnings("rawtypes")
public class PrintSqlHandler implements ResultSetHandler, BeforeSqlHandler {

	public PrintSqlHandler() {
		//Default constructor
	}

	@Override
	public String handleSql(QueryRunner query, String sql, Object... params) {
		System.out.println("Sql=" + sql);//NOSONAR
		System.out.println("Parameters=" + Arrays.toString(params));//NOSONAR
		return sql;
	}
  
	@Override
	public Object handle(ResultSet result) throws SQLException {
		return result;
	}
}
