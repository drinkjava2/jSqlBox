package com.github.drinkjava2.jtransactions.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.DataSourceHolder;

/**
 * A Tiny JDBC tool only for unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class JTransTinyJdbc implements DataSourceHolder {
	ConnectionManager cm;
	DataSource ds;

	public JTransTinyJdbc(DataSource ds, ConnectionManager cm) {
		this.ds = ds;
		this.cm = cm;
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(String sql) {
		Systemout.println("SQL=" + sql);
		Connection con = getConnection();
		PreparedStatement pst = null;
		try {
			// Systemout.println("Con="+con.hashCode()+", autoCommit="+con.getAutoCommit());
			pst = con.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			rs.next();
			return (T) rs.getObject(1);
		} catch (SQLException e) {
			throw new RuntimeException("Fail to execute queryForInteger:", e);
		} finally {
			RuntimeException e = closePST(pst);
			releaseConnection(con);
			if (e != null)
				throw e;
		}
	}

	public void executeSql(String sql) {
		Systemout.println("SQL=" + sql);
		Connection con = getConnection();
		PreparedStatement pst = null;
		try {
			// Systemout.println("Con="+con.hashCode()+", autoCommit="+con.getAutoCommit());
			pst = con.prepareStatement(sql);
			pst.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Exception found:", e);
		} finally {
			RuntimeException e = closePST(pst);
			releaseConnection(con);
			if (e != null)
				throw e;
		}
	}

	private static RuntimeException closePST(PreparedStatement pst) {
		try {
			if (pst != null)
				pst.close();
			return null;
		} catch (SQLException e) {
			return new RuntimeException("Fail to close PreparedStatement", e);
		}
	}

	private Connection getConnection() {
		try {
			return cm.getConnection(this);
		} catch (SQLException e) {
			throw new RuntimeException("Fail to get Connection", e);
		}
	}

	private void releaseConnection(Connection conn) {
		try {
			cm.releaseConnection(conn, this);
		} catch (SQLException e) {
			throw new RuntimeException("Fail to release Connection", e);
		}
	}

	@Override
	public DataSource getDataSource() {
		return ds;
	}

	@Override
	public Object getHolder() {
		return this;
	}

}