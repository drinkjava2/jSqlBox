package com.github.drinkjava2.jsqlbox.java8;
/*- JAVA8_BEGIN
import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JAVA8.*;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.createAliasProxy;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.table;
import static com.github.drinkjava2.jsqlbox.DB.gctx;
import static com.github.drinkjava2.jsqlbox.DB.iExecute;
import static com.github.drinkjava2.jsqlbox.DB.iQuery;
import static com.github.drinkjava2.jsqlbox.DB.iQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.DB.iQueryForMapList;

import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.LambdSqlItem.ALIAS;
import com.github.drinkjava2.jsqlbox.LambdSqlItem.COL;
import com.github.drinkjava2.jsqlbox.LambdSqlItem.C_ALIAS;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
 

public class Java8EampleTest { 
	@Before
	public void init() {
		DbContext ctx = new DbContext(JdbcConnectionPool
				.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", ""));
		DbContext.setGlobalDbContext(ctx);
		for (String ddl : ctx.toCreateDDL(User.class))
			iExecute(ddl);
		for (int i = 0; i < 100; i++)
			new User().putField("name", "Foo" + i, "age", i).insert();
		Assert.assertEquals(100, iQueryForLongValue("select count(*) from usertb"));
	}

	@After
	public void cleanup() {
		for (String ddl : gctx().toDropDDL(User.class))
			gctx().iExecute(ddl);
	}

	@Test
	public void lambdaTest() {
		Assert.assertEquals(100, iQueryForLongValue("select count(*) from usertb"));

		List<User> totalUsers = iQuery(new EntityListHandler(), User.class, "select * from usertb");
		Assert.assertEquals(100, totalUsers.size());

		User u = proxy(User.class);
		List<?> list1 = iQueryForMapList( //
				"select "//
				, (ALIAS) u::getId//
				, (C_ALIAS) u::getAddress //
				, (C_ALIAS) u::getName //
				, " from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertEquals(10, list1.size());

		u = proxy(User.class, "u");
		List<User> list2 = iQuery(new EntityListHandler(), User.class //
				, "select * from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertEquals(10, list2.size());
	}

	@Test
	public void lambdaTest2() {
		Assert.assertEquals(100, iQueryForLongValue("select count(*) from usertb"));

		List<User> totalUsers = iQuery(new EntityListHandler(), User.class, "select * from usertb");
		Assert.assertEquals(100, totalUsers.size());

		User u = proxy(User.class);
		List<?> list1 = iQueryForMapList( //
				"select "//
				, $(u::getId)//
				, c$(u::getAddress) //
				, c$(u::getName) //
				, " from ", table(u), " where "//
				, $(u::getName), ">=?", param("Foo90") //
				, " and ", $(u::getAge), ">?", param(1) //
		);
		Assert.assertEquals(10, list1.size());

		u = createAliasProxy(User.class, "u");
		List<User> list2 = iQuery(new EntityListHandler(), User.class //
				, "select * from ", table(u), " where "//
				, $(u::getName), ">=?", param("Foo90") //
				, " and ", $(u::getAge), ">?", param(1) //
		);
		Assert.assertEquals(10, list2.size());
	}
	
}
JAVA8_END */