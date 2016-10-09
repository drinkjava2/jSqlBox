package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.sql.DataSource;

import com.github.drinkjava2.cglib3_2_0.proxy.Enhancer;

@SuppressWarnings("unchecked")
public class SQLBoxContext {
	public static final String daoIdentity = "Dao";
	public static final SQLBoxContext defaultContext = new SQLBoxContext(null);
	private DataSource dataSource;

	public SQLBoxContext(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public ThreadLocal<HashMap<Object, Object>> poCache = new ThreadLocal<HashMap<Object, Object>>() {
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<Object, Object>();
		}
	};

	public static <T> T createDefaultBean(Class<?> clazz) {
		return (T) defaultContext.createBean(clazz);
	}

	public static <T> T createDefaultProxyBean(Class<?> clazz) {
		return (T) defaultContext.createProxyBean(clazz);
	}

	public <T> T createBean(Class<?> clazz) {
		Dao dao = SQLBoxUtils.findDao(clazz, this);
		Object bean = null;
		try {
			bean = clazz.newInstance();
			Method m = clazz.getMethod("putDao", new Class[] { Dao.class });
			m.invoke(bean, new Object[] { dao });
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLBoxContext createBean error, clazz=" + clazz);
		}
		dao.setBeanClass(clazz);
		dao.setBean(bean);
		return (T) bean;
	}

	public <T> T createProxyBean(Class<?> clazz) {
		Dao dao = SQLBoxUtils.findDao(clazz, this);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(new ProxyBean(clazz, dao));
		Object proxyBean = enhancer.create();
		try {
			Method m = clazz.getMethod("putDao", new Class[] { Dao.class });
			m.invoke(proxyBean, new Object[] { dao });
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLBoxContext createProxyBean error, clazz=" + clazz);
		}
		dao.setBeanClass(clazz);
		dao.setBean(proxyBean);
		return (T) proxyBean;
	}

}