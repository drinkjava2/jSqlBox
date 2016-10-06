
package com.github.drinkjava2.jsqlbox;

import java.util.ArrayList;

public class SQLHelper {
	public static class SqlAndParameters {
		public String sql;
		public String[] parameters;
	}

	private static ThreadLocal<ArrayList<String>> sqlCache = new ThreadLocal<ArrayList<String>>() {
		protected ArrayList<String> initialValue() {
			return new ArrayList<String>();
		}
	};

	public static void clearSQLCache() {
		sqlCache.get().clear();
	}

	public static String s(Object obj) {
		sqlCache.get().add("" + obj);
		return "?";
	}

	public static String s0(Object obj) {
		sqlCache.get().add("" + obj);
		return "";
	}

	public static SqlAndParameters splitSQLandParameters(String sql) {
		SqlAndParameters sp = new SqlAndParameters();
		ArrayList<String> list = sqlCache.get();
		sp.parameters = (String[]) list.toArray(new String[list.size()]);
		clearSQLCache();
		return sp;
	}

}
