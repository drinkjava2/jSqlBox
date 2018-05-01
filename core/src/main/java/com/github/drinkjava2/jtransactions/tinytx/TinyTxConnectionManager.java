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
package com.github.drinkjava2.jtransactions.tinytx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * DataSourceManager determine how to get or release connection from DataSource,
 * it can be different transaction strategies like JDBC/SpringManaged/JTA..
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxConnectionManager implements ConnectionManager {

	private static class InnerTinyTxConnectionManager {
		private static final TinyTxConnectionManager INSTANCE = new TinyTxConnectionManager();
	}

	/**
	 * @return A singleton instance of TinyTxConnectionManager
	 */
	public static final TinyTxConnectionManager instance() {
		return InnerTinyTxConnectionManager.INSTANCE;
	}

	public static final ThreadLocal<Map<DataSource, Connection>> threadLocalConnections = new ThreadLocal<Map<DataSource, Connection>>() {
		@Override
		protected Map<DataSource, Connection> initialValue() {
			return new HashMap<DataSource, Connection>();
		}
	};

	private static final ThreadLocal<Map<DataSource, Boolean>> threadLocalTransactionStatus = new ThreadLocal<Map<DataSource, Boolean>>() {
		@Override
		protected Map<DataSource, Boolean> initialValue() {
			return new HashMap<DataSource, Boolean>();
		}
	};

	public boolean isInTransaction(DataSource ds) {
		TinyTxRuntimeException.assertNotNull(ds, "DataSource can not be null in isInTransaction method");
		Boolean intx = threadLocalTransactionStatus.get().get(ds);
		return intx == null ? false : intx;
	}

	public void startTransaction(DataSource ds, Connection conn) {
		TinyTxRuntimeException.assertNotNull(ds, "DataSource can not be null in startTransaction method");
		threadLocalTransactionStatus.get().put(ds, true);
		threadLocalConnections.get().put(ds, conn);
	}

	public void endTransaction(DataSource ds) {
		TinyTxRuntimeException.assertNotNull(ds, "DataSource can not be null in endTransaction method");
		threadLocalTransactionStatus.get().put(ds, false);
		threadLocalConnections.get().remove(ds);
	}

	@Override
	public Connection getConnection(DataSource ds) throws SQLException {
		TinyTxRuntimeException.assertNotNull(ds, "DataSource can not be null");
		Connection conn = null;
		if (!isInTransaction(ds))
			conn = ds.getConnection();
		else
			conn = threadLocalConnections.get().get(ds);
		TinyTxRuntimeException.assertNotNull(conn, "Fail to get a connection from DataSource");
		return conn;
	}

	@Override
	public void releaseConnection(Connection conn, DataSource ds) throws SQLException {
		if (isInTransaction(ds)) {
			// do nothing, connection will keep in Threadlocal
		} else {
			if (conn != null)
				conn.close();
		}
	}
}
