package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;

class ProxyBean implements MethodInterceptor {
	@SuppressWarnings("unused")
	private Class<?> beanClass;
	private Boolean dirty = false;
	private Dao dao;

	public ProxyBean(Class<?> beanClass, Dao dao) {
		this.beanClass = beanClass;
		this.dao = dao;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy) throws Throwable {
		String methodname = method.getName();
		if ("dao".equals(methodname))
			return dao;
		else if ("save".equals(methodname)) {
			dao.save();
			return null;
		}
		if (!dirty && methodname.startsWith("set")) {
			dirty = true;
		}
		return cgLibMethodProxy.invokeSuper(obj, args);
	}
}