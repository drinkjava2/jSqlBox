/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlType;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * Guess and execute the SQL for a annotated ActiveRecord entity's method
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public class SqlMapperDefaultGuesser implements SqlMapperGuesser {
	public static final SqlMapperGuesser instance = new SqlMapperDefaultGuesser();

	@Override
	@SuppressWarnings("all")
	public <T> T guess(SqlBoxContext ctx, Object entity, Object... params) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.SqlMapperDefaultGuesser".equals(stack.getClassName())
					&& "guess".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos + 1].getClassName();
		String callerMethodName = stacks[callerPos + 1].getMethodName();
		Class<?> callerClass = ClassCacheUtils.checkClassExist(callerClassName);
		if (callerClass == null)
			throw new SqlBoxException("Can not find class '" + callerClassName + "'");
		Method callerMethod = ClassCacheUtils.checkMethodExist(callerClass, callerMethodName);
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		return theGuessBody(ctx, entity, callerClassName, callerMethod, params);
	}

	/**
	 * The main logic part of guess, SubClass can override this method to do
	 * different guess logic
	 * 
	 * @param ctx
	 *            The SqlBoxContext instance
	 * @param entity
	 *            The ActiveRecord entity
	 * @param callerClassName
	 *            The caller Class Name
	 * @param callerMethod
	 *            The caller Method
	 * @param params
	 *            The SQL params
	 * @return T The Result
	 */
	@SuppressWarnings("unchecked")
	protected <T> T theGuessBody(SqlBoxContext ctx, Object entity, String callerClassName, Method callerMethod,
			Object... params) {
		PreparedSQL ps = SqlMapperUtils.getPreparedSqlAndHandles(callerClassName, callerMethod, ctx.getIocTool());
		String sql = ps.getSql().trim();
		if (StrUtils.startsWithIgnoreCase(sql, "select"))
			ps.setType(SqlType.QUERY);
		else if (StrUtils.startsWithIgnoreCase(sql, "delete"))
			ps.setType(SqlType.UPDATE);
		else if (StrUtils.startsWithIgnoreCase(sql, "update"))
			ps.setType(SqlType.UPDATE);
		else if (StrUtils.startsWithIgnoreCase(sql, "insert"))
			ps.setType(SqlType.UPDATE);
		else
			ps.setType(SqlType.EXECUTE);// execute
		boolean hasQuestionMark = sql.indexOf('?') > -1;
		boolean useTemplate = sql.indexOf(':') > -1 || sql.indexOf("#{") > -1;
		ps.setUseTemplate(useTemplate);
		if (useTemplate && hasQuestionMark)
			throw new SqlBoxException(
					"guess() method can not determine use template or normal style for SQL '" + sql + "'");
		if (useTemplate)
			ps.setTemplateParamMap(SqlMapperUtils.buildParamMap(callerClassName, callerMethod.getName(), params));
		else
			ps.setParams(params);
		SqlMapperUtils.autoGuessHandler(entity, ps, sql, callerMethod); // guess handler
		return (T) ctx.runPreparedSQL(ps);
	}

	@Override
	public String guessSQL(SqlBoxContext ctx, Object ac) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.ActiveRecord".equals(stack.getClassName())
					&& "guessSQL".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos].getClassName();
		String callerMethodName = stacks[callerPos].getMethodName();
		Class<?> callerClass = ClassCacheUtils.checkClassExist(callerClassName);
		if (callerClass == null)
			throw new SqlBoxException("Can not find class '" + callerClassName + "'");
		Method callerMethod = ClassCacheUtils.checkMethodExist(callerClass, callerMethodName);
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		PreparedSQL sp = SqlMapperUtils.getPreparedSqlAndHandles(callerClassName, callerMethod, ctx.getIocTool());
		return sp.getSql();
	}

	@Override
	public PreparedSQL doGuessPreparedSQL(SqlBoxContext ctx, Object ac, Object... params) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.ActiveRecord".equals(stack.getClassName())
					&& "guessPreparedSQL".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos].getClassName();
		String callerMethodName = stacks[callerPos].getMethodName();
		Class<?> callerClass = ClassCacheUtils.checkClassExist(callerClassName);
		if (callerClass == null)
			throw new SqlBoxException("Can not find class '" + callerClassName + "'");
		Method callerMethod = ClassCacheUtils.checkMethodExist(callerClass, callerMethodName);
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");

		PreparedSQL sp = SqlMapperUtils.getPreparedSqlAndHandles(callerClassName, callerMethod, ctx.getIocTool());
		sp.setParams(params);
		return sp;
	}

}