/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

/**
 * Guess Dialect Utils
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
@SuppressWarnings("all")
public class GuessDialectUtils {
	private static final Map<DataSource, Dialect> dataSourceDialectCache = new ConcurrentHashMap<DataSource, Dialect>();

	/**
	 * Guess dialect based on given JDBC connection instance, Note: this method does
	 * not close connection
	 * 
	 * @param jdbcConnection
	 *            The connection
	 * @return dialect or null if can not guess out which dialect
	 */
	public static Dialect guessDialect(Connection jdbcConnection) {
		String databaseName;
		String driverName;
		int majorVersion;
		int minorVersion;
		try {
			DatabaseMetaData meta = jdbcConnection.getMetaData();
			driverName = meta.getDriverName();
			databaseName = meta.getDatabaseProductName();
			majorVersion = meta.getDatabaseMajorVersion();
			minorVersion = meta.getDatabaseMinorVersion();
		} catch (SQLException e) {
			return (Dialect) DialectException.throwEX(e);
		}
		return guessDialect(driverName, databaseName, majorVersion, minorVersion);
	}

	/**
	 * Guess dialect based on given dataSource
	 * 
	 * @param datasource
	 *            The dataSource
	 * @return dialect or null if can not guess out which dialect
	 */
	public static Dialect guessDialect(DataSource dataSource) {
		Dialect result = dataSourceDialectCache.get(dataSource);
		if (result != null)
			return result;
		Connection con = null;
		try {
			con = dataSource.getConnection();
			result = guessDialect(con);
			if (result == null)
				return (Dialect) DialectException
						.throwEX("Can not get dialect from DataSource, please submit this bug.");
			dataSourceDialectCache.put(dataSource, result);
			return result;
		} catch (SQLException e) {
			return (Dialect) DialectException.throwEX(e);
		} finally {
			try {
				if (con != null && !con.isClosed()) {
					try {// NOSONAR
						con.close();
					} catch (SQLException e) {
						DialectException.throwEX(e);
					}
				}
			} catch (SQLException e) {
				DialectException.throwEX(e);
			}
		}
	}

	/**
	 * @param databaseName
	 *            The database name
	 * @param majorVersionMinorVersion
	 *            The major version,The minor version, Optional optional
	 * @return dialect or null if not found
	 */
	public static Dialect guessDialect(String driverName, String databaseName, Object... majorVersionMinorVersion) {// NOSONAR
		int majorVersion = 0;
		int minorVersion = 0;
		for (int i = 0; i < majorVersionMinorVersion.length; i++) {
			if (i == 0)
				majorVersion = (Integer) majorVersionMinorVersion[i];
			if (i == 1)
				minorVersion = (Integer) majorVersionMinorVersion[i];
		}
		if ("CUBRID".equalsIgnoreCase(databaseName))
			return Dialect.CUBRIDDialect;
		if ("HSQL Database Engine".equals(databaseName))
			return Dialect.HSQLDialect;
		if ("H2".equals(databaseName))
			return Dialect.H2Dialect;
		if ("MySQL".equals(databaseName)) {
			if (majorVersion < 5)
				return Dialect.MySQLDialect;
			else if (majorVersion == 5) {
				if (minorVersion < 5)
					return Dialect.MySQL5Dialect;
				else if (minorVersion < 7)
					return Dialect.MySQL55Dialect;
				else
					return Dialect.MySQL57Dialect;
			}
			return Dialect.MySQL57Dialect;
		}
		if (driverName != null && driverName.startsWith("MariaDB")) {
			if (majorVersion == 10) {
				if (minorVersion >= 3)
					return Dialect.MariaDB103Dialect;
				else if (minorVersion == 2)
					return Dialect.MariaDB102Dialect;
				else if (minorVersion >= 0)
					return Dialect.MariaDB10Dialect;
				return Dialect.MariaDB53Dialect;
			} else if (majorVersion > 5 || (majorVersion == 5 && minorVersion >= 3)) {
				return Dialect.MariaDB53Dialect;
			}
			return Dialect.MariaDBDialect;
		}
		if ("PostgreSQL".equals(databaseName)) {
			if (majorVersion == 9) {
				if (minorVersion >= 4) {
					return Dialect.PostgreSQL94Dialect;
				} else if (minorVersion >= 2) {
					return Dialect.PostgreSQL92Dialect;
				}
				return Dialect.PostgreSQL9Dialect;
			}
			if (majorVersion == 8 && minorVersion >= 2) {
				return Dialect.PostgreSQL82Dialect;
			}
			return Dialect.PostgreSQL81Dialect;
		}
		if ("EnterpriseDB".equals(databaseName))
			return Dialect.PostgresPlusDialect;
		if ("Apache Derby".equals(databaseName)) {
			if (majorVersion > 10 || (majorVersion == 10 && minorVersion >= 7))
				return Dialect.DerbyTenSevenDialect;
			else if (majorVersion == 10 && minorVersion == 6)
				return Dialect.DerbyTenSixDialect;
			else if (majorVersion == 10 && minorVersion == 5)
				return Dialect.DerbyTenFiveDialect;
			else
				return Dialect.DerbyDialect;
		}
		if ("ingres".equalsIgnoreCase(databaseName)) {
			switch (majorVersion) {
			case 9:
				if (minorVersion > 2)
					return Dialect.Ingres9Dialect;
				else
					return Dialect.IngresDialect;
			case 10:
				return Dialect.Ingres10Dialect;
			default:
			}
			return Dialect.IngresDialect;
		}
		if (databaseName.startsWith("Microsoft SQL Server")) {
			switch (majorVersion) {
			case 8:
				return Dialect.SQLServerDialect;
			case 9:
				return Dialect.SQLServer2005Dialect;
			case 10:
				return Dialect.SQLServer2008Dialect;
			case 11:
			case 12:
			case 13:
				return Dialect.SQLServer2012Dialect;
			default:
				if (majorVersion < 8)
					return Dialect.SQLServerDialect;
				else
					return Dialect.SQLServer2012Dialect;
			}
		}
		if ("Sybase SQL Server".equals(databaseName) || "Adaptive Server Enterprise".equals(databaseName))
			return Dialect.SybaseASE15Dialect;
		if (databaseName.startsWith("Adaptive Server Anywhere"))
			return Dialect.SybaseAnywhereDialect;
		if ("Informix Dynamic Server".equals(databaseName))
			return Dialect.InformixDialect;
		if ("DB2 UDB for AS/400".equals(databaseName))
			return Dialect.DB2400Dialect;
		if (databaseName.startsWith("DB2/"))
			return Dialect.DB2Dialect;
		if ("Oracle".equals(databaseName)) {
			switch (majorVersion) {
			case 12:
				return Dialect.Oracle12cDialect;
			case 11:
			case 10:
				return Dialect.Oracle10gDialect;
			case 9:
				return Dialect.Oracle9iDialect;
			case 8:
				return Dialect.Oracle8iDialect;
			default:
			}
			return Dialect.Oracle12cDialect;
		}
		if ("HDB".equals(databaseName))
			return Dialect.HANAColumnStoreDialect;
		if (databaseName.startsWith("Firebird"))
			return Dialect.FirebirdDialect;
		if (StrUtils.containsIgnoreCase(databaseName, "sqlite"))
			return Dialect.SQLiteDialect;
		return null;
	}
}
