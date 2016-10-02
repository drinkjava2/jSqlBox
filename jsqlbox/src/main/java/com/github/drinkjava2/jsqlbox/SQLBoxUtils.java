package com.github.drinkjava2.jsqlbox;

import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.cglib3_2_0.proxy.Enhancer;

@SuppressWarnings("unchecked")
public class SQLBoxUtils {
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<String, Integer>();

	public static boolean isEmptyStr(String str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Transfer all Exceptions to RuntimeException. The only place throw Exception in this project
	 */
	public static void throwEX(Exception e, String errorMsg) throws AssertionError {
		if (e != null)
			e.printStackTrace();
		throw new RuntimeException(errorMsg);
	}

	public static Object createProxyPO(Class<?> clazz, SQLBox sqlboxClass, SQLBoxContext context) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(new ProxyPO(clazz, sqlboxClass,context));
		return enhancer.create();
	}

	public static Object findID(Object po, SQLBox sqlbox) {
		return null;
	}

	public static Class<SQLBox> findSQLBoxClass(Class<?> fieldClass) {
		Class<?> box = null;
		{
			if (fieldClass == null)
				SQLBoxUtils.throwEX(null, "SQLBoxUtils getBeanBox error! target class not set");
			if (SQLBox.class.isAssignableFrom(fieldClass))
				box = fieldClass;
			if (box == null)
				box = SQLBoxUtils.checkIfExist(fieldClass.getName() + SQLBox.SQLBoxIdentity);// #5
			if (box == null)
				box = SQLBoxUtils
						.checkIfExist(fieldClass.getName() + "$" + fieldClass.getSimpleName() + SQLBox.SQLBoxIdentity);// #6
		}
		return (Class<SQLBox>) box;
	}

	public static Class<?> checkIfExist(String className) {
		Integer i = classExistCache.get(className);
		if (i == null)
			try {
				Class<?> clazz = Class.forName(className);
				if (SQLBox.class.isAssignableFrom((Class<?>) clazz)) {
					classExistCache.put(className, 1);
					return clazz;
				}
				classExistCache.put(className, 0);
				return null;
			} catch (Throwable e) {
				classExistCache.put(className, 0);
				return null;
			}
		if (1 == i) {
			try {
				return Class.forName(className);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
