/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.ReservedDBWords;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;

/**
 * StrUtils Unit Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@Table(name = "order") // "order" is a reserved word for all database
public class ReservedDBWordsTest {

	@Test
	public void doTest() {
		Assert.assertTrue(ReservedDBWords.isReservedWord(Dialect.H2Dialect, "CURRENT_TIMESTAMP"));
		Assert.assertFalse(ReservedDBWords.isReservedWord(Dialect.H2Dialect, "CURRENT_TIMESTAMP___"));
		Assert.assertTrue(ReservedDBWords.isReservedWord(Dialect.H2Dialect, "AUTHORIZATION"));
	}

	@Test(expected = DialectException.class)
	public void doTestExceptionThrow() {
		Dialect.setGlobalAllowReservedWords(false);
		Dialect.MySQL55Dialect.toCreateDDL(ReservedDBWordsTest.class);
	}

	@Test
	public void doTestNoExceptionThrow() {
		Dialect.setGlobalAllowReservedWords(true);
		Systemout.println(Dialect.MySQL55Dialect.toCreateDDL(ReservedDBWordsTest.class)[0]);
		Dialect.setGlobalAllowReservedWords(false);// remember to restore this global setting!
	}
}
