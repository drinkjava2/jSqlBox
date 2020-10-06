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
package com.github.drinkjava2.jsqlbox.function;

import static com.github.drinkjava2.jsqlbox.DB.USE_BOTH;
import static com.github.drinkjava2.jsqlbox.DB.USE_MASTER;
import static com.github.drinkjava2.jsqlbox.DB.USE_SLAVE;
import static com.github.drinkjava2.jsqlbox.DB.par;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxAOP;
import com.zaxxer.hikari.HikariDataSource;

/*- 
SqlOptions: 
USE_MASTER (write: Master              read：  Master)
USE_AUTO   (write: Master              read: If not in TX random choose 1 Slave, otherwise use Master))
USE_BOTH   (write: Master+All Slaves!  read: Master)
USE_SLAVE  (write: All Slaves!         read: Random 1 Slave ) 
*/

/**
 * MasterSlaveTest of jSqlBox
 * 
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class MasterSlaveTest {
	static final int SLAVE_DATABASE_QTY = 20;
	static final int SLAVE_RECORD_ROWS = 5;
	static final int MASTER_RECORD_ROWS = 10;
	DbContext master;

	public static class TheUser extends ActiveRecord<TheUser> {
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
		DbContext[] slaves = new DbContext[SLAVE_DATABASE_QTY];
		for (int i = 0; i < SLAVE_DATABASE_QTY; i++)
			slaves[i] = new DbContext(TestBase.createH2_HikariDataSource("SlaveDB" + i));
		master = new DbContext(TestBase.createH2_HikariDataSource("MasterDb"));
		master.setSlaves(slaves);
		String[] ddls = master.toCreateDDL(TheUser.class);
		for (String ddl : ddls)
			master.exe(ddl, USE_BOTH);

		for (long j = 0; j < SLAVE_RECORD_ROWS; j++)// insert 5 row in slaves
			new TheUser().useContext(master).putField("id", j, "name", "Slave_Row" + j).insert(USE_SLAVE);

		for (long j = 0; j < MASTER_RECORD_ROWS; j++)// insert 10 row in masters
			new TheUser().useContext(master).putField("id", j, "name", "Master_Row" + j).insert(USE_MASTER);
	}

	@After
	public void cleanup() {
		for (String ddl : master.toDropDDL(TheUser.class))
			master.exe(ddl, USE_BOTH);
		for (DbPro pro : master.getSlaves())
			((HikariDataSource) pro.getDataSource()).close();
		((HikariDataSource) master.getDataSource()).close();
	}

	@Test
	public void testCreateTables() {
		Assert.assertEquals(10L, master.qryLongValue("select count(*) from TheUser", USE_MASTER));
		Assert.assertEquals(5L, master.qryLongValue("select count(*) from TheUser", USE_SLAVE));
		TheUser u = new TheUser().useContext(master).loadById(0L, " or name=?", par("Tom"), USE_MASTER, new PrintSqlHandler());
		Systemout.println(u.getName());
	}

	@Test
	public void testMasterSlaveUpdate() {
		Systemout.println("============Test testMasterSlaveUpdate==================");
		// AutoChoose, not in Transaction, should use Master
		master.upd("update TheUser set name=? where id=3", par("NewValue"));
		// TheUser u1 = master.loadById(TheUser.class, 3L, USE_MASTER);
		TheUser u1 = new TheUser().useContext(master).putField("id", 3L).load(USE_MASTER);
		Assert.assertEquals("NewValue", u1.getName());
		TheUser u2 = master.entityLoadById(TheUser.class, 3L, USE_SLAVE);
		Assert.assertEquals("Slave_Row3", u2.getName());
	}

	@Test
	public void testMasterSlaveQuery() {
		Systemout.println("============Test testMasterSlaveNoTransaction==================");
		// AutoChoose, not in Transaction, should use slave
		Assert.assertEquals(SLAVE_RECORD_ROWS, master.qryLongValue("select count(*) from TheUser"));
		TheUser u1 = master.entityLoadById(TheUser.class, 1L);
		Assert.assertEquals("Slave_Row1", u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_RECORD_ROWS, master.qryLongValue(USE_MASTER, "select count(*) from TheUser"));
		TheUser u2 = master.entityLoadById(TheUser.class, 1L, USE_MASTER);
		Assert.assertEquals("Master_Row1", u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_RECORD_ROWS, master.qryLongValue("select count(*)", USE_SLAVE, " from TheUser"));
		TheUser u3 = master.entityLoadById(TheUser.class, 1L, USE_SLAVE);
		Assert.assertEquals("Slave_Row1", u3.getName());
	}

	private static HikariDataSource txDataSource;

	@Test
	public void testMasterSlaveQueryInTransaction() {
		Systemout.println("============Test testMasterSlaveInTransaction==============");
		DbContext.resetGlobalVariants();
		txDataSource = TestBase.createH2_HikariDataSource("MasterDb");
		// Build another master but run in Transaction mode
		DbContext masterWithTx = new DbContext(txDataSource);

		masterWithTx.setSlaves(master.getSlaves());
		MasterSlaveTest tester = JBEANBOX.getBean(MasterSlaveTest.class); // Proxy
		tester.queryInTransaction(masterWithTx);
		txDataSource.close();// don't forget close DataSource pool
	}

	@TX
	public void queryInTransaction(DbContext ctx) {
		// AutoChoose, in Transaction, should use master
		Assert.assertEquals(MASTER_RECORD_ROWS, ctx.qryLongValue("select count(*) from TheUser"));
		TheUser u1 = ctx.entityLoadById(TheUser.class, 1L);
		Assert.assertEquals("Master_Row1", u1.getName());

		// Force use master
		Assert.assertEquals(MASTER_RECORD_ROWS, ctx.qryLongValue(USE_MASTER, "select count(*) from TheUser"));
		TheUser u2 = ctx.entityLoadById(TheUser.class, 1L, USE_MASTER);
		Assert.assertEquals("Master_Row1", u2.getName());

		// Force use slave
		Assert.assertEquals(SLAVE_RECORD_ROWS, ctx.qryLongValue(USE_SLAVE, "select count(*) from TheUser"));
		TheUser u3 = new TheUser().useContext(ctx).putField("id", 1L).load(USE_SLAVE);
		Assert.assertEquals("Slave_Row1", u3.getName());
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface TX {
		public Class<?> value() default TinyTxAOP.class;
	}

}