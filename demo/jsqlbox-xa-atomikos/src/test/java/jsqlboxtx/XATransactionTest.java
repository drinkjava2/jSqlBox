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

import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForLongValue;

import java.util.Properties;

import javax.transaction.SystemException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.AtomikosSQLException;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.TX;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class XATransactionTest {

	static int MASTER_DATABASE_QTY = 3; // total 3 databases
	static SqlBoxContext[] masters;
	static AtomikosDataSourceBean[] xaDataSources = new AtomikosDataSourceBean[MASTER_DATABASE_QTY];

	public static class Bank extends ActiveRecord {
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
	
	public static class SpringJtaBox extends BeanBox {
		public TransactionInterceptor create() {
			JtaTransactionManager springJta = new JtaTransactionManager();
			springJta.setUserTransaction(new UserTransactionImp());

			UserTransactionManager um = new UserTransactionManager();
			um.setForceShutdown(true);
			try {
				um.init();
			} catch (SystemException e) {
				e.printStackTrace();
			}
			springJta.setTransactionManager(um);
			springJta.setAllowCustomIsolationLevels(true);
			Properties props = new Properties();
			props.put("*", "PROPAGATION_REQUIRED, ISOLATION_READ_COMMITTED");
			TransactionInterceptor springTxI = new TransactionInterceptor(springJta, props);
			return springTxI;
		}
	}
	
	@Before
	public void init() {
		BeanBox.regAopAroundAnnotation(TX.class, SpringJtaBox.class);
		BeanBox.getBean(SpringJtaBox.class);// Not lazy!

		masters = new SqlBoxContext[MASTER_DATABASE_QTY];
		SqlBoxContext.setGlobalNextDialect(Dialect.MySQL57Dialect);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			JdbcDataSource ds = new JdbcDataSource();
			ds.setUrl("jdbc:h2:mem:H2DB" + i + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			ds.setUser("sa");
			ds.setPassword("");
			xaDataSources[i] = new AtomikosDataSourceBean();
			xaDataSources[i].setUniqueResourceName("xads" + i);
			xaDataSources[i].setXaDataSource(ds);
			xaDataSources[i].setPoolSize(5);
			try {
				xaDataSources[i].init();
			} catch (AtomikosSQLException e) {
				e.printStackTrace();
			}
			masters[i] = new SqlBoxContext(xaDataSources[i]);
			masters[i].setMasters(masters);

		}
		SqlBoxContext.setGlobalSqlBoxContext(masters[0]);// random choose 1
		TableModel model = TableModelUtils.entity2Model(Bank.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++)
			for (String ddl : masters[i].toCreateDDL(model))
				masters[i].iExecute(ddl);
	}

	@After
	public void cleanup() {
		// Did not close XAdataSource pools, did not close UserTransactionManager because no other unit tests use them
	}

	@TX
	public void doInsertThreeAccount() {
		new Bank().put("bankId", 0L, "balance", 100L).insert();
		Assert.assertEquals(1, giQueryForLongValue("select count(*) from bank", masters[0]));
		System.out.println("1 record inserted in database0, but will rollback");
		new Bank().put("bankId", 1L, "balance" + 1 / 0, -100L).insert();// div 0!
	} 

	@Test
	public void testXATransaction() {
		XATransactionTest tester = BeanBox.getBean(XATransactionTest.class);
		try {
			tester.doInsertThreeAccount();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals(0, giQueryForLongValue("select count(*) from bank", masters[0]));
		Assert.assertEquals(0, giQueryForLongValue("select count(*) from bank", masters[1])); 
	} 
}