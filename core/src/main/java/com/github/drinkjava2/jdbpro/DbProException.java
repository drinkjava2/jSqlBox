/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro;

/**
 * This DbProException only used to wrap a SQLException to a Runtime type
 * Exception to avoid each time to catch annoying SQLException. Usually used
 * together with an IOC/AOP tool like Spring, it will catch runtime Exception
 * and roll back transaction.
 *
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DbProException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DbProException() {
		super();
	}

	public DbProException(Throwable cause) {
		super(cause);
	}

	public DbProException(String msg) {
		super(msg);
	}

	public DbProException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public static void assertNotNull(Object object, String msg) {
		if (object == null)
			throw new DbProException(msg);
	}
}
