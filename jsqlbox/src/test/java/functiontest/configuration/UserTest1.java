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
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import functiontest.TestBase;

/**
 * Demo of jDialects configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class UserTest1 extends TestBase {
	@Column(name = "user_name2", length = 32)
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static void config(TableModel t) {
		t.setTableName("table1");
		t.column("user_name2").setColumnName("user_name3");
	}

	@Test
	public void doTest() {
		SqlBoxContext.setDefaultContext(null);
		SqlBoxContext context = new SqlBoxContext(dataSource);
		context.setAllowShowSQL(true);
		String[] ddls = context.pojos2CreateDDLs(UserTest1.class);
		for (String ddl : ddls)
			context.nExecute(ddl);

		UserTest1 u = new UserTest1();
		u.setUserName("Sam");
		context.insert(u);
		
		UserTest1 u2=new UserTest1();
		System.out.println(u==u2);
		SqlBox box1=context.findSqlBox(u);
		SqlBox box2=context.findSqlBox(u2);
		System.out.println(box1==box2);
		TableModel tm=box1.getTableModel();
		TableModel tm2=box2.getTableModel();
		System.out.println(tm==tm2); 
	}

}