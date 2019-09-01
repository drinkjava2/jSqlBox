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
package com.github.drinkjava2.jtransactions.jfinal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * JFinalTxManager is the implementation of ConnectionManager, get connection
 * and release connection from jFinal environment
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class JFinalTxConnectionManager implements ConnectionManager {
	protected final Method getConfigMethod;
	protected final Method getConnectionMethod;
	protected final Method releaseConnectionMethod;
	protected final Method isInTransactionMethod;

	public JFinalTxConnectionManager() {
		Class<?> dbKitClass;
		Class<?> configClass;
		try {
			dbKitClass = Class.forName("com.jfinal.plugin.activerecord.DbKit");
			configClass = Class.forName("com.jfinal.plugin.activerecord.Config");
		} catch (ClassNotFoundException e) {
			throw new TransactionsException("Error: jfinal jar missing, fail to build JFinalTxManager.", e);
		}

		try {
			getConfigMethod = dbKitClass.getMethod("getConfig");
			getConnectionMethod = configClass.getMethod("getConnection");
			releaseConnectionMethod = configClass.getMethod("close", Connection.class);
			isInTransactionMethod = configClass.getMethod("isInTransaction");

		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxManager initialize failed.", e);
		}
		TransactionsException.assureNotNull(getConfigMethod, "Fail to get getConfigMethod method in JFinalTxManager.");
		TransactionsException.assureNotNull(releaseConnectionMethod,
				"Fail to get releaseConnectionMethod method in JFinalTxManager.");
		TransactionsException.assureNotNull(getConnectionMethod,
				"Fail to get getConnectionMethod method in JFinalTxManager.");
		TransactionsException.assureNotNull(isInTransactionMethod,
				"Fail to get isInTransactionMethod method in JFinalTxManager.");
	}

	private static class JFinalTxConnectionManagerSingleton {// NOSONAR
		private static final JFinalTxConnectionManager INSTANCE = new JFinalTxConnectionManager();
	}

	/** @return A singleton */
	public static final JFinalTxConnectionManager instance() {
		return JFinalTxConnectionManagerSingleton.INSTANCE;
	}

	private Object getConfig() throws IllegalAccessException, InvocationTargetException {
		Object config = getConfigMethod.invoke(null);
		TransactionsException.assureNotNull(config, "Error: get a null jFinal config in JFinalTxManager.");
		return config;
	}

	@Override
	public boolean isInTransaction() {
		try {
			Object config = getConfig();
			return null != isInTransactionMethod.invoke(config);
		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxManager fail to get transaction status.", e);
		}
	}

	@Override
	public void startTransaction() {
		throw new TransactionsException(
				"startTransaction method not implemented by current version, please use JFinal's method directly or submit a pull request");
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		throw new TransactionsException(
				"startTransaction method not implemented by current version, please use JFinal's method directly or submit a pull request");
	}

	@Override
	public Connection getConnection(Object dsOrHolder) {
		try {
			Object config = getConfig();
			return (Connection) getConnectionMethod.invoke(config);
		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxManager fail to get connection.", e);
		}
	}

	@Override
	public void releaseConnection(Connection conn, Object dsOrHolder) {
		try {
			Object config = getConfig();
			releaseConnectionMethod.invoke(config, conn);
		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxManager fail to release connection.", e);
		}
	}

	@Override
	public TxResult commitTransaction() throws Exception {
		throw new TransactionsException(
				"commit method not implemented by current version, please use JFinal's method directly");
	}

	@Override
	public TxResult rollbackTransaction() {
		throw new TransactionsException(
				"rollback method not implemented by current version, please use JFinal's method directly");
	}

}
