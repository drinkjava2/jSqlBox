/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.ClassCacheUtils;

/**
 * Coverage unit test for ClassCacheUtils
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class ClassCacheUtilsTest {

	public static class BeanExample {
		private String field1;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

	}

	public static class BeanExample2 extends BeanExample {
		private String field2;

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}

	}

	@Test
	public void cacheReadWriteMethodsTest() {
		for (int i = 0; i < 10000; i++) {
			int length = ClassCacheUtils.getClassReadMethods(BeanExample2.class).size();
			Assert.assertTrue(length == 2);
			length = ClassCacheUtils.getClassWriteMethods(BeanExample2.class).size();
			Assert.assertTrue(length == 2);
		}
	}

}
