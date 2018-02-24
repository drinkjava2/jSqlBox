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
package com.github.drinkjava2.jdbpro.improve;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.improve.SqlHandler;

/**
 * Handlers used to wrap a set of SqlHandlers into one
 * 
 * @since 1.7.0.1
 */
@SuppressWarnings({ "all" })
public class Handlers implements ResultSetHandler, SqlHandler {
	private final ResultSetHandler[] handlers;

	public Handlers(ResultSetHandler... sqlHandles) {
		this.handlers = sqlHandles;
	}

	@Override
	public String handleSql(QueryRunner query, String sql, Object... params) {
		String newSql = sql;
		for (ResultSetHandler handle : handlers) {
			if (handle instanceof SqlHandler)
				newSql = ((SqlHandler) handle).handleSql(query, newSql, params);
		}
		return newSql;
	}

	@Override
	public Object handleResult(QueryRunner query, Object result) {
		Object newResult = result;
		for (ResultSetHandler handle : handlers) {
			if (handle instanceof SqlHandler)
				newResult = ((SqlHandler) handle).handleResult(query, newResult);
		}
		return newResult;
	}

	@Override
	public Object handle(ResultSet result) throws SQLException {
		Object newResult = result;
		for (ResultSetHandler handle : handlers)
			if (newResult instanceof ResultSet)
				newResult = handle.handle((ResultSet) newResult);
		return newResult;
	}
}
