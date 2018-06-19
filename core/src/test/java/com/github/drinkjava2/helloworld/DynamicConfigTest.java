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

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class DynamicConfigTest extends TestBase {

	public static class UserDemo extends ActiveRecord {
		@UUID32
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

	public static class UserDemoSqlBox extends SqlBox {
		{
			TableModel t = TableModelUtils.entity2Model(UserDemo.class);
			t.addColumn("anotherColumn1").STRING(40);
			this.setTableModel(t);
		}
	}

	@Test
	public void doTest() {
		TableModel t = new UserDemoSqlBox().getTableModel();

		// A new column dynamically created
		t.addColumn("anotherColumn2").VARCHAR(10);
		createAndRegTables(t);
		UserDemo u = new UserDemo();

		// A Fake PKey dynamically cretated
		u.columnModel("id").pkey();
		u.setUserName("Sam");
		ctx.insert(u);

		u.setUserName("Tom");
		u.update();

		Assert.assertEquals(1L, ctx.nQueryForObject("select count(*) from table2"));
	}

	@Test
	public void doQueryTest() {
		createAndRegTables(UserDemo.class);
		UserDemo u = new UserDemo().put("userName", "Tom").insert();

		u.columnModel("id").pkey();
		u.setUserName(null);
		u.load();
		Assert.assertEquals("Tom", u.getUserName());

		u.setUserName(null);
		u.loadById(u.getId());
		Assert.assertEquals("Tom", u.getUserName());

		// TODO: different load configurations
		UserDemo u2 = ctx.loadById(UserDemo.class, u.getId(), u.tableModel());
		Assert.assertEquals("Tom", u2.getUserName());

		u.columnModel("userName").setTransientable(true);
		UserDemo u3 = ctx.loadById(UserDemo.class, u.getId(), u);
		Assert.assertEquals(null, u3.getUserName());

		UserDemo u4 = ctx.loadById(UserDemo.class, u.getId(), u.tableModel());
		Assert.assertEquals(null, u4.getUserName());

		UserDemo u5 = ctx.loadById(UserDemo.class, u.getId(), u.box());
		Assert.assertEquals(null, u5.getUserName());
	}

	@Test(expected = SqlBoxException.class)
	public void doExceptionTest() {
		createAndRegTables(UserDemo.class);
		UserDemo u = new UserDemo().put("userName", "Tom").insert();
		ctx.loadById(UserDemo.class, u.getId());
	}

}