package com.github.drinkjava2.jsqlbox.function.jtransactions.grouptx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.HikariCPBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxAOP;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Annotation GroupTx unit Test
 *
 * @author Yong Zhu
 * @since 2.0
 */
public class AnnotationGroupTxTest {
	SqlBoxContext ctx1 = JBEANBOX.getBean(SqlBoxContextBox1.class);
	SqlBoxContext ctx2 = JBEANBOX.getBean(SqlBoxContextBox2.class);

	@Before
	public void init() {
		ctx1.executeDDL(ctx1.toCreateDDL(Usr.class));
		ctx2.executeDDL(ctx2.toCreateDDL(Usr.class));
		for (int i = 1; i <= 100; i++) {
			new Usr().setFirstName("Foo" + i).setLastName("Bar" + i).setAge(i).insert(ctx1);
			new Usr().setFirstName("FOO" + i).setLastName("BAR" + i).setAge(i).insert(ctx2);
		}

		Assert.assertEquals(100, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(100, ctx2.eCountAll(Usr.class));
	}

	@After
	public void clean() {
		JBEANBOX.close();
	}

	@GTransaction
	public void groupRollback() { // test group roll back
		new Usr().setFirstName("Foo").insert(ctx1);
		new Usr().setFirstName("Bar").insert(ctx2);
		System.out.println(1 / 0); // div 0!
	}

	@GTransaction
	public void groupCommit() { // test group roll back
		new Usr().setFirstName("Foo").insert(ctx1);
		new Usr().setFirstName("Bar").insert(ctx2);
	}

	@GTransaction
	public void groupPartialCommit() { // test group Partial Commit
		new Usr().setFirstName("Foo").insert(ctx1);
		new Usr().setFirstName("Bar").insert(ctx2);
		((HikariDataSource) JBEANBOX.getBean(Ds2.class)).close();
	}

	@Test
	public void groupTest() {
		AnnotationGroupTxTest t = JBEANBOX.getBean(AnnotationGroupTxTest.class);
		try {
			t.groupRollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals(100, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(100, ctx2.eCountAll(Usr.class));

		t.groupCommit();
		Assert.assertEquals(101, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(101, ctx2.eCountAll(Usr.class));

		try {
			t.groupPartialCommit();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		Assert.assertEquals(102, ctx1.eCountAll(Usr.class));

	}

	// ========== Singleton settings =======================

	public static class Ds1 extends HikariCPBox {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:AnnoGroupTxDs1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			injectValue("driverClassName", "org.h2.Driver");
			injectValue("username", "sa");
			injectValue("password", "");
		}
	}

	public static class Ds2 extends Ds1 {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:AnnoGroupTxDs2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		}
	}

	public static class SqlBoxContextBox1 extends BeanBox {
		public Object create() {
			SqlBoxContext ctx = new SqlBoxContext((DataSource) JBEANBOX.getBean(Ds1.class));
			ctx.setConnectionManager(GroupTxConnectionManager.instance());
			return ctx;
		}
	}

	public static class SqlBoxContextBox2 extends BeanBox {
		public Object create() {
			SqlBoxContext ctx = new SqlBoxContext((DataSource) JBEANBOX.getBean(Ds2.class));
			ctx.setConnectionManager(GroupTxConnectionManager.instance());
			return ctx;
		}
	}

	// ========== AOP settings =======================
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface GTransaction {
		public Class<?> value() default GroupTxAOP.class;
	}

}