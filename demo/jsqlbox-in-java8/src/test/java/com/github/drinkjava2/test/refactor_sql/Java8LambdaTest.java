package com.github.drinkjava2.test.refactor_sql;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQuery;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForMapList;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.createAliasProxy;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.table;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.test.TestBase;
import com.github.drinkjava2.test.User;
import com.github.drinkjava2.test.refactor_sql.LambdSqlItemPreparer.ALIAS;
import com.github.drinkjava2.test.refactor_sql.LambdSqlItemPreparer.COL;
import com.github.drinkjava2.test.refactor_sql.LambdSqlItemPreparer.C_ALIAS;

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
		List<?> list1 = giQueryForMapList( //
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