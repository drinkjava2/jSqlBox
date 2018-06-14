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

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SpecialSqlItem;
import com.github.drinkjava2.jdbpro.SpecialSqlItemPreparer;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * This is function test for SqlHandlers
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class SpecialSqlItemPreparerTest extends TestBase {

	public static class SpecialDemoUser extends ActiveRecord {
		@Id
		String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	@Before
	public void init() {
		super.init();
		createAndRegTables(SpecialDemoUser.class);
		for (int i = 0; i < 10; i++)
			new SpecialDemoUser().put("id", "" + i).insert();
	}

	public static class MySqlItem1 implements SpecialSqlItem {//// NOSONAR , a.clo as a_col

	}

	public static class MySqlItem2 implements SpecialSqlItem {//// NOSONAR , a.clo as a_col

	}

	@Test
	public void testSpecialSqlItemPreparer() throws SQLException {

		SpecialSqlItemPreparer sp1 = new SpecialSqlItemPreparer() {
			@Override
			public boolean doPrepare(PreparedSQL ps, StringBuilder sql, SpecialSqlItem item) {
				if (item instanceof MySqlItem1) {
					System.out.println("This is MySqlItem1 preparer");
					return true;
				}
				return false;
			}
		};

		SpecialSqlItemPreparer sp2 = new SpecialSqlItemPreparer() {
			@Override
			public boolean doPrepare(PreparedSQL ps, StringBuilder sql, SpecialSqlItem item) {
				if (item instanceof MySqlItem2) {
					System.out.println("This is MySqlItem2 preparer");
					return true;
				}
				return false;
			}
		};

		SqlBoxContext ct = new SqlBoxContext(ctx.getDataSource());
		ct.setSpecialSqlItemPreparers(new SpecialSqlItemPreparer[] { sp1 });
		System.out.println(ct.iQueryForLongValue("select count(1) from SpecialDemoUser", new MySqlItem1()));

		SqlBoxContext ct2 = new SqlBoxContext(ctx.getDataSource());
		ct2.addSpecialSqlItemPreparer(sp1);
		System.out.println(ct2.iQueryForLongValue("select count(1) from SpecialDemoUser", new MySqlItem1()));
		ct2.addSpecialSqlItemPreparer(sp2);
		System.out.println(
				ct2.iQueryForLongValue("select count(1) from SpecialDemoUser", new MySqlItem1(), new MySqlItem2()));
	}
}