/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxUtils {
	// Use standard JDK logger

	// To check if a class exist, if exist, cache it to avoid check again
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<>();

	private SqlBoxUtils() {
	}

	/**
	 * Return true if a String is null or ""
	 */
	public static boolean isEmptyStr(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Check class if exist
	 */
	public static Class<?> checkSqlBoxClassExist(String className) {
		Integer i = classExistCache.get(className);
		if (i == null)
			try {
				Class<?> clazz = Class.forName(className);
				if (SqlBox.class.isAssignableFrom((Class<?>) clazz)) {
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

	/**
	 * If first letter is Capitalized, return true
	 */
	public static boolean isCapitalizedString(String str) {
		char c = str.substring(0, 1).toCharArray()[0];
		return c >= 'A' && c <= 'Z';
	}

	/**
	 * Change first letter to lower case
	 */
	public static String toFirstLetterLowerCase(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * Change first letter to upper case
	 */
	public static String toFirstLetterUpperCase(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * Invoke to get field value by its fieldID
	 */
	public static String getStaticStringField(Class<?> beanClass, String fieldID) {
		try {
			Field field = beanClass.getField(fieldID);
			return (String) field.get(null);
		} catch (Exception e) {
			SqlBoxException.eatException(e);
		}
		return null;
	}

	/**
	 * Camel string change to lower case underline string, "AbcDef" to "abc_def"
	 */
	public static String camelToLowerCaseUnderline(String name) {
		StringBuilder sb = new StringBuilder();
		if (name != null && name.length() > 0) {
			sb.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				char c = s.substring(0, 1).toCharArray()[0];
				if (c >= 'A' && c <= 'Z')
					sb.append("_");
				sb.append(s.toLowerCase());
			}
		}
		return sb.toString();
	}

	/**
	 * Make the given field accessible, only called when actually necessary, to avoid unnecessary conflicts with a JVM
	 * SecurityManager (if active).
	 */
	public static void makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	/**
	 * Get JDK random type4 UUID
	 */
	public static String getHex32UUID() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

}