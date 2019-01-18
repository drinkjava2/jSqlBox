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
import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.github.drinkjava2.jtransactions.grouptx.GroupTx;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;

/**
 * Annotation GroupTx unit Test
 *
 * @author Yong Zhu
 * @since 2.0
 */
public class GroupShardTxTest {
	SqlBoxContext ctx1 = JBEANBOX.getBean(SqlBoxContextBox1.class);
	SqlBoxContext ctx2 = JBEANBOX.getBean(SqlBoxContextBox2.class);

	@Before
	public void init() {
		ctx1.setAllowShowSQL(true);
		String[] ddlArray = ctx1.toDropAndCreateDDL(ShardUser.class);
		for (String ddl : ddlArray) {
			ctx1.nExecute(ddl);
			ctx2.nExecute(ddl);
		}
		SqlBoxContext[] masters = new SqlBoxContext[] { ctx1, ctx2 };
		ctx1.setMasters(masters);
		ctx1.setName("ctx1");
		ctx2.setMasters(masters);
		ctx2.setName("ctx2");

		for (int i = 1; i <= 100; i++)
			new ShardUser().setId(i).setName("Foo" + i).insert(ctx1); // Sharded!

		Assert.assertEquals(50, ctx1.eCountAll(ShardUser.class));
		Assert.assertEquals(50, ctx2.eCountAll(ShardUser.class));
	}

	@After
	public void clean() {
		JBEANBOX.close();
	}

	@GTransaction
	public void groupRollback() { // test group roll back
		new ShardUser().setName("Foo").insert(ctx1);
		new ShardUser().setName("Bar").insert(ctx2);
		System.out.println(1 / 0); // div 0!
	}

	@GTransaction
	public void groupCommit() { // test group roll back
		new ShardUser().setId(200).setName("Foo").insert();
		new ShardUser().setId(201).setName("Bar").insert();
	}

	@Test
	public void groupTest() {
		GroupShardTxTest t = JBEANBOX.getInstance(GroupShardTxTest.class);
		try {
			t.groupRollback();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		Assert.assertEquals(50, ctx1.eCountAll(ShardUser.class));
		Assert.assertEquals(50, ctx2.eCountAll(ShardUser.class));

		t.groupCommit();
		Assert.assertEquals(51, ctx1.eCountAll(ShardUser.class));
		Assert.assertEquals(51, ctx2.eCountAll(ShardUser.class));

	}

	// ========== Singleton settings =======================

	public static class Ds1 extends HikariCPBox {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:Ds1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			injectValue("driverClassName", "org.h2.Driver");
			injectValue("username", "sa");
			injectValue("password", "");
		}
	}

	public static class Ds2 extends Ds1 {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:Ds2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		}
	}

	public static class GroupConnMgr extends BeanBox {
		public Object create() {
			return new GroupTxConnectionManager((DataSource) JBEANBOX.getBean(Ds1.class),
					(DataSource) JBEANBOX.getBean(Ds2.class));
		}
	}

	public static class SqlBoxContextBox1 extends BeanBox {
		public Object create() {
			SqlBoxContext ctx = new SqlBoxContext((DataSource) JBEANBOX.getBean(Ds1.class));
			ctx.setConnectionManager((ConnectionManager) JBEANBOX.getBean(GroupConnMgr.class));
			return ctx;
		}
	}

	public static class SqlBoxContextBox2 extends BeanBox {
		public Object create() {
			SqlBoxContext ctx = new SqlBoxContext((DataSource) JBEANBOX.getBean(Ds2.class));
			ctx.setConnectionManager((ConnectionManager) JBEANBOX.getBean(GroupConnMgr.class));
			return ctx;
		}
	}

	// ========== AOP settings =======================
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface GTransaction {
		public Class<?> value() default GpTXBox.class;
	}

	public static class GpTXBox extends BeanBox {
		public Object create() {
			return new GroupTx((GroupTxConnectionManager) JBEANBOX.getBean(GroupConnMgr.class));
		}
	}

}