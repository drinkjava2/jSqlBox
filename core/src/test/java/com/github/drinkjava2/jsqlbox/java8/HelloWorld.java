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
package com.github.drinkjava2.jsqlbox.java8;

/*- JAVA8_BEGIN */
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import static com.github.drinkjava2.jsqlbox.DB.*;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;

public class HelloWorld implements ActiveEntity<HelloWorld> {
	@Id
	@UUID25
	private String id;

	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public HelloWorld setName(String name) {
		this.name = name;
		return this;
	}

	public static void main(String[] args) {
		DataSource ds = JdbcConnectionPool
				.create("jdbc:h2:mem:demo;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
		DbContext ctx = new DbContext(ds);
		ctx.setAllowShowSQL(true);
		DbContext.setGlobalDbContext(ctx);
		ctx.quiteExecute(ctx.toDropAndCreateDDL(HelloWorld.class));
		ctx.tx(() -> {
			HelloWorld h = new HelloWorld().setName("Foo").insert().putField("name", "Hello jSqlBox").update();
			System.out.println(DB.qryString("select name from HelloWorld where name like", que("H%"),
					" or name=", que("1"), " or name =", que("2")));
			h.delete();
		});
		ctx.executeDDL(ctx.toDropDDL(HelloWorld.class));
	}
}
/* JAVA8_END */