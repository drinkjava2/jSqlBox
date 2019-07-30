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
import java.sql.SQLException;

/**
 * A ConnectionManager implementation determine how to get or release connection
 * from DataSource or ThreadLocal or from Spring or JTA or some container...
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class ThreadConnectionManager implements ConnectionManager {

	private ThreadLocal<TxInfo> threadedTxInfo = new ThreadLocal<TxInfo>();

	@Override
	public boolean isInTransaction() {
		return threadedTxInfo.get() != null;
	}

	@Override
	public void releaseConnection(Connection conn, Object dsOwner) throws SQLException {
		if (isInTransaction()) {
			// do nothing
		} else if (conn != null)
			conn.close();
	}

	@Override
	public void startTransaction() {
		threadedTxInfo.set(new TxInfo());
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		threadedTxInfo.set(new TxInfo(txIsolationLevel));
	}

	public TxInfo getThreadTxInfo() {
		return threadedTxInfo.get();
	}

	public void setThreadTxInfo(TxInfo txInfo) {
		threadedTxInfo.set(txInfo);
	}

}
