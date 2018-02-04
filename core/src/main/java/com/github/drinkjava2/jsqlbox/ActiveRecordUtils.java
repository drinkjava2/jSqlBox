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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.inline.SqlAndParams;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.compiler.DynamicCompileEngine;

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
	private static final Map<String, SqlAndParams> methodSQLCache = new ConcurrentHashMap<String, SqlAndParams>();

	/**
	 * This is the method body to build an instance based on abstract class extended
	 * from ActiveRecord
	 * 
	 * @param activeClass
	 * @return Object instance
	 */
	public static Class<?> createChildClass(Class<?> abstractClass) {
		String fullClassName = abstractClass.getName() + "_AutoChild";
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
		sb.append("public class ").append(abstractClass.getSimpleName()).append("_AutoChild extends ")
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
	 * Execute operation to access database, based on current method @Sql annotated
	 * String or Text String and parameters, guess a best fit
	 * query/update/delete/execute method to run
	 * 
	 * @param ac
	 * @param params
	 * @return <T> T
	 */
	@SuppressWarnings("all")
	protected static <T> T doGuess(ActiveRecord ac, Object... params) {// NOSONAR
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

		SqlAndParams sp = getSQLbyClassAndMethodName(callerClassName, callerMethodName, callerMethod);
		String sql = sp.getSql();
		Class<?> handlerClass = sp.getHandlerClass();
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
		boolean hasQuestionMark = sql.indexOf('?') > -1;
		boolean hasColon = sql.indexOf(':') > -1;
		boolean useTemplate = false;
		if (hasColon && !hasQuestionMark)
			useTemplate = true;
		if (hasColon && hasQuestionMark)
			throw new SqlBoxException(
					"Here ActiveRecord's guess() method can not determine use template or normal style for SQL '" + sql
							+ "', please do operation manual.");
		Map<String, Object> map = null;
		if (useTemplate)
			map = buildParamMap(callerClassName, callerMethodName, params);
		Object o = null;
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
			if (useTemplate)
				return ac.ctx().tQuery(map, resultSetHandler, sql);
			else
				return ac.ctx().nQuery(resultSetHandler, sql, params);
		}
		case 'u': {
			if (useTemplate)
				o = ac.ctx().tUpdate(map, sql);
			else
				o = ac.ctx().nUpdate(sql, params);
			return (T) o;
		}
		default:
			if (handlerClass == null) {
				if (useTemplate)
					o = ac.ctx().tExecute(map, sql);
				else
					o = ac.ctx().nExecute(sql, params);
				return (T) o;
			}
			ResultSetHandler<T> resultSetHandler = null;
			try {
				resultSetHandler = (ResultSetHandler<T>) handlerClass.newInstance();
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
			if (useTemplate)
				o = ac.ctx().tExecute(map, resultSetHandler, sql);
			else
				o = ac.ctx().nExecute(resultSetHandler, sql, params);
			return (T) o;
		}
	}

	private static SqlAndParams getSQLbyClassAndMethodName(String callerClassName, String callerMethodName,
			Method callerMethod) {
		String key = callerClassName + "@#$^!" + callerMethodName;
		SqlAndParams result = methodSQLCache.get(key);
		if (result != null)
			return result;
		else
			result = new SqlAndParams();

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
		result.setHandlerClass(handlerClass);
		methodSQLCache.put(key, result);
		return result;
	}

	private static Map<String, Object> buildParamMap(String callerClassName, String callerMethodName,
			Object... params) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] methodParamNames = getMethodParamNames(callerClassName, callerMethodName);
		if (methodParamNames != null)
			for (int i = 0; i < methodParamNames.length; i++) {
				map.put(methodParamNames[i], params[i]);
			}
		return map;
	}

	private static String[] getMethodParamNames(String classFullName, String callerMethodName) {
		String key = classFullName + "+" + callerMethodName;
		if (methodParamNamesCache.containsKey(key))
			return methodParamNamesCache.get(key);
		String src = TextUtils.getJavaSourceCodeUTF8(classFullName);
		src = StrUtils.substringBetween(src, callerMethodName + "(", "}");
		src = StrUtils.substringBetween(src, ".guess(", ")");
		String[] result = StrUtils.split(src, ',');
		if (result != null)
			methodParamNamesCache.put(key, result);
		return result;
	}

}