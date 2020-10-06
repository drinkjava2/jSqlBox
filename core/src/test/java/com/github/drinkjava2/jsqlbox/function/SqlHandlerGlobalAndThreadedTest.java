/**
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jsqlbox.function;

import static com.github.drinkjava2.jsqlbox.DB.par;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.function.SqlHandlersTest.DemoUser;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;

/**
 * This is function test for global SqlHandlers and ThreadLocaled SqlHandlers
 * 
 * @author Yong Zhu
 */
public class SqlHandlerGlobalAndThreadedTest extends TestBase {

	@Before
	public void init() {
		super.init();
		createAndRegTables(DemoUser.class);
		for (int i = 1; i <= 100; i++)
			new DemoUser().putField("id", "" + i).putField("userName", "user" + i).putField("age", i).insert();

	}

	public static class FirstPrintHandler extends PrintSqlHandler {
		@Override
		public Integer getOrder() {
			return 1;
		}

		@Override
		public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
			Systemout.println("This is first printed===================");
			return super.handle(runner, ps);
		}
	}

	public static class LastPrintHandler extends PrintSqlHandler {
		@Override
		public Integer getOrder() {
			return 10000;
		}

		@Override
		public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
			Systemout.println("This is last printed==================");
			return super.handle(runner, ps);
		}
	}

	@Test
	public void testHandlers() {
		List<DemoUser> result = ctx.qry(new EntityListHandler(), DemoUser.class, new PrintSqlHandler(),
				"select u.* from DemoUser u where u.age>?", par(10));
		Assert.assertEquals(90l, result.size());

		DbContext.setGlobalNextSqlHandlers(new FirstPrintHandler(), new LastPrintHandler(),
				new FirstPrintHandler(), new PaginHandler(2, 5));
		DbContext.setThreadLocalSqlHandlers(new EntityListHandler());
		try {
			DbContext newCtx = new DbContext(ctx.getDataSource());
			List<DemoUser> result2 = newCtx.qry("select u.* from DemoUser u where u.age>?", par(10), DemoUser.class);
			Assert.assertEquals(5l, result2.size());
		} finally {
			DbContext.resetGlobalVariants();
		}
	}

}