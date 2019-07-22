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
import com.github.drinkjava2.jsqlbox.gtx.GtxInfo;
import com.github.drinkjava2.jsqlbox.gtx.GtxLock;
import com.github.drinkjava2.jsqlbox.gtx.GtxUtils;

/**
 * Global Transaction in sharding environment
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxShardingTest {
	final static int DB_QTY = 3; // 3 sharding databases
	final static int TABLE_QTY = 2; // Each database has 2 sharding tables

	final static int GTX_DB_QTY = 2; // Total have 3 gtx databases
	GtxConnectionManager gtxMgr;

	@Before
	public void init() {
		SqlBoxContext.setGlobalNextAllowShowSql(true);
		SqlBoxContext.setGlobalNextDialect(Dialect.MySQL57Dialect);

		SqlBoxContext[] gtxs = new SqlBoxContext[GTX_DB_QTY];
		for (int i = 0; i < GTX_DB_QTY; i++) {
			DataSource ds = JdbcConnectionPool.create(
					"jdbc:h2:mem:gtxServ_" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
			gtxs[i] = new SqlBoxContext(ds);
			gtxs[i].setMasters(gtxs);
			gtxs[i].setName("gtxServ_" + i);
			// gtxs[i].setConnectionManager(new ManualTx(ds));
			gtxs[i].executeDDL(gtxs[i].toCreateDDL(GtxInfo.class));
			gtxs[i].executeDDL(gtxs[i].toCreateDDL(GtxLock.class));
			gtxs[i].executeDDL(gtxs[i].toCreateDDL(GtxUtils.entity2GtxUndoLogModel(BankAccount.class)));
		}
		System.out.println("================gtxs tables created======================\r");

		SqlBoxContext[] ctxs = new SqlBoxContext[DB_QTY];
		DataSource[] datasources = new DataSource[DB_QTY];
		for (int i = 0; i < DB_QTY; i++) {
			datasources[i] = JdbcConnectionPool
					.create("jdbc:h2:mem:DB_" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
			ctxs[i] = new SqlBoxContext(datasources[i]);
			ctxs[i].setMasters(ctxs);
			ctxs[i].setConnectionManager(gtxMgr);
			ctxs[i].setName("DB" + i);
			for (int j = 0; j < TABLE_QTY; j++) {
				TableModel model = TableModelUtils.entity2Model(BankAccount.class);
				model.setTableName(model.getTableName() + "_" + j); // For each database, create sharding tables
				ctxs[i].executeDDL(ctxs[i].toCreateDDL(model));
			}
		}
		SqlBoxContext.setGlobalSqlBoxContext(ctxs[0]);// random choose 1
		System.out.println("================ctxs tables created======================\r");

	}

	@Test
	public void crudTest() {
		if (1 == 1)
			return;
		gtxMgr.startTransaction();
		new BankAccount().forFields("bankId", "userId", "balance");
		new BankAccount().putValues(0L, 0L, 10L).insert();

		new BankAccount().putValues(0L, 0L, 20L).update();
		Assert.assertTrue(new BankAccount().setBankId(0L).setUserId(0L).exist());
		Assert.assertTrue(new BankAccount().setBankId(0L).setUserId(0L).load().existStrict());
		Assert.assertFalse(new BankAccount().setBankId(0L).setUserId(0L).existStrict());
		Assert.assertFalse(new BankAccount().setBankId(0L).setUserId(5L).exist());
		new BankAccount().setBankId(0L).setUserId(0L).load().delete();
		Assert.assertFalse(new BankAccount().setBankId(0L).setUserId(0L).exist());
		System.out.println("-------CURD on DB0 Test finish--------\r");

		new BankAccount().forFields("bankId", "userId", "balance");
		new BankAccount().putValues(1L, 0L, 10L).insert();
		new BankAccount().putValues(1L, 0L, 20L).update();
		Assert.assertTrue(new BankAccount().setBankId(1L).setUserId(0L).exist());
		Assert.assertFalse(new BankAccount().setBankId(1L).setUserId(5L).exist());
		new BankAccount().setBankId(1L).setUserId(0L).load().delete();
		Assert.assertFalse(new BankAccount().setBankId(1L).setUserId(0L).exist());
		System.out.println("-------CURD on DB1 Test finish--------\r");
	}

	public void insertAccountsSucess() {
		// new BankAccount().putField("bankId", 0L, "userId", 0L, "balance",
		// 100L).insert();
		// new BankAccount().putField("bankId", 0L, "userId", 1L, "balance",
		// 200L).insert();
		// new BankAccount().putField("bankId", 1L, "userId", 0L, "balance",
		// 300L).insert();
		// new BankAccount().putField("bankId", 1L, "userId", 1L, "balance",
		// 400L).insert();
		// new BankAccount().putField("bankId", 2L, "userId", 0L, "balance",
		// 500L).insert();
		// new BankAccount().putField("bankId", 2L, "userId", 1L, "balance",
		// 600L).insert();
	}

	public void testCommitTransaction() {
		// GtxShardingTest tester = new GtxShardingTest();
		// gtxMgr.startTransaction();
		// try {
		// tester.insertAccountsSucess();
		// gtxMgr.commit();
		// } catch (Exception e) {
		// gtxMgr.rollback();
		// }
		// Assert.assertEquals(100, new BankAccount(0L,
		// 0L).load().getBalance().longValue());
		// Assert.assertEquals(200, new BankAccount(0L,
		// 1L).load().getBalance().longValue());
		// Assert.assertEquals(300, new BankAccount(1L,
		// 0L).load().getBalance().longValue());
		// Assert.assertEquals(400, new BankAccount(1L,
		// 1L).load().getBalance().longValue());
		// Assert.assertEquals(500, new BankAccount(2L,
		// 0L).load().getBalance().longValue());
		// Assert.assertEquals(600, new BankAccount(2L,
		// 1L).load().getBalance().longValue());
	}

}