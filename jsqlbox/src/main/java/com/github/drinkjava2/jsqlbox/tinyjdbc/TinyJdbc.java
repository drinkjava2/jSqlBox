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

import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * A tiny Jdbc tool to access database use separated transaction not related to Spring<br/>
 * The reason I wrote this class is I need a tool to update table generator ID through a separated transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyJdbc {

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
			SqlBoxException.throwEX(null, "TinyJdbc queryForObject error: null or multiple lines found for sql:" + sql);
		return null;
	}

	public static Object queryForObject(DataSource ds, int transactionIso, String sql, Object... args) {
		TinyResult rst = executeQuery(ds, transactionIso, sql, args);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			return row.get(row.keySet().iterator().next());
		} else
			SqlBoxException.throwEX(null, "TinyJdbc queryForObject error: null or multiple lines found for sql:" + sql);
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
			con.setTransactionIsolation(transactionIso);
			con.setAutoCommit(false);
			int i = 1;
			pst = con.prepareStatement(sql);// NOSONAR
			for (Object obj : args)
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			TinyResult r = ResultSupport.toResult(rs);
			con.commit();
			return r;
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

	public static int executeUpdate(DataSource ds, int transactionIso, String sql, Object... args) {// NOSONAR
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = ds.getConnection();
			con.setTransactionIsolation(transactionIso);
			con.setAutoCommit(false);
			int i = 1;
			pst = con.prepareStatement(sql);// NOSONAR
			for (Object obj : args)
				pst.setObject(i++, obj);
			int count = pst.executeUpdate();
			con.commit();
			return count;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null)
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
			con.setTransactionIsolation(transactionIso);
			con.setAutoCommit(false);
			pst = con.prepareStatement(sql);// NOSONAR
			int i = 1;
			for (Object obj : args)
				pst.setObject(i++, obj);
			boolean bl = pst.execute();
			con.commit();
			return bl;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null)
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
			con.setTransactionIsolation(transactionIso);
			con.setAutoCommit(false);

			pst = con.prepareStatement(sql);// NOSONAR

			for (List<Object> args : argsList) {
				int i = 1;
				for (Object obj : args)
					pst.setObject(i++, obj);
				pst.addBatch();
			}
			int[] bl = pst.executeBatch();
			con.commit();
			return bl;
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, e.getMessage());
			try {
				if (con != null)
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
		try {
			con = ds.getConnection();
			DatabaseMetaData meta = con.getMetaData();
			TinyDbMetaData tiny = new TinyDbMetaData();
			tiny.setJdbcDriverName(meta.getDriverName());
			rs = meta.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				// System.out.println(rs.getString("TABLE_NAME"));
				tiny.getTableNames().put(rs.getString("TABLE_NAME").toLowerCase(), rs.getString("TABLE_NAME"));
			}
			rs.close();
			Collection<String> tables = tiny.getTableNames().values();
			for (String realTableName : tables) {
				if (!realTableName.contains("=")) {
					// System.out.println(realTableName);
					rs = con.getMetaData().getColumns(null, null, realTableName, null);
					Map<String, Column> oneTable = new HashMap<>();
					while (rs.next()) {
						Column col = new Column();
						// System.out.println("======" + rs.getString("COLUMN_NAME"));
						col.setColumnName(rs.getString("COLUMN_NAME"));
						col.setPropertyTypeName(rs.getString("TYPE_NAME"));
						col.setLength(rs.getInt("COLUMN_SIZE"));
						col.setNullable(rs.getInt("NULLABLE") > 0);
						col.setPrecision(rs.getInt("DECIMAL_DIGITS"));
						oneTable.put(rs.getString("COLUMN_NAME").toLowerCase(), col);
					}
					tiny.getTables().put(realTableName.toLowerCase(), oneTable);
					rs.close();
				}
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
