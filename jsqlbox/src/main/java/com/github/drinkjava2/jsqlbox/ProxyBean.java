package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;

class ProxyBean implements MethodInterceptor {
	private Class<?> poClass;
	private Boolean dirty = false;
	private Dao dao;

	public ProxyBean(Class<?> poClass, Dao sqlbox) {
		this.poClass = poClass;
		this.dao = sqlbox;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy) throws Throwable {
		boolean invoke = true;
		if ("dao".equals(method.getName()))
			return dao;
		else if ("save".equals(method.getName()))
			dao.save();
		else if ("load".equals(method.getName()))
			dao.load(args);
		else if ("delete".equals(method.getName()))
			dao.delete(args);
		else if ("find".equals(method.getName()))
			dao.find(args);
		else
			invoke = false;
		if (invoke)
			return null;
		if (!dirty)
			if (method.getName().startsWith("set")) {
				dirty = true;
				System.out.println("methodname=" + method.getName());
				System.out.println("sqlbox=" + dao);
				System.out.println("poClass=" + poClass);
				Dao.poCache.get().put(SQLBoxUtils.findID(obj, dao), obj);
			}
		return cgLibMethodProxy.invokeSuper(obj, args);
	}
}