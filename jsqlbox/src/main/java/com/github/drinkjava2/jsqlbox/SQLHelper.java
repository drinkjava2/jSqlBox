
package com.github.drinkjava2.jsqlbox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class SQLHelper {
	Connection connection;
	private static ThreadLocal<ArrayList<String>> sqlCache = new ThreadLocal<ArrayList<String>>() {
		protected ArrayList<String> initialValue() {
			return new ArrayList<String>();
		}
	};

	public static String V(Object obj) {
		sqlCache.get().add(""+obj);
		return "?";
	}

	public SQLHelper(Connection connection) {
		this.connection = connection;
		sqlCache.get().clear();// important!
	}

	public PreparedStatement prepareStatement(String sql) {
		ArrayList<String> list = sqlCache.get();
		String[] args = (String[]) list.toArray(new String[list.size()]);
		sqlCache.get().clear();
		try {
			System.out.println("sql=" + sql);
			PreparedStatement ps = connection.prepareStatement(sql);
			int index = 1;
			for (String string : args) {
				System.out.println(index + " " + string);
				ps.setString(index++, string);
			}
			return ps;
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLHelper exec error, sql=" + sql);
			return null;
		}
	}

	public Object execute(String sql) {
		PreparedStatement ps = prepareStatement(sql);
		try {
			return ps.execute();
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLHelper execute error, sql=" + sql);
			return null;
		}
	}

	public Object executeQuery(String sql) {
		PreparedStatement ps = prepareStatement(sql);
		try {
			return ps.executeQuery();
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLHelper executeQuery error, sql=" + sql);
			return null;
		}
	}

	public Object executeUpdate(String sql) {
		PreparedStatement ps = prepareStatement(sql);
		try {
			return ps.executeUpdate();
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLHelper executeUpdate error, sql=" + sql);
			return null;
		}
	}

}
