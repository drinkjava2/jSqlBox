
package com.github.drinkjava2.jsqlbox;

public class SqlBoxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SqlBoxException() {
		// Default public constructor
	}

	public SqlBoxException(String message) {
		super(message);
	}
}
