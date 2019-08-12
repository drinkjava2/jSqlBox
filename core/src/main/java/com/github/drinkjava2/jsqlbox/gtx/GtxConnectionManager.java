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
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.github.drinkjava2.jdialects.StrUtils;
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
	public void startTransaction() {
		setThreadTxInfo(new GtxInfo());
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		GtxInfo txInfo = new GtxInfo();
		txInfo.setTxIsolationLevel(txIsolationLevel);
		setThreadTxInfo(txInfo);
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
	public void commit() {
		if (!isInTransaction())
			throw new TransactionsException("Transaction not opened, can not commit");
		GtxInfo gtxInfo = (GtxInfo) getThreadTxInfo();
		if (StrUtils.isEmpty(gtxInfo.getGtxId()))
			gtxInfo.setGtxId(
					"G" + UUID25Generator.get25LettersRadix36UUID() + UUIDAnyGenerator.getAnyLengthRadix36UUID(6));
		GtxUtils.saveGtxInfo(gtxCtx, gtxInfo); // store lock infos on gtx server

		SQLException lastExp = null;
		try {
			for (Entry<Object, Connection> entry : gtxInfo.getConnectionCache().entrySet()) {
				SqlBoxContext ctx = (SqlBoxContext) entry.getKey();
				ctx.eInsert(new GtxId(gtxInfo.getGtxId()));// use a Tag to confirm tx committed on DB
				Connection conn = entry.getValue();
				conn.commit();
			}
		} catch (SQLException e) {
			lastExp = e;
		}

		if (lastExp != null)
			throw new TransactionsException(lastExp); // if any mistake, throw e
		else
			endTransaction(null); // if no any mistake, close transaction
	}

	@Override
	public void rollback() {
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
	}

}
