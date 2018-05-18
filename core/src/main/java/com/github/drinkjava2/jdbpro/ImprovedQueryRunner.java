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

	protected SqlTemplateEngine sqlTemplateEngine = DbProConfig.globalNextTemplateEngine;
	protected ConnectionManager connectionManager = DbProConfig.globalNextConnectionManager;
	protected Boolean allowShowSQL = DbProConfig.globalNextAllowShowSql;
	protected SqlOption masterSlaveSelect = DbProConfig.globalNextMasterSlaveSelect;
	protected DbProLogger logger = DbProConfig.globalNextLogger;
	protected Integer batchSize = DbProConfig.globalNextBatchSize;
	protected SqlHandler[] sqlHandlers = DbProConfig.globalNextSqlHandlers;
	protected SpecialSqlItemPreparer[] specialSqlItemPreparers = DbProConfig.globalNextSpecialSqlItemPreparers;
	protected DbPro[] slaves;
	protected DbPro[] masters;
	protected String name;

	/**
	 * An IOC tool is needed if want use SqlMapper style and Annotation has
	 * parameters
	 */
	protected IocTool iocTool = DbProConfig.globalNextIocTool;

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

	public ImprovedQueryRunner(DataSource ds, ConnectionManager cm) {
		super(ds);
		this.connectionManager = cm;
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
	 * Format SQL for logger output, subClass can override this method to
	 * customise SQL format
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
			throw new DbProRuntimeException("PreparedSQL can not be null.");
		Object result = null;
		List<PreparedSQL> cached = sqlBatchCache.get();
		if (cached.size() >= this.batchSize)
			result = this.nBatchFlush();
		else if (!cached.isEmpty()) {
			PreparedSQL last = cached.get(cached.size() - 1);
			if (!last.getType().equals(ps.getType()) //
					|| last.getConnection() != ps.getConnection() //
					|| !last.getSql().equals(ps.getSql()) //
					|| last.getParamSize() != ps.getParamSize()//
					|| last.getResultSetHandler() != (ps.getResultSetHandler()))//
				result = this.nBatchFlush();
		}
		sqlBatchCache.get().add(ps);

		switch (ps.getType()) {
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
			throw new DbProRuntimeException("Unknow batch sql operation type:" + ps.getType());
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
			switch (first.getType()) {
			case UPDATE:
			case EXECUTE: {
				try {
					if (first.getConnection() != null)
						result = ((int[]) batch(first.getConnection(), first.getSql(), allParams)).length;
					else
						result = ((int[]) batch(first.getSql(), allParams)).length;
				} catch (SQLException e) {
					throw new DbProRuntimeException(e);
				}
				break;
			}
			case INSERT: {
				if (first.getResultSetHandler() == null)
					throw new DbProRuntimeException("insertBatch need a ResultSetHandler.");
				try {
					if (first.getConnection() != null)
						result = insertBatch(first.getConnection(), first.getSql(), first.getResultSetHandler(),
								allParams);
					else
						result = insertBatch(first.getSql(), first.getResultSetHandler(), allParams);
				} catch (SQLException e) {
					throw new DbProRuntimeException(e);
				}
				break;
			}
			default:
				throw new DbProRuntimeException("Unknow batch sql operation type:" + first.getType());
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
	 * Query for an Object, only return the first row and first column's value
	 * if more than one column or more than 1 rows returned, a null object may
	 * return if no result found, SQLException may be threw if some SQL
	 * operation Exception happen.
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
	 * Query for an Object, only return the first row and first column's value
	 * if more than one column or more than 1 rows returned, a null object may
	 * return if no result found, SQLException may be threw if some SQL
	 * operation Exception happen.
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
	 * This is the core method of whole project, handle a PreparedSQL instance
	 * and return a result
	 */
	public Object runPreparedSQL(PreparedSQL ps) {
		if (ps.getSwitchTo() != null) {
			DbPro pro = ps.getSwitchTo();
			ps.setSwitchTo(null);
			return pro.runPreparedSQL(ps);
		}
		if (ps.getMasterSlaveSelect() == null)
			ps.setMasterSlaveSelect(this.getMasterSlaveSelect());

		if (ps.getUseTemplate()) {
			ps.setUseTemplate(false);
			SqlTemplateEngine engine = ps.getTemplateEngine();
			if (engine == null)
				engine = this.sqlTemplateEngine;
			PreparedSQL rendered = engine.render(ps.getSql(), ps.getTemplateParamMap());
			ps.setSql(rendered.getSql());
			ps.setParams(rendered.getParams());
		}

		if (ps.getSqlHandlers() != null && !ps.getSqlHandlers().isEmpty()) {
			SqlHandler newPs = ps.getSqlHandlers().get(0);
			ps.getSqlHandlers().remove(0);
			return newPs.handle(this, ps);
		}
		return runRealSqlMethod(ps);
	}

	/** Execute real SQL operation according PreparedSql's SqlType */
	public Object runRealSqlMethod(PreparedSQL ps) {
		if (ps.getType() == null)
			throw new DbProRuntimeException("PreparedSQL's type not set");

		if (batchEnabled.get()) {
			switch (ps.getType()) {
			case INSERT:
			case EXECUTE:
			case UPDATE:
				return addToCacheIfFullFlush(ps);
			}
		}

		switch (ps.getType()) {
		case EXECUTE:
		case UPDATE:
		case INSERT: {
			if (SqlOption.USE_MASTER.equals(ps.getMasterSlaveSelect())
					|| SqlOption.USE_AUTO.equals(ps.getMasterSlaveSelect())) {
				return runWriteOperations(this, ps);
			} else if (SqlOption.USE_BOTH.equals(ps.getMasterSlaveSelect())) {
				if (this.getSlaves() != null)
					for (DbPro dbPro : this.getSlaves())
						runWriteOperations(dbPro, ps);
				return runWriteOperations(this, ps);
			} else if (SqlOption.USE_SLAVE.equals(ps.getMasterSlaveSelect())) {
				Object result = null;
				if (this.getSlaves() == null || this.getSlaves().length == 0)
					throw new DbProRuntimeException("Try to write slaves but slave list not found");
				for (DbPro dbPro : this.getSlaves())
					result = runWriteOperations(dbPro, ps);
				return result;
			} else
				throw new DbProRuntimeException("Should never run to here");
		}
		case QUERY: {
			if (SqlOption.USE_MASTER.equals(ps.getMasterSlaveSelect())
					|| SqlOption.USE_BOTH.equals(ps.getMasterSlaveSelect()))
				return this.runQuery(ps);
			else if (SqlOption.USE_SLAVE.equals(ps.getMasterSlaveSelect())) {
				DbPro db = chooseOneSlave();
				if (db == null)
					throw new DbProRuntimeException("Try to query on slave but slave list not found");
				return db.runQuery(ps);
			} else if (SqlOption.USE_AUTO.equals(ps.getMasterSlaveSelect())) {
				DbPro db = autoChooseMasterOrSlaveQuery(ps);
				return db.runQuery(ps);
			} else
				throw new DbProRuntimeException("Should never run to here");
		}
		}
		throw new DbProRuntimeException("Unknow SQL operation type " + ps.getType());
	}

	private Object runReadOperation(PreparedSQL ps) {
		if (SqlOption.USE_MASTER.equals(ps.getMasterSlaveSelect())
				|| SqlOption.USE_BOTH.equals(ps.getMasterSlaveSelect()))
			return this.runQuery(ps);
		else if (SqlOption.USE_SLAVE.equals(ps.getMasterSlaveSelect())) {
			DbPro db = chooseOneSlave();
			if (db == null)
				throw new DbProRuntimeException("Try to run a slave DbPro but slave list is null or empty");
			return db.runQuery(ps);
		} else if (SqlOption.USE_AUTO.equals(ps.getMasterSlaveSelect())) {
			DbPro db = autoChooseMasterOrSlaveQuery(ps);
			return db.runQuery(ps);
		} else
			throw new DbProRuntimeException("masterSlaveSelect property not set.");
	}

	private Object runWriteOperations(ImprovedQueryRunner dbPro, PreparedSQL ps) {
		switch (ps.getType()) {
		case INSERT:
			return dbPro.runInsert(ps);
		case EXECUTE:
			return dbPro.runExecute(ps);
		case UPDATE:
			return dbPro.runUpdate(ps);
		}
		throw new DbProRuntimeException("Should never run to here");
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
				throw new DbProRuntimeException(e);
			}
		} else
			throw new DbProRuntimeException("A ResultSetHandler is required by query method");
	}

	private DbPro autoChooseMasterOrSlaveQuery(PreparedSQL ps) {
		if (this.getSlaves() == null || this.getSlaves().length == 0 || (this.getConnectionManager() != null
				&& this.getConnectionManager().isInTransaction(this.getDataSource())))
			return (DbPro) this;
		DbPro slave = chooseOneSlave();
		if (slave == null)
			throw new DbProRuntimeException("Try to run a slave DbPro but slave list is null or empty");
		return slave;
	}

	private <T> T runMasterQuery(PreparedSQL ps) throws SQLException {
		if (ps.getParams() != null)
			return (T) query(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
		else
			return (T) query(ps.getSql(), ps.getResultSetHandler());
	}

	/**
	 * Choose a slave DbPro instance, default rule is random choose, subClass
	 * can override this method to customize choosing strategy
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
				throw new DbProRuntimeException(e);
			}
		} else
			throw new DbProRuntimeException("A ResultSetHandler is required by insert method");
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
			throw new DbProRuntimeException(e);
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
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Query for an scalar Object, only return the first row and first column's
	 * value if more than one column or more than 1 rows returned, a null object
	 * may return if no result found , DbProRuntimeException may be threw if
	 * some SQL operation Exception happen.
	 * 
	 * @param ps
	 *            The PreparedSQL which included SQL parameters and
	 *            sqlHandlers(if have)
	 * @return An Object or null, Object type determined by SQL content
	 */
	private <T> T runQueryForScalar(PreparedSQL ps) {
		try {
			if (ps.getResultSetHandler() == null)
				return query(ps.getSql(), new ScalarHandler<T>(1), ps.getParams());
			else
				return (T) query(ps.getSql(), ps.getResultSetHandler(), ps.getParams());
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
	public int[] nBatch(String sql, List<Object[]> params) {
		try {
			return batch(sql, objectsListToArray2D(params));
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
	 *
	 * @param conn
	 *            The Connection to use to run the query. The caller is
	 *            responsible for closing this Connection.
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
	public <T> T nInsertBatch(String sql, ResultSetHandler<T> rsh, List<Object[]> params) {
		try {
			return insertBatch(sql, rsh, objectsListToArray2D(params));
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
	public <T> T nInsertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, List<Object[]> params) {
		try {
			return this.insertBatch(conn, sql, rsh, objectsListToArray2D(params));
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
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

	private void normalGetterSetters_____________________() {// NOSONAR
	}

	public Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setAllowShowSQL(Boolean allowShowSQL) {
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

	public DbProLogger getLogger() {
		return logger;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setLogger(DbProLogger logger) {
		this.logger = logger;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public SqlHandler[] getSqlHandlers() {
		return sqlHandlers;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSqlHandlers(SqlHandler[] sqlHandlers) {
		this.sqlHandlers = sqlHandlers;
	}

	public SpecialSqlItemPreparer[] getSpecialSqlItemPreparers() {
		return specialSqlItemPreparers;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSpecialSqlItemPreparers(SpecialSqlItemPreparer[] specialSqlItemPreparers) {
		this.specialSqlItemPreparers = specialSqlItemPreparers;
	}

	public DbPro[] getSlaves() {
		return slaves;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSlaves(DbPro[] slaves) {
		this.slaves = slaves;
	}

	public DbPro[] getMasters() {
		return masters;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setMasters(DbPro[] masters) {
		this.masters = masters;
	}

	public IocTool getIocTool() {
		return iocTool;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setIocTool(IocTool iocTool) {
		this.iocTool = iocTool;
	}

	public SqlOption getMasterSlaveSelect() {
		return masterSlaveSelect;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setMasterSlaveSelect(SqlOption masterSlaveSelect) {
		this.masterSlaveSelect = masterSlaveSelect;
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
