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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.converter.BasicJavaToJdbcConverter;
import com.github.drinkjava2.jdialects.converter.BasicJdbcToJavaConverter;
import com.github.drinkjava2.jdialects.converter.JavaToJdbcConverter;
import com.github.drinkjava2.jdialects.converter.JdbcToJavaConverter;
import com.github.drinkjava2.jlogs.Log;
import com.github.drinkjava2.jlogs.LogFactory;
import com.github.drinkjava2.jsqlbox.DbException;
import com.github.drinkjava2.jsqlbox.TxBody;
import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.DataSourceHolder;
import com.github.drinkjava2.jtransactions.TxResult;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

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
public class ImprovedQueryRunner extends QueryRunner implements DataSourceHolder {
	protected static final Log logger = LogFactory.getLog(ImprovedQueryRunner.class);

	protected static Dialect globalNextDialect = null;
	protected static Boolean globalNextAllowShowSql = false;
	protected static SqlOption globalNextMasterSlaveOption = SqlOption.USE_AUTO;
	protected static ConnectionManager globalNextConnectionManager = TinyTxConnectionManager.instance();
	protected static JavaToJdbcConverter globalNextJavaToJdbcConverter = BasicJavaToJdbcConverter.instance;
	protected static JdbcToJavaConverter globalNextJdbcToJavaConverter = BasicJdbcToJavaConverter.instance;
	protected static TenantGetter globalNextTenantGetter = null;
	protected static SqlItemHandler globalNextSqlItemHandler = null;
	

	protected static Integer globalNextBatchSize = 300;
	protected static SqlHandler[] globalNextSqlHandlers = null;

	protected ConnectionManager connectionManager = globalNextConnectionManager;
	protected Boolean allowShowSQL = globalNextAllowShowSql;
	protected SqlOption masterSlaveOption = globalNextMasterSlaveOption;
	protected Integer batchSize = globalNextBatchSize;
	protected SqlHandler[] sqlHandlers = globalNextSqlHandlers;
	protected Dialect dialect = globalNextDialect;
	protected JavaToJdbcConverter javaToJdbcConverter = globalNextJavaToJdbcConverter;
	protected JdbcToJavaConverter jdbcToJavaConverter = globalNextJdbcToJavaConverter;
	protected TenantGetter tenantGetter = globalNextTenantGetter;
	protected SqlItemHandler sqlItemHandler = globalNextSqlItemHandler;

	protected DbPro[] slaves;
	protected DbPro[] masters;
	protected String name; // A name for current runner
	protected Integer dbCode = 0; // A unique code used to identify database

	/** A ThreadLocal TxResult instance store last transation result */
	private static ThreadLocal<TxResult> lastTxResult = new ThreadLocal<TxResult>();

	/** A ThreadLocal SqlHandler instance */
	private static ThreadLocal<SqlHandler[]> threadLocalSqlHandlers = new ThreadLocal<SqlHandler[]>();

