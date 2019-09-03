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

import static com.github.drinkjava2.jtransactions.TxResult.CLEANUP_FAIL;
import static com.github.drinkjava2.jtransactions.TxResult.COMMIT_FAIL;
import static com.github.drinkjava2.jtransactions.TxResult.LOCK_FAIL;
import static com.github.drinkjava2.jtransactions.TxResult.START;
import static com.github.drinkjava2.jtransactions.TxResult.UNLOCK_FAIL;

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

	private SqlBoxContext lockCtx;

	public SqlBoxContext getLockCtx() {
		return lockCtx;
	}

	public void setLockCtx(SqlBoxContext lockCtx) {
		this.lockCtx = lockCtx;
	}

	public GtxConnectionManager(SqlBoxContext lockCtx) {
		this.lockCtx = lockCtx;
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
		TxResult result = gtxInfo.getTxResult().setStage(START);

		// Save GtxTag into DBs, use a Tag to confirm tx committed
		for (Object ctx : gtxInfo.getConnectionCache().keySet())
			((SqlBoxContext) ctx).eInsert(new GtxTag(gtxInfo.getGtxId().getGid()));

		// Save lock and log
		try {
			GtxUtils.saveLockAndLog(lockCtx, gtxInfo); // store gtxId,undo log, locks into gtx server
		} catch (Exception e) {
			result.setStage(LOCK_FAIL);
			result.addCommitEx(e);
			throw e;
		}

		// Here commit all DBs
		int committed = 0;
		try {
			for (Entry<Object, Connection> entry : gtxInfo.getConnectionCache().entrySet()) {
				SqlBoxContext ctx = (SqlBoxContext) entry.getKey();
				int forceCommitFail = ctx.getForceCommitFail();
				if (forceCommitFail > 0 || forceCommitFail < 0) {
					if (forceCommitFail > 0)
						ctx.setForceCommitFail(forceCommitFail - 1);
					throw new IllegalArgumentException("ForceCommitFail=" + forceCommitFail + " in ctx '"
							+ ctx.getName() + "', a non 0 value will force a commit fail, usually used for unit test.");
				}
				Connection conn = entry.getValue();
				conn.commit();
				committed++;
			}
		} catch (Exception e) {
			result.setCommitted(committed);
			result.setStage(COMMIT_FAIL);// last commit fail may not fail
			result.addCommitEx(e);
			throw e;
		}

		// Now GTX is success committed, left jobs are unlock and cleanup

		// Delete lock and log
		try {
			GtxUtils.deleteLockAndLog(lockCtx, gtxInfo);
		} catch (Exception e) {
			result.setStage(UNLOCK_FAIL);
			result.addCommitEx(e);
			throw e;
		}

		// Delete gtxTags
		for (Object key : gtxInfo.getConnectionCache().keySet()) {
			SqlBoxContext ctx = (SqlBoxContext) key;
			try {
				ctx.eDelete(new GtxTag(gtxInfo.getGtxId().getGid()));// In autoCommit mode delete tags
			} catch (Exception e) {
				result.setStage(CLEANUP_FAIL);
				result.addCommitEx(e);
			}
		}
		setThreadTxInfo(null);// close soft GTX
		cleanupConnections(gtxInfo);
		return result.setResult(TxResult.SUCESS);
	}

	@Override
	public TxResult rollbackTransaction() {
		if (!isInTransaction())
			throw new TransactionsException("Gtx transaction not started, can not rollback");
		GtxInfo gtxInfo = (GtxInfo) getThreadTxInfo();
		gtxInfo.getTxResult().setResult(TxResult.FAIL);
		setThreadTxInfo(null);// close soft GTX
		rollbackConnections(gtxInfo);
		return gtxInfo.getTxResult();
	}

	private void rollbackConnections(GtxInfo gtxInfo) {// NOSONAR
		Collection<Connection> conns = gtxInfo.getConnectionCache().values();
		TxResult result = gtxInfo.getTxResult();
		int index = 0;
		for (Connection con : conns) {
			if (con == null)
				continue;
			try {
				if (index >= result.getCommitted())
					con.rollback();
			} catch (SQLException e) {
				result.addRollbackEx(e);
			}
			try {
				if (!con.getAutoCommit())
					con.setAutoCommit(true);
			} catch (SQLException e) {
				result.addRollbackEx(e);
			}
			try {
				if (!con.isClosed())
					con.close();
			} catch (SQLException e) {
				result.addRollbackEx(e);
			}
			index++;
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
