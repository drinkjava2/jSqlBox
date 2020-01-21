package com.github.drinkjava2.jsqlbox.function.jtransactions.manultx;

import static com.github.drinkjava2.jsqlbox.DB.tail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jtransactions.manual.ManualTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

public class ManualTxTest {

	HikariDataSource dataSource;
	DbContext ctx;

	@Before
	public void init() {
		dataSource = new HikariDataSource();// DataSource
		// H2 is a memory database
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setUsername("sa");
		dataSource.setPassword("");

		ctx = new DbContext(dataSource);
		String[] ddlArray = ctx.toDropAndCreateDDL(Usr.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);
		for (int i = 1; i <= 100; i++)
			new Usr().putField("firstName", "Foo" + i, "lastName", "Bar" + i, "age", i).insert(ctx);
	}

	@After
	public void clean() {
		dataSource.close();
	}

	@Test
	public void DemoTest() {
		ctx.setConnectionManager(new ManualTxConnectionManager());
		for (int i = 0; i < 1000; i++) {
			ctx.startTrans();
			try {
				Assert.assertEquals(100, ctx.eCountAll(Usr.class));
				new Usr().putField("firstName", "Foo").insert(ctx);
				Assert.assertEquals(101, ctx.eCountAll(Tail.class, tail("users")));
				Systemout.println(1 / 0);
				new Usr().putField("firstName", "Bar").insert(ctx);
				ctx.commitTrans();
			} catch (Exception e) {
				ctx.rollbackTrans();
			}
			Assert.assertEquals(100, ctx.eCountAll(Tail.class, tail("users")));
		}

		ctx.startTrans();
		try {
			Assert.assertEquals(100, ctx.eCountAll(Usr.class));
			new Usr().putField("firstName", "Foo").insert(ctx);
			Assert.assertEquals(101, ctx.eCountAll(Tail.class, tail("users")));
			new Usr().putField("firstName", "Bar").insert(ctx);
			ctx.commitTrans();
		} catch (Exception e) {
			ctx.rollbackTrans();
		}
		Assert.assertEquals(102, ctx.eCountAll(Tail.class, tail("users")));

		ctx.setConnectionManager(null);
		Assert.assertEquals(102, ctx.eCountAll(Tail.class, tail("users")));
	}

}
