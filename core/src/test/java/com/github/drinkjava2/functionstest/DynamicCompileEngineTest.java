package com.github.drinkjava2.functionstest;
 
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.TextUtils;
import com.github.drinkjava2.jsqlbox.compiler.DynamicCompileEngine;

@SuppressWarnings("all")
public class DynamicCompileEngineTest {
	static String javaSrc=
			"package com.foo.bar; \n" + 
			"	import org.apache.commons.dbutils.handlers.MapListHandler;	 \n" + 
			"	 public class DynaClass { \n" + 
			"	   public String toString() { \n" + 
			"	     MapListHandler handler=null;\n" + 
			"	     return  \"Test Dynamic Compile\"; \n" + 
			"		 } \n" + 
			"	 }";
	@Test
	public void doTest() throws IllegalAccessException, InstantiationException {
		MapListHandler handler=null;
		String fullName = "com.foo.bar.DynaClass"; 
		Class<?> clazz = null;
		for (int i = 0; i < 100000; i++)// Test class cached
			clazz = DynamicCompileEngine.instance.javaCodeToClass(fullName, javaSrc);
		Object instance = DynamicCompileEngine.instance.javaCodeToNewInstance(fullName, javaSrc);
		System.out.println(instance.toString());
		Assert.assertEquals(instance.toString(), "Test Dynamic Compile");
	}
}
