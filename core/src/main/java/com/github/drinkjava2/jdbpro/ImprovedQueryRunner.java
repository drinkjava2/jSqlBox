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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * ImprovedQueryRunner made below improvements compare DbUtils's QueryRunner:
 * 
 * 
 * 1) Override close() and prepareConnection() method of QueryRunner, use a
 * ConnectionManager to manage connection, ConnectionManager can get connection
 * from DataSource or ThreadLocal or some other 3rd party tools like Spring.
 * <br/>
 * 2) Override some methods to add logger support <br/>
 * 3) Override some execute/update/query methods to support batch operation and
 * AroundSqlHandler <br/>
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "all" })
public class ImprovedQueryRunner extends QueryRunner {
	protected static final DbProLog logger = DbProLogFactory.getLog(ImprovedQueryRunner.class);

	protected static Boolean globalNextAllowShowSql = false;
	protected static SqlOption globalNextMasterSlaveOption = SqlOption.USE_AUTO;
	protected static ConnectionManager globalNextConnectionManager = null;

	protected static Integer globalNextBatchSize = 300;
	protected static SqlTemplateEngine globalNextTemplateEngine = BasicSqlTemplate.instance();
	protected static SqlHandler[] globalNextSqlHandlers = null;

	protected SqlTemplateEngine sqlTemplateEngine = globalNextTemplateEngine;
	protected ConnectionManager connectionManager = globalNextConnectionManager;
	protected Boolean allowShowSQL = globalNextAllowShowSql;
	protected SqlOption masterSlaveOption = globalNextMasterSlaveOption;
	protected Integer batchSize = globalNextBatchSize;
	protected SqlHandler[] sqlHandlers = globalNextSqlHandlers;

	protected DbPro[] slaves;
	protected DbPro[] masters;
	protected String name;

	/** A ThreadLocal SqlHandler instance */
	private static ThreadLocal<SqlHandler[]> threadLocalSqlHandlers = new ThreadLocal<SqlHandler[]>();

	/**
	 * A ThreadLocal type tag to indicate current all SQL operations should be
	 * cached
	 */
	private ThreadLocal<Boolean> batchEnabled = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * A ThreadLocal type cache to store batch SQL and parameters
	 */
	private ThreadLocal<ArrayList<PreparedSQL>> sqlBatchCache = new ThreadLocal<ArrayList<PreparedSQL>>() {
		@Override
		protected ArrayList<PreparedSQL> initialValue() {
			return new ArrayList<PreparedSQL>();
		}
	};

	public ImprovedQueryRunner() {
		super();
	}

	public ImprovedQueryRunner(DataSource ds) {
		super(ds);
	}

	@Override
	public void close(Connection conn) throws SQLException {
		if (connectionManager == null)
			super.close(conn);
		else
			connectionManager.releaseConnection(conn, this.getDataSource());
	}

	@Override
	public Connection prepareConnection() throws SQLException {
		if (connectionManager == null)
			return super.prepareConnection();
		else
			return connectionManager.getConnection(this.getDataSource());
	}

	@Override
	protected CallableStatement prepareCall(Connection conn, String sql) throws SQLException {
		if (this.getAllowShowSQL() && !batchEnabled.get())
			logger.info("SQL: " + sql);
		return super.prepareCall(conn, sql);
	}

