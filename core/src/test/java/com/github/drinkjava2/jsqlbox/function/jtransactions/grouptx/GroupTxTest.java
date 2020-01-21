package com.github.drinkjava2.jsqlbox.function.jtransactions.grouptx;

import static com.github.drinkjava2.jsqlbox.DB.tail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

public class GroupTxTest {
	HikariDataSource ds1;
	HikariDataSource ds2;
	DbContext ctx1;
	DbContext ctx2;

	@Before
	public void init() {
		ds1 = new HikariDataSource();// DataSource
		// H2 is a memory database
		ds1.setDriverClassName("org.h2.Driver");
		ds1.setJdbcUrl("jdbc:h2:mem:DBName_ds1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds1.setMaximumPoolSize(9);
		ds1.setConnectionTimeout(1000);
		ds1.setUsername("sa");
		ds1.setPassword("");

		ds2 = new HikariDataSource();// DataSource
		// H2 is a memory database
		ds2.setDriverClassName("org.h2.Driver");
		ds2.setJdbcUrl("jdbc:h2:mem:DBName_ds2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds2.setMaximumPoolSize(9);
		ds2.setConnectionTimeout(1000);
		ds2.setUsername("sa");
		ds2.setPassword("");

		ctx1 = new DbContext(ds1);
		ctx1.setConnectionManager(GroupTxConnectionManager.instance());

		ctx2 = new DbContext(ds2);
		ctx2.setConnectionManager(GroupTxConnectionManager.instance());

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

	@Test
	public void groupRollbackTest() { // test group roll back
		for (int i = 0; i < 100; i++) {
			ctx1.startTrans();
			try {
				Assert.assertEquals(100, ctx1.eCountAll(Usr.class));
				new Usr().putField("firstName", "Foo").insert(ctx1);
				Assert.assertEquals(101, ctx1.eCountAll(Tail.class, tail("users")));

				Assert.assertEquals(100, ctx2.eCountAll(Usr.class));
				new Usr().putField("firstName", "Foo").insert(ctx2);
				Assert.assertEquals(101, ctx2.eCountAll(Tail.class, tail("users")));
				Systemout.println(1 / 0); // Div 0!
				ctx1.commitTrans();
			} catch (Exception e) {
				ctx1.rollbackTrans();
			}
			Assert.assertEquals(100, ctx1.eCountAll(Tail.class, tail("users")));
			Assert.assertEquals(100, ctx2.eCountAll(Tail.class, tail("users")));
		}
	}

	@Test
	public void groupCommitTest() { // test group commit
		for (int i = 0; i < 100; i++) {
			ctx1.startTrans();
			try {
				new Usr().putField("firstName", "Foo").insert(ctx1);
				ctx1.eInsert(new Usr().setFirstName("Foo"), ctx2);
				new Usr().putField("firstName", "Bar").insert(ctx2);
				ctx1.commitTrans();
			} catch (Exception e) {
				ctx1.rollbackTrans();
			}
		}
		Assert.assertEquals(200, ctx1.eCountAll(Tail.class, tail("users")));
		Assert.assertEquals(300, ctx2.eCountAll(Tail.class, tail("users")));

		Assert.assertEquals(200, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(300, ctx2.eCountAll(Usr.class));

	}

	@Test
	public void groupPartialCommitTest() { // simulate partial commit test
		Assert.assertEquals(100, ctx1.eCountAll(Tail.class, tail("users")));
		ctx1.startTrans();
		try {
			new Usr().putField("firstName", "Foo").insert(ctx1);
			new Usr().putField("firstName", "Foo").insert(ctx2);
			ds2.close();
			ctx1.commitTrans();
		} catch (Exception e) {
			try {
				ctx1.rollbackTrans();
			} catch (Exception e1) {
				//e1.printStackTrace();
			}
		}
		Assert.assertEquals(101, ctx1.eCountAll(Tail.class, tail("users")));
	}

}
