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
package com.github.drinkjava2.jtransactions.manual;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.DataSourceHolder;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * ManualTxConnectionManager is a simple implementation of ConnectionManager,
 * get connection and release connection from dataSource directly and store in
 * txInfo
 * 
 * NOTE: This ManualTxConnectionManager class is not thread safe, to use it,
 * each DAO in each thread need create a new ManualTxConnectionManager instance
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class ManualTxConnectionManager implements ConnectionManager {
	private TxInfo txInfo;

	/** Check if manualTx already started a transaction */
	public boolean isInTransaction() {
		return txInfo != null;
	}

	/** Start a transaction, i.e., set a TxTag */
	public void startTransaction() {
		if (isInTransaction())
			throw new TransactionsException("Transaction already started");
		txInfo = new TxInfo();
	}

	/** Start a transaction, i.e., set a TxTag */
	public void startTransaction(int txIsolationLevel) {
		if (isInTransaction())
			throw new TransactionsException("Transaction already started");
		txInfo = new TxInfo(txIsolationLevel);
	}

	/** Borrow a connection from manualTx to use */
	@Override
	public Connection getConnection(Object dsOrHolder) throws SQLException {
		DataSource ds;
		if (dsOrHolder instanceof DataSource)
			ds = (DataSource) dsOrHolder;
		else
			ds = ((DataSourceHolder) dsOrHolder).getDataSource();
		if (isInTransaction()) {
			if (txInfo.getConnection() == null) {
				Connection con = ds.getConnection();
				con.setAutoCommit(false);
				con.setTransactionIsolation(txInfo.getTxIsolationLevel());
				txInfo.setConnection(con);
			}
			return txInfo.getConnection();

		} else
			return ds.getConnection(); // Should autoCommit is true
	}

	/** Return the connection to manualTx */
	@Override
	public void releaseConnection(Connection conn, Object dsOwner) throws SQLException {
		if (isInTransaction()) {
			// do nothing
		} else if (conn != null)
			conn.close();
	}

	/** Commit the transaction */
	public TxResult commitTransaction() throws Exception {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not commit");
		try {
			Connection con = txInfo.getConnection();
			if (con == null)
				return TxResult.txSucess(); // no actual transaction open
			if (!con.getAutoCommit())
				con.commit();
			else
				throw new TransactionsException("Connection is auto commit status, can not commit");
		} finally {
			endTransaction();
		}
		return TxResult.txSucess();
	}

	/** roll back the transaction, close connection */
	public TxResult rollbackTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not rollback");
		try {
			Connection con = txInfo.getConnection();
			if (con == null)
				return TxResult.txFail(); // no actual transaction open
			if (!con.getAutoCommit())
				con.rollback();
			else
				throw new TransactionsException("Connection is auto commit status, can not rollback");
		} catch (SQLException e) {
			throw new TransactionsException(e);
		} finally {
			endTransaction();
		}
		return TxResult.txFail();
	}

	/**
	 * set autoCommit to true, restore normal status. so this connection can be
	 * re-used by other thread
	 */
	private void endTransaction() {
		if (!isInTransaction())
			return;
		Connection con = txInfo.getConnection();
		try {
			if (con == null)
				return; // no actual transaction open
			if (!con.getAutoCommit())
				con.setAutoCommit(true);

		} catch (SQLException e) {
			throw new TransactionsException("Fail to setAutoCommit to true", e);
		} finally {
			txInfo.setConnection(null);
			txInfo = null;
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					throw new TransactionsException("Fail to close connection", e);// NOSONAR
				}
		}
	}

}
