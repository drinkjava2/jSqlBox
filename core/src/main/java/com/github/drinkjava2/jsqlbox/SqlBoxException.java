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
package com.github.drinkjava2.jsqlbox;

/**
 * This SqlBoxException used to wrap exception to a Runtime type Exception
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBoxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SqlBoxException() {
		super();
	}

	public SqlBoxException(Throwable cause) {
		super(cause);
	}

	public SqlBoxException(String msg) {
		super(msg);
	}

	public SqlBoxException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/** Eat exception to avoid SONAR warning */
	public static void eatException(Exception e) {
		// do nothing here
	}

	/** Assure object not null, optionMessages is optional */
	public static void assureNotNull(Object obj, String... optionMessages) {
		if (obj == null)
			throw new SqlBoxException(
					optionMessages.length == 0 ? "Assert error, Parameter can not be null" : optionMessages[0]);
	}

	/** Assure String not Empty, optionMessages is optional */
	public static void assureNotEmpty(String str, String... optionMessages) {
		if (str == null || str.length() == 0)
			throw new SqlBoxException(
					optionMessages.length == 0 ? "Assert error, String parameter can not be empty" : optionMessages[0]);
	}
	
	/** Assure true, optionMessages is optional */
	public static void assureTrue(Boolean isTrue, String... optionMessages) {
		if (!isTrue)
			throw new SqlBoxException(
					optionMessages.length == 0 ? "Assert error, Result should be true" : optionMessages[0]);
	}

}
