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

import static com.github.drinkjava2.jsqlbox.JSQLBOX.iQueryForLongValue;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Global Transaction commit Test
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxRollbackTest {
	static int DATABASE_QTY = 3;
	static SqlBoxContext[] masters = new SqlBoxContext[DATABASE_QTY];
	static DataSource[] xaDataSources = new DataSource[DATABASE_QTY];

	@Before
	public void init() {
		SqlBoxContext.setGlobalNextDialect(Dialect.MySQL57Dialect);
		for (int i = 0; i < DATABASE_QTY; i++) {
			xaDataSources[i] = JdbcConnectionPool.create(
					"jdbc:h2:mem:DBName" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
			masters[i] = new SqlBoxContext(xaDataSources[i]);
			masters[i].setMasters(masters);
			masters[i].openGtx("");
		}
		SqlBoxContext.setGlobalSqlBoxContext(masters[0]);// random choose 1
		TableModel model = TableModelUtils.entity2Model(BankAccount.class);
		for (int i = 0; i < DATABASE_QTY; i++)
			for (String ddl : masters[i].toCreateDDL(model))
				masters[i].iExecute(ddl);
	}

	public void insertAccountsFail() {
		new BankAccount().putField("bankId", 0L, "balance", 100L).insert();
		Assert.assertEquals(1, iQueryForLongValue("select count(*) from bank", masters[0]));
		System.out.println("In insertAccountsBad() method, 1 record inserted in database0, but will rollback");
		new BankAccount().putField("bankId", 1L, "balance", 200L).insert();
		new BankAccount().putField("bankId", 2L, "balance", 1 / 0).insert();// div 0!
	}

	@Test
	public void testXATransaction() {
		GtxRollbackTest tester = new GtxRollbackTest();
		try {
			tester.insertAccountsFail();
		} catch (Exception e) {
			System.out.println("Div 0 RuntimeException caused no records be inserted into any database.");
		}
		Assert.assertEquals(0, new BankAccount(0L).loadTry());// no data inserted
		Assert.assertEquals(0, new BankAccount(1L).loadTry());
		Assert.assertEquals(0, new BankAccount(2L).loadTry());
	}
}