	@Override
	protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
		if (this.getAllowShowSQL() && !batchEnabled.get())
			logger.info(formatSqlForLoggerOutput(sql));
		return super.prepareStatement(conn, sql);
	}

	@Override
	public void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {
		if (this.getAllowShowSQL() && !batchEnabled.get())
			logger.info(formatParametersForLoggerOutput(params));
		super.fillStatement(stmt, params);
	}

	// =========== Explain SQL about methods========================
	/**
	 * Format SQL for logger output, subClass can override this method to customise
	 * SQL format
	 */
	protected String formatSqlForLoggerOutput(String sql) {
		return "SQL: " + sql;
	}

	/**
	 * Format parameters for logger output, subClass can override this method to
	 * customise parameters format
	 */
	protected String formatParametersForLoggerOutput(Object... params) {
		return "PAR: " + Arrays.deepToString(params);
	}

	// ===override execute/insert/update methods to support batch and explainSql
	// BTW, some methods in QueryRunner are private, otherwise no need override
	// so many methods

	/**
	 * Add SQL to cache, if full (reach batchSize) then call batchFlush() <br/>
	 * 
	 * @throws SQLException
	 * 
	 */
	private <T> T addToCacheIfFullFlush(PreparedSQL ps) {
		if (ps == null)
			throw new DbProException("PreparedSQL can not be null.");
		Object result = null;
		List<PreparedSQL> cached = sqlBatchCache.get();
		if (cached.size() >= this.batchSize)
			result = this.nBatchFlush();
		else if (!cached.isEmpty()) {
			PreparedSQL last = cached.get(cached.size() - 1);
			if (!last.getOperationType().equals(ps.getOperationType()) //
					|| last.getConnection() != ps.getConnection() //
					|| !last.getSql().equals(ps.getSql()) //
					|| last.getParamSize() != ps.getParamSize()//
					|| last.getResultSetHandler() != (ps.getResultSetHandler()))//
				result = this.nBatchFlush();
		}
		sqlBatchCache.get().add(ps);

		switch (ps.getOperationType()) {
		case UPDATE:
		case EXECUTE: {
			result = 0;
			break;
		}
		case INSERT: {
			result = null;
			break;
		}
		default:
			throw new DbProException("Unknow batch sql operation type:" + ps.getOperationType());
		}
		return (T) result;
	}

	// === Batch execute methods======
	/**
	 * Force flush cached SQLs
	 */
	public <T> T nBatchFlush() {
		List<PreparedSQL> psList = sqlBatchCache.get();
		if (psList.isEmpty())
			return null;
		Object result = null;
		PreparedSQL first = psList.get(0);

		int paramLenth = first.getParamSize();
		Object[][] allParams = new Object[psList.size()][paramLenth];

		if (paramLenth > 0)
			for (int i = 0; i < psList.size(); i++) {// cached parameters
				allParams[i] = psList.get(i).getParams();
			}
		if (this.getAllowShowSQL()) {
			logger.info("Batch execute " + psList.size() + " SQLs");
			logger.info(formatSqlForLoggerOutput(first.getSql()));
			logger.info("First row " + formatParametersForLoggerOutput(allParams[0]));
			logger.info("Last row " + formatParametersForLoggerOutput(allParams[allParams.length - 1]));
		}

		if (batchEnabled.get()) {
			switch (first.getOperationType()) {
			case UPDATE:
			case EXECUTE: {
				try {
					if (first.getConnection() != null)
						result = ((int[]) batch(first.getConnection(), first.getSql(), allParams)).length;
					else
						result = ((int[]) batch(first.getSql(), allParams)).length;
				} catch (SQLException e) {
					throw new DbProException(e);
				}
				break;
			}
			case INSERT: {
				if (first.getResultSetHandler() == null)
					throw new DbProException("insertBatch need a ResultSetHandler.");
				try {
					if (first.getConnection() != null)
						result = insertBatch(first.getConnection(), first.getSql(), first.getResultSetHandler(),
								allParams);
					else
						result = insertBatch(first.getSql(), first.getResultSetHandler(), allParams);
				} catch (SQLException e) {
					throw new DbProException(e);
				}
				break;
			}
			default:
				throw new DbProException("Unknow batch sql operation type:" + first.getOperationType());
			}
		}
		sqlBatchCache.get().clear();
		return (T) result;
	}

	/** Start batch sql */
	public void nBatchBegin() {
		if (!sqlBatchCache.get().isEmpty())
			nBatchFlush();
		this.batchEnabled.set(true);
	}

	/** Stop batch sql */
	public void nBatchEnd() {
		try {
			if (!sqlBatchCache.get().isEmpty())
				nBatchFlush();
		} finally {
			this.batchEnabled.set(false);
		}
	}

	// ===override execute/insert/update methods to support batch and explainSql
	// BTW, some methods in QueryRunner are private, otherwise no need override
	// so many methods

	// ==========================================================
	// DbUtils style methods, throw SQLException

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
	public <T> T queryForObject(Connection conn, String sql, Object... params) throws SQLException {
		return query(conn, sql, new ScalarHandler<T>(1), params);
	}

	/**
	 * Query for a long value
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @return A long value
	 * @throws SQLException
	 */
	public long queryForLongValue(Connection conn, String sql, Object... params) throws SQLException {
		return ((Number) queryForObject(conn, sql, params)).longValue();
	}

	/**
	 * Query for a int value
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @return A int value
	 * @throws SQLException
	 */
	public int queryForIntValue(Connection conn, String sql, Object... params) throws SQLException {
		return ((Number) queryForObject(conn, sql, params)).intValue();
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
	 * Query for a long value
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @return A long value
	 * @throws SQLException
	 */
	public long queryForLongValue(String sql, Object... params) throws SQLException {
		return ((Number) queryForObject(sql, params)).longValue();
	}

	/**
	 * Query for a int value
	 * 
	 * @param sql
	 *            The SQL
	 * @param params
	 *            The parameters
	 * @return A int value
	 * @throws SQLException
	 */
	public int queryForIntValue(String sql, Object... params) throws SQLException {
		return ((Number) queryForObject(sql, params)).intValue();
	}

	/**
	 * This is the core method of whole project, handle a PreparedSQL instance and
	 * return a result
	 */
	public Object runPreparedSQL(PreparedSQL ps) {
		if (ps.getSwitchTo() != null) {
			DbPro pro = ps.getSwitchTo();
			ps.setSwitchTo(null);
			return pro.runPreparedSQL(ps);// SwitchTo run
		}
		if (ps.getMasterSlaveOption() == null)
			ps.setMasterSlaveOption(this.getMasterSlaveOption());

		if (ps.getUseTemplate() != null && ps.getUseTemplate()) {
			ps.setUseTemplate(false);
			SqlTemplateEngine engine = ps.getTemplateEngine();
			if (engine == null)
				engine = this.sqlTemplateEngine;
			PreparedSQL rendered = engine.render(ps.getSql(), ps.getTemplateParamMap(), ps.getParams());
			ps.setSql(rendered.getSql());
			ps.setParams(rendered.getParams());
		}

		while (ps.getSqlHandlers() != null && !ps.getSqlHandlers().isEmpty()) {
			SqlHandler handler = ps.getSqlHandlers().get(0);
			ps.getSqlHandlers().remove(0);
			if (!ps.isDisabledHandler(handler))
				return handler.handle(this, ps);
		}
		return runRealSqlMethod(ps);
	}

	/** Execute real SQL operation according PreparedSql's SqlType */
	public Object runRealSqlMethod(PreparedSQL ps) {
		if (ps.getOperationType() == null)
			throw new DbProException("PreparedSQL's type not set");

		if (batchEnabled.get()) {
			switch (ps.getOperationType()) {
			case INSERT:
			case EXECUTE:
			case UPDATE:
				return addToCacheIfFullFlush(ps);
			}
		}

		switch (ps.getOperationType()) {
		case EXECUTE:
		case UPDATE:
		case INSERT: {
			if (SqlOption.USE_MASTER.equals(ps.getMasterSlaveOption())
					|| SqlOption.USE_AUTO.equals(ps.getMasterSlaveOption())) {
				return runWriteOperations(this, ps);
			} else if (SqlOption.USE_BOTH.equals(ps.getMasterSlaveOption())) {
				if (this.getSlaves() != null)
					for (DbPro dbPro : this.getSlaves())
						runWriteOperations(dbPro, ps);
				return runWriteOperations(this, ps);
			} else if (SqlOption.USE_SLAVE.equals(ps.getMasterSlaveOption())) {
				Object result = null;
				if (this.getSlaves() == null || this.getSlaves().length == 0)
					throw new DbProException("Try to write slaves but slave list not found");
				for (DbPro dbPro : this.getSlaves())
					result = runWriteOperations(dbPro, ps);
				return result;
			} else
				throw new DbProException("Should never run to here");
		}
		case QUERY: {
			if (SqlOption.USE_MASTER.equals(ps.getMasterSlaveOption())
					|| SqlOption.USE_BOTH.equals(ps.getMasterSlaveOption()))
				return this.runQuery(ps);
			else if (SqlOption.USE_SLAVE.equals(ps.getMasterSlaveOption())) {
				DbPro db = chooseOneSlave();
				if (db == null)
					throw new DbProException("Try to query on slave but slave list not found");
				return db.runQuery(ps);
			} else if (SqlOption.USE_AUTO.equals(ps.getMasterSlaveOption())) {
				DbPro db = autoChooseMasterOrSlaveQuery(ps);
				return db.runQuery(ps);
			} else
				throw new DbProException("Should never run to here");
		}
		}
		throw new DbProException("Unknow SQL operation type " + ps.getOperationType());
	}

	private Object runReadOperation(PreparedSQL ps) {
		if (SqlOption.USE_MASTER.equals(ps.getMasterSlaveOption())
				|| SqlOption.USE_BOTH.equals(ps.getMasterSlaveOption()))
			return this.runQuery(ps);
		else if (SqlOption.USE_SLAVE.equals(ps.getMasterSlaveOption())) {
			DbPro db = chooseOneSlave();
			if (db == null)
				throw new DbProException("Try to run a slave DbPro but slave list is null or empty");
			return db.runQuery(ps);
		} else if (SqlOption.USE_AUTO.equals(ps.getMasterSlaveOption())) {
			DbPro db = autoChooseMasterOrSlaveQuery(ps);
			return db.runQuery(ps);
		} else
			throw new DbProException("masterSlaveSelect property not set.");
	}

	private Object runWriteOperations(ImprovedQueryRunner dbPro, PreparedSQL ps) {
		switch (ps.getOperationType()) {
		case INSERT:
			return dbPro.runInsert(ps);
		case EXECUTE:
			return dbPro.runExecute(ps);
		case UPDATE:
			return dbPro.runUpdate(ps);
		}
		throw new DbProException("Should never run to here");
	}

	/**
	 * Executes the PreparedSQL query statement
	 * 
	 * @param ps
	 *            The PreparedSQL
	 * @return object(s) generated by ResultSetHandler
	 */
	protected <T> T runQuery(PreparedSQL ps) {
		if (ps.getResultSetHandler() != null) {
			try {
				if (ps.getConnection() != null) {
					if (ps.getParams() != null)
						return (T) query(ps.getConnection(), ps.getSql(), ps.getResultSetHandler(), ps.getParams());
					else
						return (T) query(ps.getConnection(), ps.getSql(), ps.getResultSetHandler());
				} else {
					if (ps.getParams() != null)
						return (T) query(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
					else
						return (T) query(ps.getSql(), ps.getResultSetHandler());
				}
			} catch (SQLException e) {
				throw new DbProException(e);
			}
		} else
			throw new DbProException("A ResultSetHandler is required by query method");
	}

	private DbPro autoChooseMasterOrSlaveQuery(PreparedSQL ps) {
		if (this.getSlaves() == null || this.getSlaves().length == 0 || (this.getConnectionManager() != null
				&& this.getConnectionManager().isInTransaction(this.getDataSource())))
			return (DbPro) this;
		DbPro slave = chooseOneSlave();
		if (slave == null)
			throw new DbProException("Try to run a slave DbPro but slave list is null or empty");
		return slave;
	}

	private <T> T runMasterQuery(PreparedSQL ps) throws SQLException {
		if (ps.getParams() != null)
			return (T) query(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
		else
			return (T) query(ps.getSql(), ps.getResultSetHandler());
	}

	/**
	 * Choose a slave DbPro instance, default rule is random choose, subClass can
	 * override this method to customize choosing strategy
	 * 
	 * @return A slave instance, if no found, return null;
	 */
	private DbPro chooseOneSlave() {
		if (this.slaves == null || this.slaves.length == 0)
			return null;
		return slaves[(new Random().nextInt(slaves.length))];
	}

	/**
	 * Executes the PreparedSQL insert statement
	 * 
	 * @param ps
	 *            The PreparedSQL
	 * @return An object generated by ResultSetHandler
	 */
	protected <T> T runInsert(PreparedSQL ps) {
		if (ps.getResultSetHandler() != null) {
			try {
				if (ps.getConnection() != null) {
					if (ps.getParams() != null)
						return (T) insert(ps.getConnection(), ps.getSql(), ps.getResultSetHandler(), ps.getParams());
					else
						return (T) insert(ps.getConnection(), ps.getSql(), ps.getResultSetHandler());
				} else {
					if (ps.getParams() != null)
						return (T) insert(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
					else
						return (T) insert(ps.getSql(), ps.getResultSetHandler());
				}
			} catch (SQLException e) {
				throw new DbProException(e);
			}
		} else
			throw new DbProException("A ResultSetHandler is required by insert method");
	}

	/**
	 * Executes the PreparedSQL query statement
	 * 
	 * @param ps
	 *            The PreparedSQL
	 * @return The number of rows updated.
	 */
	protected <T> T runExecute(PreparedSQL ps) {
		try {
			if (ps.getResultSetHandler() != null) {
				if (ps.getConnection() != null)
					return (T) this.execute(ps.getConnection(), ps.getSql(), ps.getResultSetHandler(), ps.getParams());
				else
					return (T) this.execute(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
			} else {
				if (ps.getConnection() != null)
					return (T) (Integer) this.execute(ps.getConnection(), ps.getSql(), ps.getParams());
				else
					return (T) (Integer) this.execute(ps.getSql(), ps.getParams());
			}
		} catch (SQLException e) {
			throw new DbProException(e);
		}
	}

	/**
	 * Executes the PreparedSQL update statement
	 * 
	 * @param ps
	 *            The PreparedSQL
	 * @return The number of rows updated
	 */
	protected int runUpdate(PreparedSQL ps) {
		try {
			if (ps.getResultSetHandler() != null) {
				// Ignore ResultSetHandler for Update methods
			}
			if (ps.getConnection() != null) {
				if (ps.getParams() != null)
					return update(ps.getConnection(), ps.getSql(), ps.getParams());
				else
					return update(ps.getConnection(), ps.getSql());
			} else {
				if (ps.getParams() != null)
					return update(ps.getSql(), ps.getParams());
				else
					return update(ps.getSql());
			}
		} catch (SQLException e) {
			throw new DbProException(e);
		}
	}

	/**
	 * Query for an scalar Object, only return the first row and first column's
	 * value if more than one column or more than 1 rows returned, a null object may
	 * return if no result found , DbProRuntimeException may be threw if some SQL
	 * operation Exception happen.
	 * 
	 * @param ps
	 *            The PreparedSQL which included SQL parameters and sqlHandlers(if
	 *            have)
	 * @return An Object or null, Object type determined by SQL content
	 */
	private <T> T runQueryForScalar(PreparedSQL ps) {
		try {
			if (ps.getResultSetHandler() == null)
				return query(ps.getSql(), new ScalarHandler<T>(1), ps.getParams());
			else
				return (T) query(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
		} catch (SQLException e) {
			throw new DbProException(e);
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
	public int[] nBatch(String sql, List<Object[]> params) {
		try {
			return batch(sql, objectsListToArray2D(params));
		} catch (SQLException e) {
			throw new DbProException(e);
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
	public int[] nBatch(Connection conn, String sql, List<Object[]> params) throws SQLException {
		try {
			return batch(conn, sql, objectsListToArray2D(params));
		} catch (SQLException e) {
			throw new DbProException(e);
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
	public <T> T nInsertBatch(String sql, ResultSetHandler<T> rsh, List<Object[]> params) {
		try {
			return insertBatch(sql, rsh, objectsListToArray2D(params));
		} catch (SQLException e) {
			throw new DbProException(e);
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
	public <T> T nInsertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, List<Object[]> params) {
		try {
			return this.insertBatch(conn, sql, rsh, objectsListToArray2D(params));
		} catch (SQLException e) {
			throw new DbProException(e);
		}
	}

	private void specialStaticMethods_____________________() {// NOSONAR
	}

	// ===override execute/insert/update methods to support batch and explainSql
	// BTW, some methods in QueryRunner are private, otherwise no need override
	// so many methods
	/**
	 * Convert Objects List to 2d array for insertBatch use, insertBatch's last
	 * parameter is a 2d array, not easy to use
	 */
	public static Object[][] listListToArray2D(List<List<?>> paramList) {
		Object[][] array = new Object[paramList.size()][];
		int i = 0;
		for (List<?> item : paramList)
			array[i++] = item.toArray(new Object[item.size()]);
		return array;
	}

	/**
	 * Convert List List to 2d array for insertBatch use, insertBatch's last
	 * parameter is a 2d array, not easy to use
	 */
	public static Object[][] objectsListToArray2D(List<Object[]> paramList) {
		Object[][] array = new Object[paramList.size()][];
		int i = 0;
		for (Object[] item : paramList)
			array[i++] = item;
		return array;
	}

	/**
	 * Get current thread's ThreadLocal SqlHandler
	 */
	public static SqlHandler[] getThreadLocalSqlHandlers() {
		return threadLocalSqlHandlers.get();
	}

	/**
	 * Set current thread's ThreadLocal SqlHandler
	 */
	public static void setThreadLocalSqlHandlers(SqlHandler... handlers) {
		threadLocalSqlHandlers.set(handlers);
	}

	protected void staticGlobalNextMethods_____________________() {// NOSONAR
	}

	public static Integer getGlobalNextBatchSize() {
		return globalNextBatchSize;
	}

	public static void setGlobalNextBatchSize(Integer batchSize) {
		globalNextBatchSize = batchSize;
	}

	public static SqlTemplateEngine getGlobalNextTemplateEngine() {
		return globalNextTemplateEngine;
	}

	public static void setGlobalNextTemplateEngine(SqlTemplateEngine sqlTemplateEngine) {
		globalNextTemplateEngine = sqlTemplateEngine;
	}

	public static Boolean getGlobalNextAllowShowSql() {
		return globalNextAllowShowSql;
	}

	public static void setGlobalNextAllowShowSql(Boolean allowShowSql) {
		globalNextAllowShowSql = allowShowSql;
	}

	public static SqlOption getGlobalNextMasterSlaveOption() {
		return globalNextMasterSlaveOption;
	}

	public static void setGlobalNextMasterSlaveOption(SqlOption masterSlaveOption) {
		globalNextMasterSlaveOption = masterSlaveOption;
	}

	public static ConnectionManager getGlobalNextConnectionManager() {
		return globalNextConnectionManager;
	}

	public static void setGlobalNextConnectionManager(ConnectionManager connectionManager) {
		globalNextConnectionManager = connectionManager;
	}

	public static SqlHandler[] getGlobalNextSqlHandlers() {
		return globalNextSqlHandlers;
	}

	public static void setGlobalNextSqlHandlers(SqlHandler... sqlHandlers) {
		globalNextSqlHandlers = sqlHandlers;
	}

	private void normalGetterSetters_____________________() {// NOSONAR
	}

	public Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setAllowShowSQL(Boolean allowShowSQL) {// NOSONAR
		this.allowShowSQL = allowShowSQL;
	}

	public SqlTemplateEngine getSqlTemplateEngine() {
		return sqlTemplateEngine;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSqlTemplateEngine(SqlTemplateEngine sqlTemplateEngine) {
		this.sqlTemplateEngine = sqlTemplateEngine;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setBatchSize(Integer batchSize) {// NOSONAR
		this.batchSize = batchSize;
	}

	public SqlHandler[] getSqlHandlers() {
		return sqlHandlers;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSqlHandlers(SqlHandler[] sqlHandlers) {// NOSONAR
		this.sqlHandlers = sqlHandlers;
	}

	public DbPro[] getSlaves() {
		return slaves;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSlaves(DbPro[] slaves) {// NOSONAR
		this.slaves = slaves;
	}

	public DbPro[] getMasters() {
		return masters;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setMasters(DbPro[] masters) {// NOSONAR
		this.masters = masters;
	}

	public SqlOption getMasterSlaveOption() {
		return masterSlaveOption;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setMasterSlaveOption(SqlOption masterSlaveOption) {// NOSONAR
		this.masterSlaveOption = masterSlaveOption;
	}

	public String getName() {
		return name;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setName(String name) {
		this.name = name;
	}

	public boolean isBatchEnabled() {
		return batchEnabled.get();
	}

	public ThreadLocal<ArrayList<PreparedSQL>> getSqlBatchCache() {
		return sqlBatchCache;
	}

}
