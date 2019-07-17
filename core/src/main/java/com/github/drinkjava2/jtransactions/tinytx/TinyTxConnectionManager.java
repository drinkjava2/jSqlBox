/*
* Copyright 2016 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
* applicable law or agreed to in writing, software distributed under the
* License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
* OF ANY KIND, either express or implied. See the License for the specific
* language governing permissions and limitations under the License.
*/
package com.github.drinkjava2.jtransactions.tinytx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;

/**
 * TinyTxConnectionManager is the implementation of ConnectionManager, get
 * connection and release connection from dataSource
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxConnectionManager implements ConnectionManager {

	private static class InnerTinyTxCM {// NOSONAR
		private static final TinyTxConnectionManager INSTANCE = new TinyTxConnectionManager();
	}

	/**
	 * @return A singleton instance of TinyTxConnectionManager
	 */
	public static final TinyTxConnectionManager instance() {
		return InnerTinyTxCM.INSTANCE;
	}

	private static final ThreadLocal<TxInfo> threadedTxInfo = new ThreadLocal<TxInfo>();

	@Override
	public boolean isInTransaction() {
		return threadedTxInfo.get() != null;
	}

	@Override
	public void startTransaction() {
		threadedTxInfo.set(new TxInfo());
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		threadedTxInfo.set(new TxInfo(txIsolationLevel));
	}

	@Override
	public Connection getConnection(DataSource ds) throws SQLException {
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		if (isInTransaction()) {
			TxInfo tx = threadedTxInfo.get();
			if (tx.getConnectionCache().size() > 1)
				throw new TransactionsException(
						"TinyTxConnectionManager can only support one connection in one thread");
			Connection conn = tx.getConnectionCache().get(ds);
			if (conn == null && !tx.getConnectionCache().isEmpty())
				throw new TransactionsException("Error: TinyTx do not support get multiple connections in one thread");
			if (conn == null) {
				conn = ds.getConnection(); // NOSONAR
				conn.setAutoCommit(false);
				conn.setTransactionIsolation(tx.getTxIsolationLevel());
				threadedTxInfo.get().getConnectionCache().put(ds, conn);
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
		try {
			Collection<Connection> conns = threadedTxInfo.get().getConnectionCache().values();
			if (conns.size() == 0)
				return; // no actual transaction open
			if (conns.size() > 1)
				throw new TransactionsException("TinyTx can only support one dataSource in one thread, can not commit");
			Connection con = conns.iterator().next();
			if (con.getAutoCommit())
				throw new TransactionsException("Connection is auto commit status, can not commit");
			con.commit();
		} catch (SQLException e) {
			throw new TransactionsException(e);
		} finally {
			endTransaction();
		}
	}

	@Override
	public void rollback() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not rollback");
		try {
			Collection<Connection> conns = threadedTxInfo.get().getConnectionCache().values();
			if (conns.size() == 0)
				return; // no actual transaction open
			Connection con = conns.iterator().next();
			if (con.getAutoCommit())
				throw new TransactionsException("Connection is auto commit status, can not rollback");
			con.rollback();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new TransactionsException(e);
		} finally {
			endTransaction();
		}
	}

	private void endTransaction() {
		if (!isInTransaction())
			return;
		Connection con = null;
		try {
			Collection<Connection> conns = threadedTxInfo.get().getConnectionCache().values();
			if (conns.size() == 0)
				return; // no actual transaction open
			if (conns.size() > 1)
				throw new TransactionsException(
						"TinyTx can only support one dataSource in one thread, can not end transaction");
			con = conns.iterator().next();
			if (!con.getAutoCommit())
				con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new TransactionsException("Fail to setAutoCommit to true", e);
		} finally {
			threadedTxInfo.get().getConnectionCache().clear();
			threadedTxInfo.set(null);
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					throw new TransactionsException("Fail to close connection", e);// NOSONAR
				}
		}

	}

}
