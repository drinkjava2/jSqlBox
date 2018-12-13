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

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * A transaction MethodInterceptor
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTx implements MethodInterceptor {
	private static final TinyTxConnectionManager cm = TinyTxConnectionManager.instance();

	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
	private DataSource ds;

	public TinyTx(DataSource ds) {
		this.ds = ds;
	}

	public TinyTx(DataSource ds, Integer transactionIsolation) {
		this.ds = ds;
		this.transactionIsolation = transactionIsolation;
	}

	public Connection beginTransaction() {
		return cm.startTransaction(ds, transactionIsolation);
	}

	public void commit() throws SQLException {
		cm.commit(ds);
	}

	public void rollback() {
		cm.rollback(ds);
	}

	@Override
	public Object invoke(MethodInvocation caller) throws Throwable {// NOSONAR
		if (cm.isInTransaction(ds)) {
			return caller.proceed();
		} else {
			Object invokeResult = null;
			try {
				cm.startTransaction(ds, transactionIsolation);
				invokeResult = caller.proceed();
				cm.commit(ds);
			} catch (Throwable t) {
				cm.rollback(ds);
				throw new TransactionsException("TinyTx found a runtime Exception, transaction rollbacked.", t);
			}
			return invokeResult;
		}
	}

}