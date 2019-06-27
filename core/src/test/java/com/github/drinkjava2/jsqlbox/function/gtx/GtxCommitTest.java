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
package com.github.drinkjava2.jsqlbox.function.gtx;

import static com.github.drinkjava2.jdbpro.JDBPRO.USE_BOTH;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
import com.github.drinkjava2.jsqlbox.gtx.GtxUndoLog;

/**
 * Global Transaction commit Test
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxCommitTest {
	static {
		SqlBoxContext.setGlobalNextAllowShowSql(true);
	}
	final static int DATABASE_QTY = 3; // total has 3 databases
	final static int TABLE_QTY = 2; // each databases has 2 sharding tables

	static SqlBoxContext[] ctxs = new SqlBoxContext[DATABASE_QTY];
	static DataSource[] datasources = new DataSource[DATABASE_QTY];

	static DataSource lockServerDS = JdbcConnectionPool
			.create("jdbc:h2:mem:lockServer;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
	static SqlBoxContext lockerServ = new SqlBoxContext(lockServerDS);

	GtxConnectionManager gtxMgr = new GtxConnectionManager(lockerServ, datasources);
	static int db = -1;

	@Before
	public void init() {
		lockerServ.setName("GTX Lock Server");
		lockerServ.nExecute("create table gtxlock (id varchar(500),  primary key (id)) engine=InnoDB ");
		SqlBoxContext.setGlobalNextDialect(Dialect.MySQL57Dialect);
		for (int i = 0; i < DATABASE_QTY; i++) {
			datasources[i] = JdbcConnectionPool.create(
					"jdbc:h2:mem:GTXTEST_DB" + ++db + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa",
					"");
			ctxs[i] = new SqlBoxContext(datasources[i]);
			ctxs[i].setMasters(ctxs);
			ctxs[i].setConnectionManager(gtxMgr);
			ctxs[i].setName("DB" + db);
		}
		SqlBoxContext.setGlobalSqlBoxContext(ctxs[0]);// random choose 1
		for (int i = 0; i < DATABASE_QTY; i++) {
			for (String ddl : ctxs[i].toCreateDDL(GtxUndoLog.class))
				ctxs[i].iExecute(ddl);// create GtxLog table
			for (int j = 0; j < TABLE_QTY; j++) {
				TableModel model = TableModelUtils.entity2Model(BankAccount.class);
				model.setTableName("BankAccount" + "_" + j); // create sharding tables
				for (String ddl : Dialect.MySQL57Dialect.toCreateDDL(model))
					ctxs[i].iExecute(ddl, USE_BOTH);
			}
		}
	}

	@Test
	public void crudTest() {
		gtxMgr.startGtx("Test");
		new BankAccount().putField("bankId", 0L, "userId", 0L, "balance", 10L).insert();
		new BankAccount().putField("bankId", 0L, "userId", 0L, "balance", 20L).update();
		// System.out.println(new BankAccount().putField("bankId", 0L, "userId",
		// 0L).exist());
		// System.out.println(new BankAccount().putField("bankId", 0L, "userId",
		// 5L).exist());
		// new BankAccount().putField("bankId", 0L, "userId", 0L).load();
		// new BankAccount().putField("bankId", 0L, "userId", 0L).delete();
	}

	public void insertAccountsSucess() {
		new BankAccount().putField("bankId", 0L, "userId", 0L, "balance", 100L).insert();
		new BankAccount().putField("bankId", 0L, "userId", 1L, "balance", 200L).insert();
		new BankAccount().putField("bankId", 1L, "userId", 0L, "balance", 300L).insert();
		new BankAccount().putField("bankId", 1L, "userId", 1L, "balance", 400L).insert();
		new BankAccount().putField("bankId", 2L, "userId", 0L, "balance", 500L).insert();
		new BankAccount().putField("bankId", 2L, "userId", 1L, "balance", 600L).insert();
	}

	public void testCommitTransaction() {
		GtxCommitTest tester = new GtxCommitTest();
		gtxMgr.startGtx("Test");
		try {
			tester.insertAccountsSucess();
			gtxMgr.commitGtx();
		} catch (Exception e) {
			gtxMgr.rollbackGtx();
		}
		Assert.assertEquals(100, new BankAccount(0L, 0L).load().getBalance().longValue());
		Assert.assertEquals(200, new BankAccount(0L, 1L).load().getBalance().longValue());
		Assert.assertEquals(300, new BankAccount(1L, 0L).load().getBalance().longValue());
		Assert.assertEquals(400, new BankAccount(1L, 1L).load().getBalance().longValue());
		Assert.assertEquals(500, new BankAccount(2L, 0L).load().getBalance().longValue());
		Assert.assertEquals(600, new BankAccount(2L, 1L).load().getBalance().longValue());
	}

}