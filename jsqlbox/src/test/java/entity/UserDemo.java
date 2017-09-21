/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package entity;

import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.Column;
import com.github.drinkjava2.jdialects.annotation.Entity;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@Entity
@Table(name = "user_demo")
public class UserDemo extends ActiveRecord {
	private String id;
	@Column(name = "user_name2", length = 32)
	private String userName;

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

	public static TableModel tableModel() {
		TableModel t = TableModel.fromPojo(UserDemo.class);
		t.column("id").VARCHAR(32).pkey();
		t.column("user_name3").VARCHAR(50).pojoField("userName").defaultValue("'aaa'");
		return t;
	}

	public static class UserBox extends SqlBox {
		{
			this.setTableModel(tableModel());
			this.columnModel("user_name3").setColumnName("user_name4");
		}
	}

	{
		SqlBox.findBox(this).columnModel("user_name4").setColumnName("user_name5");
	}

	@Test
	public void test() {
		UserDemo u = new UserDemo();
		u.setId("001");
		u.setUserName("Sam");
		u.box().columnModel("user_name5").setColumnName("user_name6");
		u.insert();

		u.setId("002");
		u.setUserName("Tam");
		SqlBoxContext.DefaultContext.insert(u);
	}

}