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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;
import com.github.drinkjava2.jdbpro.SqlType;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handlers;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.compiler.DynamicCompileEngine;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * Store some public static methods for ActiveRecord
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public abstract class ActiveRecordUtils extends ClassCacheUtils {
	private ActiveRecordUtils() {
		// private Constructor
	}

	private static final Map<String, String[]> methodParamNamesCache = new ConcurrentHashMap<String, String[]>();
	private static final Map<String, PreparedSQL> methodSQLCache = new ConcurrentHashMap<String, PreparedSQL>();

	/**
	 * This is the method body to build an instance based on abstract class
	 * extended from ActiveRecord
	 * 
	 * @param activeClass
	 * @return Object instance
	 */
	public static Class<?> createChildClass(Class<?> abstractClass) {
		String fullClassName = abstractClass.getName() + "_Child";
		if (classExistCache.containsKey(fullClassName))
			return classExistCache.get(fullClassName);
		String src = TextUtils.getJavaSourceCodeUTF8(abstractClass);
		if (StrUtils.isEmpty(src))
			throw new SqlBoxException("No Java source code found for class '" + abstractClass.getName() + "'");
		StringBuilder sb = new StringBuilder();
		String start = StrUtils.substringBefore(src, "public abstract class " + abstractClass.getSimpleName());
		if (StrUtils.isEmpty(start))
			throw new SqlBoxException("AbstractActiveRecord Java source code should have a 'public abstract class "
					+ abstractClass.getSimpleName() + "' ");
		sb.append(start);
		sb.append("public class ").append(abstractClass.getSimpleName()).append("_Child extends ")
				.append(abstractClass.getSimpleName()).append("{");
		String body = StrUtils.substringAfter(src, "public abstract class " + abstractClass.getSimpleName());
		body = StrUtils.substringAfter(body, "{");
		String heading = StrUtils.substringAfter(body, "public abstract ");
		while (!StrUtils.isEmpty(heading)) {
			start = StrUtils.substringBefore(body, "public abstract ");
			sb.append(start).append("public ");
			// void someMethod(String name,int age)
			String title = StrUtils.substringBefore(heading, ";").trim();
			sb.append(title).append("{");
			if (!title.startsWith("void "))
				sb.append(" return ");
			if (title.indexOf("PreparedSQL ") > -1)
				sb.append("this.guessPreparedSQL(");
			else
				sb.append("this.guess(");
			String paramStr = StrUtils.substringAfter(title, "(");
			paramStr = StrUtils.substringBeforeLast(paramStr, ")");
			String[] params = StrUtils.split(paramStr, ',');
			for (int i = 0; i < params.length; i++) {
				if (i != 0)
					sb.append(",");
				String trimedStr = params[i].trim();
				sb.append(StrUtils.substringAfterLast(trimedStr, " "));
			}
			sb.append(");}");
			body = StrUtils.substringAfter(heading, ";");
			heading = StrUtils.substringAfter(body, "public abstract ");
		}
		sb.append(body);
		String childClassSrc = sb.toString();
		Class<?> childClass = null;
		childClass = DynamicCompileEngine.instance.javaCodeToClass(fullClassName, childClassSrc);
		classExistCache.put(fullClassName, childClass);
		TextUtils.javaFileCache.put(fullClassName, childClassSrc);
		return childClass;
	}

	/**
	 * Execute operation to access database, based on current method @Sql
	 * annotated String or Text String and parameters, guess a best fit
	 * query/update/delete/execute method to run
	 * 
	 * @param entity
	 * @param params
	 * @return <T> T
	 */
	@SuppressWarnings("all")
	protected static <T> T doGuess(ActiveRecord entity, Object... params) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.ActiveRecord".equals(stack.getClassName())
					&& "guess".equals(stack.getMethodName()))
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

		PreparedSQL ps = getPreparedSqlAndHandles(callerClassName, callerMethodName, callerMethod);
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
			ps.setTemplateParamMap(buildParamMap(callerClassName, callerMethodName, params));
		else
			ps.setParams(params);
		autoGuessHandler(entity, ps, sql, callerMethod); // guess handler
		return (T) entity.ctx().runPreparedSQL(ps);
	}

	/** Automatically guess the sqlHandlers */
	private static void autoGuessHandler(ActiveRecord entity, PreparedSQL ps, String sql, Method method) {
		if (ps.getSqlHandlers() != null && !ps.getSqlHandlers().isEmpty())
			return;
		if (ps.getResultSetHandler() != null)
			return;
		String methodType = method.getGenericReturnType().toString();
		if (sql.indexOf(".**") > -1) {
			if ("java.util.List<java.util.Map<java.lang.String, java.lang.Object>>".equals(methodType))
				ps.addSqlHandler(new SSMapListHandler(entity.getClass()));
			else if (methodType.startsWith("java.util.List"))
				ps.addSqlHandler(new EntityListHandler(entity.getClass()));
		} else {
			if ("java.util.List<java.util.Map<java.lang.String, java.lang.Object>>".equals(methodType))
				ps.setResultSetHandler(SingleTonHandlers.mapListHandler);
		}
	}

	protected static String doGuessSQL(ActiveRecord ac) {// NOSONAR
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
		PreparedSQL sp = getPreparedSqlAndHandles(callerClassName, callerMethodName, callerMethod);
		return sp.getSql();
	}

	protected static PreparedSQL doGuessPreparedSQL(ActiveRecord ac, Object... params) {// NOSONAR
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

		PreparedSQL sp = getPreparedSqlAndHandles(callerClassName, callerMethodName, callerMethod);
		sp.setParams(params);
		return sp;
	}

	/** Get the PreparedSQL from a abstract method, but do not set parameters */
	private static PreparedSQL getPreparedSqlAndHandles(String callerClassName, String callerMethodName,
			Method callerMethod) {
		// key is only inside used by cache
		String key = callerClassName + "@#$^!" + callerMethodName;
		PreparedSQL result = methodSQLCache.get(key);
		if (result != null)
			return result.newCopy();
		else
			result = new PreparedSQL();

		Annotation[] annos = callerMethod.getAnnotations();
		Sql sqlAnno = null;
		for (Annotation anno : annos) {
			if (Sql.class.equals(anno.annotationType()))
				sqlAnno = (Sql) anno;
			if (Handlers.class.equals(anno.annotationType())) {
				Class<?>[] array = ((Handlers) anno).value();
				for (Class<?> claz : array)
					result.addHandler(claz);
			}
		}
		String sql = null;
		if (sqlAnno != null)
			sql = sqlAnno.value()[0];
		else {
			String src = null;
			try {
				src = TextUtils.getJavaSourceCodeUTF8(callerClassName);
			} catch (Exception e) {
				throw new SqlBoxException("Method '" + callerMethodName + "' in '" + callerClassName
						+ "' have no Sql annotation or text.");
			}
			sql = StrUtils.substringAfter(src, callerMethodName + "(");
			sql = StrUtils.substringBetween(sql, "/*-", "*/");
		}
		if (sql != null)
			sql = sql.trim();
		result.setSql(sql);
		methodSQLCache.put(key, result);
		return result.newCopy();
	}

	private static Map<String, Object> buildParamMap(String callerClassName, String callerMethodName,
			Object... params) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] methodParamNames = getMethodParamNames(callerClassName, callerMethodName);
		if (methodParamNames != null && methodParamNames.length > 0) {
			for (int i = 0; i < methodParamNames.length; i++) {
				map.put(methodParamNames[i], params[i]);
			}
		}
		return map;
	}

	private static String[] getMethodParamNames(String classFullName, String callerMethodName) {
		String key = classFullName + "@#!$^" + callerMethodName;
		if (methodParamNamesCache.containsKey(key))
			return methodParamNamesCache.get(key);
		String src = TextUtils.getJavaSourceCodeUTF8(classFullName);
		src = StrUtils.substringBetween(src, callerMethodName + "(", "}");
		src = StrUtils.substringBetween(src, "guess(", ")");
		String[] result = StrUtils.split(src, ',');
		List<String> l = new ArrayList<String>();
		if (result != null)
			for (String str : result) {
				String trimed = str.trim();
				if (!StrUtils.isEmpty(trimed))
					l.add(trimed);
			}
		methodParamNamesCache.put(key, l.toArray(new String[l.size()]));
		return l.toArray(new String[l.size()]);
	}

}