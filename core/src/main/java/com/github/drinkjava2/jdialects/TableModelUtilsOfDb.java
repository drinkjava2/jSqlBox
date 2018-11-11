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
	 * 2)This method does not support sequence, foreign keys, primary keys..., but
	 * will improve later.
	 */
	public static TableModel[] db2Model(Connection con, Dialect dialect) {// NOSONAR
		List<TableModel> tableModels = new ArrayList<TableModel>();
		SQLException sqlException = null;
		ResultSet rs = null;
		PreparedStatement pst = null;

		try {
			DatabaseMetaData meta = con.getMetaData();

			// get Tables
			rs = meta.getTables(con.getCatalog(), dialect.isOracleFamily() ? meta.getUserName() : null, null,
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

			// Get Primary Keys
			for (TableModel model : tableModels) {
				rs = meta.getPrimaryKeys(con.getCatalog(), null, model.getTableName());
				while (rs.next())
					model.getColumnByColName(rs.getString("COLUMN_NAME")).setPkey(true);
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
//
//	/**
//	 * map column name to entity field name, example: <br/>
//	 * user_name -> userName <br/>
//	 * USER_NAME -> userName <br/>
//	 * User_naMe -> userName <br/>
//	 * UserName -> userName <br/>
//	 * USERNAME -> uSERNAME <br/>
//	 * userName -> userName <br/>
//	 * username -> username <br/>
//	 */
//	public static String transColumnNameToFieldName(String colName) {
//		if (StrUtils.isEmpty(colName))
//			return colName;
//		if (!colName.contains("_"))
//			return StrUtils.toLowerCaseFirstOne(colName);
//		StringBuilder sb = new StringBuilder();
//		char[] chars = colName.toLowerCase().toCharArray();
//		for (int i = 0; i < chars.length; i++) {
//			char c = chars[i];
//			if (c == '_')
//				continue;
//			if ((i > 0) && (chars[i - 1]) == '_' && sb.length() > 0)
//				sb.append(Character.toUpperCase(c));
//			else
//				sb.append(c);
//		}
//		return sb.toString();
//	}

}
