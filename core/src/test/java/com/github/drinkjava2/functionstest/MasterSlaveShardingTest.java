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

import java.sql.Connection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.TX;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.Sharding;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class MasterSlaveShardingTest {
	SqlBoxContext ctx;
	SqlBoxContext[] slaves = new SqlBoxContext[10];
	public static HikariDataSource masterDs;
	HikariDataSource[] slaveDs = new HikariDataSource[10];

	public static class TheDemoUser extends ActiveRecord {
		@Sharding({ "mod", "8" })
		@PKey
		private Long id;

		@Sharding({ "size", "2000000" })
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
		// SqlBoxContext.setGlobalNextAllowShowSql(true);
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		for (int i = 0; i < 10; i++) {
			slaveDs[i] = new HikariDataSource();
			slaveDs[i].setJdbcUrl("jdbc:h2:mem:SlaveDB" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			slaveDs[i].setDriverClassName("org.h2.Driver");
			slaveDs[i].setUsername("sa");
			slaveDs[i].setPassword("");
			slaveDs[i].setMaximumPoolSize(8);
			slaveDs[i].setConnectionTimeout(2000);
			slaves[i] = new SqlBoxContext(slaveDs[i]);
			String[] slaveDDLs = slaves[i].toCreateDDL(TheDemoUser.class);
			for (String ddl : slaveDDLs)
				slaves[i].nExecute(ddl);
			for (long l = 0; l < 10; l++) // Each slave have 10 records
				new TheDemoUser().useContext(slaves[i]).put("id", l, "name", " slave" + i + "_" + l).insert();

			config.addSlave(slaves[i]);
		}
		masterDs = new HikariDataSource();
		masterDs.setJdbcUrl("jdbc:h2:mem:MasterDb;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		masterDs.setDriverClassName("org.h2.Driver");
		masterDs.setUsername("sa");
		masterDs.setPassword("");
		masterDs.setMaximumPoolSize(8);
		masterDs.setConnectionTimeout(2000);
		ctx = new SqlBoxContext(masterDs, config);
		String[] ddls = ctx.toCreateDDL(TheDemoUser.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
		for (long l = 0; l < 5; l++) // Master has 5 records
			new TheDemoUser().useContext(ctx).put("id", l, "name", " master_" + l).insert();
	}

	@After
	public void cleanup() {
		for (DbPro dbpro : ctx.getSlaves()) {
			SqlBoxContext ctx = (SqlBoxContext) dbpro;
			for (String ddl : ctx.toDropDDL(TheDemoUser.class))
				ctx.nExecute(ddl);
		}
		for (String ddl : ctx.toDropDDL(TheDemoUser.class))
			ctx.nExecute(ddl);
		for (int i = 0; i < 10; i++)
			slaveDs[i].close();
		masterDs.close();
	}

	@Test
	public void testMasterSlaveNoTransaction() {
		System.out.println("============Test testMasterSlaveNoTransaction==============");
		// AutoChoose, not in Transaction, should use slave
		Assert.assertEquals(10, ctx.iQueryForLongValue("select count(*) from TheDemoUser"));
		TheDemoUser u1 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master, should use master
		Assert.assertEquals(5, ctx.iQueryForLongValue(SqlItem.MASTER, "select count(*) from TheDemoUser"));
		TheDemoUser u2 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u2.getName());

		// Force use slave, should use slave
		Assert.assertEquals(10, ctx.iQueryForLongValue("select count(*)", SqlItem.SLAVE, " from TheDemoUser"));
		TheDemoUser u3 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u3.getName());

	}

	@Test
	public void testMasterSlaveInTransaction() {
		System.out.println("============Test testMasterSlaveInTransaction==============");
		SqlBoxContext.resetGlobalSqlBoxVariants();
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setConnectionManager(TinyTxConnectionManager.instance());
		for (int i = 0; i < slaves.length; i++)
			config.addSlave(slaves[i]);

		SqlBoxContext anotherCTX = new SqlBoxContext(masterDs, config);
		BeanBox.regAopAroundAnnotation(TX.class, TheTxBox.class);// AOP register

		MasterSlaveShardingTest tester = BeanBox.getBean(MasterSlaveShardingTest.class);
		tester.queryInTransaction(anotherCTX);
	}

	@TX
	public void queryInTransaction(SqlBoxContext ctx) {
		// AutoChoose, in Transaction, should use master
		Assert.assertEquals(5, ctx.iQueryForLongValue("select count(*) from TheDemoUser"));
		TheDemoUser u1 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master, should use master
		Assert.assertEquals(5, ctx.iQueryForLongValue(SqlItem.MASTER, "select count(*) from TheDemoUser"));
		TheDemoUser u2 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u2.getName());

		// Force use slave, should use slave
		Assert.assertEquals(10, ctx.iQueryForLongValue(SqlItem.SLAVE, "select count(*) from TheDemoUser"));
		ctx.setUseSlave$(true);
		TheDemoUser u3 = new TheDemoUser();
		u3.useContext(ctx);
		u3 = u3.load(1L);
		System.out.println(u3.getName());
	}

	public static class TheTxBox extends BeanBox {
		{
			this.setConstructor(TinyTx.class, masterDs, Connection.TRANSACTION_READ_COMMITTED);
		}
	}

	
	@Test
	public void testSharding() {
		System.out.println("============Test testMasterSlaveNoTransaction==============");
		// AutoChoose, not in Transaction, should use slave
		Assert.assertEquals(10, ctx.iQueryForLongValue("select count(*) from TheDemoUser"));
		TheDemoUser u1 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master, should use master
		Assert.assertEquals(5, ctx.iQueryForLongValue(SqlItem.MASTER, "select count(*) from TheDemoUser"));
		TheDemoUser u2 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u2.getName());

		// Force use slave, should use slave
		Assert.assertEquals(10, ctx.iQueryForLongValue("select count(*)", SqlItem.SLAVE, " from TheDemoUser"));
		TheDemoUser u3 = ctx.load(TheDemoUser.class, 1L);
		System.out.println(u3.getName());

	}
}