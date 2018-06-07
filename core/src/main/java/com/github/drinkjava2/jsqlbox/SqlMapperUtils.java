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

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.annotation.Ioc;
import com.github.drinkjava2.jsqlbox.annotation.New;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.compiler.DynamicCompileEngine;

/**
 * Store some public static methods of Guesser
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class SqlMapperUtils {// NOSONAR
	public static final String CHILD_SUFFIX="_Child";

	private static final Map<String, String[]> methodParamNamesCache = new ConcurrentHashMap<String, String[]>();

	/**
	 * This is the method body to build an instance based on abstract class
	 * which extended from ActiveRecord or implemented ActiveRecordSupport
	 * 
	 * @param activeClass
	 * @return Object instance
	 */
	public static Class<?> createChildClass(Class<?> abstractClass) {
		String fullClassName = abstractClass.getName() + CHILD_SUFFIX;
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
				String pm=StrUtils.substringAfterLast(trimedStr, " ");
				sb.append("bind(\"").append(pm).append("\", ").append(pm).append(")");
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

	/** Get method name String[], this method only works for Text support case, i.e., put java in resouce folder */
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

	/** Get the sql from @Sql annotation or text */
	public static String getSqlOfMethod(String callerClassName, Method callerMethod) {// NOSONAR
		Annotation[] annos = callerMethod.getAnnotations();
		String sql = null;
		for (Annotation anno : annos)
			if (Sql.class.equals(anno.annotationType())) {
				sql = ((Sql) anno).value()[0];
				break;
			}
		if (sql == null) {
			String src = null;
			try {
				src = TextUtils.getJavaSourceCodeUTF8(callerClassName);
				sql = StrUtils.substringAfter(src, callerMethod.getName() + "(");
				sql = StrUtils.substringBetween(sql, "/*-", "*/");
			} catch (Exception e) {// NOSONAR
			}
		}
		if (sql != null)
			sql = sql.trim();
		return sql;
	}

	/** Get the @New and @Ioc annotation values */
	public static Class<?>[] getNewOrIocAnnotation(Class<?> annotation, Method callerMethod) {// NOSONAR
		List<Object> result = new ArrayList<Object>();
		Annotation[] annos = callerMethod.getAnnotations();
		for (Annotation anno : annos)
			if (annotation.equals(anno.annotationType())) {
				if (New.class.equals(annotation)) {
					Class<?>[] array = ((New) anno).value();
					for (Class<?> claz : array)
						result.add(claz);
				} else {
					Class<?>[] array = ((Ioc) anno).value();
					for (Class<?> claz : array)
						result.add(claz);
				}
			}
		return result.toArray(new Class<?>[result.size()]);
	}

}