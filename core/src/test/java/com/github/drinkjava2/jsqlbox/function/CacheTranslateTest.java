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
package com.github.drinkjava2.jsqlbox.function;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.KeyedHandler;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class CacheTranslateTest extends TestBase {
	{
		regTables(User.class, Order.class);
	}

	@Table(name = "users")
	public static class User extends ActiveRecord<User> {
		@Id
		Integer id;
		String name;
		Integer age;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

	}

	@Table(name = "orders")
	public static class Order extends ActiveRecord<Order> {
		@Id
		@UUID32
		String id;
		String orderNO;
		@SingleFKey(refs = { "users", "id" })
		Integer userId;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getOrderNO() {
			return orderNO;
		}

		public void setOrderNO(String orderNO) {
			this.orderNO = orderNO;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

	}

	@Test
	public void testEntityListHandler2() {// TODO add test body
		ctx.nBatchBegin();
		for (int i = 0; i < 100; i++)
			new User().putField("id", i).putField("name", "user" + i).putField("age", i).insert();
		for (int i = 0; i < 500; i++)
			new Order().putField("orderNO", "order" + i, "userId", i % 100).insert();
		ctx.nBatchEnd(); 
		Map<Integer, Map<String, Object>> users = ctx.iQuery("select id, name, age from users", new KeyedHandler<Integer>(1));
		List<Map<String,Object>> orders=ctx.iQuery("select * from orders"); 
		// List<Map<String,Object>> result2=DB.cacheTransfer(orders, "userId", userCache, "groupId",groupCache);
		// List<Orders> o

	}
}