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
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class DynamicConfigTest extends TestBase {

	public static class UserDemo extends ActiveRecord<UserDemo> {
		private String id;

		@Column(name = "user_name2", length = 32)
		private String userName;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public static void config(TableModel t) {
			t.setTableName("table2");
			t.column("user_name2").setColumnName("user_name3");
		}
	}

	@Test
	public void doTest() {
		TableModel model = TableModelUtils.entity2Model(UserDemo.class);
		// A new column dynamically created
		model.addColumn("anotherColumn2").VARCHAR(10);
		createAndRegTables(model);

		UserDemo u = new UserDemo();
		u.setId("u1");
		u.setUserName("Sam");

		// A Fake PKey dynamically created
		model.column("id").pkey();
		ctx.eInsert(u, model);

		u.setUserName("Tom");
		u.update(model);

		Assert.assertEquals(1L, ctx.iQueryForLongValue("select count(*) from table2", model));
	}

	@Test
	public void testDynamicConfig() {
		TableModel model = TableModelUtils.entity2Model(UserDemo.class);
		model.column("id").pkey();
		createAndRegTables(model);

		UserDemo u1 = new UserDemo();
		u1.setId("u1");
		u1.setUserName("Tom");
		u1.insert(model);

		UserDemo u2 = ctx.eLoadById(UserDemo.class, "u1", model);
		Assert.assertEquals("Tom", u2.getUserName());

		model.column("userName").setTransientable(true);
		UserDemo u3 = ctx.eLoadById(UserDemo.class, "u1", model);
		Assert.assertEquals(null, u3.getUserName());
	}

	@Test
	public void doQueryTest() {
		createAndRegTables(UserDemo.class);
		UserDemo u = new UserDemo().putField("id", "u1", "userName", "Tom").insert();

		TableModel t = TableModelUtils.entity2Model(UserDemo.class);
		t.getColumnByFieldName("id").pkey();// Fake Pkey
		u.setId("u1");
		u.setUserName(null);
		u.load(t);
		Assert.assertEquals("Tom", u.getUserName());

		u.setUserName(null);
		UserDemo newU = u.loadById(u.getId(), t);
		Assert.assertEquals("Tom", newU.getUserName());

		UserDemo u2 = ctx.eLoadById(UserDemo.class, u.getId(), t);
		Assert.assertEquals("Tom", u2.getUserName());

		t.getColumnByFieldName("userName").setTransientable(true);// ignore userName
		UserDemo u3 = ctx.eLoadById(UserDemo.class, u.getId(), t);
		Assert.assertEquals(null, u3.getUserName());
	}

	@Test(expected = SqlBoxException.class)
	public void doExceptionTest() {
		createAndRegTables(UserDemo.class);
		UserDemo u = new UserDemo().putField("userName", "Tom").insert();
		ctx.eLoadById(UserDemo.class, u.getId());
	}

}