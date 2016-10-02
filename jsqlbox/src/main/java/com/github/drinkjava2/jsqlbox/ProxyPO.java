package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;

class ProxyPO implements MethodInterceptor {
	private Class<?> poClass;
	private Boolean dirty = false;
	private SQLBox sqlbox;
	SQLBoxContext context;

	public ProxyPO(Class<?> poClass, SQLBox sqlbox, SQLBoxContext context) {
		this.poClass = poClass;
		this.sqlbox = sqlbox;
		this.context = context;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy) throws Throwable {
		if (!dirty)
			if (method.getName().startsWith("set")) {
				dirty = true;
				System.out.println("methodname=" + method.getName());
				System.out.println("sqlbox=" + sqlbox);
				System.out.println("poClass=" + poClass);
				SQLBox.poCache.get().put(SQLBoxUtils.findID(obj, sqlbox), obj);
			}
		return cgLibMethodProxy.invokeSuper(obj, args);
	}
}