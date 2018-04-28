package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.DbPro.param;
import static com.github.drinkjava2.jsqlbox.SqlBoxContext.giQuery;
import static com.github.drinkjava2.jsqlbox.SqlBoxContext.giQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.SqlBoxContext.giQueryForMapList;
import static com.github.drinkjava2.test.AliasProxyUtils.createAliasProxy;
import static com.github.drinkjava2.test.AliasProxyUtils.table;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.test.LambdSqlItemPreparer.ALIAS;
import com.github.drinkjava2.test.LambdSqlItemPreparer.COL;
import com.github.drinkjava2.test.LambdSqlItemPreparer.C_ALIAS;

/**
 * This is a demo shows how to write refactor-support SQL in Java8 by using
 * Lambda
 */

public class Java8LambdaTest extends TestBase {

	@Test
	public void lambdaTest() {
		Assert.assertEquals(100, giQueryForLongValue("select count(*) from usertb"));

		List<User> totalUsers = giQuery(new EntityListHandler(User.class), "select u.** from usertb u");
		Assert.assertEquals(100, totalUsers.size()); 
		
		User u = createAliasProxy(User.class);
		List<?> list1 = giQueryForMapList(new PrintSqlHandler(), //
				"select "//
				, (ALIAS) u::getId//
				, (C_ALIAS) u::getAddress //
				, (C_ALIAS) u::getName //
				, " from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertEquals(10, list1.size());

		u = createAliasProxy(User.class, "u");
		List<User> list2 = giQuery(new EntityListHandler("u", User.class) //
				, "select "//
				, (ALIAS) u::getId//
				, (C_ALIAS) u::getAddress //
				, (C_ALIAS) u::getName //
				, " from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertEquals(10, list2.size());
	}

}