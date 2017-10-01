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
package functiontest.configuration;

import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.Column;
import com.github.drinkjava2.jdialects.annotation.Entity;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.DialectUtils;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import functiontest.TestBase;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@Entity
@Table(name = "user_demo")
public class UserTest2 extends TestBase {
	@Column(name = "user_name2", length = 32)
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static void config(TableModel t) {
		t.setTableName("table2");
		t.column("id").VARCHAR(32).pkey();
		t.column("user_name2").setColumnName("user_name3");
	}

	public static class UserTest2BX extends SqlBox {
		{
			TableModel t = DialectUtils.pojo2Model(UserTest2.class);
			t.removeColumn("id");
			t.column("user_name3").STRING(40).pkey().setColumnName("user_name4");
			this.setTableModel(t);
		}

		public void config(TableModel t) {
			t.column("newField").INTEGER();
		}
	}

	@Test
	public void doTest() {
		SqlBoxContext context = new SqlBoxContext(dataSource);
		context.setAllowShowSQL(true);
		UserTest2 u = new UserTest2();

		String[] ddls = context.pojos2CreateDDLs(u);
		for (String ddl : ddls)
			context.nExecute(ddl);

		u.setUserName("Sam");
		context.insert(u);
	}

}