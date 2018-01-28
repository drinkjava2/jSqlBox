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
package com.github.drinkjava2.helloworld;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.Config;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlInvocator;
import com.zaxxer.hikari.HikariDataSource;

import textsample.SampleUser;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class ProxyCreateAndTest {
	@Before
	public void init() {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);

		SqlBoxContext ctx = new SqlBoxContext(ds);
		Config.setGlobalSqlBoxContext(ctx);
		String[] ddls = ctx.getDialect().toCreateDDL(SampleUser.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
	}

	public static class RealObject extends ActiveRecord {

	}

	@Test
	public void doTest() {
//		RealObject realObj = new RealObject();
//
//		SqlInvocator handler = new SqlInvocator(realObj, null);
//
//		ClassLoader loader = realSubject.getClass().getClassLoader();
//		Class[] interfaces = realSubject.getClass().getInterfaces();
//
//		Object sampleUser = Proxy.newProxyInstance(realObject.class.getClassLoader(), realObject.class.getInterfaces(),
//				handler);
//		System.out.println("sampleUser=" + sampleUser);
//		// sampleUser.selectUsers("Sam", "Canada");
	}
}