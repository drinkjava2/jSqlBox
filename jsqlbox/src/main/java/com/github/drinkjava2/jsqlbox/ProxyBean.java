package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;

/**
 * ProxyBean class is used to create an entity proxy
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0
 */
class ProxyBean implements MethodInterceptor {
	@SuppressWarnings("unused")
	private Class<?> beanClass;
	private Boolean dirty = false;
	private Box box;

	public ProxyBean(Class<?> beanClass, Box box) {
		this.beanClass = beanClass;
		this.box = box;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy) throws Throwable {
		String methodname = method.getName();
		if ("box".equals(methodname))
			return box;
		else if ("save".equals(methodname)) {
			box.insert();
			return null;
		}
		if (!dirty && methodname.startsWith("set")) {
			dirty = true;
		}
		return cgLibMethodProxy.invokeSuper(obj, args);
	}
}