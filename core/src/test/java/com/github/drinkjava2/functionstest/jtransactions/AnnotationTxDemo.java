package com.github.drinkjava2.functionstest.jtransactions;

import static com.github.drinkjava2.jbeanbox.JBEANBOX.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is jSqlBox Transaction Demo, will be put in wiki
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class AnnotationTxDemo {
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
		ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class));
		ctx.setConnectionManager(TinyTxConnectionManager.instance());
	}

	@TX
	public void tx_Insert1() {
		ctx.nExecute("insert into user_tb (id) values('123')");
	}

	@TX
	public void tx_Insert2() {
		ctx.nExecute("insert into user_tb (id) values('456')");
		Assert.assertEquals(2, ctx.nQueryForLongValue("select count(*) from user_tb "));
		System.out.println("Now have 2 records in user_tb, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		AnnotationTxText tester = BeanBox.getBean(AnnotationTxText.class);
		ctx.quiteExecute("drop table user_tb");
		ctx.nExecute("create table user_tb (id varchar(40))engine=InnoDB");

		Assert.assertEquals(0L, ctx.nQueryForLongValue("select count(*) from user_tb "));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));
			tester.tx_Insert2();// this one did not insert, roll back to 1
		} catch (Exception e) {
			// e.printStackTrace();
			Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));

		ctx.nExecute("drop table user_tb");
		JBEANBOX.close();// Release DataSource Pool
	}

	/*
	 * AOP is an annotation for annotation, the annotated annotation's value(target)
	 * is a class implemented AOP alliance's MethodInterceptor interface
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface TX {
		public Class<?> value() default TheTxBox.class;
	}

	public static class TheTxBox extends BeanBox {
		{
			this.injectConstruct(TinyTx.class, DataSource.class, inject(DataSourceBox.class));
		}
	}

}