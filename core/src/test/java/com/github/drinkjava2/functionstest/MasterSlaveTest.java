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

import java.sql.Connection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.TX;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.SqlOption;
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
Options: 
USE_MASTER (写操作: 主库              读操作：主库)
USE_AUTO   (写操作: 主                  读操作且无事务：从库随机选一个      读操作且有事务:主 )
USE_BOTH   (写操作:主库和全部从库     读操作:主库)
USE_SLAVE  (写操作:全部从库           读操作:从库随机选一个) 
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
	SqlBoxContext master;

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
		SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++)
			slaves[i] = new SqlBoxContext(TestBase.createH2_HikariDataSource("SlaveDB" + i));
		master = new SqlBoxContext(TestBase.createH2_HikariDataSource("MasterDb"));
		master.setSlaves(slaves);
		String[] ddls = master.toCreateDDL(TheUser.class);
		for (String ddl : ddls)
			master.iExecute(ddl, USE_BOTH);

		for (long j = 0; j < SLAVE_TOTAL_ROWS; j++)// insert 5 row in all slaves
			new TheUser().useContext(master).put("id", j, "name", " Slave_Row" + j).insert(USE_SLAVE);

		for (long j = 0; j < MASTER_TOTAL_ROWS; j++)// insert 10 row in all slaves
			new TheUser().useContext(master).put("id", j, "name", " Master_Row" + j).insert(USE_MASTER);
	}

	@After
	public void cleanup() {
		for (String ddl : master.toDropDDL(TheUser.class))
			master.iExecute(ddl, USE_BOTH);
		for (DbPro pro : master.getSlaves())
			((HikariDataSource) pro.getDataSource()).close();
		((HikariDataSource) master.getDataSource()).close();
	}

	@Test
	public void testCreateTables() {
		Assert.assertEquals(10L, master.iQueryForLongValue("select count(*) from TheUser", SqlOption.USE_MASTER));
		Assert.assertEquals(5L, master.iQueryForLongValue("select count(*) from TheUser", USE_SLAVE));
		TheUser u = new TheUser().useContext(master).load(0, " or name=?", JSQLBOX.param("Tom"), USE_MASTER,
				new PrintSqlHandler());
		System.out.println(u.getName());
	}

	@Test
	public void testMasterSlaveUpdate() {
		System.out.println("============Test testMasterSlaveUpdate==================");
		// AutoChoose, not in Transaction, should use Master
		master.pUpdate("update TheUser set name=? where id=3", "NewValue");
		TheUser u1 = master.load(TheUser.class, 3L, USE_MASTER);
		Assert.assertEquals("NewValue", u1.getName());
	}

	@Test
	public void testMasterSlaveQuery() {
		System.out.println("============Test testMasterSlaveNoTransaction==================");
		// AutoChoose, not in Transaction, should use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS, master.iQueryForLongValue("select count(*) from TheUser"));
		TheUser u1 = master.load(TheUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_TOTAL_ROWS, master.iQueryForLongValue(USE_MASTER, "select count(*) from TheUser"));
		TheUser u2 = master.load(TheUser.class, 1L, USE_MASTER);
		System.out.println(u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS, master.iQueryForLongValue("select count(*)", USE_SLAVE, " from TheUser"));
		TheUser u3 = master.load(TheUser.class, 1L, USE_SLAVE);
		System.out.println(u3.getName());
	}

	private static HikariDataSource txDataSource;

	@Test
	public void testMasterSlaveQueryInTransaction() {
		System.out.println("============Test testMasterSlaveInTransaction==============");
		SqlBoxContext.resetGlobalVariants();
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setConnectionManager(TinyTxConnectionManager.instance());
		txDataSource = TestBase.createH2_HikariDataSource("MasterDb");
		// Build another master but run in Transaction mode
		SqlBoxContext MasterWithTx = new SqlBoxContext(txDataSource, config);
		MasterWithTx.setSlaves(master.getSlaves());
		BeanBox.regAopAroundAnnotation(TX.class, TheTxBox.class);// AOP TX register

		MasterSlaveTest tester = BeanBox.getBean(MasterSlaveTest.class); // AOP proxy get
		tester.queryInTransaction(MasterWithTx);
		txDataSource.close();// don't forget close DataSource pool
	}

	@TX
	public void queryInTransaction(SqlBoxContext ctx) {
		// AutoChoose, in Transaction, should use master
		Assert.assertEquals(MASTER_TOTAL_ROWS, ctx.iQueryForLongValue("select count(*) from TheUser"));
		TheUser u1 = ctx.load(TheUser.class, 1L);
		System.out.println(u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_TOTAL_ROWS, ctx.iQueryForLongValue(USE_MASTER, "select count(*) from TheUser"));
		TheUser u2 = ctx.load(TheUser.class, 1L, USE_MASTER);
		System.out.println(u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_TOTAL_ROWS, ctx.iQueryForLongValue(USE_SLAVE, "select count(*) from TheUser"));
		TheUser u3 = new TheUser().useContext(ctx).load(1L, USE_SLAVE);
		System.out.println(u3.getName());
	}

	public static class TheTxBox extends BeanBox {
		{
			this.setConstructor(TinyTx.class, txDataSource, Connection.TRANSACTION_READ_COMMITTED);
		}
	}

}