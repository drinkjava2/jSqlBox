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
package com.github.drinkjava2.jdialects;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;

/**
 * ClassCacheUtils is utility class to cache some info of classes read and write
 * method
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public abstract class ClassCacheUtils {// NOSONAR
	// To check if a class exist, if exist, cache it to avoid check again
	protected static ConcurrentHashMap<String, Class<?>> classExistCache = new ConcurrentHashMap<String, Class<?>>();
	protected static Map<Class<?>, Map<String, Object>> uniqueMethodCache = new ConcurrentHashMap<Class<?>, Map<String, Object>>();
	protected static Map<Class<?>, Map<String, Method>> classReadMethods = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
	protected static Map<Class<?>, Map<String, Method>> classWriteMethods = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
	// protected static Map<Class<?>, Field> boxFieldCache = new
	// ConcurrentHashMap<Class<?>, Field>();

	protected static class ClassOrMethodNotExist {// NOSONAR
	}

	/** * Check class if exist, if exit return it, otherwise return null */
	public static Class<?> checkClassExist(String className) {
		Class<?> result = classExistCache.get(className);
		if (result != null) {
			if (ClassOrMethodNotExist.class.equals(result))
				return null;
			else
				return result;
		}
		try {
			result = Class.forName(className);
			if (result != null)
				classExistCache.put(className, result);
			else
				classExistCache.put(className, ClassOrMethodNotExist.class);
			return result;
		} catch (Exception e) {
			DialectException.eatException(e);
			classExistCache.put(className, ClassOrMethodNotExist.class);
			return null;
		}
	}

	public static void registerClass(Class<?> clazz) {
		classExistCache.put(clazz.getName(), clazz);
	}

	/**
	 * Check if a unique method name exists in class, if exist return the method,
	 * otherwise return null
	 */
	public static Method checkMethodExist(Class<?> clazz, String uniqueMethodName) {
		if (clazz == null || StrUtils.isEmpty(uniqueMethodName))
			return null;
		Map<String, Object> methodMap = uniqueMethodCache.get(clazz);
		if (methodMap != null && !methodMap.isEmpty()) {
			Object result = methodMap.get(uniqueMethodName);
			if (result != null) {
				if (ClassOrMethodNotExist.class.equals(result))
					return null;
				else
					return (Method) result;
			}
		}
		if (methodMap == null) {
			methodMap = new HashMap<String, Object>();
			uniqueMethodCache.put(clazz, methodMap);
		}
		Method[] methods = clazz.getMethods();
		for (Method method : methods)
			if (uniqueMethodName != null && uniqueMethodName.equals(method.getName())) {
				methodMap.put(uniqueMethodName, method);
				return method;
			}
		methodMap.put(uniqueMethodName, ClassOrMethodNotExist.class);
		return null;
	}

	private static LinkedHashMap<String, Method> sortMap(Map<String, Method> map) {
		List<Entry<String, Method>> list = new ArrayList<Entry<String, Method>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Method>>() {
			public int compare(Entry<String, Method> o1, Entry<String, Method> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		LinkedHashMap<String, Method> result = new LinkedHashMap<String, Method>();
		for (Entry<String, Method> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static synchronized void cacheReadWriteMethodsAndBoxField(Class<?> clazz) {
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			DialectException.throwEX("Class '" + clazz + "' can not get bean info", e);
		}

		Map<String, Method> readMethods = new HashMap<String, Method>();
		Map<String, Method> writeMethods = new HashMap<String, Method>();
		for (PropertyDescriptor pd : pds) {
			String fieldName = pd.getName();
			if ("class".equals(fieldName) || "simpleName".equals(fieldName) || "canonicalName".equals(fieldName)
					|| "box".equals(fieldName))
				continue;
			Method readMtd = pd.getReadMethod();
			readMethods.put(fieldName, readMtd);
			Method writeMtd = pd.getWriteMethod();
			if (writeMtd == null) {
				writeMtd = ReflectionUtils.findMethod(clazz, "set" + StrUtils.toUpperCaseFirstOne(fieldName),
						readMtd.getReturnType());
			}
			writeMethods.put(fieldName, writeMtd);
		}
		classReadMethods.put(clazz, sortMap(readMethods));
		classWriteMethods.put(clazz, sortMap(writeMethods));
	}

	/** Return cached class read methods to avoid each time use reflect */
	public static Map<String, Method> getClassReadMethods(Class<?> clazz) {
		Map<String, Method> readMethods = classReadMethods.get(clazz);
		if (readMethods == null) {
			cacheReadWriteMethodsAndBoxField(clazz);
			return classReadMethods.get(clazz);
		} else
			return readMethods;
	}

	/** Return cached class field read method to avoid each time use reflect */
	public static Method getClassFieldReadMethod(Class<?> clazz, String fieldName) {
		return getClassReadMethods(clazz).get(fieldName);
	}

	/** Return cached class write methods to avoid each time use reflect */
	public static Map<String, Method> getClassWriteMethods(Class<?> clazz) {
		Map<String, Method> writeMethods = classWriteMethods.get(clazz);
		if (writeMethods == null) {
			cacheReadWriteMethodsAndBoxField(clazz);
			return classWriteMethods.get(clazz);
		} else
			return writeMethods;
	}

	/** Return cached class field write method to avoid each time use reflect */
	public static Method getClassFieldWriteMethod(Class<?> clazz, String fieldName) {
		return getClassWriteMethods(clazz).get(fieldName);
	}

	/** Read value from entityBean field */
	public static Object readValueFromBeanField(Object entityBean, String fieldName) {
		Method readMethod = ClassCacheUtils.getClassFieldReadMethod(entityBean.getClass(), fieldName);
		if (readMethod == null) {
			throw new DialectException(
					"No mapping found for field '" + fieldName + "' in '" + entityBean.getClass() + "'");
		} else
			try {
				return readMethod.invoke(entityBean);
			} catch (Exception e) {
				throw new DialectException(e);
			}
	}

	/** write value to entityBean field */
	public static void writeValueToBeanField(Object entityBean, String fieldName, Object value) {
		Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(entityBean.getClass(), fieldName);
		if (writeMethod == null) {
			throw new DialectException("Can not find Java bean read method '" + fieldName + "'");
		} else
			try {
				writeMethod.invoke(entityBean, value);
			} catch (Exception e) {
				throw new DialectException("FieldName '" + fieldName + "' can not write with value '" + value + "'", e);
			}
	}

	/**
	 * Create a new Object by given entityClass, if any exception happen, throw
	 * {@link DialectException}
	 */
	public static Object createNewEntity(Class<?> entityClass) {
		try {
			return entityClass.newInstance();
		} catch (Exception e) {
			throw new DialectException(e);
		}
	}

}
