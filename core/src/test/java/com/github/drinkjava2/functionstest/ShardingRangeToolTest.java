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
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardDB;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardTB;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardTable;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
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

public class ShardingRangeToolTest {

	final static int MASTER_DATABASE_QTY = 7;
	final static int SLAVE_DATABASE_QTY = 7; // each master has 7 slaves
	final static int TABLE_QTY = 8; // each table has 8 sharding

	SqlBoxContext[] masters = new SqlBoxContext[MASTER_DATABASE_QTY];

	public static class TheUser extends ActiveRecord {
		// 0~99 store in TheUser_0, 100~199 store in TheUser_1...
		@ShardTable({ "RANGE", "100" })
		@Id
		private Long id;

		private String name;

		// 0~9 store in database0, 10~19 store in database1...
		@ShardDatabase({ "RANGE", "10" })
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

	@SuppressWarnings("deprecation")
	@Before
	public void init() {
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
			masters[i] = new SqlBoxContext(TestBase.createH2_HikariDataSource("masters" + i));
			masters[i].setMasters(masters);
			masters[i].setSlaves(slaves);
			masters[i].setName("Master" + i);
			for (int j = 0; j < SLAVE_DATABASE_QTY; j++)
				slaves[j] = new SqlBoxContext(TestBase.createH2_HikariDataSource("SlaveDB" + i + "_" + j));
		}

		TableModel model = TableModelUtils.entity2Model(TheUser.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			for (int j = 0; j < TABLE_QTY; j++) {// Create master/salve tables
				model.setTableName("TheUser" + "_" + j);
				for (String ddl : masters[i].getDialect().toCreateDDL(model))
					masters[i].iExecute(ddl, USE_BOTH);
			}
		}
	}

	@After
	public void cleanup() {
		TableModel model = TableModelUtils.entity2Model(TheUser.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			for (int j = 0; j < TABLE_QTY; j++) {
				model.setTableName("TheUser" + "_" + j);
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

	static long tbID = 301L;
	static long dbID = 21L;

	@Test
	public void testInsertSQLs() {
		System.out.println(masters[2].getShardedDB(TheUser.class, dbID).getName());
		masters[2].iExecute("insert into ", shardTB(TheUser.class, tbID), shardDB(TheUser.class, dbID),
				" (id, name, databaseId) values(?,?,?)", param(tbID, "u1", dbID), USE_BOTH, new PrintSqlHandler());
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from ", shardTB(TheUser.class, tbID),
				shardDB(TheUser.class, dbID), USE_SLAVE, new PrintSqlHandler()));
		Assert.assertEquals(1, masters[2].iQueryForLongValue("select count(*) from ", shardTB(TheUser.class, tbID),
				shardDB(TheUser.class, dbID)));
	}

	@Test
	public void testActiveRecord() {// issue: XA or TCC transaction needed
		SqlBoxContext.setGlobalSqlBoxContext(masters[4]);// random select one
		TheUser u1 = new TheUser();
		u1.put("id", tbID, "databaseId", dbID, "name", "Tom").insert(USE_BOTH, new PrintSqlHandler());
		Assert.assertEquals("Master2", u1.ctx().getShardedDB(u1).getName());
		Assert.assertEquals("TheUser_3", u1.ctx().getShardedTB(u1));

		u1.setName("Sam");
		u1.update(USE_BOTH, new PrintSqlHandler());

		TheUser u2 = new TheUser();
		u2.setId(u1.getId());
		u2.setDatabaseId(u1.getDatabaseId());
		u2.load(new PrintSqlHandler(), " and name=?", param("Sam")); // use slave
		Assert.assertEquals("Sam", u2.getName());

		u2.delete(new PrintSqlHandler());// only deleted master except use
											// "USE_BOTH" option
		Assert.assertEquals(0, giQueryForLongValue("select count(*) from ", shardTB(u2), shardDB(u2), USE_MASTER));
		Assert.assertEquals(1, giQueryForLongValue("select count(*) from ", shardTB(u2), shardDB(u2)));
	}

}