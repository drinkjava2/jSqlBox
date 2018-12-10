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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * For default DbProLog use DbProPrinterLog, unless put a class called
 * "DbProRootLog" in class root folder
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public abstract class DbProLogFactory {// NOSONAR

	private static Class<?> dbProLogClass = null;

	/**
	 * Find DbProLog.propertites configuration, if not found or DbProLogClass is
	 * empty, use default DbProPrinterLog
	 */
	public static DbProLog getLog(Class<?> clazz) {
		if (dbProLogClass == void.class)
			return new DbProPrintLog(clazz);

		if (dbProLogClass != null)
			try {
				Constructor<?> constr = dbProLogClass.getConstructor(Class.class);
				return (DbProLog) constr.newInstance(clazz);
			} catch (Exception e) {
				dbProLogClass = void.class;
				return new DbProPrintLog(clazz);
			}

		InputStream is = DbProLog.class.getClassLoader()
				.getResourceAsStream(DbProLog.class.getSimpleName() + ".properties");
		if (is == null) {
			dbProLogClass = void.class;
			return new DbProPrintLog(clazz);
		}

		Properties prop = new Properties();
		try {
			prop.load(is);
			String className = prop.getProperty("log");
			dbProLogClass = Class.forName(className);
			return getLog(clazz);
		} catch (Exception e) {
			dbProLogClass = void.class;
			return new DbProPrintLog(clazz);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}

}
