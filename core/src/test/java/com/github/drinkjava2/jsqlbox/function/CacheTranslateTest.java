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
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.CacheTransUtils;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class CacheTranslateTest extends TestBase {
	{
		regTables(User.class, Group.class, Order.class);
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

	@Table(name = "groupnm")
	public static class Group extends ActiveRecord<Group> {
		@Id
		Integer id;
		String groupName;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

	}

	@Table(name = "orders")
	public static class Order extends ActiveRecord<Order> {
		@Id
		String id;
		String orderNO;
		@SingleFKey(refs = { "users", "id" })
		Integer userId;
		@SingleFKey(refs = { "groupnm", "id" })
		Integer groupId;

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

		public Integer getGroupId() {
			return groupId;
		}

		public void setGroupId(Integer groupId) {
			this.groupId = groupId;
		}
	}

	@Test
	public void testCacheTranslate() {
		ctx.nBatchBegin();
		int n = 2000;
		for (int i = 0; i < n; i++)
			new User().putField("id", i).putField("name", "user" + i).putField("age", i).insert();
		for (int i = 0; i < n; i++)
			new Group().putField("id", i).putField("groupName", "group" + i).insert();
		for (int i = 0; i < n; i++)
			new Order().putField("id", "o" + i, "orderNO", "order" + i, "userId", i, "groupId", i).insert();
		ctx.nBatchEnd();
		Map<Integer, Map<String, Object>> users = ctx.iQuery("select * from users", new KeyedHandler<Integer>("id"));
		Map<Integer, Map<String, Object>> groupnm = ctx.iQuery("select * from groupnm", new KeyedHandler<Integer>("id"));
		long oldTime = System.currentTimeMillis();
		List<Map<String, Object>> orders = ctx
				.iQueryForMapList("select id,orderNo,userId,groupId from orders where id>'10' ");
		CacheTransUtils.translate(orders, users, "userID", "name", "userName", "age", "userAge", groupnm, "groupId",
				"groupName", "groupName");
		Systemout.println("Cache Translate, Time used(ms):" + (System.currentTimeMillis() - oldTime));
		Assert.assertEquals(7, orders.get(0).size());
	}

	@Test
	public void testNoTranslate() {
		ctx.nBatchBegin();
		int n = 2000;
		for (int i = 0; i < n; i++)
			new User().putField("id", i).putField("name", "user" + i).putField("age", i).insert();
		for (int i = 0; i < n; i++)
			new Group().putField("id", i).putField("groupName", "group" + i).insert();
		for (int i = 0; i < n; i++)
			new Order().putField("id", "o" + i, "orderNO", "order" + i, "userId", i, "groupId", i).insert();
		ctx.nBatchEnd();
		long oldTime = System.currentTimeMillis();
		List<Map<String, Object>> orders = ctx.iQueryForMapList("select o.*, u.*, g.* from orders o  "
				+ " left join users u on o.userId=u.id left join groupnm g on o.groupId=g.id where o.id>'10' ");
		Systemout.println("No Cache Translate, Time used(ms):" + (System.currentTimeMillis() - oldTime));
		Assert.assertEquals(7, orders.get(0).size());
	}

}