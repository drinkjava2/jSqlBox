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

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;

import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class HelloWorld extends ActiveRecord<HelloWorld>{
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) {
		DataSource ds = JdbcConnectionPool
				.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
		SqlBoxContext ctx = new SqlBoxContext(ds);
		String[] ddls = ctx.toCreateDDL(HelloWorld.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
		
        new HelloWorld().useContext(ctx).put("name","Hello jSqlBox").insert(); 
		System.out.println(ctx.pQueryForString("select name from HelloWorld"));
	}
}