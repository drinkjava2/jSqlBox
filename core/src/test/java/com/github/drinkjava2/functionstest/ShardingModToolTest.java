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
package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jdbpro.JDBPRO.USE_BOTH;
import static com.github.drinkjava2.jdbpro.JDBPRO.USE_SLAVE;
import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jdbpro.JDBPRO.valuesQuestions;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.functionstest.BatchTest.User;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.Sharding;
import com.github.drinkjava2.jdialects.annotation.jdia.Snowflake;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;
import com.zaxxer.hikari.HikariDataSource;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class ShardingModToolTest {

	final static int SLAVE_DATABASE_QTY = 20;
	final static int SHARDING_TABLE_QTY = 30;

	SqlBoxContext master;

	public static class TheModUser extends ActiveRecord {
		@Sharding({ "MOD", "8" })
		@Snowflake
		@Id
		private Long id;

		private String name;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Before
	public void init() {
		SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++) {
			slaves[i] = new SqlBoxContext(TestBase.createH2_HikariDataSource("SlaveDB" + i));
			slaves[i].setSnowflakeCreator$(new SnowflakeCreator(5, 5, 0, i));
		}
		master = new SqlBoxContext(TestBase.createH2_HikariDataSource("MasterDb"));// master DS
		master.setSnowflakeCreator$(new SnowflakeCreator(5, 5, 1, 0));
		master.setSlaves$(slaves);

		TableModel model = TableModelUtils.entity2Model(TheModUser.class);
		for (int i = 0; i < SHARDING_TABLE_QTY; i++) {// Create master/salve tables
			model.setTableName("TheModUser" + "_" + i);
			for (String ddl : master.getDialect().toCreateDDL(model))
				master.iExecute(ddl, USE_BOTH);
		}

	}

	@After
	public void cleanup() {
		TableModel model = TableModelUtils.entity2Model(TheModUser.class);
		for (int i = 0; i < SHARDING_TABLE_QTY; i++) {// drop master/salve tables
			model.setTableName("TheModUser" + "_" + i);
			for (String ddl : master.getDialect().toDropDDL(model))
				master.iExecute(ddl, USE_BOTH);
		}
		for (DbPro pro : master.getSlaves())
			((HikariDataSource) pro.getDataSource()).close();
		((HikariDataSource) master.getDataSource()).close();
	}

	@Test
	public void testSqlOnShardingTables() {// Test insert to sharding tables by SQL
		for (int id = 0; id < 100; id++) {
			String talbe = master.shardEqual(TheModUser.class, id);
			master.iUpdate("insert into ", talbe, " (" //
					, "id ", param(id) //
					, ",name ", param(talbe + "_id" + id) //
					, ") ", valuesQuestions(), USE_BOTH);
		}

		String table = master.shardEqual(TheModUser.class, 10);
		TheModUser u = new TheModUser();
		u.alias("u");
		u.tableModel().setTableName(table);
		List<TheModUser> users = master.iQuery(new EntityListHandler(TheModUser.class, u), "select u.** from ", table,
				" u");
		Assert.assertEquals(13, users.size());
		System.out.println(users.get(0).getName());

		List<Map<String, Object>> users2 = master.iQuery(new SSMapListHandler(User.class, u), "select u.** from ",
				master.shardEqual(TheModUser.class, 10), " u", USE_SLAVE);
		Assert.assertEquals(13, users2.size());
		System.out.println(users2);
	}

	@Test
	public void testActiveRecordOnShardingTables() {// Test insert to sharding tables by ActiveRecord
		for (int i = 0; i < 50; i++)
			new TheModUser().useContext(master).put("name", "master" + i).insert(USE_BOTH);

//		TheModUser u = new TheModUser().useContext(master).load(0);
//		System.out.println(u.getName());

		// String table = shardEqual(TheModUser.class, 10);
		// TheModUser u = new TheModUser();
		// u.alias("u");
		// u.tableModel().setTableName(table);
		// List<TheModUser> users = ctx.iQuery(new EntityListHandler(TheModUser.class,
		// u), "select u.** from ", table,
		// " u");
		// Assert.assertEquals(13, users.size());
		// System.out.println(users.get(0).getName());
		//
		// List<Map<String, Object>> users2 = ctx.iQuery(new
		// SSMapListHandler(User.class, u), "select u.** from ",
		// shardEqual(TheModUser.class, 10), " u", USE_SLAVE);
		// Assert.assertEquals(13, users2.size());
		// System.out.println(users2);
	}
}