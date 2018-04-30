package com.github.drinkjava2.test.refactor_sql;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQuery;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForMapList;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.alias;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.c_alias;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.clean;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.col;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.createAliasProxy;
import static com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.table;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.test.TestBase;
import com.github.drinkjava2.test.User;

/**
 * This is a demo shows how to write refactor-support SQL in Java6 and Java7.
 */
public class Java6Java7Test extends TestBase {

	@Test
	public void normalTest() {
		// ctx.setAllowShowSQL(true);
		List<User> totalUsers = giQuery(new EntityListHandler(User.class), "select u.** from usertb u");
		Assert.assertEquals(100, totalUsers.size());

		User u = createAliasProxy(User.class);
		List<Map<String, Object>> list = giQueryForMapList(clean(), //
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
		List<User> list2 = giQuery(new EntityListHandler("u", User.class), clean(), //
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