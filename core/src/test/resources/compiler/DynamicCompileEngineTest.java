package compiler;

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
	     return  "Hello, I am dog"; 
		 } 
	 } 	
	 */
	@Test
	public void doTest() throws IllegalAccessException, InstantiationException {
		String fullName = "com.foo.bar.DynaClass";
		String src = TextUtils.getJavaSourceCodeUTF8(DynamicCompileEngineTest.class);
		src = StrUtils.substringBetween(src, "/*-", "*/");
		Object instance = null;
		// test class cached, only compiled once
		for (int i = 0; i < 1000000; i++)
			instance = DynamicCompileEngine.instance.javaCodeToObject(fullName, src);
		System.out.println(instance);
		Assert.assertEquals(instance.toString(), "Hello, I am dog");
	}
}
