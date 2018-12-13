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
package com.github.drinkjava2.jtransactions.manual;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * JFinalConnectionManager is the implementation of ConnectionManager, get
 * connection and release connection from jFinal environment
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class ManualTx implements ConnectionManager {
	private Connection connection;
	private DataSource dataSource;
	private Integer transactionIsolationLevel;

	public ManualTx(DataSource ds, Integer transactionIsolationLevel) {
		dataSource = ds;
		this.transactionIsolationLevel = transactionIsolationLevel;
	}

	/** Borrow a connection from manualTx to use */
	@Override
	public Connection getConnection(DataSource dataSource) throws SQLException {
		if (connection == null)
			throw new TransactionsException("Fail to find connection in ManualTx");
		return connection;
	}

	/** Return the connection to manualTx */
	@Override
	public void releaseConnection(Connection conn, DataSource dataSource) throws SQLException {
		// do nothing
	}

	/** Check if manualTx already started a transaction */
	@Override
	public boolean isInTransaction(DataSource dataSource) {
		try {
			return connection != null && !connection.getAutoCommit();
		} catch (SQLException e) {
			throw new TransactionsException(e);
		}
	}

	/**
	 * Start a transaction, if connection is null, get a new connection, set
	 * AutoCommit =false
	 */
	public Connection beginTransaction() {
		try {
			if (connection == null) {
				connection = dataSource.getConnection();
				if (transactionIsolationLevel != null)
					connection.setTransactionIsolation(transactionIsolationLevel);
			}
		} catch (SQLException e) {
			throw new TransactionsException("Fail to begin a transaction.", e);
		}
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new TransactionsException("Fail to setAutoCommit to false", e);
		}
		return connection;
	}

	/** Commit the transaction, */
	public void commit() throws SQLException {
		if (connection != null && !connection.getAutoCommit())
			connection.commit();
		setAutoCommitTrue();
	}

	/** roll back the transaction, close connection */
	public void rollback() {
		try {
			if (connection != null && !connection.getAutoCommit())
				connection.rollback();
		} catch (SQLException e) {
			throw new TransactionsException(e);
		}
		setAutoCommitTrue();
	}

	/**
	 * set autoCommit to true, restore normal status. so this connection can be
	 * re-used by other thread
	 */
	private void setAutoCommitTrue() {
		try {
			if (connection != null && !connection.getAutoCommit())
				connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new TransactionsException("Fail to setAutoCommit to true", e);
		}
	}

}
