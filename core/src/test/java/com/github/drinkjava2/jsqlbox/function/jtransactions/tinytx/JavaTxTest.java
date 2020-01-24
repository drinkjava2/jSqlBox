package com.github.drinkjava2.jsqlbox.function.jtransactions.tinytx;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxAOP;

/**
 * TinyTx is a tiny and clean declarative transaction tool, in this unit test
 * use jBeanBox's pure Java configuration.
 * 
 * To make jSqlBox core unit test clean, I put Spring TX demos in jSqlBox's demo
 * folder.
 *
 * @author Yong Zhu
 * @since 2.0
 */
public class JavaTxTest {

	DbContext ctx;
	{
		DbContext.resetGlobalVariants();
		ctx = new DbContext((DataSource) JBEANBOX.getBean(DataSourceBox.class));
	}

	public void tx_Insert1() {
		ctx.nExecute("insert into user_tb (id) values('123')");
	}

	public void tx_Insert2() {
		ctx.nExecute("insert into user_tb (id) values('456')");
		Assert.assertEquals(2, ctx.nQueryForLongValue("select count(*) from user_tb "));
		Systemout.println("Now have 2 records in user_tb, but will roll back to 1");
		Systemout.println(1 / 0);
	}

	@Test
	public void doTest() throws Exception {
		JBEANBOX.getBeanBox(JavaTxTest.class).addBeanAop(new TinyTxAOP(), "tx_*");
		JavaTxTest tester = JBEANBOX.getBean(JavaTxTest.class);
		String ddl = "create table user_tb (id varchar(40))";
		if (ctx.getDialect().isMySqlFamily())
			ddl += "engine=InnoDB";
		ctx.nExecute(ddl);

		Assert.assertEquals(0L, ctx.nQueryForLongValue("select count(*) from user_tb "));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (Exception e) {
			Systemout.println("Exception found: ");
			//e.printStackTrace();
			Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));
			Systemout.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));

		ctx.nExecute("drop table user_tb");
		JBEANBOX.close();// Release DataSource Pool
	}
}