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
package com.github.drinkjava2.jtransactions.grouptx;

import java.sql.Connection;
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * A transaction MethodInterceptor
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class GroupTx implements MethodInterceptor {
	private static final TinyTxConnectionManager cm = TinyTxConnectionManager.instance();

	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;

	public GroupTx() {
	}

	public GroupTx(Integer transactionIsolation) {
		this.transactionIsolation = transactionIsolation;
	}

	public void beginTransaction() {
		cm.startTransaction(transactionIsolation);
	}

	public void commit() throws SQLException {
		cm.commit();
	}

	public void rollback() {
		cm.rollback();
	}

	@Override
	public Object invoke(MethodInvocation caller) throws Throwable {// NOSONAR
		if (cm.isInTransaction()) {
			return caller.proceed();
		} else {
			Object invokeResult = null;
			try {
				cm.startTransaction(transactionIsolation);
				invokeResult = caller.proceed();
				cm.commit();
			} catch (Throwable t) {
				cm.rollback();
				throw new TransactionsException("GroupTx found a runtime Exception, transaction rollbacked.", t);
			}
			return invokeResult;
		}
	}

}