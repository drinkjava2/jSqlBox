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
package com.github.drinkjava2.jtransactions;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * If a TxInfo existed in current thread's threadlocal variant, it means a
 * transaction started.
 */
public class TxInfo {

	private Integer txIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;

	// This is designed for bind connection on ds
	protected Map<Object, Connection> connectionCache;

	// This is designed for just store connection in TxInfo
	protected Connection connection = null;

	public TxInfo() {
	}

	public TxInfo(int txIsolationLevel) {
		this.txIsolationLevel = txIsolationLevel;
	}

	// ===============getter setters==================
	public Integer getTxIsolationLevel() {
		return txIsolationLevel;
	}

	public void setTxIsolationLevel(Integer txIsolationLevel) {
		this.txIsolationLevel = txIsolationLevel;
	}

	public Map<Object, Connection> getConnectionCache() {
		if (connectionCache == null)
			connectionCache = new LinkedHashMap<Object, Connection>();
		return connectionCache;
	}

	public void setConnectionCache(Map<Object, Connection> connectionCache) {
		this.connectionCache = connectionCache;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
