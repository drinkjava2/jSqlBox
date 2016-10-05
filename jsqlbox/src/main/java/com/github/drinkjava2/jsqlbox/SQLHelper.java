
package com.github.drinkjava2.jsqlbox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class SQLHelper {
	public static class SQLandParameters {
		public String sql;
		public String[] parameters;
	}

	Connection connection;
	private static ThreadLocal<ArrayList<String>> sqlCache = new ThreadLocal<ArrayList<String>>() {
		protected ArrayList<String> initialValue() {
			return new ArrayList<String>();
		}
	};

	public static String V(Object obj) {
		sqlCache.get().add("" + obj);
		return "?";
	}

	public SQLHelper() {
		// Empty sqlCache before build SQL PreparedStatement
		sqlCache.get().clear();
	}

	public SQLHelper(Connection connection) {
		this.connection = connection;
		// Empty sqlCache before build SQL PreparedStatement
		sqlCache.get().clear();
	}

	public SQLandParameters prepareSQL(String sql) {
		SQLandParameters sp = new SQLandParameters();
		ArrayList<String> list = sqlCache.get();
		sp.parameters = (String[]) list.toArray(new String[list.size()]);
		sqlCache.get().clear();
		return sp;
	}

	public PreparedStatement prepareStatement(String sql) {
		SQLandParameters sp = prepareSQL(sql);
		try {
			PreparedStatement state = connection.prepareStatement(sql);
			int index = 1;
			for (String parameter : sp.parameters) {
				state.setString(index++, parameter);
			}
			return state;
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLHelper exec error, sql=" + sql);
			return null;
		}
	}

}
