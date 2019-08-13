/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro.log;

import java.lang.reflect.Method;

import com.github.drinkjava2.jdbpro.DbProException;

/**
 * DbProSLF4JLog use SLF4J Log output, to use it, need put a file
 * “DbProLog.properties” in main/resources folder with below:
 * log=com.github.drinkjava2.jdbpro.log.DbProSLF4JLog
 * 
 * And of cause, if use this Logger, related SLF4J Log jars are needed
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class DbProSLF4JLog implements DbProLog {
	private Object logger;
	private Method info;
	private Method debug;
	private Method warn, warnExp;
	private Method error, errorExp;

	public DbProSLF4JLog(Class<?> targetClass) {
		try {
			Class<?> logFactoryClass = Class.forName("org.slf4j.LoggerFactory");
			Method method = logFactoryClass.getMethod("getLogger", Class.class);
			logger = method.invoke(logFactoryClass, targetClass);
			debug = logger.getClass().getMethod("debug", String.class);
			info = logger.getClass().getMethod("info", String.class);
			warn = logger.getClass().getMethod("warn", String.class);
			error = logger.getClass().getMethod("error", String.class);
			warnExp = logger.getClass().getMethod("warn", String.class, Throwable.class);
			errorExp = logger.getClass().getMethod("error", String.class, Throwable.class);
		} catch (Exception e) {
			throw new DbProException(e);
		}
	}

	public void call(Method method, String msg) {
		try {
			method.invoke(logger, msg);
		} catch (Exception e) {
			// do nothing
		}
	}

	@Override
	public void info(String msg) {
		call(info, msg);
	}

	@Override
	public void warn(String msg) {
		call(warn, msg);
	}

	@Override
	public void debug(String msg) {
		call(debug, msg);
	}

	@Override
	public void warn(String msg, Throwable t) {
		try {
			warnExp.invoke(logger, msg, t);
		} catch (Exception e) {
			// do nothing
		}
	}

	@Override
	public void error(String msg) {
		call(error, msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		try {
			errorExp.invoke(logger, msg, t);
		} catch (Exception e) {
			// do nothing
		}
	}

}
