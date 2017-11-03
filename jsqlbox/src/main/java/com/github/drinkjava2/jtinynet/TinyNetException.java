/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jtinynet;

/**
 * This TinyNetException used to wrap all exceptions to runtime Exception
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyNetException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TinyNetException() {
		super();
	}

	public TinyNetException(Throwable cause) {
		super(cause);
	}

	public TinyNetException(String msg) {
		super(msg);
	}

	public TinyNetException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/** Eat exception to avoid SONAR warning */
	public static void eatException(Exception e) {
		// do nothing here
	}

	/** Assure object not null, optionMessages is optional */
	public static void assureNotNull(Object obj, String... optionMessages) {
		if (obj == null)
			throw new TinyNetException(
					optionMessages.length == 0 ? "Assert error, Parameter can not be null" : optionMessages[0]);
	}

	/** Assure String not Empty, optionMessages is optional */
	public static void assureNotEmpty(String str, String... optionMessages) {
		if (str == null || str.length() == 0)
			throw new TinyNetException(
					optionMessages.length == 0 ? "Assert error, String parameter can not be empty" : optionMessages[0]);
	}

}
