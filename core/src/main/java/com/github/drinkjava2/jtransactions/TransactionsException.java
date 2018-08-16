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

/**
 * This TransactionsException used to wrap exception to a Runtime type Exception
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TransactionsException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TransactionsException() {
		super();
	}

	public TransactionsException(Throwable cause) {
		super(cause);
	}

	public TransactionsException(String msg) {
		super(msg);
	}

	public TransactionsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/** Assure object not null, optionMessages is optional */
	public static void assureNotNull(Object obj, String... optionMessages) {
		if (obj == null)
			throw new TransactionsException(
					optionMessages.length == 0 ? "Assert error, Parameter can not be null" : optionMessages[0]);
	}

}
