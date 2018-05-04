/*
 * Copyright (C) 2016 Original Author
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.Sharding;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class MasterSlaveShardingTest {
	HikariDataSource ds;
	SqlBoxContext ctx;

	@Sharding({ "mod", "8" })
	private Long userID;

	@Sharding({ "size", "2000000" })
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	@Before
	public void init() {
		ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);
		ctx = new SqlBoxContext(ds);
		String[] ddls = ctx.toCreateDDL(MasterSlaveShardingTest.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
	}

	@Test
	public void doTest() {
		MasterSlaveShardingTest tester = new MasterSlaveShardingTest();
		tester.setName("ActiveRecordDemoTest");
		ctx.insert(tester);
		Assert.assertEquals("ActiveRecordDemoTest", ctx.pQueryForString("select name from MasterSlaveShardingTest"));
		ds.close();
	}
}