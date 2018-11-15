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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
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
	 * 2)This method does not support sequence, foreign keys, primary keys..., but
	 * will improve later.
	 */
	public static TableModel[] db2Models(Connection con, Dialect dialect) {// NOSONAR
		List<TableModel> tableModels = new ArrayList<TableModel>();
		SQLException sqlException = null;
		ResultSet rs = null;
		PreparedStatement pst = null;

		try {
			DatabaseMetaData meta = con.getMetaData();
			String catalog = con.getCatalog();
			// get Tables
			rs = meta.getTables(catalog, dialect.isOracleFamily() ? meta.getUserName() : null, null,
					new String[] { "TABLE" });
			while (rs.next()) {
				String tableName = rs.getString(TABLE_NAME);
				if (!StrUtils.isEmpty(tableName)) {
					TableModel model = new TableModel(tableName);
					tableModels.add(model);
					String comment = rs.getString("REMARKS");
					if (!StrUtils.isEmpty(comment))
						model.setComment(comment);
				}
			}
			rs.close();

			// Build Columns
			for (TableModel model : tableModels) {
				String tableName = model.getTableName();
				rs = meta.getColumns(null, null, tableName, null);
				while (rs.next()) {// NOSONAR
					String colName = rs.getString("COLUMN_NAME");
					ColumnModel col = new ColumnModel(colName);
					model.addColumn(col);

					int javaSqlType = rs.getInt("DATA_TYPE");
					try {
						col.setColumnType(TypeUtils.javaSqlTypeToDialectType(javaSqlType));
					} catch (Exception e1) {
						throw new DialectException("jDialect does not supported java.sql.types value " + javaSqlType,
								e1);
					}
					col.setLength(rs.getInt("COLUMN_SIZE"));
					col.setNullable(rs.getInt("NULLABLE") > 0);
					col.setPrecision(rs.getInt("DECIMAL_DIGITS"));
					col.setLengths(new Integer[] { col.getLength(), col.getPrecision(), col.getPrecision() });

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
				rs.close();
			}

			// Get Primary Keys for each model
			for (TableModel model : tableModels) {
				rs = meta.getPrimaryKeys(catalog, null, model.getTableName());
				while (rs.next())
					model.getColumnByColName(rs.getString("COLUMN_NAME")).setPkey(true);
				rs.close();
			}

			// Get Foreign Keys for each model
			for (TableModel model : tableModels) {
				ResultSet foreignKeyResultSet = meta.getImportedKeys(catalog, null, model.getTableName());
				while (foreignKeyResultSet.next()) {
					String fkname = foreignKeyResultSet.getString("FK_NAME");
					int keyseq = foreignKeyResultSet.getInt("KEY_SEQ");
					String fkColumnName = foreignKeyResultSet.getString("FKCOLUMN_NAME");
					String pkTablenName = foreignKeyResultSet.getString("PKTABLE_NAME");
					String pkColumnName = foreignKeyResultSet.getString("PKCOLUMN_NAME");
					FKeyModel fkeyModel = model.getFkey(fkname);

					if (keyseq == 1) {
						model.fkey(fkname).columns(fkColumnName).refs(pkTablenName, pkColumnName);
					} else {
						fkeyModel.getColumnNames().add(fkColumnName);
						String[] newRefs = ArrayUtils.appendStrArray(fkeyModel.getRefTableAndColumns(), pkColumnName);
						fkeyModel.setRefTableAndColumns(newRefs);
					}
				}
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
