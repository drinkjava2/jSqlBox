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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ConsoleLog only output to console
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class ConsoleLog implements Log {

	public static final int DEBUG = 4;
	public static final int ERROR = 3;
	public static final int WARNING = 2;
	public static final int INFO = 1;
	public static final int OFF = 0;

	Class<?> clazz;
	public static int logLevel = INFO;
	public static boolean outputlogHead = false;

	public ConsoleLog(Class<?> clazz) {
		this.clazz = clazz;
	}

	public static int getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(int logLevel) {
		ConsoleLog.logLevel = logLevel;
	}

	private String loghead(String method) {
		return outputlogHead
				? new StringBuilder(clazz.getSimpleName()).append(" ").append(method).append(": ").toString()
				: "";
	}

	@Override
	public void info(String msg) {
		if (logLevel <= INFO)
			System.out.println(loghead("info") + msg);
	}

	@Override
	public void warn(String msg) {
		if (logLevel <= WARNING)
			System.out.println(loghead("warn") + msg);
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (logLevel <= WARNING)
			System.out.println(loghead("warn") + msg + getStackTrace(t));
	}

	@Override
	public void error(String msg) {
		if (logLevel <= ERROR)
			System.out.println(loghead("error") + msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		if (logLevel <= WARNING)
			System.out.println(loghead("error") + msg + getStackTrace(t));
	}

	@Override
	public void debug(String msg) {
		if (logLevel <= DEBUG)
			System.out.println(loghead("info") + msg);
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}
}
