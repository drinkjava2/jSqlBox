/**
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.functionstest;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdbpro.handler.SimpleCacheHandler;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
public class HandlersTest extends TestBase {

	@Table(name = "DemoUser")
	public static class DemoUser extends ActiveRecord {
		@Id
		String id;
		String userName;
		Integer age;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

	}

	@Before
	public void init() {
		super.init();
		TableModel[] models = TableModelUtils.entity2Models(DemoUser.class);
		dropAndCreateDatabase(models);
		for (int i = 0; i < 100; i++)
			new DemoUser().put("id", "" + i).put("userName", "user" + i).put("age", i).insert();
	}

	@Test
	public void testHandlers() {
		List<DemoUser> result = ctx.pQuery(PrintSqlHandler.class, new EntityListHandler(DemoUser.class),
				new PaginHandler(1, 5), PrintSqlHandler.class, "select u.** from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result.size() == 5);
	}

	@Test
	public void testEntityNetHandler() {
		EntityNet net = ctx.pQuery(new EntityNetHandler(DemoUser.class), "select u.** from DemoUser u where u.age>?",
				0);
		List<DemoUser> result = net.getAllEntityList(DemoUser.class);
		Assert.assertTrue(result.size() == 99);
	}

	@Test
	public void testEntityListHandler() {
		List<DemoUser> result = ctx.pQuery(new EntityListHandler(DemoUser.class),
				"select u.** from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result.size() == 99);
	}

	@Test
	public void testSimpleCacheHandler() {
		int repeatTimes = 1000;

		for (int i = 0; i < 10; i++) {// warm up
			ctx.pQuery(new SimpleCacheHandler(), new EntityListHandler(DemoUser.class),
					"select u.** from DemoUser u where u.age>?", 0);
			ctx.pQuery(new EntityListHandler(DemoUser.class), "select u.** from DemoUser u where u.age>?", 0);
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < repeatTimes; i++) {
			List<DemoUser> result = ctx.pQuery(new SimpleCacheHandler(), new EntityListHandler(DemoUser.class),
					"select u.** from DemoUser u where u.age>?", 0);
			Assert.assertTrue(result.size() == 99);
		}
		long end = System.currentTimeMillis();
		String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("%40s: %6s s", "With SimpleCacheHandler Cache", timeused));

		start = System.currentTimeMillis();
		for (int i = 0; i < repeatTimes; i++) {
			List<DemoUser> result = ctx.pQuery(new EntityListHandler(DemoUser.class),
					"select u.** from DemoUser u where u.age>?", 0);
			Assert.assertTrue(result.size() == 99);
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("%40s: %6s s", "No  Cache", timeused));

	}

	@Test
	public void testEntityMapListHandler() {
		List<Map<String, Object>> result = ctx.pQuery(new SSMapListHandler(DemoUser.class),
				"select u.** from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result.size() == 99);
	}

	@Test
	public void testPaginHandler() {
		List<Map<String, Object>> result = ctx.pQuery(new SSMapListHandler(DemoUser.class), new PaginHandler(2, 5),
				"select u.** from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result.size() == 5);

		List<DemoUser> users = ctx.pQuery(new EntityListHandler(DemoUser.class), new PaginHandler(2, 5),
				"select u.** from DemoUser u where u.age>?", 0);
		Assert.assertTrue(users.size() == 5);

	}

	@Test
	public void testPrintSqlHandler() throws SQLException {
		List<Map<String, Object>> result = ctx.pQuery(new MapListHandler(), "select u.* from DemoUser u where u.age>?",
				0);
		Assert.assertTrue(result.size() == 99);

		List<Map<String, Object>> result2 = ctx.pQuery(new MapListHandler(), new PrintSqlHandler(),
				"select u.* from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result2.size() == 99);
	}

	public static class MyDemoAroundSqlHandler extends DefaultOrderSqlHandler {
		@Override
		public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
			System.out.println("Hello");
			Object result = runner.runPreparedSQL(ps);
			System.out.println("Bye");
			return result;
		}
	}

	@Test
	public void testMyAroundSqlHandler() throws SQLException {
		List<Map<String, Object>> result2 = ctx.pQuery(MyDemoAroundSqlHandler.class, new MapListHandler(),
				PrintSqlHandler.class, new MyDemoAroundSqlHandler(), "select u.* from DemoUser u where u.age>?", 50);
		Assert.assertEquals(49, result2.size());
	}

}