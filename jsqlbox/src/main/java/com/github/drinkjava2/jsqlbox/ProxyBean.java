package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;

class ProxyBean implements MethodInterceptor {
	private Class<?> beanClass;
	private Boolean dirty = false;
	private Dao dao;

	public ProxyBean(Class<?> beanClass, Dao dao) {
		this.beanClass = beanClass;
		this.dao = dao;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy) throws Throwable {
		String methodname = method.getName();
		System.out.println("methodname=" + methodname);
		if ("dao".equals(methodname))
			return dao;
		else if ("save".equals(methodname)) {
			dao.save();
			return null;
		} else if ("load".equals(methodname)) {
			dao.load(args);
			return null;
		} else if ("delete".equals(methodname)) {
			dao.delete(args);
			return null;
		} else if ("find".equals(methodname)) {
			dao.find(args);
			return null;
		}
		if (!dirty)
			if (methodname.startsWith("set")) {
				dirty = true;
				System.out.println("methodname=" + methodname);
				System.out.println("dao=" + dao);
				System.out.println("beanClass=" + beanClass);
				dao.getContext().poCache.get().put(SQLBoxUtils.findID(obj, dao), obj);
			}
		return cgLibMethodProxy.invokeSuper(obj, args);
	}
}