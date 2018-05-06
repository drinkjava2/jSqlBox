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
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlItemType;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.JSQLBOX;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/*- 
Master (写操作: 主库              读操作：主表 )
Auto   (写操作或开启事务: 主      读操作并且无事务：从库中随机选一个)
Both   (写操作:主库和全部从库     读操作:只从主表读)
Slave  (写操作:全部从库           读操作:从库中随机选一个) 
*/

/**
 * MasterSlaveTest of jSqlBox
 * 
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class MasterSlaveTest {
	final static int SLAVE_DATABASE_QTY = 20;
	final static int SLAVE_TOTAL_ROWS = 5;
	final static int MASTER_TOTAL_ROWS = 10;
	SqlBoxContext ctx;
	SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
	public static HikariDataSource masterDs;
	HikariDataSource[] slaveDs = new HikariDataSource[SLAVE_DATABASE_QTY];

	public static class TheUser extends ActiveRecord {
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
		String[] ddls = ctx.toCreateDDL(TheUser.class);
		for (String ddl : ddls)
			ctx.iExecute(ddl, SqlItem.USE_BOTH);

		for (long j = 0; j < SLAVE_TOTAL_ROWS; j++)// insert 5 row in all slaves
			new TheUser().useContext(ctx).put("id", j, "name", " Slave_Row" + j).insert(SqlItem.USE_SLAVE);

		for (long j = 0; j < MASTER_TOTAL_ROWS; j++)// insert 10 row in all slaves
			new TheUser().useContext(ctx).put("id", j, "name", " Master_Row" + j).insert(SqlItem.USE_MASTER);
	}

	@After
	public void cleanup() {
		for (String ddl : ctx.toDropDDL(TheUser.class))
			ctx.iExecute(ddl, SqlItem.USE_BOTH);
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++)
			slaveDs[i].close();
		masterDs.close();
	}

	@Test
	public void testCreateTables() {
		Assert.assertEquals(10L, ctx.iQueryForLongValue("select count(*) from TheUser", SqlItem.USE_MASTER));
		Assert.assertEquals(5L, ctx.iQueryForLongValue("select count(*) from TheUser", SqlItem.USE_SLAVE));
		TheUser u = new TheUser().useContext(ctx).load(0, " or name=?", JSQLBOX.param("Tom"), SqlItem.USE_MASTER,
				new PrintSqlHandler());
		System.out.println(u.getName());
	}

	@Test
	public void testMasterSlaveUpdate() {
		System.out.println("============Test testMasterSlaveUpdate==================");
		// AutoChoose, not in Transaction, should use Master
		ctx.pUpdate("update TheUser set name=? where id=3", "NewValue");
		TheUser u1 = ctx.load(TheUser.class, 3L, SqlItem.USE_MASTER);
		Assert.assertEquals("NewValue", u1.getName());
	}

	@Test
	public void testMasterSlaveQuery() {
		System.out.println("============Test testMasterSlaveNoTransaction==================");
		// AutoChoose, not in Transaction, should use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS, ctx.iQueryForLongValue("select count(*) from TheUser"));
		TheUser u1 = ctx.load(TheUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_TOTAL_ROWS,
				ctx.iQueryForLongValue(SqlItem.USE_MASTER, "select count(*) from TheUser"));
		TheUser u2 = ctx.load(TheUser.class, 1L, SqlItem.USE_MASTER);
		System.out.println(u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS,
				ctx.iQueryForLongValue("select count(*)", SqlItem.USE_SLAVE, " from TheUser"));
		TheUser u3 = ctx.load(TheUser.class, 1L, SqlItem.USE_SLAVE);
		System.out.println(u3.getName());
	}

	@Test
	public void testMasterSlaveQueryInTransaction() {
		System.out.println("============Test testMasterSlaveInTransaction==============");
		SqlBoxContext.resetGlobalSqlBoxVariants();
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setConnectionManager(TinyTxConnectionManager.instance());
		for (int i = 0; i < slaves.length; i++)
			config.addSlave(slaves[i]);

		SqlBoxContext anotherCTX = new SqlBoxContext(masterDs, config);
		BeanBox.regAopAroundAnnotation(TX.class, TheTxBox.class);// AOP register

		MasterSlaveTest tester = BeanBox.getBean(MasterSlaveTest.class);
		tester.queryInTransaction(anotherCTX);
	}

	@TX
	public void queryInTransaction(SqlBoxContext ctx) {
		// AutoChoose, in Transaction, should use master
		Assert.assertEquals(MASTER_TOTAL_ROWS, ctx.iQueryForLongValue("select count(*) from TheUser"));
		TheUser u1 = ctx.load(TheUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_TOTAL_ROWS,
				ctx.iQueryForLongValue(SqlItem.USE_MASTER, "select count(*) from TheUser"));
		TheUser u2 = ctx.load(TheUser.class, 1L, SqlItem.USE_MASTER);
		System.out.println(u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS,
				ctx.iQueryForLongValue(SqlItem.USE_SLAVE, "select count(*) from TheUser"));
		ctx.setMasterSlaveSelect$(SqlItemType.USE_SLAVE); // $ series method is not thread safe, not recommend to use
		TheUser u3 = new TheUser().useContext(ctx).load(1L);
		System.out.println(u3.getName());
		ctx.setMasterSlaveSelect$(SqlItemType.USE_AUTO);// Remember to restore to Auto type
	}

	public static class TheTxBox extends BeanBox {
		{
			this.setConstructor(TinyTx.class, masterDs, Connection.TRANSACTION_READ_COMMITTED);
		}
	}

}