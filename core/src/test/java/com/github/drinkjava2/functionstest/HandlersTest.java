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
import com.github.drinkjava2.functionstest.entitynet.entities.User;
import com.github.drinkjava2.jdbpro.improve.Handlers;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.handler.EntitySqlMapListHandler;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;
import com.github.drinkjava2.jsqlbox.handler.PrintSqlHandler;

/**
 * TextUtils is base class for Java text support (multiple line Strings).
 * 
 * @author Yong Zhu
 */
public class HandlersTest extends TestBase {

	@Before
	public void init() {
		super.init();
		TableModel[] models = TableModelUtils.entity2Models(User.class);
		dropAndCreateDatabase(models);
		new User().put("id", "u1").put("userName", "user1").put("age", 10).insert();
		new User().put("id", "u2").put("userName", "user2").put("age", 20).insert();
		new User().put("id", "u3").put("userName", "user3").put("age", 30).insert();
	}

	@Test
	public void testHandlers() {
		List<User> result = ctx.nQuery(new Handlers(new PrintSqlHandler(), new EntityListHandler(User.class),
				new PaginHandler(1, 2), new PrintSqlHandler()), "select u.** from usertb u where u.age>?", 0);
		Assert.assertTrue(result.size() == 2);
	}

	@Test
	public void testEntityNetHandler() {
		EntityNet net = ctx.nQuery(new EntityNetHandler(User.class), "select u.** from usertb u where u.age>?", 0);
		List<User> result = net.getAllEntityList(User.class);
		Assert.assertTrue(result.size() == 3);
	}

	@Test
	public void testEntityListHandler() {
		List<User> result = ctx.nQuery(new EntityListHandler(User.class), "select u.** from usertb u where u.age>?", 0);
		Assert.assertTrue(result.size() == 3);
	}

	@Test
	public void testEntityMapListHandler() {
		List<Map<String, Object>> result = ctx.nQuery(new EntitySqlMapListHandler(User.class),
				"select u.** from usertb u where u.age>?", 0);
		Assert.assertTrue(result.size() == 3);
	}

	@Test
	public void testPrintSqlHandler() throws SQLException {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = ctx.query("select u.* from usertb u where u.age>?",
				new Handlers(new MapListHandler(), new PrintSqlHandler()), 0);
		Assert.assertTrue(result.size() == 3);
	}

}