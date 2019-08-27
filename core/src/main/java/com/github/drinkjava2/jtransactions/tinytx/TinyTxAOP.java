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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * The TinyTx AOP MethodInterceptor
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyTxAOP implements MethodInterceptor {
	private TinyTxConnectionManager cm = TinyTxConnectionManager.instance();

	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;

	public TinyTxAOP() {
	}

	public TinyTxAOP(Integer transactionIsolation) {
		this.transactionIsolation = transactionIsolation;
	}

	public TinyTxAOP(TinyTxConnectionManager cm, Integer transactionIsolation) {
		this.cm = cm;
		this.transactionIsolation = transactionIsolation;
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
				cm.commitTransaction();
			} catch (Throwable t) {
				cm.rollbackTransaction();
				throw new TransactionsException("TinyTx found a runtime Exception, transaction rollbacked.", t);
			}
			return invokeResult;
		}
	}

}