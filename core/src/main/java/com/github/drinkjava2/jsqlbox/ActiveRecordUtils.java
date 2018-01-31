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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/**
 * Store some public static methods for ActiveRecord
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class ActiveRecordUtils {
	private static final Map<Class<?>, Class<?>> subClassImplCache = new ConcurrentHashMap<Class<?>, Class<?>>();

	/**
	 * This is the method body to build an instance based on abstract class
	 * extended from ActiveRecord
	 * 
	 * @param activeClass
	 * @return Object instance
	 */
	public static Class<?> createInstance(Class<?> abstractClass) {
		if (subClassImplCache.containsKey(abstractClass))
			return subClassImplCache.get(abstractClass);
		String src = TextUtils.getJavaSourceCodeUTF8(abstractClass);
		if (StrUtils.isEmpty(src))
			throw new SqlBoxException("No Java source code found for class '" + abstractClass.getName() + "'");
		// System.out.println(src);
		StringBuilder sb = new StringBuilder();
		String start = StrUtils.substringBefore(src, "public abstract class " + abstractClass.getSimpleName());
		if (StrUtils.isEmpty(start))
			throw new SqlBoxException("AbstractActiveRecord Java source code should have a 'public abstract class "
					+ abstractClass.getSimpleName() + "' ");
		sb.append(start);
		sb.append("public class ").append(abstractClass.getSimpleName()).append("_AutoChild extends ")
				.append(abstractClass.getSimpleName()).append("{");
		String body = StrUtils.substringAfter(src, "public abstract class " + abstractClass.getSimpleName());
		body = StrUtils.substringAfter(body, "{");
		String piece = StrUtils.substringAfter(body, "public abstract ");
		while (!StrUtils.isEmpty(piece)) {
			start = StrUtils.substringBefore(body, "public abstract ");
			sb.append(start).append("public ");
			// void someMethod(String name,int age)
			String title = StrUtils.substringBefore(piece, ";").trim();
			sb.append(title).append("{");
			if (!title.startsWith("void "))
				sb.append(" return ");
			sb.append("this.ctx().guess(");
			sb.append(");}");
			body = StrUtils.substringAfter(piece, ";");
			piece = StrUtils.substringAfter(body, "public abstract ");
		}
		sb.append(body);
		System.out.println(sb.toString());
		System.out.println(1 / 0);
		return null;
	}

	/**
	 * The real method body do the guess operation to access database, based on
	 * current method @Sql annotated String or Text String and parameters, guess
	 * a best fit query/update/delete/execute method to run
	 * 
	 * @param ac
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T doGuess(ActiveRecord ac, Object... params) {
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
		Class<?> callerClass = null;

		try {
			callerClass = Class.forName(callerClassName);
		} catch (ClassNotFoundException e) {
			throw new SqlBoxException(e);
		}
		Method callerMethod = null;
		Method[] methods = callerClass.getMethods();
		for (Method method : methods)
			if (callerMethodName != null && callerMethodName.equals(method.getName())) {
				callerMethod = method;
				break;
			}
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		Annotation[] annos = callerMethod.getAnnotations();
		Sql sqlAnno = null;
		Class<?> handlerClass = null;
		for (Annotation anno : annos) {
			if (Sql.class.equals(anno.annotationType()))
				sqlAnno = (Sql) anno;
			if (Handler.class.equals(anno.annotationType())) {
				handlerClass = ((Handler) anno).value();
			}
		}
		String sql = null;
		if (sqlAnno != null)
			sql = sqlAnno.value()[0];
		else {
			String src;
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
		char dotype;
		if (StrUtils.startsWithIgnoreCase(sql, "select"))
			dotype = 's';
		else if (StrUtils.startsWithIgnoreCase(sql, "delete"))
			dotype = 'u';
		else if (StrUtils.startsWithIgnoreCase(sql, "update"))
			dotype = 'u';
		else if (StrUtils.startsWithIgnoreCase(sql, "insert"))
			dotype = 'u';
		else
			dotype = 'e';
		switch (dotype) {
		case 's': {
			if (handlerClass == null)
				handlerClass = ScalarHandler.class;
			ResultSetHandler<T> resultSetHandler = null;
			try {
				resultSetHandler = (ResultSetHandler<T>) handlerClass.newInstance();
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
			return ac.ctx().nQuery(resultSetHandler, sql, params);
		}
		case 'u': {
			Object o = ac.ctx().nUpdate(sql, params);
			return (T) o;
		}
		default:
			if (handlerClass == null) {
				Object o = ac.ctx().nExecute(sql, params);
				return (T) o;
			}
			ResultSetHandler<T> resultSetHandler = null;
			try {
				resultSetHandler = (ResultSetHandler<T>) handlerClass.newInstance();
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
			Object o = ac.ctx().nExecute(resultSetHandler, sql, params);
			return (T) o;
		}
	}

}