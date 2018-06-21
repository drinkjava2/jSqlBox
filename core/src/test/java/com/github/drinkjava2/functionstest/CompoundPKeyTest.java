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

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.modelAlias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

/**
 * This is function test for Entity with Compound Prime keys, a compound-pkey
 * entity need extends ActiveRecord or put a @Entity annotation
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class CompoundPKeyTest extends TestBase {

	public static class CmpEntity extends ActiveRecord {
		@Id
		String firstName;

		@Id
		String middleName;

		@Id
		String lastName;

		@Id
		Integer age;

		String address;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getMiddleName() {
			return middleName;
		}

		public void setMiddleName(String middleName) {
			this.middleName = middleName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

	}

	@Before
	public void init() {
		super.init();
		createAndRegTables(CmpEntity.class);
		for (int i = 1; i <= 10; i++)
			new CmpEntity().put("firstName", "Sam", "middleName", "Y", "lastName", "Zhu", "age", i, "address", "China")
					.insert();
	}

	@Test
	public void testOrmQry() {
		EntityNet net = ctx.iQuery(new EntityNetHandler(), "select u.** from CmpEntity u",
				modelAlias(CmpEntity.class, "u"), " where age>?", param(5));
		List<CmpEntity> entities = net.pickEntityList("u");
		Assert.assertEquals(5, entities.size());

		// Map as entityId
		Map<String, Object> idMap = new HashMap<String, Object>();
		idMap.put("lastName", "Zhu");
		idMap.put("firstName", "Sam");
		idMap.put("middleName", "Y");
		idMap.put("age", 6);
		CmpEntity entity = net.pickOneEntity("u", idMap);
		Assert.assertEquals(new Integer(6), entity.getAge());

		// Entity as entityId
		CmpEntity entityBean = new CmpEntity();
		entityBean.put("firstName", "Sam", "middleName", "Y", "lastName", "Zhu", "age", 6);
		CmpEntity entity2 = net.pickOneEntity("u", entityBean);
		Assert.assertEquals(new Integer(6), entity2.getAge());
	}

	@Test
	public void testActiveRecord() {
		ctx.setAllowShowSQL(true);
		CmpEntity entity3 = new CmpEntity();
		entity3.put("firstName", "Sam", "middleName", "Y", "lastName", "Zhu", "age", 7).load();
		Assert.assertEquals("China", entity3.getAddress());

		CmpEntity entity4 = new CmpEntity();
		entity4.put("firstName", "Sam", "middleName", "Y", "lastName", "Zhu", "age", 8);
		CmpEntity entity5 = new CmpEntity().loadById(entity4);
		Assert.assertEquals(new Integer(8), entity5.getAge());
		Assert.assertEquals("China", entity5.getAddress());
	}

}