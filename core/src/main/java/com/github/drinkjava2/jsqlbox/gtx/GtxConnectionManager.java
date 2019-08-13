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
package com.github.drinkjava2.jsqlbox.gtx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jdialects.id.UUIDAnyGenerator;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.ThreadConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;

/**
 * GTX means Global Transaction, this is a distribute transaction
 * ConnectionManager
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class GtxConnectionManager extends ThreadConnectionManager {
	protected static final DbProLog logger = DbProLogFactory.getLog(GtxConnectionManager.class);

	private SqlBoxContext gtxCtx;

	public SqlBoxContext getLockCtx() {
		return gtxCtx;
	}

	public void setLockCtx(SqlBoxContext lockCtx) {
		this.gtxCtx = lockCtx;
	}

	public GtxConnectionManager(SqlBoxContext lockCtx) {
		this.gtxCtx = lockCtx;
	}

	@Override
	public void startTransaction() {// default is TRANSACTION_READ_COMMITTED
		GtxInfo gtxInfo = new GtxInfo();
		gtxInfo.setGtxId(new GtxId("G" + UUID25Generator.getUUID25() + UUIDAnyGenerator.getAnyLengthRadix36UUID(6)));
		setThreadTxInfo(gtxInfo);
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		GtxInfo gtxInfo = new GtxInfo();
		gtxInfo.setGtxId(new GtxId("G" + UUID25Generator.getUUID25() + UUIDAnyGenerator.getAnyLengthRadix36UUID(6)));
		gtxInfo.setTxIsolationLevel(txIsolationLevel);
		setThreadTxInfo(gtxInfo);
	}

	@Override
	public Connection getConnection(Object dsHolder) throws SQLException {
		SqlBoxContext ctx = (SqlBoxContext) dsHolder;
		DataSource ds = ctx.getDataSource();
		TransactionsException.assureNotNull(dsHolder, "DataSource can not be null");
		if (isInTransaction()) {
			TxInfo tx = getThreadTxInfo();
			Connection conn = tx.getConnectionCache().get(ctx);
			if (conn == null) {
				conn = ds.getConnection(); // NOSONAR
				conn.setAutoCommit(false);
				conn.setTransactionIsolation(tx.getTxIsolationLevel());
				tx.getConnectionCache().put(ctx, conn);
			}
			return conn;
		} else
			return ds.getConnection(); // AutoCommit mode
	}

	@Override
	public void commitTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not commit");
		GtxInfo gtxInfo = (GtxInfo) getThreadTxInfo();
		for (Object ctx : gtxInfo.getConnectionCache().keySet())
			((SqlBoxContext) ctx).eInsert(gtxInfo.getGtxId());// use a Tag to confirm tx committed on DB
		GtxUtils.saveGtxInfo(gtxCtx, gtxInfo); // store lock infos on gtx server
		SQLException lastExp = null;
		try {
			for (Connection conn : gtxInfo.getConnectionCache().values())
				conn.commit();
		} catch (SQLException e) {
			lastExp = e;
		}
		if (lastExp != null)
			throw new TransactionsException(lastExp); // if any mistake, throw e
		else {
			GtxUtils.deleteGtxInfo(gtxCtx, gtxInfo); // delete locks
			for (Object ctx : gtxInfo.getConnectionCache().keySet())
				((SqlBoxContext) ctx).eDelete(gtxInfo.getGtxId());// delete tags
			endTransaction(null); // if no any mistake, close transaction
		}
	}

	@Override
	public void rollbackTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Gtx transaction already closed, can not rollback");
		SQLException lastExp = null;
		GtxInfo gtxInfo = (GtxInfo) getThreadTxInfo();
		setThreadTxInfo(null); // Immediately close GTX transaction
		
		endTransaction(lastExp);
	}

	private void endTransaction(SQLException ex) {// NOSONAR
		if (!isInTransaction())
			return;
		Collection<Connection> conns = getThreadTxInfo().getConnectionCache().values();
		setThreadTxInfo(null);
		SQLException lastExp = ex;
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
