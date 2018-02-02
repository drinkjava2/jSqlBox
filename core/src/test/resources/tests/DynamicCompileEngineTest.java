package tests;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.TextUtils;
import com.github.drinkjava2.jsqlbox.compiler.DynamicCompileEngine;

@SuppressWarnings("all")
public class DynamicCompileEngineTest {
	/*-	   
	package com.foo.bar; 	 
	 public class DynaClass { 
	   public String toString() { 
	     return  "Hello, I am a dog"; 
		 } 
	 } 	
	 */

	@Test
	public void doTest() throws IllegalAccessException, InstantiationException {
		String fullName = "com.foo.bar.DynaClass";
		String src = TextUtils.getJavaSourceCodeUTF8(DynamicCompileEngineTest.class);
		src = StrUtils.substringBetween(src, "/*-", "*/");
		Class<?> clazz = null;
		for (int i = 0; i < 1000000; i++)// Test class cached
			clazz = DynamicCompileEngine.instance.javaCodeToClass(fullName, src);
		Object instance = DynamicCompileEngine.instance.javaCodeToNewInstance(fullName, src);
		System.out.println(instance);
		Assert.assertEquals(instance.toString(), "Hello, I am a dog");
	}
}
