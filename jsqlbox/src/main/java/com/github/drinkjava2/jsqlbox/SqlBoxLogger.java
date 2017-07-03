/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For logger output, to avoid logger jar version conflict, default use JDK log,
 * if found commons log, use it, if found Log4j use it..., by this way this
 * project has no dependency to any logger jar.
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxLogger {
	boolean fatalEnabled = true;
	boolean errorEnabled = true;
	boolean warnEnabled = true;
	boolean infoEnabled = true;
	boolean debugEnabled = false;// default not allow debug output
	boolean traceEnabled = false;// default not allow trace output

	private Object commonLogger;
	private Method commonLoggerFatalMethod;
	private Method commonLoggerErrorMethod;
	private Method commonLoggerWarnMethod;
	private Method commonLoggerInfoMethod;
	private Method commonLoggerDebugMethod;
	private Logger jdkLogger;

	public SqlBoxLogger(Class<?> targetClass) {
		if (targetClass == null)
			throw new LoggingException("targetClass can not be null.");
		try {
			Class<?> logFactoryClass = Class.forName("org.apache.commons.logging.LogFactory");
			Method method = logFactoryClass.getMethod("getLog", new Class[] { Class.class });
			commonLogger = method.invoke(logFactoryClass, new Object[] { targetClass });

			commonLoggerFatalMethod = commonLogger.getClass().getMethod("fatal", new Class[] { Object.class });
			commonLoggerErrorMethod = commonLogger.getClass().getMethod("error", new Class[] { Object.class });
			commonLoggerWarnMethod = commonLogger.getClass().getMethod("warn", new Class[] { Object.class });
			commonLoggerInfoMethod = commonLogger.getClass().getMethod("info", new Class[] { Object.class });
			commonLoggerDebugMethod = commonLogger.getClass().getMethod("debug", new Class[] { Object.class });
		} catch (Exception e) {
			// eat exception
		}

		if (commonLogger == null || commonLoggerFatalMethod == null)
			jdkLogger = Logger.getLogger(targetClass.getName());// use JDK log
	}

	public static SqlBoxLogger getLog(Class<?> targetClass) {
		return new SqlBoxLogger(targetClass);
	}

	public boolean isFatalEnabled() {
		return fatalEnabled;
	}

	public boolean isErrorEnabled() {
		return errorEnabled;
	}

	public boolean isWarnEnabled() {
		return warnEnabled;
	}

	public boolean isInfoEnabled() {
		return infoEnabled;
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public boolean isTraceEnabled() {
		return traceEnabled;
	}

	public Logger getJdkLogger() {
		return jdkLogger;
	}

	public void setFatalEnabled(boolean fatalEnabled) {
		this.fatalEnabled = fatalEnabled;
	}

	public void setErrorEnabled(boolean errorEnabled) {
		this.errorEnabled = errorEnabled;
	}

	public void setWarnEnabled(boolean warnEnabled) {
		this.warnEnabled = warnEnabled;
	}

	public void setInfoEnabled(boolean infoEnabled) {
		this.infoEnabled = infoEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	public void setTraceEnabled(boolean traceEnabled) {
		this.traceEnabled = traceEnabled;
	}

	public void setJdkLogger(Logger jdkLogger) {
		this.jdkLogger = jdkLogger;
	}

	public void falal(String msg) {
		if (jdkLogger != null) {
			jdkLogger.log(Level.SEVERE, msg);
			return;
		}
		try {
			commonLoggerFatalMethod.invoke(commonLogger, new Object[] { msg });
		} catch (Exception e) {
			throw new LoggingException(e);
		}
	}

	public void error(String msg) {
		if (jdkLogger != null) {
			jdkLogger.log(Level.SEVERE, msg);
			return;
		}
		try {
			commonLoggerErrorMethod.invoke(commonLogger, new Object[] { msg });
		} catch (Exception e) {
			throw new LoggingException(e);
		}
	}

	public void warn(String msg) {
		if (jdkLogger != null) {
			jdkLogger.log(Level.WARNING, msg);
			return;
		}
		try {
			commonLoggerWarnMethod.invoke(commonLogger, new Object[] { msg });
		} catch (Exception e) {
			throw new LoggingException(e);
		}
	}

	public void info(String msg) {
		if (jdkLogger != null) {
			jdkLogger.log(Level.INFO, msg);
			return;
		}
		try {
			commonLoggerInfoMethod.invoke(commonLogger, new Object[] { msg });
		} catch (Exception e) {
			throw new LoggingException(e);
		}
	}

	public void debug(String msg) {
		if (jdkLogger != null) {
			jdkLogger.log(Level.FINE, msg);
			return;
		}
		try {
			commonLoggerDebugMethod.invoke(commonLogger, new Object[] { msg });
		} catch (Exception e) {
			throw new LoggingException(e);
		}
	}

	public void trace(String msg) {
		if (jdkLogger != null) {
			jdkLogger.log(Level.FINEST, msg);
			return;
		}
		try {
			commonLoggerDebugMethod.invoke(commonLogger, new Object[] { msg });
		} catch (Exception e) {
			throw new LoggingException(e);
		}
	}

	public void falal(String msg, Throwable e) {
		this.falal(msg + e.getMessage());
	}

	public void error(String msg, Throwable e) {
		this.error(msg + e.getMessage());
	}

	public void warn(String msg, Throwable e) {
		this.warn(msg + e.getMessage());
	}

	public void info(String msg, Throwable e) {
		this.info(msg + e.getMessage());
	}

	public void debug(String msg, Throwable e) {
		this.debug(msg + e.getMessage());
	}

	public void trace(String msg, Throwable e) {
		this.trace(msg + e.getMessage());
	}

	public static class LoggingException extends RuntimeException {
		private static final long serialVersionUID = 7036515662765027556L;

		public LoggingException(String message) {
			super(message);
		}

		public LoggingException(Exception e) {
			super(e);
		}

		public LoggingException(String message, Exception e) {
			super(message, e);
		}
	}
}
