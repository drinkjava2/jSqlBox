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

package com.github.drinkjava2.jsqlbox;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is to cache parameters in threadlocal
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlHelper {

	/**
	 * For store sql and parameters in threadlocal
	 */
	private static ThreadLocal<ArrayList<String>> sqlCache = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * For batch store SQL and Parameters in threadlocal
	 */
	private static ThreadLocal<ArrayList<SqlAndParameters>> sqlBatchCache = new ThreadLocal<ArrayList<SqlAndParameters>>() {
		@Override
		protected ArrayList<SqlAndParameters> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * For store last batch Sql in threadlocal
	 */
	private static ThreadLocal<String> sqlBatchString = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";
		}
	};

	private SqlHelper() {// Disable default public constructor
	}

	/**
	 * Get SQL cached in Threadlocal
	 */
	public static ThreadLocal<ArrayList<String>> getSqlCache() {
		return sqlCache;
	}

	/**
	 * Get SQL batch cached in threadlocal
	 */
	public static ThreadLocal<ArrayList<SqlAndParameters>> getSqlBatchCache() {
		return sqlBatchCache;
	}

	/**
	 * Get last SQL for batch cached in threadlocal
	 */
	public static ThreadLocal<String> getSqlForBatch() {
		return sqlBatchString;
	}

	/**
	 * Cache SQL in threadlocal
	 */
	public static void cacheSQL(String... sql) {
		SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
		sqlBatchCache.get().add(sp);
		sqlBatchString.set(sp.getSql());
	}

	/**
	 * Clear last SQL cached in threadlocal
	 */
	public static void clearLastSQL() {
		sqlCache.get().clear();
	}

	/**
	 * Clear batch SQL cached in threadlocal
	 */
	public static void clearBatchSQLs() {
		sqlCache.get().clear();
		sqlBatchCache.get().clear();
		sqlBatchString.set("");
	}

	/**
	 * Cache parameters in thread local and return a question mark "?" string
	 */
	public static String q(Object... parameters) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < parameters.length; i++) {
			sqlCache.get().add("" + parameters[i]);
			if (i != parameters.length - 1)
				result.append("?,");
			else
				result.append("?");
		}
		return result.toString();
	}

	/**
	 * Cache parameters in thread local and return a empty string
	 */
	public static String empty(Object... parameters) {
		for (Object o : parameters)
			sqlCache.get().add("" + o);
		return "";
	}

	/**
	 * Link parameters together only but do not cache it
	 */
	public static String link(Object... parameters) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < parameters.length; i++) {
			if (i != parameters.length - 1)
				result.append("" + parameters[i] + ",");
			else
				result.append("" + parameters[i]);
		}
		return result.toString();
	}

	/**
	 * Get cached sql and parameters from threadlocal, return a SqlAndParameters instance
	 */
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

	/**
	 * Get cached sql and parameters from threadlocal, return a sublisted SqlAndParameters list
	 */
	public static List<List<SqlAndParameters>> getSQLandParameterSubList() {
		return subList(sqlBatchCache.get(), 500);
	}

	public static String questionMarks() {
		return createValueString(sqlCache.get().size());
	}

	/**
	 * Create "value(?,?,?,?)" string by given howManyQuestionMark
	 */
	public static String createValueString(int howManyQuestionMark) {
		StringBuilder sb = new StringBuilder(" values(");
		for (int i = 0; i < howManyQuestionMark; i++) {
			if (i != howManyQuestionMark - 1)
				sb.append("?,");
			else
				sb.append("?");
		}
		sb.append(")");
		return sb.toString();
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
