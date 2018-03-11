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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.OutParameter;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.inline.InlineQueryRunner;
import com.github.drinkjava2.jdbpro.inline.PreparedSQL;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdbpro.template.TemplateQueryRunner;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * DbPro is the enhanced version of Apache Commons DbUtils's QueryRunner, add
 * below improvements:
 * 
 * <pre>
 * 1)Use ConnectionManager to manage connection for better transaction support
 * 2)normal style methods but no longer throw SQLException, methods named as nXxxxx() format
 * 3)In-line style methods, methods named as iXxxxx() format
 * 4)SQL Template style methods, methods named as tXxxxx() format
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DbPro extends TemplateQueryRunner implements NormalJdbcTool {// NOSONAR
	public DbPro() {
		super();
	}

	public DbPro(DataSource ds) {
		super(ds);
	}

	public DbPro(DbProConfig config) {
		super();
		this.connectionManager = config.getConnectionManager();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.handlers = config.getHandlers();
	}

	public DbPro(DataSource ds, DbProConfig config) {
		super(ds);
		this.connectionManager = config.getConnectionManager();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.handlers = config.getHandlers();
	}

	public DbPro(SqlTemplateEngine templateEngine) {
		super(templateEngine);
	}

	public DbPro(DataSource ds, Object... args) {
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
	 * Clear all In-Line parameters and Template parameters stored in ThreadLocal
	 */
	public static void clearAll() {
		TemplateQueryRunner.clearBind();
		InlineQueryRunner.clearParams();
	}

	// ==========================================================
	// DbUtils style methods, throw SQLException

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found, SQLException may be threw if some SQL operation Exception
	 * happen. Note: This method does not close connection.
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @return An Object or null, Object type determined by SQL content
	 * @throws SQLException
	 */
	public <T> T queryForObject(Connection conn, String sql, Object... params) throws SQLException {
		return query(conn, sql, new ScalarHandler<T>(1), params);
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found, SQLException may be threw if some SQL operation Exception
	 * happen.
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @return An Object or null, Object type determined by SQL content
	 * @throws SQLException
	 */
	public <T> T queryForObject(String sql, Object... params) throws SQLException {
		return query(sql, new ScalarHandler<T>(1), params);
	}

	/**
	 * Execute query and force return a String object, Note: This method does not
	 * close connection.
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @throws SQLException
	 */
	public String queryForString(Connection conn, String sql, Object... params) throws SQLException {
		return String.valueOf(queryForObject(conn, sql, params));
	}

	/**
	 * Execute query and force return a String object
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @throws SQLException
	 */
	public String queryForString(String sql, Object... params) throws SQLException {
		return String.valueOf(queryForObject(sql, params));
	}

	/**
	 * Execute query and force return a Long object, runtime exception may throw if
	 * result can not be cast to long, SQLException may throw if SQL exception
	 * happen, Note: This method does not close connection.
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @throws SQLException
	 */
	public long queryForLongValue(Connection conn, String sql, Object... params) throws SQLException {
		return ((Number) queryForObject(conn, sql, params)).longValue();// NOSONAR
	}

	/**
	 * Execute query and force return a Long object, runtime exception may throw if
	 * result can not be cast to long, SQLException may throw if SQL exception
	 * happen
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @throws SQLException
	 */
	public long queryForLongValue(String sql, Object... params) throws SQLException {
		return ((Number) queryForObject(sql, params)).longValue();// NOSONAR
	}

	/**
	 * Execute a query and wrap result to Map List, Note: This method does not close
	 * connection.
	 * 
	 * @param sql
	 *            The SQL String
	 * @param params
	 *            The parameters
	 * @return A MapList result
	 * @throws SQLException
	 */
	public List<Map<String, Object>> queryForMapList(Connection conn, String sql, Object... params)
			throws SQLException {
		return query(conn, sql, new MapListHandler(), params);
	}

	/**
	 * Execute a query and wrap result to Map List
	 * 
	 * @param sql
	 *            The SQL String
	 * @param params
	 *            The parameters
	 * @return A MapList result
	 * @throws SQLException
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object... params) throws SQLException {
		return query(sql, new MapListHandler(), params);
	}

	// =======================================================================
	// Normal style methods but transfer SQLException to DbProRuntimeException

	/**
	 * Executes the given SELECT SQL query and returns a result object. Transaction
	 * mode is determined by connectionManager property. Note: this method does not
	 * close connection
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param sql
	 *            the SQL
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(Connection conn, ResultSetHandler rsh, String sql, Object... params) {
		try {
			return (T) query(conn, sql, rsh, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given SELECT SQL query and returns a result object. Transaction
	 * mode is determined by connectionManager property. Note: this method does not
	 * close connection
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param preparedSQL
	 *            The preparedSQL with include SQL, parameters and handlers
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(Connection conn, PreparedSQL preparedSQL) {
		return nQuery(conn, preparedSQL.getWrappedHandler(), preparedSQL.getSql(), preparedSQL.getParams());
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found , DbProRuntimeException may be threw if some SQL operation
	 * Exception happen. Note: This method does not close connection.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T nQueryForObject(Connection conn, String sql, Object... params) {
		return nQuery(conn, new ScalarHandler<T>(1), sql, params);
	}

	/**
	 * Execute query and force return a String object, no need catch SQLException.
	 * Note: This method does not close connection.
	 */
	public String nQueryForString(Connection conn, String sql, Object... params) {
		return String.valueOf(nQueryForObject(conn, sql, params));
	}

	/**
	 * Execute query and force return a Long object, no need catch SQLException,
	 * runtime exception may throw if result can not be cast to long. Note: This
	 * method does not close connection.
	 */
	public long nQueryForLongValue(Connection conn, String sql, Object... params) {
		try {
			return queryForLongValue(conn, sql, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute query and force return a List<Map<String, Object>> type result, no
	 * need catch SQLException. Note: This method does not close connection.
	 */
	public List<Map<String, Object>> nQueryForMapList(Connection conn, String sql, Object... params) {
		try {
			return query(conn, sql, new MapListHandler(), params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Note: This method
	 * does not close connection.
	 * 
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return The number of rows updated.
	 */
	public int nUpdate(Connection conn, String sql, Object... params) {
		try {
			return update(conn, sql, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT SQL statement. Note: This method does not close
	 * connection.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nInsert(Connection conn, ResultSetHandler rsh, String sql, Object... params) {
		try {
			return (T) insert(conn, sql, rsh, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Note: This method
	 * does not close connection.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param sql
	 *            the SQL
	 * @return The number of rows updated.
	 */
	public int nExecute(Connection conn, String sql, Object... params) {
		try {
			return execute(conn, sql, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters. Note: This method does not close
	 * connection.
	 * 
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param sql
	 *            the SQL
	 * @return A list of objects generated by the handler
	 * 
	 */
	public <T> List<T> nExecute(Connection conn, ResultSetHandler rsh, String sql, Object... params) {
		try {
			return execute(conn, sql, rsh, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters. Note: This method does not close
	 * connection.
	 * 
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param sql
	 *            the SQL
	 * @return A list of objects generated by the handler
	 * 
	 */
	public <T> List<T> nExecute(Connection conn, PreparedSQL preparedSQL) {
		return nExecute(conn, preparedSQL.getWrappedHandler(), preparedSQL.getSql(), preparedSQL.getParams());
	}

	/**
	 * Force flush cached SQLs
	 */
	public void nBatchFlush() {
		try {
			batchFlush();
		} catch (Exception e) {
			throw new DbProRuntimeException(e);
		}
	}

	/** Start batch sql */
	public void nBatchBegin() {
		try {
			batchBegin();
		} catch (Exception e) {
			throw new DbProRuntimeException(e);
		}
	}

	/** Stop batch sql */
	public void nBatchEnd() {
		try {
			batchEnd();
		} catch (Exception e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given SELECT SQL query and returns a result object. Transaction
	 * mode is determined by connectionManager property.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param sql
	 *            the SQL
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(ResultSetHandler rsh, String sql, Object... params) {
		try {
			return (T) query(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given SELECT SQL query and returns a result object. Transaction
	 * mode is determined by connectionManager property.
	 * 
	 * @param preparedSQL
	 *            The preparedSQL with include SQL, parameters and handlers
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(PreparedSQL preparedSQL) {
		return nQuery(preparedSQL.getWrappedHandler(), preparedSQL.getSql(), preparedSQL.getParams());
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found , DbProRuntimeException may be threw if some SQL operation
	 * Exception happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	@Override
	public <T> T nQueryForObject(String sql, Object... params) {
		return nQuery(new ScalarHandler<T>(1), sql, params);
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found , DbProRuntimeException may be threw if some SQL operation
	 * Exception happen.
	 * 
	 * @param preparedSQL
	 *            The preparedSQL which included SQL、parameters and handlers(if
	 *            have)
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T nQueryForObject(PreparedSQL preparedSQL) {
		ResultSetHandler handler = preparedSQL.getWrappedHandler();
		if (handler == null)
			return nQuery(new ScalarHandler<T>(1), preparedSQL.getSql(), preparedSQL.getParams());
		else
			return nQuery(handler, preparedSQL.getSql(), preparedSQL.getParams());
	}

	/**
	 * Execute query and force return a String object, no need catch SQLException
	 */
	public String nQueryForString(String sql, Object... params) {
		return String.valueOf(nQueryForObject(sql, params));
	}

	/**
	 * Execute query and force return a Long object, no need catch SQLException,
	 * runtime exception may throw if result can not be cast to long
	 */
	public long nQueryForLongValue(String sql, Object... params) {
		try {
			return queryForLongValue(sql, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute query and force return a List<Map<String, Object>> type result, no
	 * need catch SQLException
	 */
	public List<Map<String, Object>> nQueryForMapList(String sql, Object... params) {
		try {
			return query(sql, new MapListHandler(), params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Transaction mode
	 * is determined by connectionManager property.
	 * 
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return The number of rows updated.
	 */
	@Override
	public int nUpdate(String sql, Object... params) {
		try {
			return update(sql, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Transaction mode
	 * is determined by connectionManager property.
	 * 
	 * @param preparedSQL
	 *            The preparedSQL with include SQL, parameters and handlers
	 * @return The number of rows updated.
	 */
	public int nUpdate(PreparedSQL preparedSQL) {
		return nUpdate(preparedSQL.getSql(), preparedSQL.getParams());
	}

	/**
	 * Executes the given INSERT SQL statement. Transaction mode is determined by
	 * connectionManager property.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nInsert(ResultSetHandler rsh, String sql, Object... params) {
		try {
			return (T) insert(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param sql
	 *            the SQL
	 * @return The number of rows updated.
	 */
	@Override
	public int nExecute(String sql, Object... params) {
		try {
			return execute(sql, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param preparedSQL
	 *            the preparedSQL include SQL, parameters and Handlers(if have)
	 * @return The number of rows updated.
	 */
	public int nExecute(PreparedSQL preparedSQL) {
		return nExecute(preparedSQL.getSql(), preparedSQL.getParams());
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters. Transaction mode is determined by
	 * connectionManager property.
	 * 
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param sql
	 *            the SQL
	 * @return A list of objects generated by the handler
	 * 
	 */
	public <T> List<T> nExecute(ResultSetHandler rsh, String sql, Object... params) {
		try {
			return execute(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
	 * 
	 * @param sql
	 *            The SQL to execute.
	 * @param params
	 *            A List of parameter list.
	 * @return The number of rows updated per statement.
	 */
	public int[] nBatch(String sql, List<List<?>> params) {
		try {
			return batch(sql, listList2Arrays(params));
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
	 *
	 * @param conn
	 *            The Connection to use to run the query. The caller is responsible
	 *            for closing this Connection.
	 * @param sql
	 *            The SQL to execute.
	 * @param params
	 *            A List of parameter list.
	 * @return The number of rows updated per statement.
	 */
	public int[] nBatch(Connection conn, String sql, List<List<?>> params) throws SQLException {
		try {
			return batch(conn, sql, listList2Arrays(params));
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given batch of INSERT SQL statements.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param sql
	 *            The SQL statement to execute.
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param params
	 *            A List of parameter list.
	 * @return The result generated by the handler.
	 */
	public <T> T nInsertBatch(String sql, ResultSetHandler<T> rsh, List<List<?>> params) throws SQLException {
		try {
			return insertBatch(sql, rsh, listList2Arrays(params));
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given batch of INSERT SQL statements.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param conn
	 *            The connection to use to run the query.
	 * @param sql
	 *            The SQL to execute.
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param params
	 *            A List of parameter list.
	 * @return The result generated by the handler.
	 */
	public <T> T nInsertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, List<List<?>> params)
			throws SQLException {
		try {
			return this.insertBatch(conn, sql, rsh, listList2Arrays(params));
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/** Quite execute a SQL, do not throw any exception */
	public int quiteExecute(String sql, Object... params) {
		try {
			return execute(sql, params);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Build a PreparedSQL instance by given template style SQL and parameters
	 * stored in ThreadLocal
	 * 
	 * @param sqlTemplate
	 * @return PreparedSQL instance
	 */
	protected PreparedSQL mixedToSqlAndParams(String... sqlTemplate) {
		try {
			String sql = null;
			if (sqlTemplate != null) {
				StringBuilder sb = new StringBuilder("");
				for (String str : sqlTemplate)
					sb.append(str);
				sql = sb.toString();
			}
			Map<String, Object> paramMap = templateThreadlocalParamMapCache.get();
			return sqlTemplateEngine.render(sql, paramMap, directReplaceKeysCache.get());
		} finally {
			clearBind();
		}
	}

	/**
	 * Executes the Mixed-style(Inline+Template) given SELECT SQL query and returns
	 * a result object. Note: This method does not close connection.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T xQuery(Connection conn, ResultSetHandler rsh, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return (T) this.query(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an Mixed-style(Inline+Template) SQL query for an Object, only return
	 * the first row and first column's value if more than one column or more than 1
	 * rows returned, a null object may return if no result found ,
	 * DbProRuntimeException may be threw if some SQL operation Exception happen.
	 * Note: This method does not close connection.
	 * 
	 * @param templateSQL
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T xQueryForObject(Connection conn, String... templateSQL) {
		return xQuery(conn, new ScalarHandler<T>(), templateSQL);
	}

	/**
	 * Executes the Mixed-style(Inline+Template) given INSERT, UPDATE, or DELETE SQL
	 * statement. Note: This method does not close connection.
	 * 
	 * @param templateSQL
	 *            the SQL template
	 * @return The number of rows updated.
	 */
	public int xUpdate(Connection conn, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return update(conn, sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the Mixed-style(Inline+Template) given INSERT SQL statement. Note:
	 * This method does not close connection.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T xInsert(Connection conn, ResultSetHandler rsh, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return (T) insert(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an Mixed-style(Inline+Template) SQL statement, including a stored
	 * procedure call, which does not return any result sets. Any parameters which
	 * are instances of {@link OutParameter} will be registered as OUT parameters.
	 * Note: This method does not close connection.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 *
	 * @param templateSQL
	 *            the SQL template.
	 * @return The number of rows updated.
	 */
	public int xExecute(Connection conn, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return this.execute(conn, sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an Mixed-style(Inline+Template) SQL statement, including a stored
	 * procedure call, which returns one or more result sets. Any parameters which
	 * are instances of {@link OutParameter} will be registered as OUT parameters.
	 * Note: This method does not close connection.
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param templateSQL
	 *            the SQL template
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> xExecute(Connection conn, ResultSetHandler rsh, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return (List<T>) this.execute(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	// ====================================================================
	// SQL Template style and transfer SQLException to DbProRuntimeException,
	// parameters carried by a Map<String, Object> instance

	/**
	 * Mixed-style(Inline+Template) execute SQL query, force return a String value.
	 * Note: This method does not close connection
	 */
	public String xQueryForString(Connection conn, String... templateSQL) {
		return String.valueOf(xQueryForObject(conn, templateSQL));
	}

	/**
	 * Mixed-style(Inline+Template) execute SQL query, force return a Long value,
	 * runtime Exception may throw if result can not cast to long. Note: This method
	 * does not close connection
	 */
	public long xQueryForLongValue(Connection conn, String... templateSQL) {
		return ((Number) xQueryForObject(conn, templateSQL)).longValue();// NOSONAR
	}

	/**
	 * Mixed-style(Inline+Template) query and force return a List<Map<String,
	 * Object>> type result. Note: This method does not close connection
	 */
	public List<Map<String, Object>> xQueryForMapList(Connection conn, String... templateSQL) {
		return this.xQuery(conn, new MapListHandler(), templateSQL);
	}

	/**
	 * Executes the Mixed-style(Inline+Template) given SELECT SQL query and returns
	 * a result object. Transaction mode is determined by connectionManager
	 * property.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T xQuery(ResultSetHandler rsh, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return (T) this.query(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an Mixed-style(Inline+Template) SQL query for an Object, only return
	 * the first row and first column's value if more than one column or more than 1
	 * rows returned, a null object may return if no result found ,
	 * DbProRuntimeException may be threw if some SQL operation Exception happen.
	 * 
	 * @param templateSQL
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T xQueryForObject(String... templateSQL) {
		return xQuery(new ScalarHandler<T>(), templateSQL);
	}

	/**
	 * Executes the Mixed-style(Inline+Template) given INSERT, UPDATE, or DELETE SQL
	 * statement. Transaction mode is determined by connectionManager property.
	 * 
	 * @param templateSQL
	 *            the SQL template
	 * @return The number of rows updated.
	 */
	public int xUpdate(String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return this.update(sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the Mixed-style(Inline+Template) given INSERT SQL statement.
	 * Transaction mode is determined by connectionManager property.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param templateSQL
	 *            the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T xInsert(ResultSetHandler rsh, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return (T) insert(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an Mixed-style(Inline+Template) SQL statement, including a stored
	 * procedure call, which does not return any result sets. Any parameters which
	 * are instances of {@link OutParameter} will be registered as OUT parameters.
	 * Transaction mode is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 *
	 * @param templateSQL
	 *            the SQL template.
	 * @return The number of rows updated.
	 */
	public int xExecute(String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return this.execute(sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an Mixed-style(Inline+Template) SQL statement, including a stored
	 * procedure call, which returns one or more result sets. Any parameters which
	 * are instances of {@link OutParameter} will be registered as OUT parameters.
	 * Transaction mode is determined by connectionManager property.
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param templateSQL
	 *            the SQL template
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> xExecute(ResultSetHandler rsh, String... templateSQL) {
		try {
			PreparedSQL sp = mixedToSqlAndParams(templateSQL);
			return (List<T>) this.execute(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	// ====================================================================
	// SQL Template style and transfer SQLException to DbProRuntimeException,
	// parameters carried by a Map<String, Object> instance

	/**
	 * Mixed-style(Inline+Template) execute SQL query, force return a String value
	 */
	public String xQueryForString(String... templateSQL) {
		return String.valueOf(xQueryForObject(templateSQL));
	}

	/**
	 * Mixed-style(Inline+Template) execute SQL query, force return a Long value,
	 * runtime Exception may throw if result can not cast to long
	 */
	public long xQueryForLongValue(String... templateSQL) {
		return ((Number) xQueryForObject(templateSQL)).longValue();// NOSONAR
	}

	/**
	 * Mixed-style(Inline+Template) query and force return a List<Map<String,
	 * Object>> type result
	 */
	public List<Map<String, Object>> xQueryForMapList(String... templateSQL) {
		return this.xQuery(new MapListHandler(), templateSQL);
	}

}
