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
public interface ConnectionManager {

	/** Start a transaction with default connection isolation level */
	public void startTransaction();

	/** Start a transaction with given connection isolation level */
	public void startTransaction(int txIsolationLevel);

	/** Check if is in transaction */
	public boolean isInTransaction();

	/**
	 * A ConnectionManager implementation determine how to get connection from
	 * DataSource or DataSourceHolder or ThreadLocal or from Spring or JTA or some
	 * container...
	 */
	public Connection getConnection(Object dsOrHolder) throws SQLException;

	/**
	 * A ConnectionManager implementation determine how to close connection or
	 * return to ThreadLocal or return to Spring or JTA or some container...
	 */
	public void releaseConnection(Connection conn, Object dsOrHolder) throws SQLException;

	/** Commit the transaction, */
	public TxResult commitTransaction() throws Exception;

	/** Roll back soft transaction */
	public TxResult rollbackTransaction();
}
