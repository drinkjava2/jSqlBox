
package com.github.drinkjava2.jsqlbox;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SqlBoxException() {
		// Default public constructor
	}

	public SqlBoxException(String message) {
		super(message);
	}
}
