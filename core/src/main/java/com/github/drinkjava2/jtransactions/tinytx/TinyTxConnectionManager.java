/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jtransactions.tinytx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * DataSourceManager determine how to get or release connection from DataSource,
 * it can be different transaction strategies like JDBC/SpringManaged/JTA..
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxConnectionManager implements ConnectionManager {

	private static class InnerTinyTxConnectionManager {// NOSONAR
		private static final TinyTxConnectionManager INSTANCE = new TinyTxConnectionManager();
	}

	/**
	 * @return A singleton instance of TinyTxConnectionManager
	 */
	public static final TinyTxConnectionManager instance() {
		return InnerTinyTxConnectionManager.INSTANCE;
	}

	private static final ThreadLocal<Map<DataSource, Connection>> threadLocalConnections = new ThreadLocal<Map<DataSource, Connection>>() {
		@Override
		protected Map<DataSource, Connection> initialValue() {
			return new HashMap<DataSource, Connection>();
		}
	};

	@Override
	public boolean isInTransaction(DataSource ds) {
		TransactionsException.assureNotNull(ds, "DataSource can not be null in isInTransaction method");
		return null != threadLocalConnections.get().get(ds);
	}

	public Connection startTransaction(DataSource ds, int transactionIsolation) {
		TransactionsException.assureNotNull(ds, "DataSource can not be null in startTransaction method");
		if (null != threadLocalConnections.get().get(ds))
			throw new TransactionsException("Can not start transaction in an existing transaction.");
		Connection conn = null;
		try {
			conn = getConnection(ds);
			TransactionsException.assureNotNull(conn, "Can not obtain a connection from DataSource");
			conn.setTransactionIsolation(transactionIsolation);
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e2) {
					throw new TransactionsException("Fail to close connection" + e2 + ", root cause:" + e);
				}
			}
			throw new TransactionsException(e);
		}

		threadLocalConnections.get().put(ds, conn);
		return conn;
	}

	public void commit(DataSource ds) throws SQLException {
		Connection conn = null;
		try {
			conn = threadLocalConnections.get().get(ds);
			TransactionsException.assureNotNull(conn, "Connection can not get from DataSource");
			conn.commit();
			setAutoCommitTrue(conn);
		} finally {
			if (conn != null)
				endTransaction(conn, ds);
		}
	}

	public void rollback(DataSource ds) {
		Connection conn = threadLocalConnections.get().get(ds);
		try {
			if (conn != null) {
				conn.rollback();
				setAutoCommitTrue(conn);
				endTransaction(conn, ds);
			}
		} catch (SQLException e) {
			throw new TransactionsException(e);
		}
	}

	private void endTransaction(Connection conn, DataSource ds) throws SQLException {
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		threadLocalConnections.get().remove(ds);
		releaseConnection(conn, ds);
	}

	@Override
	public Connection getConnection(DataSource ds) throws SQLException {
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		// Try get a connection already in current transaction
		Connection conn = threadLocalConnections.get().get(ds);
		if (conn == null)
			conn = ds.getConnection(); // Have to get a new connection
		TransactionsException.assureNotNull(conn, "Fail to get a connection from DataSource");
		return conn;
	}

	@Override
	public void releaseConnection(Connection conn, DataSource ds) throws SQLException {
		Connection saved = threadLocalConnections.get().get(ds);
		if (saved != null && saved == conn) {
			// Do nothing, because this connection is used in a current thread's transaction
		} else {
			if (conn != null)
				conn.close();
		}
	}

	/**
	 * set autoCommit to true, restore normal status. so this connection can be
	 * re-used by other thread
	 */
	private void setAutoCommitTrue(Connection conn) {
		try {
			if (conn != null && !conn.getAutoCommit())
				conn.setAutoCommit(true);
		} catch (SQLException e) {
			throw new TransactionsException("Fail to setAutoCommit to true", e);
		}
	}

}
