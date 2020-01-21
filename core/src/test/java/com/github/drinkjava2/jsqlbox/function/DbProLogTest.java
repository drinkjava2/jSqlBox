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
package com.github.drinkjava2.jsqlbox.function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jlogs.Log;
import com.github.drinkjava2.jlogs.LogFactory;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class DbProLogTest {
	Log log = LogFactory.getLog(DbProLogTest.class);

	@Before
	public void init() {
		DbContext.resetGlobalVariants();
	}

	@After
	public void cleanUp() {
		DbContext.resetGlobalVariants();
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Test
	public void doSqlBoxLoggerTest() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		DbContext ctx = new DbContext(dataSource);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(DbProLogTest.class))
			ctx.quiteExecute(ddl);
		DbProLogTest t = new DbProLogTest();
		t.setName("Tom");
		ctx.eInsert(t);
		log.info("Logger test ok");
		Systemout.println(log);
	}

}