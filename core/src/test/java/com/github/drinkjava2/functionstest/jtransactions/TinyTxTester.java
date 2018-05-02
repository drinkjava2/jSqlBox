package com.github.drinkjava2.functionstest.jtransactions;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.TX;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * This is to test TinyTx Declarative Transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyTxTester {
	SqlBoxContext txCtx;
	{
		SqlBoxContext.resetGlobalSqlBoxVariants();
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setConnectionManager(TinyTxConnectionManager.instance());
		txCtx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class), config);
		BeanBox.regAopAroundAnnotation(TX.class, TinyTxBox.class);
	}

	@TX
	public void tx_Insert1() {
		txCtx.nExecute("insert into user_tb (id) values('123')");
	}

	@TX
	public void tx_Insert2() {
		txCtx.nExecute("insert into user_tb (id) values('456')");
		Assert.assertEquals(2, txCtx.nQueryForLongValue("select count(*) from user_tb "));
		System.out.println("Now have 2 records in user_tb, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {

		TinyTxTester tester = BeanBox.getBean(TinyTxTester.class);

		try {
			txCtx.nExecute("drop table user_tb ");
		} catch (Exception e) {
		}
		txCtx.nExecute("create table user_tb (id varchar(40))engine=InnoDB");

		Assert.assertEquals(0L, txCtx.nQueryForLongValue("select count(*) from user_tb "));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1L, txCtx.nQueryForLongValue("select count(*) from user_tb "));
		BeanBox.defaultContext.close();// Release DataSource Pool
	}

}