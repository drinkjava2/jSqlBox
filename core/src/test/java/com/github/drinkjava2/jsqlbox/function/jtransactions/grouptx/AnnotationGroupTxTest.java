package com.github.drinkjava2.jsqlbox.function.jtransactions.grouptx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jtransactions.grouptx.GroupTx;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

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
public class AnnotationGroupTxTest {
	static GroupTxConnectionManager gm;
	static HikariDataSource ds1;
	static HikariDataSource ds2;
	static SqlBoxContext ctx1;
	static SqlBoxContext ctx2;
	static {
		ds1 = new HikariDataSource();// DataSource
		// H2 is a memory database
		ds1.setDriverClassName("org.h2.Driver");
		ds1.setJdbcUrl("jdbc:h2:mem:DBName1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds1.setMaximumPoolSize(9);
		ds1.setConnectionTimeout(1000);
		ds1.setUsername("sa");
		ds1.setPassword("");

		ds2 = new HikariDataSource();// DataSource
		// H2 is a memory database
		ds2.setDriverClassName("org.h2.Driver");
		ds2.setJdbcUrl("jdbc:h2:mem:DBName2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds2.setMaximumPoolSize(9);
		ds2.setConnectionTimeout(1000);
		ds2.setUsername("sa");
		ds2.setPassword("");

		gm = new GroupTxConnectionManager(ds1, ds2);
	}

	@Before
	public void init() {
		ctx1 = new SqlBoxContext(ds1);
		ctx1.setConnectionManager(gm);

		ctx2 = new SqlBoxContext(ds2);
		ctx2.setConnectionManager(gm);

		String[] ddlArray = ctx1.toDropAndCreateDDL(Usr.class);
		for (String ddl : ddlArray) {
			ctx1.nExecute(ddl);
			ctx2.nExecute(ddl);
		}

		for (int i = 1; i <= 100; i++)
			new Usr().setFirstName("Foo" + i).setLastName("Bar" + i).setAge(i).insert(ctx1);
		for (int i = 1; i <= 100; i++)
			new Usr().setFirstName("FOO" + i).setLastName("BAR" + i).setAge(i).insert(ctx2);
		Assert.assertEquals(100, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(100, ctx2.eCountAll(Usr.class));
	}

	@After
	public void clean() {
		ds1.close();
		ds2.close();
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
		public Object create() {
			return new GroupTx(gm);
		}
	}

}