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
package com.github.drinkjava2.jdialects.log;

/**
 * DbProConsoleLog output log to console
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DialectPrintLog implements DialectLog {
	private static boolean allowPrint = false;

	Class<?> clazz;

	public static boolean isAllowPrint() {
		return allowPrint;
	}

	public static void setAllowPrint(boolean allowPrint) {
		DialectPrintLog.allowPrint = allowPrint;
	}

	public DialectPrintLog(Class<?> clazz) {
		this.clazz = clazz;
	}

	public void info(String msg) {
		if (allowPrint)
			System.out.println(msg);// NOSONAR
	}

	public void warn(String msg) {
		if (allowPrint)
			System.out.println(msg);// NOSONAR
	}

	public void error(String msg) {
		if (allowPrint)
			System.out.println(msg);// NOSONAR
	}

	public void debug(String msg) {
		if (allowPrint)
			System.out.println(msg);// NOSONAR
	}

}
