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
package functiontest;

import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.Column;
import com.github.drinkjava2.jdialects.annotation.Entity;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DebugUtils;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@Entity
@Table(name = "user_demo")
public class ConfigTest1 extends TestBase {
	@Column(name = "user_name2", length = 32)
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static void config(TableModel t) {
		t.setTableName("t3");
		t.column("id").VARCHAR(32).pkey();
		t.column("user_name2").setColumnName("user_name3");
	}

	@Test
	public void doTest() {
		SqlBoxContext context = new SqlBoxContext(dataSource);
		context.setAllowShowSQL(true);
		ConfigTest1 u = new ConfigTest1();

		SqlBox box = SqlBoxUtils.findBox(u);
		System.out.println("box=" + box);
		System.out
				.println("=====getTableModelDebugInfo====\r" + DebugUtils.getTableModelDebugInfo(box.getTableModel()));

		String[] ddls = context.pojos2CreateDDLs(u);
		for (String ddl : ddls)
			context.nExecute(ddl);

		u.setUserName("Sam");
		context.insert(u);
	}

}