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
import com.github.drinkjava2.jtransactions.DataSourceHolder;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * SpringTxConnectionManager is the implementation of ConnectionManager, get
 * connection and release connection from Spring environment
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SpringTxConnectionManager implements ConnectionManager {
	protected final Method getConnectionMethod;
	protected final Method releaseConnectionMethod;
	protected final Method isActualTransactionActive;

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
			isActualTransactionActive = transactionSynchronizationManagerClass.getMethod("isActualTransactionActive");
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

	@Override
	public void startTransaction() {
		throw new TransactionsException(
				"startTransaction method not implemented by current version, please use Spring's method directly or submit a pull request");
	}

	@Override
	public void startTransaction(int txIsolationLevel) {
		throw new TransactionsException(
				"startTransaction method not implemented by current version, please use Spring's method directly or submit a pull request");
	}

	/*
	 * Equal to Spring's DataSourceUtils.getConnection()
	 */
	@Override
	public Connection getConnection(Object dsOrHolder) {
		DataSource ds;
		if (dsOrHolder instanceof DataSource)
			ds = (DataSource) dsOrHolder;
		else
			ds = ((DataSourceHolder) dsOrHolder).getDataSource();
		try {
			return (Connection) getConnectionMethod.invoke(null, ds);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager fail to get connection from dataSource.",
					e);
		}
	}

	/*
	 * Equal to Spring's DataSourceUtils.releaseConnection()
	 */
	@Override
	public void releaseConnection(Connection conn, Object dsOrHolder) {
		DataSource ds;
		if (dsOrHolder instanceof DataSource)
			ds = (DataSource) dsOrHolder;
		else
			ds = ((DataSourceHolder) dsOrHolder).getDataSource();
		try {
			releaseConnectionMethod.invoke(null, conn, ds);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager fail to release connection.", e);
		}
	}

	@Override
	public boolean isInTransaction() {
		try {
			return null != isActualTransactionActive.invoke(null);
		} catch (Exception e) {
			throw new TransactionsException("Error: SpringTxConnectionManager fail to get transaction status.", e);
		}
	}

	@Override
	public TxResult commitTransaction() throws Exception {
		throw new TransactionsException(
				"commit method not implemented by current version, please use Spring's method directly");
	}

	@Override
	public TxResult rollbackTransaction() {
		throw new TransactionsException(
				"rollback method not implemented by current version, please use Spring's method directly");
	}

}
