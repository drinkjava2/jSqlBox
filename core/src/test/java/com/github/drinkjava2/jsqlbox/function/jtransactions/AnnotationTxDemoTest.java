package com.github.drinkjava2.jsqlbox.function.jtransactions;

import static com.github.drinkjava2.jbeanbox.JBEANBOX.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is jSqlBox Transaction Demo by using Annotation
 * 
 * To make jSqlBox core unit test clean, I put Spring TX demos in jSqlBox's demo
 * folder.
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class AnnotationTxDemoTest {
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

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface TX { // This is a customized AOP annotation
		public Class<?> value() default TheTxBox.class;
	}

	public static class TheTxBox extends BeanBox {
		{
			this.injectConstruct(TinyTx.class, DataSource.class, inject(DataSourceBox.class));
		}
	}

	SqlBoxContext ctx;
	{
		SqlBoxContext.resetGlobalVariants();
		ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class));
		ctx.setConnectionManager(TinyTxConnectionManager.instance());
	}

	@TX
	public void txInsert() {
		ctx.nExecute("insert into user_tb (id) values('123')");
		Systemout.println(1 / 0); // DIV 0!
	}

	@Test
	public void doTest() {
		AnnotationTxDemoTest tester = BeanBox.getBean(AnnotationTxDemoTest.class);
		ctx.quiteExecute("drop table user_tb");
		String ddl = "create table user_tb (id varchar(40))";
		if (ctx.getDialect().isMySqlFamily())
			ddl += "engine=InnoDB";
		ctx.nExecute(ddl);
		try {
			tester.txInsert();// this one did not insert, rolled back
		} catch (Exception e) {
			Systemout.println("Exception found: " + e.getMessage());
		}
		ctx.nExecute("drop table user_tb");
		JBEANBOX.close();// Close DataSource Pool
	}

}