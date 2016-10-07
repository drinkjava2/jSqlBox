
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

	public static String S(Object... obj) {
		for (Object o : obj)
			sqlCache.get().add("" + o);
		return "?";
	}

	public static String D(Object... obj) {
		String result = "";
		for (int i = 0; i < obj.length; i++) {
			sqlCache.get().add("" + obj[i]);
			if (i != obj.length - 1)
				result += "?,";
			else
				result += "?";
		}
		return result;
	}

	public static String K(Object... obj) {
		for (Object o : obj)
			sqlCache.get().add("" + o);
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
