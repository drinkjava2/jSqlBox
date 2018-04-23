package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.DbPro.param;
import static com.github.drinkjava2.test.AliasProxyUtils.createAliasProxy;
import static com.github.drinkjava2.test.AliasProxyUtils.table;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SpecialSqlItem;
import com.github.drinkjava2.jdbpro.SpecialSqlItemPreparer;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.test.AliasProxyUtils.AliasItemInfo;

/**
 * This is a demo shows how to write refactor-support SQL in Java8 by using
 * Lambda
 */

public class Java8LambdaTest extends TestBase {

	public static interface ALIAS extends SpecialSqlItem {// a.col as a_col
		public Object get();
	}

	public static interface C_ALIAS extends SpecialSqlItem {// , a.clo as a_col
		public Object get();
	}

	public static interface COL extends SpecialSqlItem {// a.col
		public Object get();
	}

	public static class LambdSqlItemPreparer implements SpecialSqlItemPreparer {

		@Override
		public void doPrepare(PreparedSQL ps, StringBuilder sql, SpecialSqlItem item) {
			AliasProxyUtils.thdMethodName.remove();
			if (item instanceof ALIAS) {
				((ALIAS) item).get();
				AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
				if (StrUtils.isEmpty(a.colName))
					throw new SqlBoxException("Column name not found.");
				if (StrUtils.isEmpty(a.alias))
					sql.append(a.colName);
				else
					sql.append(new StringBuilder(a.alias).append(".").append(a.colName).append(" as ").append(a.alias)
							.append("_").append(a.colName).toString());
			} else if (item instanceof C_ALIAS) {
				((C_ALIAS) item).get();
				AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
				if (StrUtils.isEmpty(a.colName))
					throw new SqlBoxException("Column name not found.");
				if (StrUtils.isEmpty(a.alias))
					sql.append(", " + a.colName);
				else
					sql.append(new StringBuilder(", ").append(a.alias).append(".").append(a.colName).append(" as ")
							.append(a.alias).append("_").append(a.colName).toString());
			} else if (item instanceof COL) {
				((COL) item).get();
				AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
				if (StrUtils.isEmpty(a.colName))
					throw new SqlBoxException("Column name not found.");
				if (StrUtils.isEmpty(a.alias))
					sql.append(a.colName);
				else
					sql.append(new StringBuilder(a.alias).append(".").append(a.colName).toString());
			} else
				throw new SqlBoxException("Unknow SpecialSqlItem: " + item);
		}
	}

	@Test
	public void lambdaTest() {
		List<User> totalUsers = ctx.iQuery(new EntityListHandler(User.class), "select u.** from usertb u");
		Assert.assertEquals(100, totalUsers.size());

		SqlBoxContext.setGlobalSpecialSqlItemPreparer(new LambdSqlItemPreparer());
		User u = createAliasProxy(User.class, null);

		List<?> list1 = ctx.iQueryForMapList(new PrintSqlHandler(), //
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
		List<User> list2 = ctx.iQuery(new EntityListHandler(User.class, (User) new User().alias("u")),
				new PrintSqlHandler(), //
				"select "//
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