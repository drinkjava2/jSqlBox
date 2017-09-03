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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * JdbcDataSourceTool is a simple DataSource tool simply get and release to
 * DataSource.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class JdbcDataSourceManager extends DataSourceManager {

	public Connection getConnection(DataSource dataSource) {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new JdbcDataSourceException("Can not get Connection from datasource", e);
		}
	}

	public void applyTimeout(Statement stmt, DataSource dataSource, int timeout) throws SQLException {
		// Do nothing;
	}

	public void releaseConnection(Connection con, DataSource dataSource) {
		try {
			if (con != null)
				con.close();
		} catch (SQLException e) {
			throw new JdbcDataSourceException("Fail to close connection", e);
		}
	}

	public static class JdbcDataSourceException extends RuntimeException {
		private static final long serialVersionUID = 7036515662765027556L;

		public JdbcDataSourceException(String message, Exception e) {
			super(message, e);
		}
	}
}
