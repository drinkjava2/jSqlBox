package com.github.drinkjava2.common;

/**
 * Debug Util
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class Systemout {
	private static final boolean allowPrint = false;

	public static void print(Object obj) {
		if (allowPrint)
			Systemout.println(obj);
	}

	public static void println(Object obj) {
		if (allowPrint)
			Systemout.println(obj);
	}

	public static void println() {
		if (allowPrint)
			Systemout.println();
	}
}
