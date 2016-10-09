package com.github.drinkjava2.jsqlbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class SQLBoxUtils {
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<String, Integer>();

	public static boolean isEmptyStr(String str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Transfer all Exceptions to RuntimeException. The only place throw Exception in this project
	 */
	public static void throwEX(Exception e, String errorMsg) throws SQLBoxException {
		if (e != null)
			e.printStackTrace();
		throw new SQLBoxException(errorMsg);
	}

	public static Class<Dao> findDaoClass(Class<?> fieldClass, SQLBoxContext context) {
		Class<?> box = null;
		{
			if (fieldClass == null)
				SQLBoxUtils.throwEX(null, "SQLBoxUtils findDaoClass error! target class not set");
			if (Dao.class.isAssignableFrom(fieldClass))
				box = fieldClass;
			if (box == null)
				box = SQLBoxUtils.checkIfExist(fieldClass.getName() + SQLBoxContext.daoIdentity);
			if (box == null)
				box = SQLBoxUtils.checkIfExist(
						fieldClass.getName() + "$" + fieldClass.getSimpleName() + SQLBoxContext.daoIdentity);
		}
		return (Class<Dao>) box;
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

	public static Dao findDao(Class<?> clazz, SQLBoxContext context) {
		Class<Dao> daoClass = SQLBoxUtils.findDaoClass(clazz, context);
		if (daoClass != null)
			try {
				Dao box = daoClass.newInstance().setContext(context);
				return box;
			} catch (Exception e) {
				SQLBoxUtils.throwEX(e, "SQLBoxUtils findDao error, clazz=" + clazz);
			}
		Dao dao = new Dao().setBeanClass(clazz).setContext(context);
		return dao;
	}

	/**
	 * SubList a List, divide a list by given blockSize
	 */
	public static <T> List<List<T>> subList(List<T> list, int blockSize) {
		List<List<T>> lists = new ArrayList<List<T>>();
		if (list != null && blockSize > 0) {
			int listSize = list.size();
			if (listSize <= blockSize) {
				lists.add(list);
				return lists;
			}
			int batchSize = listSize / blockSize;
			int remain = listSize % blockSize;
			for (int i = 0; i < batchSize; i++) {
				int fromIndex = i * blockSize;
				int toIndex = fromIndex + blockSize;
				lists.add(list.subList(fromIndex, toIndex));
			}
			if (remain > 0)
				lists.add(list.subList(listSize - remain, listSize));
		}
		return lists;
	}

}
