
package com.github.drinkjava2.jsqlbox;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * For Debug use only
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxLogger {
	private Class<?> targetClass;
	private Object log;

	public static class VanillaFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			return record.getMessage();
		}
	}

	public SqlBoxLogger(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

}
