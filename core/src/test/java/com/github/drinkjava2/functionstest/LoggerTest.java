/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.functionstest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectLogger;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class LoggerTest {
	@Before
	public void init() {
		SqlBoxContext.resetGlobalVariants();
	}

	@After
	public void cleanUp() {
		SqlBoxContext.resetGlobalVariants();
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	DialectLogger logger = DialectLogger.getLog(LoggerTest.class);

	@Test
	public void doDialectLoggerTest() {
		Dialect.setGlobalAllowShowSql(true);
		Dialect.MySQL55Dialect.paginAndTrans(10, 10, "select * from sometable");
		logger.info("Logger test message1 output ok");
		System.out.println("Logger test message2 output ok");
		Dialect.setGlobalAllowShowSql(false);
	}

	@Test
	public void doSqlBoxLoggerTest() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		SqlBoxContext.setGlobalNextAllowShowSql(true);
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(LoggerTest.class))
			ctx.quiteExecute(ddl);
		LoggerTest t = new LoggerTest();
		t.setName("Tom");
		ctx.eInsert(t);
		SqlBoxContext.getGlobalNextLogger().info("Logger test message3 output ok");
		SqlBoxContext.getGlobalNextLogger().info("Logger test message4 output ok");
		SqlBoxContext.setGlobalNextAllowShowSql(false);
	}

}