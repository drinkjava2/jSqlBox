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

import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

/**
 * JDBPRO store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public abstract class JDBPRO {
	public static final SqlOption USE_MASTER = SqlOption.USE_MASTER;
	public static final SqlOption USE_SLAVE = SqlOption.USE_SLAVE;
	public static final SqlOption USE_BOTH = SqlOption.USE_BOTH;
	public static final SqlOption USE_AUTO = SqlOption.USE_AUTO;
	public static final SqlTemplateEngine TEMPLATE = BasicSqlTemplate.instance();

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
	public static SqlItem ques(Object... parameters) {// NOSONAR
		return new SqlItem(SqlOption.QUESTION_PARAM, parameters);
	}

	/**
	 * Cache parameters and return a "?" String
	 */
	public static SqlItem question(Object... parameters) {
		return new SqlItem(SqlOption.QUESTION_PARAM, parameters);
	}

	/**
	 * If last param is not null, then add all items in SQL<br/>
	 * Example: query("select * from a where 1=1",notNull(" and name=?",name));
	 */
	public static SqlItem notNull(Object... items) {
		return new SqlItem(SqlOption.NOT_NULL, items);
	}

	/**
	 * If no any param is null, then add all items in SQL<br/>
	 * Example: query("select * from a where 1=1",noNull("and name like
	 * ?","%",name,"%"));
	 */
	public static SqlItem noNull(Object... args) {
		if (args.length <= 2)
			return notNull(args);
		for (int i = 1; i <= args.length - 1; i++)
			if (args[i] == null)
				return notNull(null, null);
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= args.length - 1; i++)
			sb.append(args[i]);
		return notNull(args[0], sb.toString());
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
	public static SqlItem bind(Object... parameters) {
		return new SqlItem(SqlOption.BIND, parameters);
	}

	/**
	 * Create a SqlOption.IOC_OBJECT type SqlItem instance, args will create
	 * instance by IocTool
	 */
	public static SqlItem disableHandlers(Class<?>... args) {
		return new SqlItem(SqlOption.DISABLE_HANDLERS, (Object[]) args);
	}
}
