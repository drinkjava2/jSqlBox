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
package com.github.drinkjava2.jtransactions.grouptx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;

/**
 * GroupTxConnectionManager determine how to get or release connection from a
 * group of DataSource, but transaction only available for 1 dataSource, usually
 * used for sharding databases.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class GroupTxConnectionManager implements ConnectionManager {

	private static class InnerGroupTxMgr {// NOSONAR
		private static final GroupTxConnectionManager INSTANCE = new GroupTxConnectionManager();
	}

	/**
	 * @return A singleton instance of GroupTxConnectionManager
	 */
	public static final GroupTxConnectionManager instance() {
		return InnerGroupTxMgr.INSTANCE;
	}

	private static final ThreadLocal<TxInfo> threadedGroupTxInfo = new ThreadLocal<TxInfo>();

	@Override
	public boolean isInTransaction() {
		return threadedGroupTxInfo.get() != null;
	}

	@Override
	public void startTransaction() {
		threadedGroupTxInfo.set(new TxInfo());
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		threadedGroupTxInfo.set(new TxInfo(txIsolationLevel));
	}

	@Override
	public Connection getConnection(DataSource ds) throws SQLException {
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		if (isInTransaction()) {
			TxInfo tx = threadedGroupTxInfo.get();
			Connection conn = tx.getConnectionCache().get(ds);
			if (conn == null) {
				conn = ds.getConnection(); // NOSONAR
				conn.setAutoCommit(false);
				conn.setTransactionIsolation(tx.getTxIsolationLevel());
				threadedGroupTxInfo.get().getConnectionCache().put(ds, conn);
			}
			return conn;
		} else
			return ds.getConnection(); // AutoCommit mode
	}

	@Override
	public void releaseConnection(Connection conn, DataSource ds) throws SQLException {
		if (isInTransaction()) {
			// do nothing
		} else if (conn != null)
			conn.close();
	}

	@Override
	public void commit() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not commit");
		SQLException lastExp = null;
		Collection<Connection> conns = threadedGroupTxInfo.get().getConnectionCache().values();
		for (Connection con : conns) {
			try {
				con.commit();
			} catch (SQLException e) {
				if (lastExp != null)
					e.setNextException(lastExp);
				lastExp = e;
			}
		}
		if (lastExp != null)
			throw new TransactionsException(lastExp);
		else
			endTransaction(null);
	}

	@Override
	public void rollback() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not rollback");
		SQLException lastExp = null;
		Collection<Connection> conns = threadedGroupTxInfo.get().getConnectionCache().values();
		for (Connection con : conns) {
			try {
				con.rollback();
			} catch (SQLException e) {
				if (lastExp != null)
					e.setNextException(lastExp);
				lastExp = e;
			}
		}
		endTransaction(lastExp);
	}

	private void endTransaction(SQLException lastExp) {// NOSONAR
		if (!isInTransaction())
			return;
		Collection<Connection> conns = threadedGroupTxInfo.get().getConnectionCache().values();
		threadedGroupTxInfo.set(null);
		if (conns.isEmpty())
			return; // no actual transaction open
		for (Connection con : conns) {
			if (con == null)
				continue;
			try {
				if (!con.getAutoCommit())
					con.setAutoCommit(true);
			} catch (SQLException e) {
				if (lastExp != null)
					e.setNextException(lastExp);
				lastExp = e;
			}
			try {
				con.close();
			} catch (SQLException e) {
				if (lastExp != null)
					e.setNextException(lastExp);
				lastExp = e;
			}
		}
		conns.clear();
		// if (lastExp != null)
		// throw new TransactionsException(lastExp);
	}

}
