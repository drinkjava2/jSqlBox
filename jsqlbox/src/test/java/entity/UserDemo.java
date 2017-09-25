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

import java.util.List;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.Column;
import com.github.drinkjava2.jdialects.annotation.Entity;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
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
		t.column("user_name2").setColumnName("user_name3");;
		return t;
	}

	public static class UserDemoBX extends SqlBox {
		{
			System.out.println("tableModel()=" + tableModel());
			this.setTableModel(tableModel());
			System.out.println("this tablemodel=" + this.getTableModel());
			this.getColumnModel("user_name3").setColumnName("user_name4");
		}
	}

	{
		System.out.println("================aaaa=");
		SqlBox box = SqlBoxUtils.findBox(this);
		System.out.println("box=" + box);
		System.out.println("tableModel=" + box.getTableModel());
		// System.out.println("getTableModelDebugInfo=" +
		// DebugUtils.getTableModelDebugInfo(box.getTableModel()));

		TableModel t = box.getTableModel();
		System.out.println(DebugUtils.getTableModelDebugInfo(t));
		List<ColumnModel> columns = t.getColumns();

		System.out.println("columns=" + columns);
		System.out.println("Rest=" + t.getColumn("user_name4"));

		ColumnModel c = box.getColumnModel("user_name4");
		System.out.println("c=" + c);
		c.setColumnName("user_name5");
	}

	public static void main(String[] args) {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(3);
		ds.setConnectionTimeout(5000);

		UserDemo u = new UserDemo();

		SqlBoxContext ctx = new SqlBoxContext(ds);
		SqlBox box=u.box();
		box.setContext(ctx);
		System.out.println("box="+box);
		
		String[] ddls = Dialect.H2Dialect.toCreateDDL(u.box().getTableModel());
		for (String ddl : ddls)
			ctx.nExecute(ddl);

		u.setId("001");
		u.setUserName("Sam");
		u.box().getColumnModel("user_name5").setColumnName("user_name6");
		u.insert();

		u.setId("002");
		u.setUserName("Tam"); 
	}
	
}