/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * MetaData of database
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class DBMetaData {
	private static final String TABLE_NAME = "TABLE_NAME";
	String jdbcDriverName;
	Map<String, String> tableNames = new HashMap<>();

	/**
	 * Store whole database meta data in tables <br/>
	 * for tables, key is lower case real table name <br/>
	 * for map, key is lower case real column name
	 */
	Map<String, Map<String, Column>> tables = new HashMap<>();

	public boolean existTable(String tableName) {
		return tables.containsKey(tableName);
	}

	public Map<String, Column> getOneTable(String tableName) {
		return tables.get(tableName);
	}
	// getter & setters==============

	public DatabaseType getDatabaseType() {
		return DatabaseType.getDatabaseType(getJdbcDriverName());
	}

	public String getJdbcDriverName() {
		return jdbcDriverName;
	}

	public void setJdbcDriverName(String jdbcDriverName) {
		this.jdbcDriverName = jdbcDriverName;
	}

	public Map<String, Map<String, Column>> getTables() {
		return tables;
	}

	public void setTables(Map<String, Map<String, Column>> tables) {
		this.tables = tables;
	}

	public Map<String, String> getTableNames() {
		return tableNames;
	}

	public void setTableNames(Map<String, String> tableNames) {
		this.tableNames = tableNames;
	}

	public static DBMetaData getMetaData(SqlBoxContext context) {// NOSONAR
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			con = DataSourceUtils.getConnection(context.getDataSource());// NOSONAR
			DatabaseMetaData meta = con.getMetaData();
			DBMetaData tiny = new DBMetaData();
			tiny.setJdbcDriverName(meta.getDriverName());
			DatabaseType dbType = DatabaseType.getDatabaseType(meta.getDriverName());
			if (dbType == DatabaseType.ORACLE) {
				pst = con.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES");// NOSONAR
				rs = pst.executeQuery();
				while (rs.next()) {
					tiny.getTableNames().put(rs.getString(TABLE_NAME).toLowerCase(), rs.getString(TABLE_NAME));
				}
				rs.close();
				pst.close();
			} else if (dbType == DatabaseType.MSSQLSERVER) {
				pst = con.prepareStatement("select name from sysobjects where xtype='U'");
				rs = pst.executeQuery();
				while (rs.next()) {
					tiny.getTableNames().put(rs.getString("name").toLowerCase(), rs.getString("name"));
				}
				rs.close();
				pst.close();
			} else {
				rs = meta.getTables(null, null, null, new String[] { "TABLE" });
				while (rs.next()) {
					tiny.getTableNames().put(rs.getString(TABLE_NAME).toLowerCase(), rs.getString(TABLE_NAME));
				}
				rs.close();

			}

			Collection<String> tables = tiny.getTableNames().values();
			for (String realTableName : tables) {
				rs = con.getMetaData().getColumns(null, null, realTableName, null);
				Map<String, Column> oneTable = new HashMap<>();
				while (rs.next()) {// NOSONAR
					Column col = new Column();
					col.setColumnName(rs.getString("COLUMN_NAME"));
					col.setPropertyTypeName(rs.getString("TYPE_NAME"));
					col.setLength(rs.getInt("COLUMN_SIZE"));
					col.setNullable(rs.getInt("NULLABLE") > 0);
					col.setPrecision(rs.getInt("DECIMAL_DIGITS"));
					if (dbType == DatabaseType.MYSQL)// NOSONAR
						col.setAutoIncreament(rs.getBoolean("IS_AUTOINCREMENT"));
					else if (dbType == DatabaseType.MSSQLSERVER)// NOSONAR
					{
						boolean isautoInc = "YES".equals(rs.getString("IS_AUTOINCREMENT"));
						col.setAutoIncreament(isautoInc);
					}
					oneTable.put(rs.getString("COLUMN_NAME").toLowerCase(), col);
				}
				tiny.getTables().put(realTableName.toLowerCase(), oneTable);
				rs.close();
			}
			return tiny;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null)
					con.rollback();
			} catch (SQLException e1) {
				SqlBoxException.throwEX(e1, e1.getMessage());
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					DataSourceUtils.releaseConnection(con, context.getDataSource());
					SqlBoxException.throwEX(e, e.getMessage());
				}
			}
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException e) {
				DataSourceUtils.releaseConnection(con, context.getDataSource());
				SqlBoxException.throwEX(e, e.getMessage());
			}
			DataSourceUtils.releaseConnection(con, context.getDataSource());
		}
		return null;
	}

	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("\r\nTables:\r\n");
		sb.append(this.getTableNames().toString()).append("\r\n");

		for (Entry<String, Map<String, Column>> entry : tables.entrySet()) {
			String tableName = entry.getKey();
			sb.append("\r\n" + tableName).append("\r\n");
			Map<String, Column> onetable = entry.getValue();
			for (Entry<String, Column> t : onetable.entrySet()) {
				Column col = t.getValue();
				sb.append(col.getColumnName()).append(",");
				sb.append(col.getPropertyTypeName()).append(",");
				sb.append(col.getLength()).append("\r\n");
			}
		}
		return sb.toString();
	}
}