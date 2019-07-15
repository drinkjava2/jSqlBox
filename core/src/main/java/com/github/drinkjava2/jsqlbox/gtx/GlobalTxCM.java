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
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jdialects.id.UUID32Generator;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * GlobalTxCM determine how to get or release connection from
 * dataSources, this is a distribute transaction tool
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class GlobalTxCM implements ConnectionManager {

	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
	private SqlBoxContext gtxServer;

	public GlobalTxCM(SqlBoxContext gtxServer) {
		this.gtxServer = gtxServer;
	}

	public GlobalTxCM(SqlBoxContext gtxServer, Integer transactionIsolation) {
		this.gtxServer = gtxServer;
		this.transactionIsolation = transactionIsolation;
	}

	private ThreadLocal<GtxId> gtxId = new ThreadLocal<GtxId>();

	private ThreadLocal<Map<DataSource, Connection>> threadLocalConnections = new ThreadLocal<Map<DataSource, Connection>>() {
		@Override
		protected Map<DataSource, Connection> initialValue() {
			return new HashMap<DataSource, Connection>();
		}
	};

	public int insertGtxLockId(String gtxLock) {
		return gtxServer.nUpdate("insert into gtxlock (id) values(?)", gtxLock);
	}

	public int deleteGtxLockId(String gtxLock) {
		return gtxServer.nUpdate("delete from gtxlock where id=?", gtxLock);
	}

	public GtxId getGtx() {
		return gtxId.get();
	}

	@Override
	public boolean isInTransaction() {
		return gtxId.get() != null;
	}

	public void startTransaction() {
		GtxId gtx = new GtxId();
		gtx.setGtxId((String) UUID32Generator.INSTANCE.getNextID(null, null, null));
		gtxId.set(gtx);
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		// TODO Auto-generated method stub

	}

	@Override
	public Connection getConnection(DataSource ds) throws SQLException {
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		Connection conn = null;
		if (isInTransaction()) {// TODO
			conn = threadLocalConnections.get().get(ds);
			if (conn == null) {
				conn = ds.getConnection(); // NOSONAR Have to get a new connection
				TransactionsException.assureNotNull(conn, "Can not obtain a connection from DataSource");
				conn.setTransactionIsolation(transactionIsolation);
				conn.setAutoCommit(false); // start real transaction
				threadLocalConnections.get().put(ds, conn);
			}
		} else {
			conn = ds.getConnection(); // Have to get a new connection
		}
		TransactionsException.assureNotNull(conn, "Fail to get a connection from DataSource");
		return conn;
	}

	@Override
	public void releaseConnection(Connection conn, DataSource ds) throws SQLException {
		if (isInTransaction()) {
			// Do nothing, because this connection is used in a current thread's transaction
		} else {
			if (conn != null)
				conn.close();
		}
	}

	public void commit() {// TODO

	}

	public void rollback() {// TODO

	}

}
