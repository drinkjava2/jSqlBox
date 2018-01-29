package com.github.drinkjava2.jsqlbox.compiler;

import java.net.URL;
import java.net.URLClassLoader;

@SuppressWarnings("all")
public class DynamicClassLoader extends URLClassLoader {
	public DynamicClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	public Class findClassByClassName(String className) throws ClassNotFoundException {
		return this.findClass(className);
	}

	public Class loadClass(String fullName, JavaClassObject jco) {
		byte[] classData = jco.getBytes();
		return this.defineClass(fullName, classData, 0, classData.length);
	}
}
