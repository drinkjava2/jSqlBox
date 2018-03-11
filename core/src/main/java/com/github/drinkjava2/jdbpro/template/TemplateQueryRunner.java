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
package com.github.drinkjava2.jdbpro.template;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.OutParameter;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.inline.InlineQueryRunner;
import com.github.drinkjava2.jdbpro.inline.PreparedSQL;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * TemplateQueryRunner added SQL Template support for QueryRunner
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemplateQueryRunner extends InlineQueryRunner {

	public TemplateQueryRunner() {
		super();
	}

	public TemplateQueryRunner(SqlTemplateEngine templateEngine) {
		super();
		this.sqlTemplateEngine = templateEngine;
	}

	public TemplateQueryRunner(DataSource ds) {
		super(ds);
	}

	public TemplateQueryRunner(DataSource ds, Object... args) {
		super(ds);
		for (Object arg : args) {
			if (arg instanceof ConnectionManager)
				this.connectionManager = (ConnectionManager) arg;
			else if (arg instanceof SqlTemplateEngine)
				this.sqlTemplateEngine = (SqlTemplateEngine) arg;
			else if (arg instanceof DbProLogger)
				this.logger = (DbProLogger) arg;
		}
	}

	/**
	 * A ThreadLocal variant for temporally store parameter key names which is a
	 * direct-replace type parameter in current Thread
	 */
	protected static ThreadLocal<Set<String>> directReplaceKeysCache = new ThreadLocal<Set<String>>() {
		@Override
		protected Set<String> initialValue() {
			return new HashSet<String>();
		}
	};

	// getter && setter ===========
	public SqlTemplateEngine getSqlTemplateEngine() {
		return sqlTemplateEngine;
	}

	/**
	 * A ThreadLocal variant for temporally store parameter Map in current Thread
	 */
	protected static ThreadLocal<Map<String, Object>> templateThreadlocalParamMapCache = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	/**
	 * Put a name-value pair into ThreadLocal parameter Map, return an empty String
	 * ""
	 */
	public static String put(String name, Object value) {
		templateThreadlocalParamMapCache.get().put(name, value);
		return "";
	}

	/**
	 * put a name-value into ThreadLocal parameter Map, return an empty String,
	 * Note: use replace() method the value will directly replace text in template
	 */
	public static String replace(String name, Object value) {
		templateThreadlocalParamMapCache.get().put(name, value);
		directReplaceKeysCache.get().add(name);
		return "";
	}

	/**
	 * Clear all template ThreadLocal parameters, put a name-value pair into
	 * ThreadLocal parameter Map, return an empty String ""
	 */
	public static String put0(String name, Object value) {
		clearBind();
		return put(name, value);
	}

	/**
	 * Clear all template ThreadLocal parameters, return an empty String ""
	 */
	public static String put0() {
		clearBind();
		return "";
	}

	/**
	 * Clear all template ThreadLocal parameters, then put a name-value into
	 * ThreadLocal parameter Map, return an empty String, Note: use replace() method
	 * the value will directly replace text in template
	 */
	public static String replace0(String name, Object value) {
		clearBind();
		return replace(name, value);
	}

	/**
	 * Clear all template ThreadLocal parameters
	 */
	protected static void clearBind() {
		templateThreadlocalParamMapCache.get().clear();
		directReplaceKeysCache.get().clear();
	}

	/**
	 * Build a PreparedSQL instance by given template style SQL and parameters
	 * stored in ThreadLocal
	 * 
	 * @param sqlTemplate
	 * @return PreparedSQL instance
	 */
	protected PreparedSQL templateToSqlAndParams(Map<String, Object> paramMap, String... sqlTemplate) {
		try {
			String sql = null;
			if (sqlTemplate != null) {
				StringBuilder sb = new StringBuilder("");
				for (String str : sqlTemplate)
					sb.append(str);
				sql = sb.toString();
			}
			return sqlTemplateEngine.render(sql, paramMap, null);
		} finally {
			clearBind();
		}
	}

	// ======================================================
	// =============== SQL methods below ====================
	// ======================================================

	/**
	 * Template style execute SQL query, force return a Long value, runtime
	 * Exception may throw if result can not cast to long
	 */
	public long tQueryForLongValue(String templateSQL, Map<String, Object> paramMap) {
		return ((Number) tQueryForObject(templateSQL, paramMap)).longValue();// NOSONAR
	}

	/**
	 * Template style query and force return a List<Map<String, Object>> type result
	 */
	public List<Map<String, Object>> tQueryForMapList(String templateSQL, Map<String, Object> paramMap) {
		return this.tQuery(new MapListHandler(), templateSQL, paramMap);
	}

	/**
	 * Executes the template style given SELECT SQL query and returns a result
	 * object. Transaction mode is determined by connectionManager property.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T tQuery(ResultSetHandler rsh, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return (T) this.query(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the template style given INSERT SQL statement. Note: This method
	 * does not close connection
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T tInsert(Connection conn, ResultSetHandler rsh, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return (T) insert(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the template style given INSERT, UPDATE, or DELETE SQL statement.
	 * Note: This method does not close connection
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param templateSQL
	 *            the SQL template
	 * @return The number of rows updated.
	 */
	public int tUpdate(Connection conn, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return update(conn, sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Template style query and force return a List<Map<String, Object>> type
	 * result. Note: This method does not close connection
	 */
	public List<Map<String, Object>> tQueryForMapList(Connection conn, String templateSQL,
			Map<String, Object> paramMap) {
		return this.tQuery(conn, new MapListHandler(), templateSQL, paramMap);
	}

	/**
	 * Template style execute SQL query, force return a Long value, runtime
	 * Exception may throw if result can not cast to long. Note: This method does
	 * not close connection
	 */
	public long tQueryForLongValue(Connection conn, String templateSQL, Map<String, Object> paramMap) {
		return ((Number) tQueryForObject(conn, templateSQL, paramMap)).longValue();// NOSONAR
	}

	/**
	 * Executes the template style given SELECT SQL query and returns a result
	 * object. Note: This method does not close connection
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T tQuery(Connection conn, ResultSetHandler rsh, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return (T) this.query(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL Template query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned, a
	 * null object may return if no result found , DbProRuntimeException may be
	 * threw if some SQL operation Exception happen. Note: This method does not
	 * close connection
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param templateSQL
	 *            the SQL template
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T tQueryForObject(Connection conn, String templateSQL, Map<String, Object> paramMap) {
		return tQuery(conn, new ScalarHandler<T>(), templateSQL, paramMap);
	}

	/**
	 * Template style execute SQL query, force return a String value. Note: This
	 * method does not close connection
	 */
	public String tQueryForString(Connection conn, String templateSQL, Map<String, Object> paramMap) {
		return String.valueOf(tQueryForObject(conn, templateSQL, paramMap));
	}

	/**
	 * Execute an SQL template statement, including a stored procedure call, which
	 * does not return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Note: This method
	 * does not close connection
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param templateSQL
	 *            the SQL template.
	 * @return The number of rows updated.
	 */
	public int tExecute(Connection conn, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return this.execute(conn, sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL template statement, including a stored procedure call, which
	 * returns one or more result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Note: This method
	 * does not close connection
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param templateSQL
	 *            the SQL template
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> tExecute(Connection conn, ResultSetHandler rsh, String templateSQL,
			Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return this.execute(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL Template query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned, a
	 * null object may return if no result found , DbProRuntimeException may be
	 * threw if some SQL operation Exception happen.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param templateSQL
	 *            the SQL template
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T tQueryForObject(String templateSQL, Map<String, Object> paramMap) {
		return tQuery(new ScalarHandler<T>(), templateSQL, paramMap);
	}

	/** Template style execute SQL query, force return a String value */
	public String tQueryForString(String templateSQL, Map<String, Object> paramMap) {
		return String.valueOf(tQueryForObject(templateSQL, paramMap));
	}

	/**
	 * Executes the template style given INSERT, UPDATE, or DELETE SQL statement.
	 * Transaction mode is determined by connectionManager property.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param templateSQL
	 *            the SQL template
	 * @return The number of rows updated.
	 */
	public int tUpdate(String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return this.update(sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the template style given INSERT SQL statement. Transaction mode is
	 * determined by connectionManager property.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T tInsert(ResultSetHandler rsh, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return (T) insert(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL template statement, including a stored procedure call, which
	 * does not return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param templateSQL
	 *            the SQL template.
	 * @return The number of rows updated.
	 */
	public int tExecute(String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return this.execute(sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL template statement, including a stored procedure call, which
	 * returns one or more result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 * 
	 * @param paramMap
	 *            The parameters stored in Map
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param templateSQL
	 *            the SQL template
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> tExecute(ResultSetHandler rsh, String templateSQL, Map<String, Object> paramMap) {
		try {
			PreparedSQL sp = templateToSqlAndParams(paramMap, templateSQL);
			return this.execute(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

}
