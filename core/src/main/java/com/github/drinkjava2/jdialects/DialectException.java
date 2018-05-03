/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