	/** A ThreadLocal instance, if >0, will cause commit fail, for unit test only */
	protected ThreadLocal<Integer> threadLocalForceCommitFail = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};

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
	
	/** others store other infos writen in in-line sql */
	private ThreadLocal<List<Object[]>> others = new ThreadLocal<List<Object[]>>();

	public ImprovedQueryRunner() {
		super();
		this.dialect = globalNextDialect;
		pmdKnownBroken = true; // MSSql Server newest JDBC driver doesnot support pmd
	}

	public ImprovedQueryRunner(DataSource ds) {
		super(ds);
		if (globalNextDialect != null)
			dialect = globalNextDialect;
		else
			dialect = Dialect.guessDialect(ds);
		pmdKnownBroken = true; // MSSql Server newest JDBC driver doesnot support pmd
	}

	public ImprovedQueryRunner(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
		pmdKnownBroken = true; // MSSql Server newest JDBC driver doesnot support pmd
	}

	@Override
	public Object getHolder() {// This is to implement DataSourceHolder interface
		return this;
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
			return connectionManager.getConnection(this);
	}


	// =========== Explain SQL about methods========================
	/**
	 * Format SQL for logger output, subClass can override this method to customise
	 * SQL format
	 */
	protected String formatSqlForLoggerOutput(String sql) {
		return new StringBuilder(name == null ? "" : name).append(dbCode <= 0 ? "" : dbCode + " ").append("SQL: ")
				.append(sql).toString();
	}

	/**
	 * Format parameters for logger output, subClass can override this method to
	 * customise parameters format
	 */
	protected String formatParametersForLoggerOutput(Object... params) {
		return new StringBuilder(name == null ? "" : name).append(dbCode <= 0 ? "" : dbCode + " ").append("PAR: ")
				.append(Arrays.deepToString(params)).toString();
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
	    if(this.tenantGetter!=null) {
	        ImprovedQueryRunner runner=tenantGetter.getTenant();
	        if(runner==null)
	            throw new DbException("tenantGetter can not return a null Object");
	        return runner.runPreparedSQL(ps);
	    }
        if(ps.getSwitchTo() != null){
            DbPro pro = ps.getSwitchTo();
            ps.setSwitchTo(null);
            return pro.runPreparedSQL(ps);// SwitchTo run
        }
		if (ps.getParams().length > 0) {
			for (int i = 0; i < ps.getParams().length; i++) {
				ps.getParams()[i] = javaToJdbcConverter.convert(ps.getParams()[i]);
			}
		}
		if (ps.getMasterSlaveOption() == null)
			ps.setMasterSlaveOption(this.getMasterSlaveOption());

		if (ps.getTemplateEngine()!=null) {
			PreparedSQL rendered = ps.getTemplateEngine().render(ps.getSql(), ps.getTemplateParamMap(), ps.getParams());
			ps.setSql(rendered.getSql());
			ps.setParams(rendered.getParams());
			ps.setTemplateEngine(null);
		}

		while (ps.getSqlHandlers() != null && !ps.getSqlHandlers().isEmpty()) {
			SqlHandler handler = ps.getSqlHandlers().get(0);
			ps.getSqlHandlers().remove(0);
			if (!ps.isDisabledHandler(handler)) { 
				return jdbcToJavaConverter.convert(handler.handle(this, ps));
			}
		}
		
		others.remove();
		if(ps.getOthers()!=null) { //has other info? store in threadLocal for user fetch
			others.set(ps.getOthers());
		}
		return jdbcToJavaConverter.convert(runRealSqlMethod(ps)); 
	}

	/** Execute real SQL operation according PreparedSql's SqlType */
	public Object runRealSqlMethod(PreparedSQL ps) {
		if (ps.getOperationType() == null)
			throw new DbProException("PreparedSQL's type not set");
		
	      if (this.getAllowShowSQL() && !batchEnabled.get()) {
	            logger.info(formatSqlForLoggerOutput(ps.getSql()));
	            logger.info(formatParametersForLoggerOutput(ps.getParams()));
	      }

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
		if (this.getSlaves() == null || this.getSlaves().length == 0
				|| (this.getConnectionManager() != null && this.getConnectionManager().isInTransaction()))
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

	private void specialMethods_____________________() {// NOSONAR
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
 
	
	/** Get current thread's ThreadLocal SqlOption.Other type SqlItems */
	public List<Object[]> getOthers() {
		return others.get();
	} 

	/**
	 * Set current thread's ThreadLocal SqlHandler
	 */
	public static void setThreadLocalSqlHandlers(SqlHandler... handlers) {
		threadLocalSqlHandlers.set(handlers);
	}

	public Integer getForceCommitFail() {
		return threadLocalForceCommitFail.get();
	}

	/** Force current runner commit fail forever */
	public void setForceCommitFail() {
		threadLocalForceCommitFail.set(-1);
	}

	/** Force current runner commit fail failCount times */
	public void setForceCommitFail(Integer failCount) {
		threadLocalForceCommitFail.set(failCount);
	}

	protected void connectionManagerWrapMethods_____________________() {// NOSONAR
	}

	/**
	 * Check if a connection already be get from given dataSource and be cached as
	 * it started a Transaction
	 */
	public boolean isIntrans() {
		return this.getConnectionManager().isInTransaction();
	}

	/** Start a transaction */
	public void startTrans() {
		this.getConnectionManager().startTransaction();
	}

	/** Start a transaction with given connection isolation level */
	public void startTrans(int txIsolationLevel) {
		this.getConnectionManager().startTransaction(txIsolationLevel);
	}

	/**
	 * A ConnectionManager implementation determine how to get connection from
	 * DataSource or ThreadLocal or from Spring or JTA or some container...
	 */
	public Connection getConnection() throws SQLException {
		return this.getConnectionManager().getConnection(this);
	}

	/**
	 * A ConnectionManager implementation determine how to close connection or
	 * return connection to ThreadLocal or return to Spring or JTA or some
	 * container...
	 */
	public void releaseConnection(Connection conn) throws SQLException {
		this.getConnectionManager().releaseConnection(conn, this);
	}

	/** Commit the transaction */
	public TxResult commitTrans() throws Exception {
		return this.getConnectionManager().commitTransaction();
	}

	/** Roll back the transaction */
	public TxResult rollbackTrans() {
		return this.getConnectionManager().rollbackTransaction();
	}

	protected void txTemplateMethods______________________________() {// NOSONAR
	}

	/**  */
	public boolean tryTx(TxBody txBody) {
		TxResult txResult;
		this.startTrans();
		try {
			txBody.run();
			lastTxResult.set(commitTrans());
			return true;
		} catch (Exception e) {
			lastTxResult.set(rollbackTrans().addCommitEx(e));
			return false;
		}
	}

	public void tx(TxBody txBody) {
		this.startTrans();
		try {
			txBody.run();
			lastTxResult.set(commitTrans());
		} catch (Exception e) {
			lastTxResult.set(rollbackTrans().addCommitEx(e));
			throw new DbException(e);
		}
	}

	public static TxResult getLastTxResult() {
		return lastTxResult.get();
	}

	protected void staticGlobalNextMethods_____________________() {// NOSONAR
	}

	public static Dialect getGlobalNextDialect() {
		return globalNextDialect;
	}

	public static void setGlobalNextDialect(Dialect dialect) {
		globalNextDialect = dialect;
	}

	public static Integer getGlobalNextBatchSize() {
		return globalNextBatchSize;
	}

	public static void setGlobalNextBatchSize(Integer batchSize) {
		globalNextBatchSize = batchSize;
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

	public static JavaToJdbcConverter getGlobalNextJavaToJdbcConverter() {
		return globalNextJavaToJdbcConverter;
	}

	public static void setGlobalNextJavaToJdbcConverter(JavaToJdbcConverter globalNextJavaToJdbcConverter) {
		ImprovedQueryRunner.globalNextJavaToJdbcConverter = globalNextJavaToJdbcConverter;
	}

	public static JdbcToJavaConverter getGlobalNextJdbcToJavaConverter() {
		return globalNextJdbcToJavaConverter;
	}

	public static void setGlobalNextJdbcToJavaConverter(JdbcToJavaConverter globalNextJdbcToJavaConverter) {
		ImprovedQueryRunner.globalNextJdbcToJavaConverter = globalNextJdbcToJavaConverter;
	}

    public static TenantGetter getGlobalNextTenantGetter() {
        return globalNextTenantGetter;
    }

    public static void setGlobalNextTenantGetter(TenantGetter globalNextTenantGetter) {
        ImprovedQueryRunner.globalNextTenantGetter = globalNextTenantGetter;
    }
    
    public static void setGlobalNextSqlItemHandler(SqlItemHandler globalNextSqlItemHandler) {
        ImprovedQueryRunner.globalNextSqlItemHandler = globalNextSqlItemHandler;
    }

	private void normalGetterSetters_____________________() {// NOSONAR
	}

	public Dialect getDialect() {
		return dialect;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setDialect(Dialect dialect) {// NOSONAR
		this.dialect = dialect;
	}

	public Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setAllowShowSQL(Boolean allowShowSQL) {// NOSONAR
		this.allowShowSQL = allowShowSQL;
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
		for (DbPro dbPro : slaves)
			if (dbPro != null)
				dbPro.setConnectionManager(null); // Slave should not open transaction
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
	
	public JavaToJdbcConverter getJavaToJdbcConverter() {
		return javaToJdbcConverter;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setJavaToJdbcConverter(JavaToJdbcConverter javaToJdbcConverter) {
		this.javaToJdbcConverter = javaToJdbcConverter;
	}

	public JdbcToJavaConverter getJdbcToJavaConverter() {
		return jdbcToJavaConverter;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setJdbcToJavaConverter(JdbcToJavaConverter jdbcToJavaConverter) {
		this.jdbcToJavaConverter = jdbcToJavaConverter;
	}

	/** This method is not thread safe, suggest only use at program starting */
    public TenantGetter getTenantGetter() {
        return tenantGetter;
    }

    /** This method is not thread safe, suggest only use at program starting */
    public void setTenantGetter(TenantGetter tenantGetter) {
        this.tenantGetter = tenantGetter;
    }

    /** This method is not thread safe, suggest only use at program starting */
    public SqlItemHandler getSqlItemHandler() {
		return sqlItemHandler;
	}

    /** This method is not thread safe, suggest only use at program starting */
	public void setSqlItemHandler(SqlItemHandler sqlItemHandler) {
		this.sqlItemHandler = sqlItemHandler;
	}

	public boolean isBatchEnabled() {
		return batchEnabled.get();
	}

	public ThreadLocal<ArrayList<PreparedSQL>> getSqlBatchCache() {
		return sqlBatchCache;
	}

	public Integer getDbCode() {
		return dbCode;
	}

	public void setDbCode(Integer dbCode) {
		this.dbCode = dbCode;
	}

}
