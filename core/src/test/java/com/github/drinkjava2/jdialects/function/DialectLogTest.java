/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.log.DialectLog;
import com.github.drinkjava2.jdialects.log.DialectLogFactory;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class DialectLogTest extends JdialectsTestBase {
	private static final DialectLog logger = DialectLogFactory.getLog(DialectLogTest.class);
  
	@Test
	public void doLoggerTest() {
		Dialect.setGlobalAllowShowSql(true);
		Dialect.MySQL55Dialect.pagin(10, 10, "select * from sometable");
		logger.info("Message1 output");
		Systemout.println("Message2 output");
		System.out.println(logger);
	}
}