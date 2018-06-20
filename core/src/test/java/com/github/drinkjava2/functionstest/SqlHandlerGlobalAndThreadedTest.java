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
package com.github.drinkjava2.functionstest;

import java.util.List;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.SqlHandlersTest.DemoUser;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
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
			new DemoUser().put("id", "" + i).put("userName", "user" + i).put("age", i).insert();

	}

	public static class FirstPrintHandler extends PrintSqlHandler {
		@Override
		public Integer getOrder() {
			return 1;
		}

		@Override
		public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
			System.out.println("This is first printed===================");
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
			System.out.println("This is last printed==================");
			return super.handle(runner, ps);
		}
	}

	@Test
	public void testHandlers() {
		List<DemoUser> result = ctx.pQuery(new EntityListHandler(), model(DemoUser.class), new PrintSqlHandler(),
				"select u.* from DemoUser u where u.age>?", 10);
		Assert.assertEquals(90l, result.size());

		SqlBoxContextConfig.setGlobalNextSqlHandlers(new FirstPrintHandler(), new LastPrintHandler(),
				new FirstPrintHandler(), new PaginHandler(2, 5));
		SqlBoxContext.setThreadLocalSqlHandlers(new EntityListHandler());
		try {
			SqlBoxContext newCtx = new SqlBoxContext(ctx.getDataSource());
			List<DemoUser> result2 = newCtx.pQuery("select u.* from DemoUser u where u.age>?", 10,
					model(DemoUser.class));
			Assert.assertEquals(5l, result2.size());
		} finally {
			SqlBoxContext.resetGlobalVariants();
		}
	}

}