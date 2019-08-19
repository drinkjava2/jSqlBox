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

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.ThreadConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxInfo;
import com.github.drinkjava2.jtransactions.TxResult;

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
		setThreadTxInfo(new GtxInfo());// start soft GTX
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		GtxInfo gtxInfo = new GtxInfo();
		gtxInfo.setTxIsolationLevel(txIsolationLevel);
		setThreadTxInfo(gtxInfo);// start soft GTX
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
	public TxResult commitTransaction() throws Exception {
		GtxInfo gtxInfo = (GtxInfo) getThreadTxInfo();
		if (gtxInfo == null)
			throw new TransactionsException("GTX not started, can not commit");

		// Save GtxId tags into DBs
		for (Object ctx : gtxInfo.getConnectionCache().keySet())
			((SqlBoxContext) ctx).eInsert(gtxInfo.getGtxId());// use a Tag to confirm tx committed on DB

		// Save lock and log
		try {
			GtxUtils.saveLockAndLog(gtxCtx, gtxInfo); // store gtxId,undo log, locks into gtx server
		} catch (Exception e) {
			gtxInfo.setGtxStep(GtxInfo.LOCK_FAIL);
			gtxInfo.getTxResult().addCommitEx(e);
			throw e;
		}

		// Here commit all DBs
		int commitIndex = 0;
		try {
			for (Entry<Object, Connection> entry : gtxInfo.getConnectionCache().entrySet()) {
				SqlBoxContext ctx = (SqlBoxContext) entry.getKey();
				System.out.println("debug, name="+ctx.getName());
				int forceCommitFail = ctx.getForceCommitFail();
				if (forceCommitFail > 0 || forceCommitFail < 0) {
					if (forceCommitFail > 0)
						ctx.setForceCommitFail(forceCommitFail - 1);
					throw new IllegalArgumentException("ForceCommitFail=" + forceCommitFail + " in ctx '"
							+ ctx.getName() + "', a non 0 value will force a commit fail usually used for unit test.");
				}
				Connection conn = entry.getValue();
				conn.commit();
				commitIndex++;
			}
		} catch (Exception e) {
			if (commitIndex < gtxInfo.getConnectionCache().size() - 1) {
				gtxInfo.setGtxStep(GtxInfo.PARTIAL_COMMIT_FAIL);// partial commit is 100% fail
				gtxInfo.setPartialCommitQty(commitIndex);
			} else
				gtxInfo.setGtxStep(GtxInfo.LAST_COMMIT_FAIL);// last commit fail may not fail
			gtxInfo.getTxResult().addCommitEx(e);
			throw e;
		}

		// Now GTX is success committed, left jobs are unlock and cleanup

		// Delete lock and log
		try {
			GtxUtils.deleteLockAndLog(gtxCtx, gtxInfo);
		} catch (Exception e) {
			gtxInfo.setGtxStep(GtxInfo.UNLOCK_FAIL);
			gtxInfo.getTxResult().addCommitEx(e);
			throw e;
		}

		// Delete gtxId tags
		for (Object key : gtxInfo.getConnectionCache().keySet()) {
			SqlBoxContext ctx = (SqlBoxContext) key;
			try {
				ctx.eDelete(gtxInfo.getGtxId());// delete tags, work on autoCommit mode
			} catch (Exception e) {
				gtxInfo.setGtxStep(GtxInfo.CLEANUP_FAIL);
				gtxInfo.getTxResult().addCommitEx(e);
			}
		}
		setThreadTxInfo(null);// close soft GTX
		cleanupConnections(gtxInfo);
		return gtxInfo.getTxResult().setResult(TxResult.SUCESS);
	}

	@Override
	public TxResult rollbackTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Gtx transaction not started, can not rollback");
		GtxInfo gtxInfo = (GtxInfo) getThreadTxInfo();
		setThreadTxInfo(null);// close soft GTX
		String step = gtxInfo.getGtxStep();

		if (GtxInfo.START.equals(step) || GtxInfo.LOCK_FAIL.equals(step) || GtxInfo.PARTIAL_COMMIT_FAIL.equals(step)) {
			gtxInfo.getTxResult().setResult(TxResult.FAIL);
			rollbackConnections(gtxInfo);
		} else if (GtxInfo.LAST_COMMIT_FAIL.equals(step)) {
			gtxInfo.getTxResult().setResult(TxResult.UNKNOW);
		} else if (GtxInfo.UNLOCK_FAIL.equals(step)) {
			gtxInfo.getTxResult().setResult(TxResult.UNKNOW);
		} else
			throw new TransactionsException("I'm a teapot");// unreachable
		return gtxInfo.getTxResult();
	}

	private void rollbackConnections(GtxInfo gtxInfo) {// NOSONAR
		Collection<Connection> conns = gtxInfo.getConnectionCache().values();
		for (Connection con : conns) {
			if (con == null)
				continue;
			try {
				con.rollback();
			} catch (SQLException e) {
				gtxInfo.getTxResult().addRollbackEx(e);
			}
			try {
				if (!con.getAutoCommit())
					con.setAutoCommit(true);
			} catch (SQLException e) {
				gtxInfo.getTxResult().addRollbackEx(e);
			}
			try {
				if (!con.isClosed())
					con.close();
			} catch (SQLException e) {
				gtxInfo.getTxResult().addRollbackEx(e);
			}
		}
		conns.clear(); // free memory
	}

	private void cleanupConnections(GtxInfo gtxInfo) {// NOSONAR
		Collection<Connection> conns = gtxInfo.getConnectionCache().values();
		for (Connection con : conns) {
			if (con == null)
				continue;
			try {
				if (!con.getAutoCommit())
					con.setAutoCommit(true);
			} catch (SQLException e) {
				gtxInfo.getTxResult().addCleanupEx(e);
			}
			try {
				if (!con.isClosed())
					con.close();
			} catch (SQLException e) {
				gtxInfo.getTxResult().addCleanupEx(e);
			}
		}
		conns.clear(); // free memory
	}

}
