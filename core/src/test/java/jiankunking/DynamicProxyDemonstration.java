package jiankunking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 动态代理演示
 */
public class DynamicProxyDemonstration {
	public static void main(String[] args) {
		// 代理的真实对象
		Subject realSubject = new RealSubject();
		InvocationHandler handler = new InvocationHandlerImpl(realSubject);
		ClassLoader loader = realSubject.getClass().getClassLoader();
		Class<?>[] interfaces = realSubject.getClass().getInterfaces();
		Subject subject = (Subject) Proxy.newProxyInstance(loader, interfaces, handler);

		subject.SayHello("jiankunking");

	}

}