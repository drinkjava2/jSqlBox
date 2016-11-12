
package com.github.drinkjava2.jsqlbox;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * For Debug use only
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class Debugger {

	private static final Logger jdkLogger = Logger.getLogger(Debugger.class.toString());

	private static final Formatter vanillaFormatter = new VanillaFormatter();

	public static class VanillaFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			return record.getMessage();
		}
	}

	static {
		ConsoleHandler c = new ConsoleHandler();
		c.setFormatter(vanillaFormatter);

		java.util.logging.FileHandler f = null;
		try {
			f = new FileHandler("%h/DebuggerLog%g.log", 5000, 10, false);
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "Failed initialize jdk logger");
		}
		f.setFormatter(vanillaFormatter);

		jdkLogger.setUseParentHandlers(false);
		jdkLogger.addHandler(c);
		jdkLogger.addHandler(f);
	}

	private Debugger() {
	}

	/**
	 * Print message to console and log
	 */
	public static void print(String msg) {
		jdkLogger.info(msg);
	}

	/**
	 * Println message to console and log
	 */
	public static void println(String msg) {
		print(msg + "\r\n");
	}

	/**
	 * Println to console and log
	 */
	public static void println() {
		print("\r\n");
	}

	public static void main(String[] args) {
		println("Test JDK Log2");
	}
}
