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
package functiontest.helloworld;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class HelloWorld {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Test
	public void doText() {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);

		SqlBoxContext ctx = new SqlBoxContext(ds);
		ctx.setAllowShowSQL(true);
		String[] ddls = ctx.getDialect().toCreateDDL(HelloWorld.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);

		HelloWorld hello = new HelloWorld();
		hello.setName("Demo");
		ctx.insert(hello);
		Assert.assertEquals("Demo", ctx.nQueryForObject("select name from helloworld"));
		ds.close();
	}
}