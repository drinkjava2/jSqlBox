package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.DbPro.param;
import static com.github.drinkjava2.test.AliasProxyUtils.alias;
import static com.github.drinkjava2.test.AliasProxyUtils.c_alias;
import static com.github.drinkjava2.test.AliasProxyUtils.clean;
import static com.github.drinkjava2.test.AliasProxyUtils.col;
import static com.github.drinkjava2.test.AliasProxyUtils.createAliasProxy;
import static com.github.drinkjava2.test.AliasProxyUtils.table;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

/**
 * This is a demo shows how to write refactor-support SQL in Java6 and Java7.
 */
public class Java6Java7Test extends TestBase {

	@Test
	public void normalTest() {
		List<User> totalUsers = ctx.iQuery(new EntityListHandler(User.class), "select u.** from usertb u");
		Assert.assertEquals(100, totalUsers.size());

		User u = createAliasProxy(User.class, null);
		List<Map<String, Object>> list = ctx.iQueryForMapList(new PrintSqlHandler(), clean(), //
				"select "//
				, alias(u.getId())//
				, ", ", alias(u.getAddress())//
				, ", ", alias(u.getName())//
				, " from ", table(u), " where "//
				, col(u.getName()), ">=?", param("Foo90") //
				, " and ", col(u.getAge()), ">?", param(1) //
		);
		Assert.assertEquals(10, list.size());

		u = createAliasProxy(User.class, "u");
		List<User> list2 = ctx.iQuery(new EntityListHandler(User.class, (User) new User().alias("u")),
				new PrintSqlHandler(), clean(), //
				"select "//
				, alias(u.getId())//
				, c_alias(u.getAddress())//
				, c_alias(u.getName())//
				, " from ", table(u), " where "//
				, col(u.getName()), ">=?", param("Foo90") //
				, " and ", col(u.getAge()), ">?", param(1) //
		);
		Assert.assertEquals(10, list2.size());
	}
}