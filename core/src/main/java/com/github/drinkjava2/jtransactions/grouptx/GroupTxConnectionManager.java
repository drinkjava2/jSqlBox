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

import com.github.drinkjava2.jtransactions.DataSourceHolder;
import com.github.drinkjava2.jtransactions.ThreadConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * GroupTxConnectionManager determine how to get or release connection from a
 * group of DataSource, but transaction only available for 1 dataSource, usually
 * used for sharding databases.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class GroupTxConnectionManager extends ThreadConnectionManager {

	private static class InnerGroupTxMgr {// NOSONAR
		private static final GroupTxConnectionManager INSTANCE = new GroupTxConnectionManager();
	}

	/**
	 * @return A singleton instance of GroupTxConnectionManager
	 */
	public static final GroupTxConnectionManager instance() {
		return InnerGroupTxMgr.INSTANCE;
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
			Connection conn = tx.getConnectionCache().get(ds);
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
		Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
		for (Connection con : conns)
			con.commit();
		endTransaction(null);
		return TxResult.txSucess();
	}

	@Override
	public TxResult rollbackTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not rollback");
		SQLException lastExp = null;
		Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
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
		return TxResult.txFail();
	}

	private void endTransaction(SQLException lastExp) {// NOSONAR
		if (!isInTransaction())
			return;
		Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
		setThreadTxInfo(null);
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
		if (lastExp != null)
			throw new TransactionsException(lastExp);
	}

}
