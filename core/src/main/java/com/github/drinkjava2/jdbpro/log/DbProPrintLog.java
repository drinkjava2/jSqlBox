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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * DbProPrintLog is the default logger of jDbPro and jSqlBox project
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DbProPrintLog implements DbProLog {
	Class<?> clazz;

	public DbProPrintLog(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void info(String msg) {
		System.out.println(msg);// NOSONAR
	}

	@Override
	public void warn(String msg) {
		System.out.println(msg);// NOSONAR
	}

	@Override
	public void warn(String msg, Throwable t) {
		System.out.println(msg + getStackTrace(t));
	}

	@Override
	public void error(String msg) {
		System.out.println(msg);// NOSONAR
	}

	@Override
	public void error(String msg, Throwable t) {
		System.out.println(msg + getStackTrace(t));
	}

	@Override
	public void debug(String msg) {
		System.out.println(msg);// NOSONAR
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
