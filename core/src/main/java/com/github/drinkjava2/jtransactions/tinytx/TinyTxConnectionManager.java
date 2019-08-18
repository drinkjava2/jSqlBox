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

import com.github.drinkjava2.jtransactions.DataSourceHolder;
import com.github.drinkjava2.jtransactions.ThreadConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * TinyTxConnectionManager is the implementation of ConnectionManager, get
 * connection and release connection from dataSource
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxConnectionManager extends ThreadConnectionManager {

	private static class InnerTinyTxCM {// NOSONAR
		private static final TinyTxConnectionManager INSTANCE = new TinyTxConnectionManager();
	}

	/**
	 * @return A singleton instance of TinyTxConnectionManager
	 */
	public static final TinyTxConnectionManager instance() {
		return InnerTinyTxCM.INSTANCE;
	}

	@Override
	public Connection getConnection(Object dsOrHolder) throws SQLException {
		DataSource ds;
		if (dsOrHolder instanceof DataSource)
			ds = (DataSource) dsOrHolder;
		else
			ds = ((DataSourceHolder) dsOrHolder).getDataSource();
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		if (isInTransaction()) {
			TxInfo tx = getThreadTxInfo();
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
				tx.getConnectionCache().put(ds, conn);
			}
			return conn;
		} else
			return ds.getConnection(); // AutoCommit mode
	}

	@Override
	public TxResult commitTransaction() throws Exception {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not commit");
		try {
			Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
			if (conns.isEmpty())
				return TxResult.TX_SUCESS; // no actual transaction open
			if (conns.size() > 1)
				throw new TransactionsException("TinyTx can only support one dataSource in one thread, can not commit");
			Connection con = conns.iterator().next();
			if (con.getAutoCommit())
				throw new TransactionsException("Connection is auto commit status, can not commit");
			con.commit();
		} finally {
			endTransaction();
		}
		return TxResult.TX_SUCESS;
	}

	@Override
	public TxResult rollbackTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not rollback");
		try {
			Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
			if (conns.isEmpty())
				return TxResult.TX_FAIL; // no actual transaction open
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
		return TxResult.TX_FAIL;
	}

	private void endTransaction() {
		if (!isInTransaction())
			return;
		Connection con = null;
		try {
			Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
			if (conns.isEmpty())
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
			getThreadTxInfo().getConnectionCache().clear();
			setThreadTxInfo(null);
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					throw new TransactionsException("Fail to close connection", e);// NOSONAR
				}
		}

	}

}
