
package com.github.drinkjava2.jsqlbox;

public class SQLBoxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SQLBoxException() {
		// Default public constructor
	}

	public SQLBoxException(String message) {
		super(message);
	}
}
