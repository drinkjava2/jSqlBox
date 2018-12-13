package com.github.drinkjava2.jsqlbox.function.jtransactions;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.tail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.mysql.jdbc.Connection;
import com.zaxxer.hikari.HikariDataSource;

public class TinyTxTest {

	HikariDataSource dataSource;
	SqlBoxContext ctx;

	@Before
	public void init() {
		dataSource = new HikariDataSource();// DataSource
		// H2 is a memory database
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setUsername("sa");
		dataSource.setPassword("");

		ctx = new SqlBoxContext(dataSource);
		ctx.setConnectionManager(TinyTxConnectionManager.instance());
		String[] ddlArray = ctx.toDropAndCreateDDL(User.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);
		for (int i = 1; i <= 100; i++)
			new User().putField("firstName", "Foo" + i, "lastName", "Bar" + i, "age", i).insert(ctx);
	}

	@After
	public void clean() {
		dataSource.close();
	}

	@Test
	public void DemoTest() { 
		TinyTx tx = new TinyTx(dataSource, Connection.TRANSACTION_READ_COMMITTED);
		for (int i = 0; i < 1000; i++) {
			tx.beginTransaction();
			try {
				Assert.assertEquals(100, ctx.eCountAll(User.class));
				new User().putField("firstName", "Foo").insert(ctx);
				Assert.assertEquals(101, ctx.eCountAll(Tail.class, tail("users")));
				System.out.println(1 / 0);
				new User().putField("firstName", "Bar").insert(ctx);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
			}
			Assert.assertEquals(100, ctx.eCountAll(Tail.class, tail("users")));
		}

		tx.beginTransaction();
		try {
			Assert.assertEquals(100, ctx.eCountAll(User.class));
			new User().putField("firstName", "Foo").insert(ctx);
			Assert.assertEquals(101, ctx.eCountAll(Tail.class, tail("users")));
			new User().putField("firstName", "Bar").insert(ctx);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		}
		Assert.assertEquals(102, ctx.eCountAll(Tail.class, tail("users")));

		ctx.setConnectionManager(null);
		Assert.assertEquals(102, ctx.eCountAll(Tail.class, tail("users")));

	}

}
