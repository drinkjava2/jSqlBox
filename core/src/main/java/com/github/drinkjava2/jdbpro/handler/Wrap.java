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
package com.github.drinkjava2.jdbpro.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Wrap used to wrap a set of ResultSetHandler together so can be put into query
 * method, Usage example:<br/>
 * ctx.nQuery(new Wrap(new someHandler1(), new someHandler2(), "select * from
 * users")
 * 
 * @since 1.7.0.2
 */
@SuppressWarnings({ "all" })
public class Wrap implements ResultSetHandler, AroundSqlHandler, CacheSqlHandler {
	private final ResultSetHandler[] handlers;

	public Wrap(ResultSetHandler... sqlHandles) {
		this.handlers = sqlHandles;
	}

	@Override
	public String handleSql(QueryRunner query, String sql, Object... params) {
		String newSql = sql;
		for (ResultSetHandler handle : handlers) {
			if (handle instanceof AroundSqlHandler)
				newSql = ((AroundSqlHandler) handle).handleSql(query, newSql, params);
		}
		return newSql;
	}

	@Override
	public Object handleResult(QueryRunner query, Object result) {
		Object newResult = result;
		for (ResultSetHandler handle : handlers) {
			if (handle instanceof AroundSqlHandler)
				newResult = ((AroundSqlHandler) handle).handleResult(query, newResult);
		}
		return newResult;
	}

	@Override
	public Object handle(ResultSet result) throws SQLException {
		Object newResult = result;
		for (ResultSetHandler handle : handlers)
			if (newResult != null && newResult instanceof ResultSet)
				newResult = handle.handle((ResultSet) newResult);
		return newResult;
	}

	@Override
	public Object readFromCache(String key) {
		for (ResultSetHandler handle : handlers)
			if (handle instanceof CacheSqlHandler) {
				Object result = ((CacheSqlHandler) handle).readFromCache(key);
				if (result != null)
					return result;
			}
		return null;
	}

	@Override
	public void writeToCache(String key, Object value) {
		for (ResultSetHandler handle : handlers)
			if (handle instanceof CacheSqlHandler) {
				((CacheSqlHandler) handle).writeToCache(key, value);
				return;
			}
	}
}
