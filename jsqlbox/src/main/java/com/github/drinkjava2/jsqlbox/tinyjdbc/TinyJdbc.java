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
			} else if (dbType == DatabaseType.MSSQLSERVER) {
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
