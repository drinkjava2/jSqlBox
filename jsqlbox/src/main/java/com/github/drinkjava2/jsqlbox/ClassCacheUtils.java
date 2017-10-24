/*
 * Copyright (C) 2016 Yong Zhu.
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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
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

import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;

/**
 * SqlBoxUtility is utility class to store some public methods concern to SqlBox
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public abstract class ClassCacheUtils {
	// To check if a class exist, if exist, cache it to avoid check again
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<String, Integer>();
	private static Map<Class<?>, Map<String, Method>> classReadMethods = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
	private static Map<Class<?>, Map<String, Method>> classWriteMethods = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
	private static Map<Class<?>, Field> boxFieldCache = new ConcurrentHashMap<Class<?>, Field>();

	/** * Check class if exist */
	public static Class<?> checkClassExist(String className) {
		Integer i = classExistCache.get(className);
		if (i == null)
			try {
				Class<?> clazz = Class.forName(className);
				if (clazz != null) {
					classExistCache.put(className, 1);
					return clazz;
				}
				classExistCache.put(className, 0);
				return null;
			} catch (Exception e) {
				SqlBoxException.eatException(e);
				classExistCache.put(className, 0);
				return null;
			}
		if (1 == i) {
			try {
				return Class.forName(className);
			} catch (Exception e) {
				SqlBoxException.eatException(e);
			}
		}
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
			DialectException.throwEX(e, "Class '" + clazz + "' can not get bean info");
		}

		Map<String, Method> readMethods = new HashMap<String, Method>();
		Map<String, Method> writeMethods = new HashMap<String, Method>();
		for (PropertyDescriptor pd : pds) {
			String fieldName = pd.getName();
			if ("class".equals(fieldName) || "simpleName".equals(fieldName) || "canonicalName".equals(fieldName)
					|| "box".equals(fieldName))
				continue;
			readMethods.put(fieldName, pd.getReadMethod());
			writeMethods.put(fieldName, pd.getWriteMethod());
		}
		classReadMethods.put(clazz, sortMap(readMethods));
		classWriteMethods.put(clazz, sortMap(writeMethods));
		if (!ActiveRecordSupport.class.isAssignableFrom(clazz)) {
			Field boxField = ReflectionUtils.findField(clazz, "box");
			if (boxField != null) {
				ReflectionUtils.makeAccessible(boxField);
				boxFieldCache.put(clazz, boxField);
			}
		}
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

	/**
	 * Return field box, this method is used for jSqlBox to directly access box
	 * field to bind SqlBox instance to a entity with its box field
	 */
	public static Field getBoxField(Class<?> clazz) {
		Map<String, Method> writeMethods = classWriteMethods.get(clazz);
		if (writeMethods == null)  
			cacheReadWriteMethodsAndBoxField(clazz);
		return boxFieldCache.get(clazz);
	}

}
