
package com.github.drinkjava2.jsqlbox;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Just use JDK Logger
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class LogUtils {

	private static final Logger log = Logger.getLogger(LogUtils.class.toString());
	private static final Formatter vanillaFormatter = new VanillaFormatter();
	private static final Formatter[] oldFormatters = new Formatter[5];

	static {
		Handler[] handlers = log.getParent().getHandlers();
		for (int i = 0; i < handlers.length; i++)
			oldFormatters[i] = handlers[i].getFormatter();
	}

	public static class VanillaFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			return record.getMessage();
		}
	}

	private LogUtils() {
	}

	/**
	 * Use java.util.logging.Logger, level set to Level.info <br/>
	 */
	public static void info(String msg) {
		log.info(msg);
	}

	/**
	 * Use java.util.logging.Logger <br/>
	 */
	public static void log(Level level, String msg, Throwable thrown) {
		log.log(level, msg, thrown);
	}

	/**
	 * Print message to console and log without formatter
	 */
	public static void print(String msg) {
		Handler[] handlers = log.getParent().getHandlers();
		for (int i = 0; i < handlers.length; i++)
			handlers[i].setFormatter(vanillaFormatter);
		log.info(msg);
		for (int i = 0; i < handlers.length; i++)
			handlers[i].setFormatter(oldFormatters[i]);
	}

	/**
	 * Println message to console and log without formatter
	 */
	public static void println(String msg) {
		print(msg + "\r\n");
	}

	/**
	 * Println to console and log without formatter
	 */
	public static void println() {
		print("\r\n");
	}

}
