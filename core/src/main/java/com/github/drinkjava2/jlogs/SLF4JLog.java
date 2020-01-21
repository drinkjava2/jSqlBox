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
package com.github.drinkjava2.jlogs;

import java.lang.reflect.Method;

import com.github.drinkjava2.jdbpro.DbProException;

/**
 * SLF4JLog use SLF4J Log output, to use it, need put a file “jlogs.properties”
 * in main/resources or test/resources folder with below line:
 * log=com.github.drinkjava2.jlogs.SLF4JLog
 * 
 * And of cause, if use this SLF4JLog Logger, related SLF4J dependencies need
 * added in pom.xml
 * 
 * Please note: this SLF4JLog is only used inside of DbUtil-Plus for debugging
 * purpose, not recommended to use in application.
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class SLF4JLog implements Log {
	private Object logger;
	private Method info;
	private Method debug;
	private Method warn;
	private Method warnExp;
	private Method error;
	private Method errorExp;

	public SLF4JLog(Class<?> targetClass) {
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
