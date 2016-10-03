package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;

class ProxyPO implements MethodInterceptor {
	private Class<?> poClass;
	private Boolean dirty = false;
	private BaseDao sqlbox; 

	public ProxyPO(Class<?> poClass, BaseDao sqlbox ) {
		this.poClass = poClass;
		this.sqlbox = sqlbox; 
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy) throws Throwable {
		if (Context.daoMethod.equals(method.getName()))
			return sqlbox;
		if (!dirty)
			if (method.getName().startsWith("set")) {
				dirty = true;
				System.out.println("methodname=" + method.getName());
				System.out.println("sqlbox=" + sqlbox);
				System.out.println("poClass=" + poClass);
				BaseDao.poCache.get().put(SQLBoxUtils.findID(obj, sqlbox), obj);
			}
		return cgLibMethodProxy.invokeSuper(obj, args);
	}
}