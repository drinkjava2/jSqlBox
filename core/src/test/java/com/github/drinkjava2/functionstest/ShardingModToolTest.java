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

import static com.github.drinkjava2.jdbpro.JDBPRO.*;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardEqual;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.functionstest.BatchTest.User;
import com.github.drinkjava2.functionstest.MasterSlaveTest.TheUser;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.Sharding;
import com.github.drinkjava2.jdialects.annotation.jdia.Snowflake;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
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
	final static int SLAVE_TOTAL_ROWS = 5;
	final static int SHARDING_TABLE_QTY = 30;

	final static int MASTER_TOTAL_ROWS = 10;
	SqlBoxContext ctx;
	SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
	public static HikariDataSource masterDs;
	HikariDataSource[] slaveDs = new HikariDataSource[SLAVE_DATABASE_QTY];

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

	private static HikariDataSource createNewH2DataSource(String name) {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:" + name + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);
		return ds;
	}

	@Before
	public void init() {
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++) {// slave DS
			slaveDs[i] = createNewH2DataSource("SlaveDB" + i);
			slaves[i] = new SqlBoxContext(slaveDs[i]);
			config.addSlave(slaves[i]);
		}
		masterDs = createNewH2DataSource("MasterDb");
		ctx = new SqlBoxContext(masterDs, config);// master ds
		TableModel model = TableModelUtils.entity2Model(TheModUser.class);

		for (int i = 0; i < SHARDING_TABLE_QTY; i++) {// Create master/salve tables
			model.setTableName("TheModUser" + "_" + i);
			for (String ddl : ctx.getDialect().toCreateDDL(model))
				ctx.iExecute(ddl, USE_BOTH);
		}

	}

	@After
	public void cleanup() {
		TableModel model = TableModelUtils.entity2Model(TheModUser.class);
		for (int i = 0; i < SHARDING_TABLE_QTY; i++) {// Create master/salve tables
			model.setTableName("TheModUser" + "_" + i);
			for (String ddl : ctx.getDialect().toDropDDL(model))
				ctx.iExecute(ddl, USE_BOTH);
		}
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++)
			slaveDs[i].close();
		masterDs.close();
	}

	@Test
	public void testInsertSQL() {
		for (int id = 0; id < 100; id++) {
			String talbe = shardEqual(TheModUser.class, id);
			ctx.iUpdate("insert into ", talbe, " (" //
					, "id ", param(id) //
					, ",name ", param(talbe + "_id" + id) //
					, ") ", valuesQuestions(), USE_BOTH);
		}

		String table = shardEqual(TheModUser.class, 10);
		TheModUser u = new TheModUser();
		u.alias("u");
		u.tableModel().setTableName(table);
		List<TheModUser> users = ctx.iQuery(new EntityListHandler(TheModUser.class, u), "select u.** from ", table,
				" u");
		Assert.assertEquals(13, users.size());
		System.out.println(users.get(0).getName());

		List<Map<String, Object>> users2 = ctx.iQuery(new SSMapListHandler(User.class, u), "select u.** from ",
				shardEqual(TheModUser.class, 10), " u", USE_SLAVE);
		Assert.assertEquals(13, users2.size());
		System.out.println(users2);
	}

	@Test
	public void testInsertActiveRecord() {
		for (int i = 0; i < 100; i++)  
			new TheUser().useContext(ctx).put( "name", "master" + i).insert(USE_MASTER); 

		String table = shardEqual(TheModUser.class, 10);
		TheModUser u = new TheModUser();
		u.alias("u");
		u.tableModel().setTableName(table);
		List<TheModUser> users = ctx.iQuery(new EntityListHandler(TheModUser.class, u), "select u.** from ", table,
				" u");
		Assert.assertEquals(13, users.size());
		System.out.println(users.get(0).getName());

		List<Map<String, Object>> users2 = ctx.iQuery(new SSMapListHandler(User.class, u), "select u.** from ",
				shardEqual(TheModUser.class, 10), " u", USE_SLAVE);
		Assert.assertEquals(13, users2.size());
		System.out.println(users2);
	}
}