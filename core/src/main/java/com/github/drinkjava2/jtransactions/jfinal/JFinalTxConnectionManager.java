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

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * JFinalConnectionManager is the implementation of ConnectionManager, get
 * connection and release connection from jFinal environment
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
			throw new TransactionsException("Error: jfinal jar missing, fail to build JFinalTxConnectionManager.", e);
		}

		try {
			getConfigMethod = dbKitClass.getMethod("getConfig");
			getConnectionMethod = configClass.getMethod("getConnection");
			releaseConnectionMethod = configClass.getMethod("close", Connection.class);
			isInTransactionMethod = configClass.getMethod("isInTransaction");

		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxConnectionManager initialize failed.", e);
		}
		TransactionsException.assureNotNull(getConfigMethod,
				"Fail to get getConfigMethod method in JFinalTxConnectionManager.");
		TransactionsException.assureNotNull(releaseConnectionMethod,
				"Fail to get releaseConnectionMethod method in JFinalTxConnectionManager.");
		TransactionsException.assureNotNull(getConnectionMethod,
				"Fail to get getConnectionMethod method in JFinalTxConnectionManager.");
		TransactionsException.assureNotNull(isInTransactionMethod,
				"Fail to get isInTransactionMethod method in JFinalTxConnectionManager.");
	}

	private static class JFinalTxConnectionManagerSingleton {// NOSONAR
		private static final JFinalTxConnectionManager INSTANCE = new JFinalTxConnectionManager();
	}

	/** @return A singleton */
	public static final JFinalTxConnectionManager instance() {
		return JFinalTxConnectionManagerSingleton.INSTANCE;
	}

	@Override
	public Connection getConnection(DataSource dataSource) {
		try {
			Object config = getConfig();
			return (Connection) getConnectionMethod.invoke(config);
		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxConnectionManager fail to get connection.", e);
		}
	}

	@Override
	public void releaseConnection(Connection conn, DataSource dataSource) {
		try {
			Object config = getConfig();
			releaseConnectionMethod.invoke(config, conn);
		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxConnectionManager fail to release connection.", e);
		}
	}

	@Override
	public boolean isInTransaction(DataSource ds) {
		if (ds == null)
			return false;
		try {
			Object config = getConfig();
			return null != isInTransactionMethod.invoke(config);
		} catch (Exception e) {
			throw new TransactionsException("Error: JFinalTxConnectionManager fail to get transaction status.", e);
		}
	}

	private Object getConfig() throws IllegalAccessException, InvocationTargetException {
		Object config = getConfigMethod.invoke(null);
		TransactionsException.assureNotNull(config, "Error: get a null jFinal config in JFinalTxConnectionManager.");
		return config;
	}

}
