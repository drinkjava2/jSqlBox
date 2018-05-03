/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For logger output, to avoid logger jar version conflict, default use JDK log,
 * if found commons log, use it, if found Log4j use it..., by this way this
 * project has no dependency to any logger jar.
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class DialectLogger {

	private Object commonLogger;
	private Method commonLoggerInfoMethod;
	private Method commonLoggerWarnMethod;
	private Method commonLoggerErrorMethod;
	private Logger jdkLogger;
	private static boolean firstRun = true;
	private static boolean enableLog = true;
	public static DialectLogger INSTANCE = null;// NOSONAR

	static {
		INSTANCE = new DialectLogger(DialectLogger.class);
		firstRun = false;
	}

	public DialectLogger(Class<?> targetClass) {
		if (targetClass == null)
			throw new AssertionError("DbProLogger error: targetClass can not be null.");
		try {
			Class<?> logFactoryClass = Class.forName("org.apache.commons.logging.LogFactory");
			Method method = logFactoryClass.getMethod("getLog", Class.class);
			commonLogger = method.invoke(logFactoryClass, targetClass);
			commonLoggerInfoMethod = commonLogger.getClass().getMethod("info", Object.class);
			commonLoggerWarnMethod = commonLogger.getClass().getMethod("warn", Object.class);
			commonLoggerErrorMethod = commonLogger.getClass().getMethod("error", Object.class);
		} catch (Exception e) {
			// do nothing
		}

		if (commonLogger == null || commonLoggerWarnMethod == null) {
			if (firstRun)
				System.err.println("DialectLogger failed to load org.apache.commons.logging.LogFactory. Use JDK logger.");// NOSONAR
			jdkLogger = Logger.getLogger(targetClass.getName());// use JDK log
		} else if (firstRun)
			System.out.println("org.apache.commons.logging.LogFactory loaded, DialectLogger use it as logger.");// NOSONAR
	}

	/**
	 * Build a DbProLogger instance by given targetClass
	 * @param targetClass
	 * @return A DbProLogger instance
	 */
	public static DialectLogger getLog(Class<?> targetClass) {
		return new DialectLogger(targetClass);
	}

	public static void setEnableLog(boolean enablelog) {
		enableLog = enablelog;
	}

	public void info(String msg) {
		if (!enableLog)
			return;
		if (jdkLogger != null) {
			jdkLogger.log(Level.INFO, msg);
			return;
		}
		try {
			commonLoggerInfoMethod.invoke(commonLogger, msg);
		} catch (Exception e) {
			throw new AssertionError(e.getMessage());
		}
	}

	public void warn(String msg) {
		if (!enableLog)
			return;
		if (jdkLogger != null) {
			jdkLogger.log(Level.WARNING, msg);
			return;
		}
		try {
			commonLoggerWarnMethod.invoke(commonLogger, msg);
		} catch (Exception e) {
			throw new AssertionError(e.getMessage());
		}
	}

	public void error(String msg) {
		if (!enableLog)
			return;
		if (jdkLogger != null) {
			jdkLogger.log(Level.SEVERE, msg);
			return;
		}
		try {
			commonLoggerErrorMethod.invoke(commonLogger, msg);
		} catch (Exception e) {
			throw new AssertionError(e.getMessage());
		}
	}

}
