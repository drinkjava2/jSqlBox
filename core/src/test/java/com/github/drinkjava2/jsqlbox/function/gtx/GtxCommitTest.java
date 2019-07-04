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
	final static int DATABASE_QTY = 10; // 10 sharding databases
	final static int TABLE_QTY = 2; // Each database have 2 sharding tables

	final static int GTX_SERV_QTY = 2; // Total have 2 gtx server
	GtxConnectionManager gtxMgr = null;

	@Before
	public void init() {
		SqlBoxContext.setGlobalNextAllowShowSql(true);
		SqlBoxContext.setGlobalNextDialect(Dialect.MySQL57Dialect);

		SqlBoxContext[] dbs = new SqlBoxContext[DATABASE_QTY];
		SqlBoxContext.setGlobalSqlBoxContext(dbs[0]);// random choose 1
		DataSource[] datasources = new DataSource[DATABASE_QTY];
		for (int i = 0; i < DATABASE_QTY; i++) {
			datasources[i] = JdbcConnectionPool.create(
					"jdbc:h2:mem:GTXTEST_DB" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
			dbs[i] = new SqlBoxContext(datasources[i]);
			dbs[i].setMasters(dbs);
			dbs[i].setConnectionManager(gtxMgr);
			dbs[i].setName("DB" + i);

			for (String ddl : dbs[i].toCreateDDL(GtxUndoLog.class))
				dbs[i].iExecute(ddl);// create GtxLog table
			for (int j = 0; j < TABLE_QTY; j++) {
				TableModel model = TableModelUtils.entity2Model(BankAccount.class);
				model.setTableName(model.getTableName() + "_" + j); // create sharding tables
				for (String ddl : Dialect.MySQL57Dialect.toCreateDDL(model))
					dbs[i].iExecute(ddl);
			}
		}

		DataSource gtxServDS = JdbcConnectionPool
				.create("jdbc:h2:mem:gtxServ;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
		SqlBoxContext gtxServ = new SqlBoxContext(gtxServDS);
		gtxServ.setName("GTX Lock Server");
		gtxServ.nExecute("create table gtxlock (id varchar(500),  primary key (id)) engine=InnoDB ");

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