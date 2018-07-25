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

import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.IocTool;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdbpro.handler.SimpleCacheHandler;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jsqlbox.annotation.Ioc;
import com.github.drinkjava2.jsqlbox.annotation.Model;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class SqlHandlersTest extends TestBase {

	@Table(name = "DemoUser")
	public static class DemoUser extends ActiveRecord<DemoUser> {
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

		@Sql("select u.* from DemoUser u where u.age>?")
		public List<DemoUser> selectAgeBiggerThan1(Integer age) {
			return this.guess(age, new EntityListHandler(), DemoUser.class);
		}

		@Model(DemoUser.class)
		@Ioc(EntityListHandlerCfg.class)
		@Sql("select * from DemoUser u where u.age>?")
		public List<DemoUser> selectAgeBiggerThan2(Integer age) {
			return this.guess(age);
		}
	}

	public static class EntityListHandlerCfg extends BeanBox {
		public EntityListHandler create() {
			return new EntityListHandler();
		}
	}

	@Before
	public void init() {
		super.init();
		createAndRegTables(DemoUser.class);
		for (int i = 0; i < 100; i++)
			new DemoUser().put("id", "" + i).put("userName", "user" + i).put("age", i).insert();
	}

	@Test
	public void testGuessAnnotationNoParam() {
		DemoUser user = new DemoUser();
		List<DemoUser> result = user.selectAgeBiggerThan1(0);
		Assert.assertTrue(result.size() == 99);
	}

	@Test
	public void testGuessAnnotationHasParam() {
		SqlBoxContextConfig cfg = new SqlBoxContextConfig();
		cfg.setIocTool(new IocTool() {
			@Override
			public <T> T getBean(Class<?> configClass) {
				return BeanBox.getBean(configClass);
			}
		});
		SqlBoxContext ctx2 = new SqlBoxContext(ctx.getDataSource(), cfg);

		DemoUser user = new DemoUser();
		user.useContext(ctx2);
		List<DemoUser> result2 = user.selectAgeBiggerThan2(0);
		Assert.assertTrue(result2.size() == 99);
	}

	@Test
	public void testEntityListHandler2() {
		List<DemoUser> result = pQuery(new PrintSqlHandler(), new EntityListHandler(), DemoUser.class,
				"select * from DemoUser where age>=?", 90);
		Assert.assertTrue(result.size() == 10);

		result = pQuery(new EntityListHandler(), DemoUser.class, "select * from DemoUser where age>=?", 90);
		Assert.assertTrue(result.size() == 10);

		result = pQuery(new EntityListHandler(), DemoUser.class, "select * from DemoUser u where age>=?", 90);
		Assert.assertTrue(result.size() == 10);

		result = pQuery(new EntityListHandler(), DemoUser.class, "select * from DemoUser where  age>=?", 90);
		Assert.assertTrue(result.size() == 10);

		result = pQuery(new EntityListHandler(), DemoUser.class, "select u.* from DemoUser u where u.age>=?", 90);
		Assert.assertTrue(result.size() == 10);
	}

	@Test
	public void testSimpleCacheHandler() {
		int repeatTimes = 20;// Change to 10000 to test!
		SimpleCacheHandler cache = new SimpleCacheHandler();
		for (int i = 0; i < 10; i++) {// warm up
			pQuery(cache, new EntityListHandler(), DemoUser.class, "select u.* from DemoUser u where u.age>?", 0);
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < repeatTimes; i++) {
			List<DemoUser> result = pQuery(cache, new EntityListHandler(), DemoUser.class,
					"select u.* from DemoUser u where u.age>?", 0);
			Assert.assertTrue(result.size() == 99);
		}
		long end = System.currentTimeMillis();
		String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("%40s: %6s s", "With SimpleCacheHandler Cache", timeused));

		start = System.currentTimeMillis();
		for (int i = 0; i < repeatTimes; i++) {
			List<DemoUser> result = pQuery(new EntityListHandler(), DemoUser.class,
					"select u.* from DemoUser u where u.age>?", 0);
			Assert.assertTrue(result.size() == 99);
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("%40s: %6s s", "No  Cache", timeused));

	}

	@Test
	public void testEntityMapListHandler() {
		List<Map<String, Object>> result = pQuery(new SSMapListHandler(), DemoUser.class, alias("u"),
				"select u.** from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result.size() == 99);
	}

	@Test
	public void testPaginHandler() {
		List<Map<String, Object>> result1 = pQuery(new MapListHandler(),
				gctx().getDialect().pagin(2, 5, "select u.* from DemoUser u where u.age>?"), 0);
		Assert.assertTrue(result1.size() == 5);

		List<Map<String, Object>> result2 = pQuery(new MapListHandler(),
				gctx().pagin(2, 5, "select u.* from DemoUser u where u.age>?"), 0);
		Assert.assertTrue(result2.size() == 5);

		List<DemoUser> users1 = pQuery(new EntityListHandler(), DemoUser.class, new PaginHandler(2, 5),
				"select u.* from DemoUser u where u.age>?", 0);
		Assert.assertTrue(users1.size() == 5);

		List<DemoUser> users2 = pQuery(new EntityListHandler(), DemoUser.class,
				"select u.* from DemoUser u where u.age>?", 0, pagin(2, 5));
		Assert.assertTrue(users2.size() == 5);

		List<DemoUser> users3 = entityFindAll(DemoUser.class, " where age>?", param(0), pagin(2, 5));
		Assert.assertTrue(users3.size() == 5);

		SqlBoxContext.setThreadLocalSqlHandlers(new PaginHandler(2, 5));
		List<DemoUser> users4 = pQuery(new EntityListHandler(), DemoUser.class,
				"select u.* from DemoUser u where u.age>?", 0);
		Assert.assertTrue(users4.size() == 5);
		SqlBoxContext.setThreadLocalSqlHandlers(null);
	}

	@Test
	public void testPrintSqlHandler() throws SQLException {
		List<Map<String, Object>> result = pQuery(new MapListHandler(), "select u.* from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result.size() == 99);

		List<Map<String, Object>> result2 = pQuery(new MapListHandler(), new PrintSqlHandler(),
				"select u.* from DemoUser u where u.age>?", 0);
		Assert.assertTrue(result2.size() == 99);
	}

	@Test
	public void testEnableDisableSqlHandler() throws SQLException {
		List<Map<String, Object>> result2 = pQuery(new MapListHandler(), new PrintSqlHandler(),
				new SqlItem(SqlOption.ENABLE_HANDLERS), "select u.* from DemoUser u where u.age>?", 0,
				new SqlItem(SqlOption.DISABLE_HANDLERS, PrintSqlHandler.class));
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
		List<Map<String, Object>> result2 = pQuery(new MyDemoAroundSqlHandler(), new MapListHandler(),
				new PrintSqlHandler(), new MyDemoAroundSqlHandler(), "select u.* from DemoUser u where u.age>?", 50);
		Assert.assertEquals(49, result2.size());
	}

}