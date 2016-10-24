package com.github.drinkjava2.jsqlbox;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLBoxUtils {
	private static final Logger log = Logger.getLogger(SQLBoxUtils.class.toString());
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<>();

	private SQLBoxUtils() {
	}

	public static boolean isEmptyStr(String str) {
		return str == null || "".equals(str);
	}

	/**
	 * Transfer all Exceptions to RuntimeException. The only place throw Exception in this project
	 */
	public static void throwEX(Exception e, String errorMsg) {
		log.log(Level.SEVERE, errorMsg, e);
		throw new SQLBoxException(errorMsg);
	}

	public static void logException(Exception e) {
		log.log(Level.WARNING, "", e);
	}

	public static Class<?> checkIfExist(String className) {
		Integer i = classExistCache.get(className);
		if (i == null)
			try {
				Class<?> clazz = Class.forName(className);
				if (Dao.class.isAssignableFrom((Class<?>) clazz)) {
					classExistCache.put(className, 1);
					return clazz;
				}
				classExistCache.put(className, 0);
				return null;
			} catch (Exception e) {
				logException(e);
				classExistCache.put(className, 0);
				return null;
			}
		if (1 == i) {
			try {
				return Class.forName(className);
			} catch (Exception e) {
				logException(e);
			}
		}
		return null;
	}

	public static boolean isCapitalizedString(String str) {
		char c = str.substring(0, 1).toCharArray()[0];
		return c >= 'A' && c <= 'Z';
	}

	public static String toFirstLetterLowerCase(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

}
