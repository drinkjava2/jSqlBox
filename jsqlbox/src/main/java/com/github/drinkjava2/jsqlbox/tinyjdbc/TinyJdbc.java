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
package com.github.drinkjava2.jsqlbox.tinyjdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jsqlbox.Column;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * A tiny Jdbc tool to access database use separated transaction not related to current Spring transaction<br/>
 * Usually there is no need to use TinyJdbc in project.<br/>
 * Can use DBUtils for this purpose but I think DBUtils is still too big.
 * 
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyJdbc {
	public static final int TRANSACTION_NONE = Connection.TRANSACTION_NONE;
	public static final int TRANSACTION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;
	public static final int TRANSACTION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;
	public static final int TRANSACTION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
	public static final int TRANSACTION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;
	public static final int TRANSACTION_NOT_OPEN_NEW = -1;

	private TinyJdbc() {
		// hide default constructor
	}

	public static Integer queryForInteger(DataSource ds, int transactionIso, String sql, Object... args) {
		TinyResult rst = executeQuery(ds, transactionIso, sql, args);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			Object s = row.get(row.keySet().iterator().next());
			if (s == null)
				return 0;
			return Integer.parseInt("" + s);
		} else
			SqlBoxException.throwEX("TinyJdbc queryForObject error: null or multiple lines found for sql:" + sql);
		return null;
	}

	public static Object queryForObject(DataSource ds, int transactionIso, String sql, Object... args) {
		TinyResult rst = executeQuery(ds, transactionIso, sql, args);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			return row.get(row.keySet().iterator().next());
		} else
			SqlBoxException.throwEX("TinyJdbc queryForObject error: null or multiple lines found for sql:" + sql);
		return null;
	}

	public static String queryForString(DataSource ds, int transactionIso, String sql, Object... args) {
		return (String) queryForObject(ds, transactionIso, sql, args);
	}

	public static TinyResult executeQuery(DataSource ds, int transactionIso, String sql, Object... args) {// NOSONAR
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			if (transactionIso != -1)
				con.setTransactionIsolation(transactionIso);
			if (transactionIso != -1)
				con.setAutoCommit(false);
			int i = 1;
			pst = con.prepareStatement(sql);// NOSONAR
			for (Object obj : args)
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			TinyResult r = ResultSupport.toResult(rs);
			if (transactionIso != -1)
				con.commit();
			return r;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null && transactionIso != -1)
					con.rollback();
			} catch (SQLException e1) {
				SqlBoxException.throwEX(e1, e1.getMessage());
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					SqlBoxException.throwEX(e, e.getMessage());
				}
			}
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
			try {
				if (con != null && !con.isClosed()) {
					try {// NOSONAR
						con.close();
					} catch (SQLException e) {
						SqlBoxException.throwEX(e, e.getMessage());
					}
				}
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
		}
		return null;
	}

	public static int executeUpdate(DataSource ds, int transactionIso, String sql, Object... args) {// NOSONAR
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			if (transactionIso != -1)
				con.setTransactionIsolation(transactionIso);
			if (transactionIso != -1)
				con.setAutoCommit(false);
			int i = 1;
			pst = con.prepareStatement(sql);// NOSONAR
			for (Object obj : args)
				pst.setObject(i++, obj);
			int count = pst.executeUpdate();
			if (transactionIso != -1)
				con.commit();
			return count;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null && transactionIso != -1)
					con.rollback();
			} catch (SQLException e1) {
				SqlBoxException.throwEX(e1, e1.getMessage());
			}
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
			try {
				if (con != null && !con.isClosed()) {
					try {// NOSONAR
						con.close();
					} catch (SQLException e) {
						SqlBoxException.throwEX(e, e.getMessage());
					}
				}
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
		}
		return 0;
	}

	public static boolean execute(DataSource ds, int transactionIso, String sql, Object... args) {// NOSONAR
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			if (transactionIso != -1)
				con.setTransactionIsolation(transactionIso);
			if (transactionIso != -1)
				con.setAutoCommit(false);
			pst = con.prepareStatement(sql);// NOSONAR
			int i = 1;
			for (Object obj : args)
				pst.setObject(i++, obj);
			boolean bl = pst.execute();
			if (transactionIso != -1)
				con.commit();
			return bl;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null && transactionIso != -1)
					con.rollback();
			} catch (SQLException e1) {
				SqlBoxException.throwEX(e1, e1.getMessage());
			}
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
			try {
				if (con != null && !con.isClosed()) {
					try {// NOSONAR
						con.close();
					} catch (SQLException e) {
						SqlBoxException.throwEX(e, e.getMessage());
					}
				}
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
		}
		return false;
	}

	public static int[] executeBatch(DataSource ds, int transactionIso, String sql, List<List<Object>> argsList) {// NOSONAR
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			if (transactionIso != -1)
				con.setTransactionIsolation(transactionIso);
			if (transactionIso != -1)
				con.setAutoCommit(false);

			pst = con.prepareStatement(sql);// NOSONAR

			for (List<Object> args : argsList) {
				int i = 1;
				for (Object obj : args)
					pst.setObject(i++, obj);
				pst.addBatch();
			}
			int[] bl = pst.executeBatch();
			if (transactionIso != -1)
				con.commit();
			return bl;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null && transactionIso != -1)
					con.rollback();
			} catch (SQLException e1) {
				SqlBoxException.throwEX(e1, e1.getMessage());
			}
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
			try {
				if (con != null && !con.isClosed()) {
					try {// NOSONAR
						con.close();
					} catch (SQLException e) {
						SqlBoxException.throwEX(e, e.getMessage());
					}
				}
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
		}
		return new int[0];
	}

	public static TinyDbMetaData getMetaData(DataSource ds) {// NOSONAR
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			DatabaseMetaData meta = con.getMetaData();
			TinyDbMetaData tiny = new TinyDbMetaData();
			tiny.setJdbcDriverName(meta.getDriverName());
			DatabaseType dbType = DatabaseType.getDatabaseType(meta.getDriverName());
			if (dbType == DatabaseType.ORACLE) {
				pst = con.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES");// NOSONAR
				rs = pst.executeQuery();
				TinyResult tinyRs = ResultSupport.toResult(rs);
				for (Object tablename : tinyRs.getFirstColumns())
					tiny.getTableNames().put(((String) tablename).toLowerCase(), (String) tablename);
				rs.close();
				pst.close();
			} else if (dbType == DatabaseType.MS_SQLSERVER) {
				pst = con.prepareStatement("select name from sysobjects where xtype='U'");
				rs = pst.executeQuery();
				TinyResult tinyRs = ResultSupport.toResult(rs);
				for (Object tablename : tinyRs.getFirstColumns())
					tiny.getTableNames().put(((String) tablename).toLowerCase(), (String) tablename);
				rs.close();
				pst.close();
			} else {
				rs = meta.getTables(null, null, null, new String[] { "TABLE" });
				while (rs.next())
					tiny.getTableNames().put(rs.getString("TABLE_NAME").toLowerCase(), rs.getString("TABLE_NAME"));
				rs.close();

			}

			Collection<String> tables = tiny.getTableNames().values();
			for (String realTableName : tables) {
				rs = con.getMetaData().getColumns(null, null, realTableName, null);
				Map<String, Column> oneTable = new HashMap<>();
				while (rs.next()) {// NOSONAR
					Column col = new Column();

					// System.out.println("COLUMN_NAME=" + rs.getString("COLUMN_NAME"));
					// System.out.println("SOURCE_DATA_TYPE=" + rs.getString("SOURCE_DATA_TYPE"));
					// System.out.println("IS_AUTOINCREMENT=" + rs.getString("IS_AUTOINCREMENT"));

					/**
					 * For MySQL:<br/>
					 * TABLE_CAT String => 表类别（可为 null） <br/>
					 * TABLE_SCHEM String => 表模式（可为 null） <br/>
					 * TABLE_NAME String => 表名称 <br/>
					 * COLUMN_NAME String => 列名称 <br/>
					 * DATA_TYPE int => 来自 java.sql.Types 的 SQL 类型 <br/>
					 * TYPE_NAME String => 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的 <br/>
					 * COLUMN_SIZE int => 列的大小。 <br/>
					 * BUFFER_LENGTH 未被使用。 <br/>
					 * DECIMAL_DIGITS int => 小数部分的位数。对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null。 <br/>
					 * NUM_PREC_RADIX int => 基数（通常为 10 或 2） <br/>
					 * NULLABLE int => 是否允许使用 NULL。 <br/>
					 * columnNoNulls - 可能不允许使用 NULL 值 <br/>
					 * columnNullable - 明确允许使用 NULL 值 <br/>
					 * columnNullableUnknown - 不知道是否可使用 null <br/>
					 * REMARKS String => 描述列的注释（可为 null） <br/>
					 * COLUMN_DEF String => 该列的默认值，当值在单引号内时应被解释为一个字符串（可为 null） <br/>
					 * SQL_DATA_TYPE int => 未使用 <br/>
					 * SQL_DATETIME_SUB int => 未使用 <br/>
					 * CHAR_OCTET_LENGTH int => 对于 char 类型，该长度是列中的最大字节数 <br/>
					 * ORDINAL_POSITION int => 表中的列的索引（从 1 开始） <br/>
					 * IS_NULLABLE String => ISO 规则用于确定列是否包括 null。 YES --- 如果参数可以包 NO --- 如果参数不可以 空字符串 - 如果不知道是否可以<br/>
					 * SCOPE_CATLOG String => 表的类别，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null） <br/>
					 * SCOPE_SCHEMA String => 表的模式，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null） <br/>
					 * SCOPE_TABLE String => 表名称，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null） <br/>
					 * SOURCE_DATA_TYPE short => 不同类型或用户生成 Ref 类型、来自 java.sql.Types 的 SQL 类型的源类型（如果 DATA_TYPE <br/>
					 * 不是 DISTINCT 或用户生成的 REF，则为 null） <br/>
					 * IS_AUTOINCREMENT String => 指示此列是否自动增加 YES --- 如果该列自动增加 NO --- 空 如果不能确定该列是否是自动增加参数 <br/>
					 * COLUMN_SIZE 列表示给定列的指定列大小。对于数值数据，这是最大精度。对于字符数据，这是字符长度。对于日期时间数据类型，这是 String
					 * 表示形式的字符长度（假定允许的最大小数秒组件的精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，这是字节长度。对于列大小不适用的数据类型，则返回 Null。 <br/>
					 * 
					 */
					col.setColumnName(rs.getString("COLUMN_NAME"));
					col.setPropertyTypeName(rs.getString("TYPE_NAME"));
					col.setLength(rs.getInt("COLUMN_SIZE"));
					col.setNullable(rs.getInt("NULLABLE") > 0);
					col.setPrecision(rs.getInt("DECIMAL_DIGITS"));
					if (dbType == DatabaseType.MYSQL)// NOSONAR
						col.setAutoIncreament(rs.getBoolean("IS_AUTOINCREMENT"));
					oneTable.put(rs.getString("COLUMN_NAME").toLowerCase(), col);
				}
				tiny.getTables().put(realTableName.toLowerCase(), oneTable);
				rs.close();
			}
			return tiny;
		} catch (

		SQLException e) {
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
					SqlBoxException.throwEX(e, e.getMessage());
				}
			}
			try {
				if (pst != null)
					pst.close();
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
			try {
				if (con != null && !con.isClosed()) {
					try {// NOSONAR
						con.close();
					} catch (SQLException e) {
						SqlBoxException.throwEX(e, e.getMessage());
					}
				}
			} catch (SQLException e) {
				SqlBoxException.throwEX(e, e.getMessage());
			}
		}
		return null;
	}

}
