/*
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Unit test for DbPro
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
public class BatchInsertTest extends TestBase {

	public static class DummyUser extends ActiveRecord<DummyUser> {
		private String name;
		private Integer age;

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

	@Before
	public void init() {
		super.init();
		createAndRegTables(DummyUser.class); 
	}
 
	@Test
	public void nBatchTest() {
		List<Object[]> params = new ArrayList<Object[]>();
		params.add(new Object[] { "test1", 1 });
		params.add(new Object[] { "test2", 2 });
		ctx.nBatch("INSERT INTO DummyUser(name, age) VALUES(?,?)", params);
		Assert.assertEquals(2L, ctx.pQueryForLongValue("select count(*) from DummyUser"));
	}
}
