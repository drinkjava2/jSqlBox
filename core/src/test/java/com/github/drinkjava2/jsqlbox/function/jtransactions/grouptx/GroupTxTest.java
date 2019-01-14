package com.github.drinkjava2.jsqlbox.function.jtransactions.grouptx;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.tail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jsqlbox.function.jtransactions.User;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.mysql.jdbc.Connection;
import com.zaxxer.hikari.HikariDataSource;

public class GroupTxTest {
	GroupTxConnectionManager gm;
	HikariDataSource ds1;
	HikariDataSource ds2;
	SqlBoxContext ctx1;
	SqlBoxContext ctx2;

	@Before
	public void init() {
		ds1 = new HikariDataSource();// DataSource
		// H2 is a memory database
		ds1.setDriverClassName("org.h2.Driver");
		ds1.setJdbcUrl("jdbc:h2:mem:DBName1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds1.setUsername("sa");
		ds1.setPassword("");

		ds2 = new HikariDataSource();// DataSource
		// H2 is a memory database
		ds2.setDriverClassName("org.h2.Driver");
		ds2.setJdbcUrl("jdbc:h2:mem:DBName2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds2.setUsername("sa");
		ds2.setPassword("");

		gm = new GroupTxConnectionManager(ds1, ds2);

		ctx1 = new SqlBoxContext(ds1);
		ctx1.setConnectionManager(gm);

		ctx2 = new SqlBoxContext(ds2);
		ctx2.setConnectionManager(gm);

		String[] ddlArray = ctx1.toDropAndCreateDDL(User.class);
		for (String ddl : ddlArray) {
			ctx1.nExecute(ddl);
			ctx2.nExecute(ddl);
		}

		for (int i = 1; i <= 100; i++)
			new User().putField("firstName", "Foo" + i, "lastName", "Bar" + i, "age", i).insert(ctx1);
		for (int i = 1; i <= 100; i++)
			new User().putField("firstName", "FOO" + i, "lastName", "BAR" + i, "age", i).insert(ctx2);
	}

	@After
	public void clean() {
		ds1.close();
		ds2.close();
	}

	@Test
	public void DemoTest() {
		gm.startGroupTransaction();
		try {
			Assert.assertEquals(100, ctx1.eCountAll(User.class));
			//new User().putField("firstName", "Foo").insert(ctx1);
			//Assert.assertEquals(101, ctx1.eCountAll(Tail.class, tail("users")));
			// System.out.println(1 / 0);
			//new User().putField("firstName", "Bar").insert(ctx1);
			gm.commitGroupTx();
			System.out.println("Commited");
		} catch (Exception e) {
			e.printStackTrace( );
			gm.rollbackGroupTx(); 
		}
		Assert.assertEquals(100, ctx1.eCountAll(Tail.class, tail("users")));

		gm.startGroupTransaction();
		try {
			Assert.assertEquals(100, ctx1.eCountAll(User.class));
			new User().putField("firstName", "Foo").insert(ctx1);
			Assert.assertEquals(101, ctx1.eCountAll(Tail.class, tail("users")));
			new User().putField("firstName", "Bar").insert(ctx1);
			gm.commitGroupTx();
		} catch (Exception e) {
			gm.rollbackGroupTx();
		}
		Assert.assertEquals(102, ctx1.eCountAll(Tail.class, tail("users")));

		ctx1.setConnectionManager(null);
		Assert.assertEquals(102, ctx1.eCountAll(Tail.class, tail("users")));

	}

}
