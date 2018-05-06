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

import com.github.drinkjava2.jdbpro.IocTool;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handlers;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.compiler.DynamicCompileEngine;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * Store some public static methods of Guesser
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class SqlMapperUtils {// NOSONAR

	private static final Map<String, String[]> methodParamNamesCache = new ConcurrentHashMap<String, String[]>();
	private static final Map<String, PreparedSQL> methodSQLCache = new ConcurrentHashMap<String, PreparedSQL>();

	/**
	 * This is the method body to build an instance based on abstract class which
	 * extended from ActiveRecord or implemented ActiveRecordSupport
	 * 
	 * @param activeClass
	 * @return Object instance
	 */
	public static Class<?> createChildClass(Class<?> abstractClass) {
		String fullClassName = abstractClass.getName() + "_Child";
		Class<?> check = ClassCacheUtils.checkClassExist(fullClassName);
		if (check != null)
			return check;
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
		String heading = StrUtils.substringAfter(body, "public abstract ");// NOSONAR
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
		ClassCacheUtils.registerClass(childClass);
		TextUtils.javaFileCache.put(fullClassName, childClassSrc);
		return childClass;
	}

	/** Automatically guess the sqlHandlers */
	public static void autoGuessHandler(Object entity, PreparedSQL ps, String sql, Method method) {
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

	/** Get the PreparedSQL from a abstract method, but do not set parameters */
	public static PreparedSQL getPreparedSqlAndHandles(String callerClassName, Method callerMethod, IocTool iocTool) {// NOSONAR
		// key is only inside used by cache
		String key = callerClassName + "@#$^!" + callerMethod.getName();
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
					result.addHandler(claz, iocTool);
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
				throw new SqlBoxException("Method '" + callerMethod.getName() + "' in '" + callerClassName
						+ "' have no Sql annotation or text.");
			}
			sql = StrUtils.substringAfter(src, callerMethod.getName() + "(");
			sql = StrUtils.substringBetween(sql, "/*-", "*/");
		}
		if (sql != null)
			sql = sql.trim();
		result.setSql(sql);
		methodSQLCache.put(key, result);
		return result.newCopy();
	}

	public static Map<String, Object> buildParamMap(String callerClassName, String callerMethodName, Object... params) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] methodParamNames = getMethodParamNames(callerClassName, callerMethodName);
		if (methodParamNames != null && methodParamNames.length > 0) {
			for (int i = 0; i < methodParamNames.length; i++) {
				map.put(methodParamNames[i], params[i]);
			}
		}
		return map;
	}

	public static String[] getMethodParamNames(String classFullName, String callerMethodName) {
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