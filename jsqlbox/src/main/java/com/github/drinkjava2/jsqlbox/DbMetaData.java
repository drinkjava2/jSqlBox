/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TypeUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Database Meta data cache
 */
@SuppressWarnings("all")
public class DbMetaData {
	private static final String TABLE_NAME = "TABLE_NAME";
	private List<String> tableNames = new ArrayList<String>();
	private Map<String, TableModel> tableModels = new HashMap<String, TableModel>();

	public synchronized void refreshMetaData(SqlBoxContext ctx) {// NOSONAR
		SQLException sqlException = null;
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			con = ctx.prepareConnection();
			DatabaseMetaData meta = con.getMetaData();
			Dialect dialect = ctx.getDialect();
			if (dialect.isOracleFamily()) {
				pst = con.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES");// NOSONAR
				rs = pst.executeQuery();
				while (rs.next())
					tableNames.add(rs.getString(TABLE_NAME));
				rs.close();
				pst.close();
			} else if (dialect.isSQLServerFamily()) {
				pst = con.prepareStatement("select name from sysobjects where xtype='U'");
				rs = pst.executeQuery();
				while (rs.next())
					tableNames.add(rs.getString(TABLE_NAME));
				rs.close();
				pst.close();
			} else {
				rs = meta.getTables(null, null, null, new String[] { "TABLE" });
				while (rs.next())
					tableNames.add(rs.getString(TABLE_NAME));
				rs.close();
			}

			for (String dbTableName : tableNames) {
				rs = con.getMetaData().getColumns(null, null, dbTableName, null);
				TableModel oneTable = new TableModel(dbTableName);
				while (rs.next()) {// NOSONAR
					String colName = rs.getString("COLUMN_NAME");
					oneTable.addColumn(colName);
					ColumnModel col = oneTable.getColumn(colName);
					int javaSqlType=rs.getInt("DATA_TYPE");
					//col.setPropertyTypeName(rs.getString("TYPE_NAME")); 
					col.setColumnType(TypeUtils.javaSqlTypeToDialectType(javaSqlType));
					
					col.setLength(rs.getInt("COLUMN_SIZE"));
					col.setNullable(rs.getInt("NULLABLE") > 0);
					col.setPrecision(rs.getInt("DECIMAL_DIGITS"));
					try {
						if (((Boolean) (true)).equals(rs.getBoolean("IS_AUTOINCREMENT")))
							col.identityId();
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						if ("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")))
							col.identityId();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				tableModels.put(dbTableName.toLowerCase(), oneTable);
				rs.close();
			}
		} catch (SQLException e) {
			sqlException = e;
		} finally {
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e1) {
					if (sqlException != null)
						sqlException.setNextException(e1);
					else
						sqlException = e1;
				}
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e2) {
				if (sqlException != null)
					sqlException.setNextException(e2);
				else
					sqlException = e2;
			}
			try {
				ctx.close(con);
			} catch (SQLException e3) {
				if (sqlException != null)
					sqlException.setNextException(e3);
				else
					sqlException = e3;
			}
		}
		if (sqlException != null)
			throw new SqlBoxException(sqlException);
	}

	// ==== getter & setter===========
	public List<String> getTableNames() {
		return tableNames;
	}

	public void setTableNames(List<String> tableNames) {
		this.tableNames = tableNames;
	}

	public Map<String, TableModel> getTableModels() {
		return tableModels;
	}

	public void setTableModels(Map<String, TableModel> tableModels) {
		this.tableModels = tableModels;
	}

}