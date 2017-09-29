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
import com.github.drinkjava2.jsqlbox.DebugUtils;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@Entity
@Table(name = "user_demo")
public class UserDemo {
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

	public static void config(TableModel t) {
		t.setTableName("t3");
		t.column("id").VARCHAR(32).pkey();
		t.column("user_name2").setColumnName("user_name3");
	}

	public static class UserDemoBX extends SqlBox {
		{
			TableModel t = new TableModel("tb2");
			t.column("a").STRING(40).pkey();
			this.setTableModel(t);
		}
	}

	@Test
	public void doTest() {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);

		SqlBoxContext context = new SqlBoxContext(ds);
		context.setAllowShowSQL(true);
		UserDemo u = new UserDemo();

		SqlBox box = SqlBoxUtils.findBox(u);
		System.out.println("box=" + box);
		System.out
				.println("=====getTableModelDebugInfo====\r" + DebugUtils.getTableModelDebugInfo(box.getTableModel()));

		String[] ddls = context.pojos2CreateDDLs(u);
		for (String ddl : ddls)
			context.nExecute(ddl);

		u.setId("001");
		u.setUserName("Sam");
		context.insert(u);
	}

}