
package com.github.drinkjava2.jsqlbox;

import java.util.ArrayList;
import java.util.List;

public class SQLHelper {
	private static ThreadLocal<ArrayList<String>> sqlCache = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	private static ThreadLocal<ArrayList<SqlAndParameters>> sqlBatchCache = new ThreadLocal<ArrayList<SqlAndParameters>>() {
		@Override
		protected ArrayList<SqlAndParameters> initialValue() {
			return new ArrayList<>();
		}
	};
	private static ThreadLocal<String> sqlBatchString = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";
		}
	};

	private SQLHelper() {
	}

	public static ThreadLocal<ArrayList<String>> getSqlCache() {
		return sqlCache;
	}

	public static ThreadLocal<ArrayList<SqlAndParameters>> getSqlBatchCache() {
		return sqlBatchCache;
	}

	public static ThreadLocal<String> getSqlForBatch() {
		return sqlBatchString;
	}

	public static void cacheSQL(String... sql) {
		SqlAndParameters sp = SQLHelper.splitSQLandParameters(sql);
		sqlBatchCache.get().add(sp);
		sqlBatchString.set(sp.getSql());
	}

	public static void clearLastSQL() {
		sqlCache.get().clear();
	}

	public static void clearBatchSQLs() {
		sqlCache.get().clear();
		sqlBatchCache.get().clear();
		sqlBatchString.set("");
	}

	public static String q(Object... obj) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < obj.length; i++) {
			sqlCache.get().add("" + obj[i]);
			if (i != obj.length - 1)
				result.append("?,");
			else
				result.append("?");
		}
		return result.toString();
	}

	public static String e(Object... obj) {
		for (Object o : obj)
			sqlCache.get().add("" + o);
		return "";
	}

	public static SqlAndParameters splitSQLandParameters(String[] sqls) {
		StringBuilder sql = new StringBuilder("");
		for (String str : sqls) {
			sql.append(str);
		}
		SqlAndParameters sp = new SqlAndParameters();
		ArrayList<String> list = sqlCache.get();
		sp.setParameters(list.toArray(new String[list.size()]));
		sp.setSql(sql.toString());
		clearLastSQL();
		return sp;
	}

	public static List<List<SqlAndParameters>> getSQLandParameterSubList() {
		return subList(sqlBatchCache.get(), 500);
	}

	/**
	 * SubList a List, divide a list by given blockSize
	 */
	private static <T> List<List<T>> subList(List<T> list, int blockSize) {
		List<List<T>> lists = new ArrayList<>();
		if (list != null && blockSize > 0) {
			int listSize = list.size();
			if (listSize <= blockSize) {
				lists.add(list);
				return lists;
			}
			int batchSize = listSize / blockSize;
			int remain = listSize % blockSize;
			for (int i = 0; i < batchSize; i++) {
				int fromIndex = i * blockSize;
				int toIndex = fromIndex + blockSize;
				lists.add(list.subList(fromIndex, toIndex));
			}
			if (remain > 0)
				lists.add(list.subList(listSize - remain, listSize));
		}
		return lists;
	}
}
