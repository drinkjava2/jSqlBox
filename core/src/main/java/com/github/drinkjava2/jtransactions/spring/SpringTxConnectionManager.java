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
package com.github.drinkjava2.jtransactions.spring;

import java.lang.reflect.Method;
import java.sql.Connection;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * SpringConnectionManager is the implementation of ConnectionManager, get
 * connection and release connection from Spring environment
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SpringTxConnectionManager implements ConnectionManager {
	protected final Method getConnectionMethod;
	protected final Method releaseConnectionMethod;
	protected final Method getResourceMethod;

	public SpringTxConnectionManager() {
		Class<?> dataSourceUtilClass;
		try {
			dataSourceUtilClass = Class.forName("org.springframework.jdbc.datasource.DataSourceUtils");
		} catch (ClassNotFoundException e) {
			throw new TransactionsException("Error:spring-jdbc jar missing, fail to build SpringTxConnectionManager.",
					e);
		}
		Class<?> transactionSynchronizationManagerClass;
		try {
			transactionSynchronizationManagerClass = Class
					.forName("org.springframework.transaction.support.TransactionSynchronizationManager");
		} catch (ClassNotFoundException e) {
			throw new TransactionsException("Error:spring-tx jar missing, fail to build SpringTxConnectionManager.", e);
		}

		try {
			getConnectionMethod = dataSourceUtilClass.getMethod("getConnection", DataSource.class);
			releaseConnectionMethod = dataSourceUtilClass.getMethod("releaseConnection", Connection.class,
					DataSource.class);
			getResourceMethod = transactionSynchronizationManagerClass.getMethod("getResource", Object.class);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager initialize failed.", e);
		}
	}

	private static class SpringTxConnectionManagerSingleton {// NOSONAR
		private static final SpringTxConnectionManager INSTANCE = new SpringTxConnectionManager();
	}

	/** @return A singleton */
	public static final SpringTxConnectionManager instance() {
		return SpringTxConnectionManagerSingleton.INSTANCE;
	}

	/*
	 * Equal to Spring's DataSourceUtils.getConnection()
	 */
	@Override
	public Connection getConnection(DataSource dataSource) {
		try {
			return (Connection) getConnectionMethod.invoke(null, dataSource);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager fail to get connection from dataSource.",
					e);
		}
	}

	/*
	 * Equal to Spring's DataSourceUtils.releaseConnection()
	 */
	@Override
	public void releaseConnection(Connection conn, DataSource dataSource) {
		try {
			releaseConnectionMethod.invoke(null, conn, dataSource);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager fail to release connection.", e);
		}
	}

	@Override
	public boolean isInTransaction(DataSource ds) {
		if (ds == null)
			return false;
		try {
			return null != getResourceMethod.invoke(null, ds);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager fail to get transaction status.", e);
		}
	}

}
