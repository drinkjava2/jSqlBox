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

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class CacheTranslateTest extends TestBase {
	{
		createAndRegTables(User.class, Order.class);
	}

	@Table(name = "users")
	public static class User extends ActiveRecord<User> {
		@Id
		String id;
		String userName;
		Integer userAge;

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

		public Integer getUserAge() {
			return userAge;
		}

		public void setUserAge(Integer userAge) {
			this.userAge = userAge;
		}
	}

	@Table(name = "orders")
	public static class Order extends ActiveRecord<Order> {
		@Id
		Integer id;
		String OrderNO;
		String userId;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getOrderNO() {
			return OrderNO;
		}

		public void setOrderNO(String orderNO) {
			OrderNO = orderNO;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}
	}

	@Before
	public void init() {

		for (int i = 0; i < 100; i++)
			new User().putField("id", "" + i).putField("userName", "user" + i).putField("age", i).insert();
	}

	@Test
	public void testEntityListHandler2() {// TODO add test body
		// MemeCache c=new Cache(100);
		// Map<Object, Map<String, Object>> userCache=iQuery("xxx", c);
		// List<Map<String,Object>> result=iQuery();
		// List<Map<String,Object>> result2=DB.cacheTransfer(result, "userId",
		// userCache, "groupId",groupCache);
		// List<Orders> o

	}
}