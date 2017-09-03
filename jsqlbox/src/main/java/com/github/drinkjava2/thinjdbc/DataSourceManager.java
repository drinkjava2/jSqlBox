/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.drinkjava2.thinjdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * ConnectionManager determine the strategy how to get/release connection from
 * DataSource, it can be different transaction strategies like
 * JDBC/SpringManaged/JTA...
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class DataSourceManager {
	protected Class<?> dataSourceUtilClass;
	protected Method getConnectionMethod;
	protected Method applyTimeoutMethod;
	protected Method releaseConnectionMethod;

	private static class SpringDataSourceManagerClass {
		private static final DataSourceManager INSTANCE = new DataSourceManager(
				"org.springframework.jdbc.datasource.DataSourceUtils");
	}

	public static final DataSourceManager springDataSourceManager() {
		return SpringDataSourceManagerClass.INSTANCE;
	}

	private static class JdbcDataSourceManagerClass {
		private static final DataSourceManager INSTANCE = new JdbcDataSourceManager();
	}

	public static final DataSourceManager jdbcDataSourceManager() {
		return JdbcDataSourceManagerClass.INSTANCE;
	}

	public DataSourceManager() {
	}

	/**
	 * Build a DataSourceManage
	 * 
	 * @param DataSourceUtilClassName
	 *            A class name in which class should have at least 3
	 *            method:getConnectionMethod,applyTimeoutMethod,releaseConnectionMethod,
	 *            like "org.springframework.jdbc.datasource.DataSourceUtils"
	 */
	public DataSourceManager(String dataSourceUtilClassName) {
		try {
			Class<?> dataSourceUtilClass = Class.forName(dataSourceUtilClassName);
			cacheMethods(dataSourceUtilClass);
		} catch (ClassNotFoundException e) {
			throw new DataSourceManagerException(
					"Class not found Exception for class: \"" + dataSourceUtilClassName + "\" ", e);
		}
	}

	public DataSourceManager(Class<?> dataSourceUtilClass) {
		cacheMethods(dataSourceUtilClass);
	}

	private void cacheMethods(Class<?> dataSourceUtilClass) {
		this.dataSourceUtilClass = dataSourceUtilClass;
		try {
			getConnectionMethod = dataSourceUtilClass.getMethod("getConnection", new Class[] { DataSource.class });
			applyTimeoutMethod = dataSourceUtilClass.getMethod("applyTimeout",
					new Class[] { Statement.class, DataSource.class, int.class });
			releaseConnectionMethod = dataSourceUtilClass.getMethod("releaseConnection",
					new Class[] { Connection.class, DataSource.class });
		} catch (NoSuchMethodException e) {
			throw new DataSourceManagerException("No methods exception for class: \"" + dataSourceUtilClass + "\" ", e);
		} catch (SecurityException e) {
			throw new DataSourceManagerException("Security exception for class: \"" + dataSourceUtilClass + "\" ", e);
		}
	}

	public Connection getConnection(DataSource dataSource) {
		try {
			return (Connection) getConnectionMethod.invoke(null, new Object[] { dataSource });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void applyTimeout(Statement stmt, DataSource dataSource, int timeout) throws SQLException {
		try {
			applyTimeoutMethod.invoke(null, new Object[] { stmt, dataSource, timeout });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void releaseConnection(Connection con, DataSource dataSource) {
		try {
			releaseConnectionMethod.invoke(null, new Object[] { con, dataSource });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	// Getter & Setter============

	public Class<?> getDataSourceUtilClass() {
		return dataSourceUtilClass;
	}

	public Method getGetConnectionMethod() {
		return getConnectionMethod;
	}

	public Method getApplyTimeoutMethod() {
		return applyTimeoutMethod;
	}

	public Method getReleaseConnectionMethod() {
		return releaseConnectionMethod;
	}

	public void setDataSourceUtilClass(Class<?> dataSourceUtilClass) {
		this.dataSourceUtilClass = dataSourceUtilClass;
	}

	public void setGetConnectionMethod(Method getConnectionMethod) {
		this.getConnectionMethod = getConnectionMethod;
	}

	public void setApplyTimeoutMethod(Method applyTimeoutMethod) {
		this.applyTimeoutMethod = applyTimeoutMethod;
	}

	public void setReleaseConnectionMethod(Method releaseConnectionMethod) {
		this.releaseConnectionMethod = releaseConnectionMethod;
	}

	// Getter & Setter end============

	public static class DataSourceManagerException extends RuntimeException {
		private static final long serialVersionUID = 1352967226525740020L;

		public DataSourceManagerException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
}
