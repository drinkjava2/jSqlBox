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

	public static interface ALIAS extends SpecialSqlItem {
		public Object get();
	}

	public static interface COL extends SpecialSqlItem {
		public Object get();
	}

	public static interface Table extends SpecialSqlItem {
		public Object get();
	}

	public class LambdSqlItemPreparer implements SpecialSqlItemPreparer {

		@Override
		public void doPrepare(PreparedSQL ps, StringBuilder sql, SpecialSqlItem item) {
			AliasProxyUtils.thdMethodName.remove();
			if (item instanceof ALIAS) {
				((ALIAS) item).get();
				AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
				if (StrUtils.isEmpty(a.colName))
					throw new SqlBoxException("Column name not found.");
				sql.append(new StringBuilder(a.alias).append(".").append(a.colName).append(" as ").append(a.alias)
						.append("_").append(a.colName).toString());
			} else if (item instanceof COL) {
				((COL) item).get();
				AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
				if (StrUtils.isEmpty(a.colName))
					throw new SqlBoxException("Column name not found.");
				sql.append(new StringBuilder(a.alias).append(".").append(a.colName).toString());
			} else
				throw new SqlBoxException("Unknow SpecialSqlItem: " + item);
		}
	}

	@Test
	public void lambdaTest() {

		SqlBoxContext.setGlobalSpecialSqlItemPreparer(new LambdSqlItemPreparer());
		User u = createAliasProxy(User.class, "u");

 
		List<User> u1 = ctx.iQuery(new EntityListHandler(User.class,   new User().alias("u") ), new PrintSqlHandler(),
				"select u.address as u_address, u.usr_age as u_usr_age, u.id as u_id,"
				+ " u.usr_name as u_usr_name from usertb u where u.usr_name>=? and u.usr_age>? ", ctx.param("Foo90",1));
		System.out.println(u1);

		List<User> list = ctx.iQuery(new EntityListHandler(User.class), new PrintSqlHandler(), //
				"select u.**,"//
				, (ALIAS) u::getId//
				, ", ", (ALIAS) u::getAddress //
				, ", ", (ALIAS) u::getName //
				, " from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertTrue(list.size() == 10);
	}

}