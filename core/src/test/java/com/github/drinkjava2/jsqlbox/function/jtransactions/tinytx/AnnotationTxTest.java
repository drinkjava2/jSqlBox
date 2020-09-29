package com.github.drinkjava2.jsqlbox.function.jtransactions.tinytx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxAOP;

/**
 * TinyTx is a tiny and clean declarative transaction tool, in this unit test
 * use jBeanBox's annotation configuration.
 * 
 * To make jSqlBox core unit test clean, I put Spring TX demos in jSqlBox's demo
 * folder.
 *
 * @author Yong Zhu
 * @since 2.0
 */
public class AnnotationTxTest {
	DbContext ctx;
	{
		DbContext.resetGlobalVariants();
		ctx = new DbContext((DataSource) JBEANBOX.getBean(DataSourceBox.class));
	}

	@TX
	public void tx_Insert1() {
		ctx.jdbcExecute("insert into user_tb (id) values('123')");
	}

	@TX
	public void tx_Insert2() {
		ctx.jdbcExecute("insert into user_tb (id) values('456')");
		Assert.assertEquals(2, ctx.qryLongValue("select count(*) from user_tb "));
		Systemout.println("Now have 2 records in user_tb, but will roll back to 1");
		Systemout.println(1 / 0);
	}

	@Test
	public void doTest() {
		AnnotationTxTest tester = JBEANBOX.getBean(AnnotationTxTest.class);
		ctx.quiteExecute("drop table user_tb");
		String ddl = "create table user_tb (id varchar(40))";
		if (ctx.getDialect().isMySqlFamily())
			ddl += "engine=InnoDB";
		ctx.jdbcExecute(ddl);

		Assert.assertEquals(0L, ctx.qryLongValue("select count(*) from user_tb "));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			Assert.assertEquals(1L, ctx.qryLongValue("select count(*) from user_tb "));
			tester.tx_Insert2();// this one did not insert, roll back to 1
		} catch (Exception e) {
			// Systemout.println("Exception found: " + e.getMessage());
			Assert.assertEquals(1L, ctx.qryLongValue("select count(*) from user_tb "));
			Systemout.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1L, ctx.qryLongValue("select count(*) from user_tb "));

		ctx.jdbcExecute("drop table user_tb");
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
		public Class<?> value() default TinyTxAOP.class;
	}

}