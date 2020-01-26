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
package jsqlboxtx;

import static com.github.drinkjava2.jsqlbox.DB.iQueryForLongValue;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxAOP;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * Non-XA Transaction test, H2 + jBeanBox + jSqlBox + jTransaction
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class NonXATransactionTest {

	final static int MASTER_DATABASE_QTY = 2; // total 2 databases
	final static DbContext[] masters = new DbContext[MASTER_DATABASE_QTY];

	@Before
	public void init() {
		DbContext.setGlobalNextAllowShowSql(true);
		DbContext.setGlobalNextConnectionManager(TinyTxConnectionManager.instance());
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			masters[i] = new DbContext(JdbcConnectionPool.create(
					"jdbc:h2:mem:Database" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", ""));
			masters[i].setMasters(masters);
			masters[i].setConnectionManager(null);
		}
		DbContext.setGlobalDbContext(masters[0]);// random choose 1
		TableModel model = TableModelUtils.entity2Model(Bank.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++)
			for (String ddl : masters[i].toCreateDDL(model))
				masters[i].iExecute(ddl);
	}

	@After
	public void cleanup() {
		for (DbContext ctx : masters)
			((JdbcConnectionPool) ctx.getDataSource()).dispose();
		JBEANBOX.close();
	}

	public void insertAccount() {
		new Bank().putField("bankId", 0L, "balance", 100L).insert(); // committed
		new Bank().putField("bankId", 1L, "balance", 1 / 0).insert();// Div 0!
	}

	@Test
	public void doTest() {
		JBEANBOX.ctx().addContextAop(new TinyTxAOP(), NonXATransactionTest.class, "insert*");
		NonXATransactionTest tester = JBEANBOX.getBean(NonXATransactionTest.class);
		try {
			tester.insertAccount();
		} catch (Exception e) {
			System.out.println("Note:" + e.getMessage());
			e.printStackTrace();
			System.out.println("Div 0 RuntimeException happened, but 1 database did not rollback ");
		}
		Assert.assertEquals(1L, iQueryForLongValue("select count(*) from bank", masters[0]));
		Assert.assertEquals(0L, iQueryForLongValue("select count(*) from bank", masters[1]));
	}

	public static class Bank extends ActiveRecord<Bank> {
		@ShardDatabase({ "MOD", "3" })
		@Id
		private Long bankId;

		private Long balance;

		//@formatter:off 
		public Long getBankId() {return bankId;}
		public void setBankId(Long bankId) {this.bankId = bankId;}
		public Long getBalance() {return balance;}
		public void setBalance(Long balance) {this.balance = balance;}
	}
}