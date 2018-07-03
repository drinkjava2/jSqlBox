/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * The tool to convert database structure(meta data) to TableModels
 * 
 * @author Yong Zhu
 * @since 1.0.6
 */
public abstract class TableModelUtilsOfDb {
	private static final String TABLE_NAME = "TABLE_NAME";

	/**
	 * Convert JDBC connected database structure to TableModels, note: <br/>
	 * 1)This method does not close connection <br/>
	 * 2)This method does not support sequence, foreign keys, primary keys...,
	 * but will improve later.
	 */
	public static TableModel[] db2Model(Connection con, Dialect dialect) {// NOSONAR
		List<String> tableNames = new ArrayList<String>();
		List<TableModel> tableModels = new ArrayList<TableModel>();
		SQLException sqlException = null;
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			DatabaseMetaData meta = con.getMetaData();
			if (dialect.isOracleFamily()) {
				pst = con.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES");// NOSONAR
				rs = pst.executeQuery();
				while (rs.next())
					tableNames.add(rs.getString(TABLE_NAME));
				rs.close();
				pst.close();
				// } else if (dialect.isSQLServerFamily()) {
				// pst = con.prepareStatement("select name from sysobjects where
				// xtype='U'");
				// rs = pst.executeQuery();
				// while (rs.next())
				// tableNames.add(rs.getString(TABLE_NAME));
				// rs.close();
				// pst.close();
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
					oneTable.column(colName);
					ColumnModel col = oneTable.getColumnByColName(colName);
					int javaSqlType = rs.getInt("DATA_TYPE");
					// col.setPropertyTypeName(rs.getString("TYPE_NAME"));

					try {
						col.setColumnType(TypeUtils.javaSqlTypeToDialectType(javaSqlType));
					} catch (Exception e1) {
						throw new DialectException("jDialect does not supported java.sql.types value " + javaSqlType,
								e1);
					}
					col.setLength(rs.getInt("COLUMN_SIZE"));
					col.setNullable(rs.getInt("NULLABLE") > 0);
					col.setPrecision(rs.getInt("DECIMAL_DIGITS"));
					try {
						if (((Boolean) (true)).equals(rs.getBoolean("IS_AUTOINCREMENT")))
							col.identityId();
					} catch (Exception e) {
					}

					try {
						if ("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")))
							col.identityId();
					} catch (Exception e) {
					}

				}
				tableModels.add(oneTable);
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
		}
		if (sqlException != null)
			throw new DialectException(sqlException);
		return tableModels.toArray(new TableModel[tableModels.size()]);
	}

}
