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

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdbpro.CustomizedSqlItem;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class CustomizedSqlItemTest extends TestBase {
	{
		regTables(CustUser.class);
	}

	public static class CustUser extends ActiveRecord<CustUser> {
		@Id
		String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	public static class SelectCount implements CustomizedSqlItem {

		@Override
		public void doPrepare(PreparedSQL ps) {
			Systemout.println("This is DemoCustomizedSqlItem");
			ps.addSql("select count(*) from ");
		}
	}

	@Test
	public void testSpecialSqlItemPreparer() throws SQLException {
		Assert.assertEquals(0, ctx.iQueryForLongValue(new PrintSqlHandler(), new SelectCount(), "CustUser"));
	}
}