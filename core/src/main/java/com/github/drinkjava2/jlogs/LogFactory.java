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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * This LogFactory is designed for get a jLog implement used inside of jSqlBox
 * project.
 * 
 * Usage: Log log= LogFactory.getLog(Xxx.class);
 * 
 * JLog used for inside of jSqlBox project, if a "jlogs.properties" file if
 * found on class root folder (main/resources), will try load the designated
 * JLog implementation, otherwise use default EmptyLog<br/>
 * 
 * An example of "jlogs.properties": <br/>
 * log=com.github.drinkjava2.jlogs.ConsoleLog
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public abstract class LogFactory {// NOSONAR

	private static boolean printed = false;
	private static Class<?> dbProLogClass = null;

	/**
	 * Find jlogs.properties configuration, if not found or jlogs.properties is
	 * empty, use default ConsoleLog
	 */
	public static Log getLog(Class<?> clazz) {
		if (dbProLogClass == void.class)
			return new ConsoleLog(clazz);

		if (dbProLogClass != null)
			try {
				Constructor<?> constr = dbProLogClass.getConstructor(Class.class);
				return (Log) constr.newInstance(clazz);
			} catch (Exception e) {
				if (!printed())
					System.err.println("Can not load log class: " + dbProLogClass // NOSONAR
							+ ", will use ConsoleLog JLog logger. \r\n" + e.getMessage());
				dbProLogClass = void.class;
				return new ConsoleLog(clazz);
			}

		InputStream is = Log.class.getClassLoader().getResourceAsStream("jlogs.properties");
		if (is == null) {
			if (!printed())
				System.out.println("Not found jlogs.properties,  will use ConsoleLog as JLog logger.");// NOSONAR
			dbProLogClass = void.class;
			return new ConsoleLog(clazz);
		}

		Properties prop = new Properties();
		String className = "";
		try {
			prop.load(is);
			className = prop.getProperty("log");
			dbProLogClass = Class.forName(className);
			if (!printed())
				System.out.print("jlog.properties found, will use " + className + " as JLog logger."); // NOSONAR
			return getLog(clazz);
		} catch (Exception e) {
			if (!printed())
				System.err.println("No or wrong jlog.properties file: " + className // NOSONAR
						+ ", will use ConsoleLog as JLog logger. \r\n" + e.getMessage());
			dbProLogClass = void.class;
			return new ConsoleLog(clazz);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}

	private static boolean printed() {
		boolean old = printed;
		printed = true;
		return old;
	}

}
