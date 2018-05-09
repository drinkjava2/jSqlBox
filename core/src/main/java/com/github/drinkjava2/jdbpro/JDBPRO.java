/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro;

import java.util.ArrayList;

/**
 * JDBPRO store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public abstract class JDBPRO extends DbPro {
	public static final SqlOption USE_MASTER = SqlOption.USE_MASTER;
	public static final SqlOption USE_SLAVE = SqlOption.USE_SLAVE;
	public static final SqlOption USE_BOTH = SqlOption.USE_BOTH;
	public static final SqlOption USE_AUTO = SqlOption.USE_AUTO;

	protected void ________INLINE_Methods________() {// NOSONAR
	}

	/** ThreadLocal variant cache INLINE methods */
	private static ThreadLocal<ArrayList<Object>> INLINE_PARAM_CACHE = new ThreadLocal<ArrayList<Object>>() {// NOSONAR
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<Object>();
		}
	};

	/**
	 * Clear all ThreadLocal parameters first, then cache parameters in ThreadLocal
	 * and return an empty String, a non parameter param0() call equal to
	 * clearAllInlineThreadlocalParams()
	 */
	public static String PARA0(Object... params) {// NOSONAR
		INLINE_PARAM_CACHE.get().clear();
		return PARA(params);
	}

	/** Cache parameters in ThreadLocal and return an empty String */
	public static String PARA(Object... params) {// NOSONAR
		for (Object o : params)
			INLINE_PARAM_CACHE.get().add(o);
		return "";
	}

	/**
	 * Build a PreparedSQL instance by given in-line style SQL and parameters stored
	 * in ThreadLocal
	 * 
	 * @param inlineSQL
	 * @return PreparedSQL instance
	 */
	public static Object[] PARAMS() {// NOSONAR
		try {
			return INLINE_PARAM_CACHE.get().toArray();
		} finally {
			INLINE_PARAM_CACHE.get().clear();
		}
	}

	/**
	 * Clear all ThreadLocal parameters first, then cache parameters in ThreadLocal,
	 * then return a "?" String
	 */
	public static String QUES0(Object... params) {// NOSONAR
		INLINE_PARAM_CACHE.get().clear();
		return QUES(params);
	}

	/** Cache parameters in ThreadLocal and return a "?" String */
	public static String QUES(Object... params) {// NOSONAR
		for (Object o : params)
			INLINE_PARAM_CACHE.get().add(o);
		return "?";
	}

	/**
	 * Create "values(?,?,?...,?)" String according how many SQL parameters be
	 * cached in ThreadLocal
	 */
	public static String VALUESQUES() {// NOSONAR
		int howManyQuestionMarks = INLINE_PARAM_CACHE.get().size();
		StringBuilder sb = new StringBuilder("values(");
		for (int i = 0; i < howManyQuestionMarks; i++) {
			if (i != howManyQuestionMarks - 1)
				sb.append("?,");
			else
				sb.append("?");
		}
		sb.append(")");
		return sb.toString();
	}

	protected void ________SqlItem_Methods________() {// NOSONAR
	}

	/** Return a SqlItemType.PARAM type SqlItem instance */
	public static SqlItem param(Object... parameters) {
		return new SqlItem(SqlOption.PARAM, parameters);
	}

	/**
	 * Cache parameters and return an empty String
	 */
	public static SqlItem sql(Object... parameters) {
		return new SqlItem(SqlOption.SQL, parameters);
	}

	/**
	 * Cache parameters and return a "?" String
	 */
	public static SqlItem question(Object... parameters) {
		return new SqlItem(SqlOption.QUESTION_PARAM, parameters);
	}

	/**
	 * If param is not null, then append sqlPiece
	 * 
	 * @param sqlPiece
	 *            The SQL piece will be appended
	 * @param param
	 *            The param
	 * @return a SqlItem instance will be used by iPrepare method
	 */
	public static SqlItem notNull(String sqlPiece, Object param) {
		return new SqlItem(SqlOption.NOT_NULL, sqlPiece, param);
	}

	/**
	 * Create "values(?,?,?...,?)" String according how many SQL parameters be
	 * cached
	 */
	public static SqlItem valuesQuestions() {
		return new SqlItem(SqlOption.VALUES_QUESTIONS);
	}

	/**
	 * Switch to another DbPro
	 */
	public static SqlItem switchTo(DbPro dpPro) {
		return new SqlItem(SqlOption.SWITCHTO, dpPro);
	}

	
	
	/**
	 * For tXxxx style templateEngine use, return a SqlItemType.PUT type SqlItem
	 * instance,
	 * 
	 * Usage: put("key1",value1,"key2",value2...);
	 */
	public static SqlItem put(Object... parameters) {
		return new SqlItem(SqlOption.PUT, parameters);
	}

}
