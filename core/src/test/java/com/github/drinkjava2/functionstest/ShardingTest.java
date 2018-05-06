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
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdialects.annotation.jdia.Sharding;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.zaxxer.hikari.HikariDataSource;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class ShardingTest {

	final static int SLAVE_DATABASE_QTY = 20;
	final static int SLAVE_TOTAL_ROWS = 5;
	final static int MASTER_TOTAL_ROWS = 10;
	SqlBoxContext ctx;
	SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
	public static HikariDataSource masterDs;
	HikariDataSource[] slaveDs = new HikariDataSource[SLAVE_DATABASE_QTY];

	public static class TheModUser extends ActiveRecord {
		@Sharding({ "MOD", "16" })
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

	public static class RangeUser extends ActiveRecord {
		@Sharding({ "RANGE", "500" })
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
		// SqlBoxContext.setGlobalNextAllowShowSql(true);
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++) {
			slaveDs[i] = new HikariDataSource();
			slaveDs[i].setJdbcUrl("jdbc:h2:mem:SlaveDB" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			slaveDs[i].setDriverClassName("org.h2.Driver");
			slaveDs[i].setUsername("sa");
			slaveDs[i].setPassword("");
			slaveDs[i].setMaximumPoolSize(8);
			slaveDs[i].setConnectionTimeout(2000);
			slaves[i] = new SqlBoxContext(slaveDs[i]);
			String[] slaveDDLs = slaves[i].toCreateDDL(TheModUser.class);
			for (String ddl : slaveDDLs)
				slaves[i].nExecute(ddl);
			for (long l = 0; l < SLAVE_TOTAL_ROWS; l++)
				new TheModUser().useContext(slaves[i]).put("id", l, "name", " Slave" + i + "_Row" + l).insert();

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
		String[] ddls = ctx.toCreateDDL(TheModUser.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
		for (long l = 0; l < MASTER_TOTAL_ROWS; l++)
			new TheModUser().useContext(ctx).put("id", l, "name", " Master_Row" + l).insert();
	}

	@After
	public void cleanup() {
		for (DbPro dbpro : ctx.getSlaves()) {
			SqlBoxContext ctx = (SqlBoxContext) dbpro;
			for (String ddl : ctx.toDropDDL(TheModUser.class))
				ctx.nExecute(ddl);
		}
		for (String ddl : ctx.toDropDDL(TheModUser.class))
			ctx.nExecute(ddl);
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++)
			slaveDs[i].close();
		masterDs.close();
	}

	@Test
	public void testMasterSlaveUpdate() {
		System.out.println("============Test testMasterSlaveUpdate==================");
		// AutoChoose, not in Transaction, should use Master
		ctx.pUpdate("update TheModUser set name=? where id=3", "NewValue");
		TheModUser u1 = ctx.load(TheModUser.class, 3L, SqlItem.USE_MASTER);
		Assert.assertEquals("NewValue", u1.getName());
	}

	@Test
	public void testMasterSlaveQuery() {
		System.out.println("============Test testMasterSlaveNoTransaction==================");
		// AutoChoose, not in Transaction, should use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS, ctx.iQueryForLongValue("select count(*) from TheModUser"));
		TheModUser u1 = ctx.load(TheModUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_TOTAL_ROWS,
				ctx.iQueryForLongValue(SqlItem.USE_MASTER, "select count(*) from TheModUser"));
		TheModUser u2 = ctx.load(TheModUser.class, 1L, SqlItem.USE_MASTER);
		System.out.println(u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS,
				ctx.iQueryForLongValue("select count(*)", SqlItem.USE_SLAVE, " from TheModUser"));
		TheModUser u3 = ctx.load(TheModUser.class, 1L, SqlItem.USE_SLAVE);
		System.out.println(u3.getName());
	}

	public static class TheTxBox extends BeanBox {
		{
			this.setConstructor(TinyTx.class, masterDs, Connection.TRANSACTION_READ_COMMITTED);
		}
	}

}