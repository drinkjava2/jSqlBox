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
/**
 * 
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
	 * inSelect Tag
	 */
	private static ThreadLocal<Boolean> inSelectTag = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * inAlias Tag
	 */
	private static ThreadLocal<Boolean> inAliasTag = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
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
	 * Get inSlectTag in Threadlocal
	 */
	public static Boolean getInSelectTag() {
		return inSelectTag.get();
	}

	/**
	 * Get inAliasTag in Threadlocal
	 */
	public static Boolean getInAliasTag() {
		return inAliasTag.get();
	}

	/**
	 * Get inSqlTag in Threadlocal
	 */
	public static Boolean getInSqlTag() {
		return inSqlTag.get();
	}

	/**
	 * Cache SQL in threadlocal
	 */
	public static void cacheSQL(String... sql) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(sql);
		sqlBatchCache.get().add(sp);
		sqlBatchString.set(sp.getSql());
	}

	/**
	 * Clear last SQL cached in threadlocal
	 */
	public static String clear() {
		sqlCache.get().clear();
		inSqlTag.set(false);
		inSelectTag.set(false);
		return "";
	}

	/**
	 * Clear batch SQL cached in threadlocal
	 */
	protected static void clearBatchSQLs() {
		clear();
		sqlBatchCache.get().clear();
	}

	protected static void clearBatchSQLStringOnly() {
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
	 * Return a empty string and cache parameters in thread local for SQL use
	 */
	public static String empty(Object... parameters) {
		for (Object o : parameters)
			sqlCache.get().add("" + o);
		return "";
	}

	/**
	 * Merely return a empty string but do not cache parameters for SQL
	 */
	public static String use(Object... parameters) {
		if (parameters == null)
			return "";
		return "";
	}

	/**
	 * Return String "" but cancel the inSelectTag tag in ThreadLocal
	 */
	public static String selectEnd() {
		inSelectTag.set(false);
		return "";
	}

	/**
	 * Return empty String "", set a alias tag in ThreadLocal
	 */
	public static String aliasBegin() {
		inAliasTag.set(true);
		return "";
	}

	/**
	 * Return String "", cancel the alias tag in ThreadLocal
	 */
	public static String aliasEnd() {
		inAliasTag.set(false);
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
	 * Equal to " select "+ sql() + selectBegin()
	 */
	public static String select() {
		inSqlTag.set(true);
		inSelectTag.set(true);
		return " select ";
	}

	/**
	 * Equal to " from "+ sql() + selectEnd()
	 */
	public static String from() {
		selectEnd();
		return " from ";
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
	public static SqlAndParameters prepareSQLandParameters(String... sqls) {
		try {
			StringBuilder sb = new StringBuilder("");
			for (String str : sqls) {
				sb.append(str);
			}
			if (SqlBoxContext.paginationEndCache.get() != null)
				sb.append(SqlBoxContext.paginationEndCache.get());
			SqlAndParameters sp = new SqlAndParameters();
			String sql = sb.toString();
			if (SqlBoxContext.paginationOrderByCache.get() != null)
				sql = sql.replaceFirst("__ORDERBY__", SqlBoxContext.paginationOrderByCache.get());
			sp.setSql(sql);

			ArrayList<String> list = sqlCache.get();
			sp.setParameters(list.toArray(new String[list.size()]));

			// get the mapping config in SQL
			List<Mapping> mappings = new ArrayList<>(MappingHelper.getMappingListCache());
			sp.setMappingList(mappings);

			List<Entity> templates = new ArrayList<>(MappingHelper.getEntityTemplates());
			sp.setEntityTemplates(templates);

			// join mapping configs stored in entity templates to mappings
			for (Entity entity : templates) {
				if (entity.box().getConfigMappings() != null)
					for (Mapping mp : entity.box().getConfigMappings())
						mappings.add(mp);
			}

			return sp;
		} finally {
			SqlHelper.clear();
			MappingHelper.clearAllMappingCached();
			SqlBoxContext.paginationEndCache.set(null);
			SqlBoxContext.paginationOrderByCache.set(null);

		}
	}

	/**
	 * Get cached sql and parameters from threadlocal, return a sublisted SqlAndParameters list
	 */
	public static List<List<SqlAndParameters>> getAndClearBatchSQLs() {
		try {
			return subList(sqlBatchCache.get(), 500);
		} finally {
			clearBatchSQLs();
		}
	}

	/**
	 * Get cached sql and parameters from threadlocal, return a sublisted SqlAndParameters list
	 */
	public static String getAndClearBatchSqlString() {
		try {
			return sqlBatchString.get();
		} finally {
			clearBatchSQLStringOnly();
		}
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
	private static <T> List<List<T>> subList(List<T> srcList, int blockSize) {// NOSONAR
		List<T> copiedList = new ArrayList<>(srcList); // srcList not safe it stored in ThreadLocal
		List<List<T>> resultList = new ArrayList<>();
		if (blockSize > 0) {
			int listSize = copiedList.size();
			if (listSize <= blockSize) {
				resultList.add(copiedList);
				return resultList;
			}
			int batchSize = listSize / blockSize;
			int remain = listSize % blockSize;
			for (int i = 0; i < batchSize; i++) {
				int fromIndex = i * blockSize;
				int toIndex = fromIndex + blockSize;
				resultList.add(copiedList.subList(fromIndex, toIndex));
			}
			if (remain > 0)
				resultList.add(copiedList.subList(listSize - remain, listSize));
		}
		return resultList;
	}

}
