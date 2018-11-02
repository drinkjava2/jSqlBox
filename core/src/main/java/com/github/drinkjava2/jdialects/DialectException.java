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
package com.github.drinkjava2.jdialects;

/**
 * DialectException for jDialects
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class DialectException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DialectException() {
		// Default constructor
	}

	public DialectException(String message) {
		super(message);
	}

	public DialectException(Throwable cause) {
		super(cause);
	}

	public DialectException(String message, Throwable cause) {
		super(message, cause);
	}

	public static Object throwEX(String errorMsg, Throwable e) {
		throw new DialectException(errorMsg, e);
	}

	public static Object throwEX(Throwable cause) {
		throw new DialectException(cause);
	}

	public static Object throwEX(String errorMsg) {
		throw new DialectException(errorMsg);
	}

	public static void eatException(Exception e) {
		// do nothing here
	}

	public static void assureNotNull(Object obj, String... optionMessages) {
		if (obj == null)
			throw new DialectException(
					optionMessages.length == 0 ? "Assert error, Object parameter can not be null" : optionMessages[0]);
	}

	public static void assureNotEmpty(String str, String... optionMessages) {
		if (str == null || str.length() == 0)
			throw new DialectException(
					optionMessages.length == 0 ? "Assert error, String parameter can not be empty" : optionMessages[0]);
	}
}
