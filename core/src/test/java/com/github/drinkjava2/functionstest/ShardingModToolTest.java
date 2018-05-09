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
import static com.github.drinkjava2.jdbpro.JDBPRO.USE_MASTER;
import static com.github.drinkjava2.jdbpro.JDBPRO.USE_SLAVE;
import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jdbpro.JDBPRO.switchTo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardTable;
import com.github.drinkjava2.jdialects.annotation.jdia.Snowflake;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class ShardingModToolTest {

	final static int MASTER_DATABASE_QTY = 5;
	final static int SLAVE_DATABASE_QTY = 10; // each master has 10 slaves
	final static int TABLE_QTY = 15; // each table be sharded to 15 tables

	SqlBoxContext[] masters = new SqlBoxContext[MASTER_DATABASE_QTY];

	public static class TheModUser extends ActiveRecord {
		@ShardTable({ "MOD", "8" })
		@Snowflake
		@Id
		private Long id;

		private String name;

		@Snowflake
		@ShardDatabase({ "MOD", "3" })
		private Long databaseId;

		//@formatter:off
		public Long getId() {return id;}
		public void setId(Long id) {this.id = id;}
		public String getName() {return name;}
		public void setName(String name) {this.name = name;}
		public Long getDatabaseId() {return databaseId;}
		public void setDatabaseId(Long databaseId) {this.databaseId = databaseId; } 
		//@formatter:on
	}

	@Before
	public void init() {
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
			masters[i] = new SqlBoxContext(TestBase.createH2_HikariDataSource("masters" + i));
			masters[i].setMasters(masters);
			masters[i].setSlaves(slaves);
			masters[i].setSnowflakeCreator(new SnowflakeCreator(5, 5, 0, i));
			for (int j = 0; j < SLAVE_DATABASE_QTY; j++)
				slaves[j] = new SqlBoxContext(TestBase.createH2_HikariDataSource("SlaveDB" + i + "_" + j));
		}

		TableModel model = TableModelUtils.entity2Model(TheModUser.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			for (int j = 0; j < TABLE_QTY; j++) {// Create master/salve tables
				model.setTableName("TheModUser" + "_" + j);
				for (String ddl : masters[i].getDialect().toCreateDDL(model))
					masters[i].iExecute(ddl, USE_BOTH);
			}
		}
	}

	@After
	public void cleanup() {
		TableModel model = TableModelUtils.entity2Model(TheModUser.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			for (int j = 0; j < TABLE_QTY; j++) {
				model.setTableName("TheModUser" + "_" + j);
				for (String ddl : masters[i].getDialect().toDropDDL(model))
					masters[i].iExecute(ddl, USE_BOTH);
			}
		}
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			((HikariDataSource) masters[i].getDataSource()).close();
			for (int j = 0; j < SLAVE_DATABASE_QTY; j++)
				((HikariDataSource) masters[i].getSlaves()[j].getDataSource()).close();
		}
	}

	@Test
	public void testInsertSqlOnMaseterSlave() {
		masters[2].iExecute("insert into TheModUser_0 (id, name, databaseId) values(?,?,?)", param(1, "u1", 1),
				USE_BOTH);
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from TheModUser_0", USE_SLAVE));
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from TheModUser_0", USE_MASTER));
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from TheModUser_0"));

		masters[2].iExecute("insert into TheModUser_0 (id, name, databaseId) values(?,?,?)", param(1, "u2", 1),
				switchTo(masters[3]), USE_BOTH);
		Assert.assertEquals(1, masters[3].iQueryForLongValue("select count(*) from TheModUser_0", USE_MASTER));
		Assert.assertEquals(1, masters[3].iQueryForLongValue("select count(*) from TheModUser_0", USE_SLAVE));
		Assert.assertEquals(1, masters[3].iQueryForLongValue("select count(*) from TheModUser_0"));

		masters[2].iExecute("insert into TheModUser_0 (id, name, databaseId) values(?,?,?)", param(1, "u2", 1),
				switchTo(masters[4]));// default insert to master only
		Assert.assertEquals(1, masters[4].iQueryForLongValue("select count(*) from TheModUser_0", USE_MASTER));
		Assert.assertEquals(0, masters[4].iQueryForLongValue("select count(*) from TheModUser_0", USE_SLAVE));
		Assert.assertEquals(0, masters[4].iQueryForLongValue("select count(*) from TheModUser_0"));
	}

	@Test
	public void testActiveRecordOnMasterSlaves() {
		new TheModUser().useContext(masters[2]).put("name", "user").insert(USE_BOTH);
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from TheModUser_0", USE_SLAVE));
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from TheModUser_0", USE_MASTER));
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from TheModUser_0"));

		new TheModUser().useContext(masters[2]).put("name", "user").insert(USE_BOTH, switchTo(masters[3]));
		Assert.assertEquals(1, masters[3].iQueryForLongValue("select count(*) from TheModUser_0", USE_MASTER));
		Assert.assertEquals(1, masters[3].iQueryForLongValue("select count(*) from TheModUser_0", USE_SLAVE));
		Assert.assertEquals(1, masters[3].iQueryForLongValue("select count(*) from TheModUser_0"));

		new TheModUser().useContext(masters[2]).put("name", "user").insert(switchTo(masters[4]));
		Assert.assertEquals(1, masters[4].iQueryForLongValue("select count(*) from TheModUser_0", USE_MASTER));
		Assert.assertEquals(0, masters[4].iQueryForLongValue("select count(*) from TheModUser_0", USE_SLAVE));
		Assert.assertEquals(0, masters[4].iQueryForLongValue("select count(*) from TheModUser_0"));

	}

	// @Test
	public void testInsertSqlOnShardingTables() {// TODO
	}

}