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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * DataSourceManager determine how to get or release connection from DataSource,
 * it can be different transaction strategies like JDBC/SpringManaged/JTA..
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class GroupTxConnectionManager implements ConnectionManager {
	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
	DataSource[] dataSources;

	public GroupTxConnectionManager(DataSource... dataSources) {
		this.dataSources = dataSources;
	}

	public GroupTxConnectionManager(Integer transactionIsolation, DataSource... dataSources) {
		this.transactionIsolation = transactionIsolation;
		this.dataSources = dataSources;
	}

	private ThreadLocal<Map<DataSource, Connection>> threadLocalConnections = new ThreadLocal<Map<DataSource, Connection>>() {
		@Override
		protected Map<DataSource, Connection> initialValue() {
			return new HashMap<DataSource, Connection>();
		}
	};

	public boolean isInGroupTransaction() {
		return threadLocalConnections.get() != null;
	}

	public void startGroupTransaction() {
		threadLocalConnections.set(new HashMap<DataSource, Connection>());
	}

	public void endGroupTransaction() {
		threadLocalConnections.remove();
	}

	public void commitGroupTx() {
		try {
			Map<DataSource, Connection> map = threadLocalConnections.get();
			if (map == null)
				return;
			boolean errorFound = false;
			SQLException lastExp = null;
			for (Entry<DataSource, Connection> entry : map.entrySet()) {
				Connection conn = entry.getValue();
				try {
					if (errorFound)
						conn.rollback();
				} catch (SQLException e) {
					errorFound = true;
					if (lastExp != null)
						e.setNextException(lastExp);
					lastExp = e;
				}
			}
			for (Entry<DataSource, Connection> entry : map.entrySet())
				try {
					entry.getValue().setAutoCommit(true);
				} catch (SQLException e) {
					if (lastExp != null)
						e.setNextException(lastExp);
					lastExp = e;
				}
			if (lastExp != null)
				throw new TransactionsException(lastExp);
		} catch (Exception e) {
			endGroupTransaction();
		}
	}

	public void rollbackGroupTx() {
		try {
			Map<DataSource, Connection> map = threadLocalConnections.get();
			if (map == null)
				return;
			SQLException lastExp = null;
			for (Entry<DataSource, Connection> entry : map.entrySet()) {
				Connection conn = entry.getValue();
				try {
					conn.rollback();
				} catch (SQLException e) {
					if (lastExp != null)
						e.setNextException(lastExp);
					lastExp = e;
				}
			}
			for (Entry<DataSource, Connection> entry : map.entrySet())
				try {
					entry.getValue().setAutoCommit(true);
				} catch (SQLException e) {
					if (lastExp != null)
						e.setNextException(lastExp);
					lastExp = e;
				}
			if (lastExp != null)
				throw new TransactionsException(lastExp);
		} catch (Exception e) {
			endGroupTransaction();
		}
	}

	@Override
	public boolean isInTransaction(DataSource ds) {
		return isInGroupTransaction();
	}

	@Override
	public Connection getConnection(DataSource ds) throws SQLException {
		TransactionsException.assureNotNull(ds, "DataSource can not be null");
		Connection conn = null;
		if (isInGroupTransaction()) {
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
		if (isInGroupTransaction()) {
			// Do nothing, because this connection is used in a current thread's transaction
		} else {
			if (conn != null)
				conn.close();
		}
	}

}
