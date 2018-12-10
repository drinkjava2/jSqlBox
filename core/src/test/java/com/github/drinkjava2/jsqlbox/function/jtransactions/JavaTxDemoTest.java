package com.github.drinkjava2.jsqlbox.function.jtransactions;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is jSqlBox Transaction Demo by using Java Configuration
 * 
 * To make jSqlBox core unit test clean, I put Spring TX demos in jSqlBox's demo
 * folder.
 *
 * @author Yong Zhu
 * @since 2.0.4
 */
public class JavaTxDemoTest {

	public static class DataSourceCfg extends BeanBox {
		{
			setProperty("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			setProperty("driverClassName", "org.h2.Driver");
			setProperty("username", "sa");
			setProperty("password", "");
		}

		public HikariDataSource create() {
			HikariDataSource ds = new HikariDataSource();
			ds.setMaximumPoolSize(10);
			ds.setConnectionTimeout(5000);
			this.setPreDestroy("close");// jBeanBox will close pool
			return ds;
		}
	}

	SqlBoxContext ctx;
	{
		SqlBoxContext.resetGlobalVariants();
		ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceCfg.class));
		ctx.setConnectionManager(TinyTxConnectionManager.instance());
	}

	public void txInsert() {
		ctx.nExecute("insert into user_tb (id) values('abc')");
		Systemout.println(1 / 0); // DIV 0!
	}

	@Test
	public void doTest() throws Exception {
		TinyTx aop = new TinyTx((DataSource) JBEANBOX.getBean(DataSourceCfg.class));
		JBEANBOX.getBeanBox(JavaTxDemoTest.class).addBeanAop(aop, "tx*");
		JavaTxDemoTest tester = BeanBox.getBean(JavaTxDemoTest.class);
		ctx.nExecute("create table user_tb (id varchar(40))engine=InnoDB");
		try {
			tester.txInsert();// this one did not insert, rolled back
		} catch (Exception e) {
			Systemout.println("Exception found: " + e.getMessage());
		}
		ctx.nExecute("drop table user_tb");
		JBEANBOX.close();// Close DataSource Pool
	}
}