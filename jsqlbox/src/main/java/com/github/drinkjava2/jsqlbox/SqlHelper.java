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
 * This class is to cache parameters in threadLocal
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlHelper {
	/**
	 * For store sql and parameters in threadLocal
	 */
	private static ThreadLocal<ArrayList<String>> sqlCache = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * form Tag
	 */
	private static ThreadLocal<Boolean> inSqlTag = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * alias Tag
	 */
	private static ThreadLocal<Boolean> aliasTag = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * one Tag
	 */
	private static ThreadLocal<ArrayList<String>> one = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * many Tag
	 */
	private static ThreadLocal<ArrayList<String>> many = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * For batch store SQL and Parameters in threadLocal
	 */
	private static ThreadLocal<ArrayList<SqlAndParameters>> sqlBatchCache = new ThreadLocal<ArrayList<SqlAndParameters>>() {
		@Override
		protected ArrayList<SqlAndParameters> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * For store last batch Sql in threadLocal
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
	 * Get slectTag in Threadlocal
	 */
	public static ThreadLocal<Boolean> getAliasTag() {
		return aliasTag;
	}

	/**
	 * Get inSqlTag in Threadlocal
	 */
	public static ThreadLocal<Boolean> getInSqlTag() {
		return inSqlTag;
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
	public static String clear() {
		sqlCache.get().clear();
		inSqlTag.set(false);
		aliasTag.set(false);
		one.get().clear();
		many.get().clear();
		return "";
	}

	/**
	 * Clear batch SQL cached in threadlocal
	 */
	public static void clearBatchSQLs() {
		clear();
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
	 * Return empty String "" but set a alias tag in ThreadLocal
	 */
	public static String alias() {
		aliasTag.set(true);
		return "";
	}

	/**
	 * Return String "" but cancel the alias tag in ThreadLocal
	 */
	public static String endAlias() {
		aliasTag.set(false);
		return "";
	}

	/**
	 * @return "" but set a inSQL tag in ThreadLocal
	 */
	public static String sql() {
		inSqlTag.set(true);
		return "";
	}

	/**
	 * Equal to " select "+ sql() + alias()
	 */
	public static String selectAlias() {
		sql();
		alias();
		return " select ";
	}

	/**
	 * Return Empty String "" but set a "one" tag
	 */
	public static String one(String columnName) {
		one.get().add(columnName);
		return columnName;
	}

	/**
	 * Return Empty String "" but set a "many" tag
	 */
	public static String many(String columnName) {
		many.get().add(columnName);
		return columnName;
	}

	/**
	 * Join parameters together and seperated with comma
	 */
	public static String comma(Object... parameters) {
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
		clear();
		return sp;
	}

	/**
	 * Get cached sql and parameters from threadlocal, return a sublisted SqlAndParameters list
	 */
	public static List<List<SqlAndParameters>> getSQLandParameterSubList() {
		return subList(sqlBatchCache.get(), 500);
	}

	/**
	 * Create " value(?,?,?,?)" strings according how many sql parameters be cached in ThreadLocal
	 */
	public static String valuesAndQuestions() {
		return createValueString(sqlCache.get().size());
	}

	/**
	 * Create " value(?,?,?,?)" string by given howManyQuestionMark quantity
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
	private static <T> List<List<T>> subList(List<T> list, int blockSize) {// NOSONAR
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
