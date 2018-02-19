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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
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
 * SqlHandler <br/>
 * 4) Add a dialect property to support dialect features like pagination, DDL...
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "all" })
public class ImprovedQueryRunner extends QueryRunner {
	protected static DbProLogger globalLogger = DbProLogger.getLog(ImprovedQueryRunner.class);

	protected static Integer globalBatchSize = 100;

	/**
	 * The ConnectionManager determine how to get and release connections from
	 * DataSource or ThreadLocal. only allow initialised by constructor
	 */
	protected ConnectionManager connectionManager;

	/**
	 * If set true will output SQL and parameters in logger, only allow initialised
	 * by constructor
	 */
	protected Boolean allowShowSQL = false;

	/**
	 * Logger of current ImprovedQueryRunner, to keep thread-safe, only allow
	 * initialised by constructor
	 */
	protected DbProLogger logger = globalLogger;

	/**
	 * Default Batch Size, current fixed to 100, only allow initialised by
	 * constructor
	 */
	protected Integer batchSize = globalBatchSize;

	/** SqlInterceptors, only allow initialised by constructor */
	protected List<SqlHandler> sqlInterceptors = null;

	/**
	 * A ThreadLocal type cache to store SqlHandler instances, all instance will be
	 * cleaned after this thread close
	 */
	private static ThreadLocal<ArrayList<SqlHandler>> threadedSqlInterceptors = new ThreadLocal<ArrayList<SqlHandler>>() {
		@Override
		protected ArrayList<SqlHandler> initialValue() {
			return new ArrayList<SqlHandler>();
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
	private ThreadLocal<ArrayList<Object[]>> sqlBatchCache = new ThreadLocal<ArrayList<Object[]>>() {
		@Override
		protected ArrayList<Object[]> initialValue() {
			return new ArrayList<Object[]>();
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
		return "Parameters: " + Arrays.deepToString(params);
	}

	/**
	 * Add a explainer
	 */
	public static ArrayList<SqlHandler> getThreadedSqlInterceptors() {
		return threadedSqlInterceptors.get();
	}

	/**
	 * Explain SQL to add extra features like pagination...
	 */
	public String explainSql(ResultSetHandler<?> rsh, String sql, Object... params) {
		String newSQL = sql;
		if (sqlInterceptors != null)
			for (SqlHandler explainer : sqlInterceptors)
				newSQL = explainer.handleSql(this, newSQL, params);
		for (SqlHandler explainer : getThreadedSqlInterceptors())
			newSQL = explainer.handleSql(this, newSQL, params);
		if (rsh != null && rsh instanceof SqlHandler)
			newSQL = ((SqlHandler) rsh).handleSql(this, sql, params);
		return newSQL;
	}

	public Object explainResult(ResultSetHandler<?> rsh, Object result) {
		Object newObj = result;
		if (sqlInterceptors != null)
			for (SqlHandler explainer : sqlInterceptors)
				newObj = explainer.handleResult(newObj);
		for (SqlHandler explainer : getThreadedSqlInterceptors())
			newObj = explainer.handleResult(newObj);
		if (rsh instanceof SqlHandler)
			result = ((SqlHandler) rsh).handleResult(result);
		return newObj;
	}

	// === Batch execute methods======
	/**
	 * Force flush cached SQLs
	 */
	public void batchFlush() throws SQLException {
		List<Object[]> sqlCacheList = sqlBatchCache.get();
		if (sqlCacheList.isEmpty())
			return;
		Object[] f = sqlCacheList.get(0);// first row
		if (f.length != 6)
			throw new DbProRuntimeException("Unexpected batch cached SQL format");
		int paramLenth = 0;
		if ("i1".equals(f[0]) || "i3".equals(f[0]) || "u1".equals(f[0]) || "u4".equals(f[0]))
			paramLenth = 0;
		if ("u2".equals(f[0]) || "u5".equals(f[0]))
			paramLenth = 1;
		else
			paramLenth = ((Object[]) sqlCacheList.get(0)[5]).length;
		Object[][] allParams = new Object[sqlCacheList.size()][paramLenth];
		int i = 0;
		for (Object[] c : sqlCacheList) {// cached parameters
			Object param = c[2];
			Object[] params = (Object[]) c[5];
			if ("i1".equals(f[0]) || "i3".equals(f[0]) || "u1".equals(f[0]) || "u4".equals(f[0]))
				allParams[i] = new Object[0];
			if ("u2".equals(f[0]) || "u5".equals(f[0]))
				allParams[i] = new Object[] { param };
			else
				allParams[i] = params;
			i++;
		}
		String sql = (String) f[3];
		Connection conn = (Connection) f[4];
		ResultSetHandler rsh = (ResultSetHandler) f[1];
		if (this.getAllowShowSQL()) {
			logger.info("Batch execute " + sqlCacheList.size() + " SQLs");
			logger.info(formatSqlForLoggerOutput(sql));
			logger.info("First row " + formatParametersForLoggerOutput(allParams[0]));
			logger.info("Last row " + formatParametersForLoggerOutput(allParams[allParams.length - 1]));
		}
		if ("e1".equals(f[0]) || "i1".equals(f[0]) || "u1".equals(f[0]) || "u2".equals(f[0]) || "u3".equals(f[0]))
			super.batch(conn, sql, allParams);
		else if ("e3".equals(f[0]) || "i3".equals(f[0]) || "u4".equals(f[0]) || "u5".equals(f[0]) || "u6".equals(f[0]))
			super.batch(sql, allParams);
		else if ("e2".equals(f[0]) || "i2".equals(f[0]))
			super.insertBatch(conn, sql, rsh, allParams);
		else if ("e4".equals(f[0]) || "i4".equals(f[0]))
			super.insertBatch(sql, rsh, allParams);
		else
			throw new DbProRuntimeException("unknow batch sql operation type +'" + f[0] + "'");
		sqlBatchCache.get().clear();
	}

	/** Start batch sql */
	public void batchBegin() throws SQLException {
		if (!sqlBatchCache.get().isEmpty())
			batchFlush();
		this.batchEnabled.set(true);
	}

	/** Stop batch sql */
	public void batchEnd() throws SQLException {
		if (!sqlBatchCache.get().isEmpty())
			batchFlush();
		this.batchEnabled.set(false);
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
	 * Add SQL to cache, if full (reach batchSize) then call batchFlush() <br/>
	 * 
	 * @throws SQLException
	 * 
	 */
	private <T> T addToCacheIfFullFlush(String execteType, ResultSetHandler<T> rsh, Object param, String sql,
			Connection conn, Object... params) throws SQLException {
		Object[] forCache = new Object[] { execteType, rsh, param, sql, conn, params };
		List<Object[]> cached = sqlBatchCache.get();
		if (cached.size() >= this.batchSize)
			this.batchFlush();
		else if (!cached.isEmpty()) {
			Object[] last = cached.get(cached.size() - 1);
			if (!last[0].equals(forCache[0]) || !last[3].equals(forCache[3]) || (last[1] != forCache[1])
					|| (last[4] != forCache[4]))
				this.batchFlush();
		}
		sqlBatchCache.get().add(forCache);
		return null;
	}

	// ===override execute/insert/update methods to support batch and explainSql
	// BTW, some methods in QueryRunner are private, otherwise no need override
	// so many methods

	@Override
	public int execute(Connection conn, String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("e1", null, null, explainedSql, conn, params);
				return 0;
			} else {
				int result = super.execute(conn, explainedSql, params);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> List<T> execute(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params)
			throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, params);

			if (batchEnabled.get()) {
				return (List<T>) addToCacheIfFullFlush("e2", rsh, null, explainedSql, conn, params);
			} else {
				List<T> result = super.execute(conn, explainedSql, rsh, params);
				result = (List<T>) explainResult(rsh, result);
				if (rsh instanceof SqlHandler)
					result = ((SqlHandler) rsh).handleResult(result);
				return result;
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int execute(String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("e3", null, null, explainedSql, null, params);
				return 0;
			} else {
				int result = super.execute(explainedSql, params);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, params);
			if (batchEnabled.get())
				return (List<T>) addToCacheIfFullFlush("e4", rsh, null, explainedSql, null, params);
			List<T> result = super.execute(explainedSql, rsh, params);
			return (List<T>) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, null);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i1", rsh, null, explainedSql, conn, null);
			T result = super.insert(conn, explainedSql, rsh);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, params);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i2", rsh, null, explainedSql, conn, params);
			T result = super.insert(conn, explainedSql, rsh, params);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, null);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i3", rsh, null, explainedSql, null, null);
			T result = super.insert(explainedSql, rsh);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, params);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i4", rsh, null, explainedSql, null, params);
			T result = super.insert(explainedSql, rsh, params);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int update(Connection conn, String sql) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, null);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u1", null, null, explainedSql, conn, null);
				return 0;
			} else {
				int result = super.update(conn, explainedSql);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int update(Connection conn, String sql, Object param) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, param);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u2", null, param, explainedSql, conn, null);
				return 0;
			} else {
				int result = super.update(conn, explainedSql, param);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int update(Connection conn, String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u3", null, null, explainedSql, conn, params);
				return 0;
			} else {
				int result = super.update(conn, explainedSql, params);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int update(String sql) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, null);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u4", null, null, explainedSql, null, null);
				return 0;
			} else {
				int result = super.update(explainedSql);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int update(String sql, Object param) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, param);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u5", null, param, explainedSql, null, null);
				return 0;
			} else {
				int result = super.update(explainedSql, param);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public int update(String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(null, sql, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u6", null, null, explainedSql, null, params);
				return 0;
			} else {
				int result = super.update(explainedSql, params);
				return (Integer) explainResult(null, result);
			}
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	/**
	 * Convert paramList to 2d array for insertBatch use, insertBatch's last
	 * parameter is a 2d array, not easy to use
	 */
	private static Object[][] toArray(List<List<?>> paramList) {
		Object[][] array = new Object[paramList.size()][];
		int i = 0;
		for (List<?> item : paramList)
			array[i++] = item.toArray(new Object[item.size()]);
		return array;
	}

	/**
	 * Executes the given batch of INSERT SQL statements, connection is get from
	 * current dataSource
	 * 
	 * @param sql
	 *            The SQL statement to execute.
	 * @param rsh
	 *            The handler used to create the result object
	 * @param paramList
	 *            the parameters for all SQLs, list.get(0) is the first SQL's
	 *            parameters
	 * @return The result generated by the handler.
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, List<List<?>> paramList) throws SQLException {
		return insertBatch(sql, rsh, toArray(paramList));
	}

	/**
	 * Executes the given batch of INSERT SQL statements
	 * 
	 * @param conn
	 *            The connection
	 * @param sql
	 *            The SQL statement to execute.
	 * @param rsh
	 *            The handler used to create the result object
	 * @param paramList
	 *            the parameters for all SQLs, list.get(0) is the first SQL's
	 *            parameters
	 * @return The result generated by the handler.
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public <T> T insertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, List<List<?>> paramList)
			throws SQLException {
		return this.insertBatch(conn, sql, rsh, toArray(paramList));
	}

	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, params);
			T result = super.query(conn, explainedSql, rsh, params);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, null);
			T result = super.query(conn, explainedSql, rsh);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, params);
			T result = super.query(explainedSql, rsh, params);
			result = (T) explainResult(rsh, result);
			return result;
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(rsh, sql, null);
			T result = super.query(explainedSql, rsh);
			return (T) explainResult(rsh, result);
		} finally {
			getThreadedSqlInterceptors().clear();
		}
	}

	// ==========getter & setter==========
	public Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	public DbProLogger getLogger() {
		return logger;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public boolean isBatchEnabled() {
		return batchEnabled.get();
	}

	// =============global getter & setter=======
	public static DbProLogger getGlobalLogger() {
		return globalLogger;
	}

	public static void setGlobalLogger(DbProLogger globalLogger) {
		ImprovedQueryRunner.globalLogger = globalLogger;
	}

	public static Integer getGlobalBatchSize() {
		return globalBatchSize;
	}

	public static void setGlobalBatchSize(Integer globalBatchSize) {
		ImprovedQueryRunner.globalBatchSize = globalBatchSize;
	}

}
