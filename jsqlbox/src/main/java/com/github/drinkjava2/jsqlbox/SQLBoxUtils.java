package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.cglib3_2_0.proxy.Enhancer;

@SuppressWarnings("unchecked")
public class SQLBoxUtils {
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<String, Integer>();

	public static boolean isEmptyStr(String str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Transfer all Exceptions to RuntimeException. The only place throw
	 * Exception in this project
	 */
	public static void throwEX(Exception e, String errorMsg) throws AssertionError {
		if (e != null)
			e.printStackTrace();// I hate Log4j package conflict
		throw new RuntimeException(errorMsg);
	}

	public static Object createProxyBean(Class<?> clazz, Dao dao) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(new ProxyBean(clazz, dao));
		Object proxyBean = enhancer.create();
		try {
			Method m = clazz.getMethod("putDao", new Class[] { Dao.class });
			m.invoke(proxyBean, new Object[] { dao });
		} catch (Exception e) {
			throwEX(e, "SQLBoxUtils createProxyBean error, clazz=" + clazz);
		}
		dao.setBeanClass(clazz);
		dao.setBean(proxyBean);
		return proxyBean;
	}

	public static Object findID(Object po, Dao sqlbox) {
		return null;
	}

	public static Class<Dao> findDaoClass(Class<?> fieldClass, Context context) {
		Class<?> box = null;
		{
			if (fieldClass == null)
				SQLBoxUtils.throwEX(null, "SQLBoxUtils getBeanBox error! target class not set");
			if (Dao.class.isAssignableFrom(fieldClass))
				box = fieldClass;
			if (box == null)
				box = SQLBoxUtils.checkIfExist(fieldClass.getName() + Context.daoIdentity);
			if (box == null)
				box = SQLBoxUtils
						.checkIfExist(fieldClass.getName() + "$" + fieldClass.getSimpleName() + Context.daoIdentity);
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

	public static void injectDao(Object bean, Dao dao) {
		try {
			Field field = bean.getClass().getDeclaredField(Context.daoMethod);
			field.set(bean, dao);
		} catch (Exception e) {
			throwEX(e, "SQLBoxUtils forceInjectFieldValue error, bean=" + bean + "dao=" + dao);
		}
	}

	public static Dao findDao(Class<?> clazz, Context context) {
		Class<Dao> daoClass = SQLBoxUtils.findDaoClass(clazz, context);
		if (daoClass != null)
			try {
				Dao box = daoClass.newInstance().setContext(context);
				return box.create();
			} catch (Exception e) {
				SQLBoxUtils.throwEX(e, "SQLBox create error, clazz=" + clazz);
			}
		Dao dao = new Dao().setBeanClass(clazz).setContext(context);
		return dao;
	}
}
