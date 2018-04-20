package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.DbPro.param;
import static com.github.drinkjava2.test.AliasProxyUtils.createAliasProxy;
import static com.github.drinkjava2.test.AliasProxyUtils.table;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.CustomSqlItem;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.test.AliasProxyUtils.SqlPieceCustomSqlItem;

/**
 * This is a demo shows how to write refactor-support SQL in Java8 by using
 * Lambda
 */

public class LambdaTest extends TestBase {

	public static interface FunInterFaceType {
		public Object get();
	}

	public static interface ACol extends FunInterFaceType {
	}

	public static interface Col extends FunInterFaceType {
	}

	public CustomSqlItem getSqlPiece(FunInterFaceType funBody) {
		try {
			// System.out.println(funBody instanceof ColAs );
			System.out.println(funBody instanceof Col);
			funBody.get();
			String[] a = AliasProxyUtils.thdMethodName.get();
			AliasProxyUtils.checkArrayStringExist(a);
			if (StrUtils.isEmpty(a[2]))
				throw new SqlBoxException("Column name not found.");
			String sqlPiece = new StringBuilder(a[1]).append(".").append(a[2]).append(" as ").append(a[1]).append("_")
					.append(a[2]).toString();
			return new SqlPieceCustomSqlItem(sqlPiece);
		} finally {
			AliasProxyUtils.thdMethodName.remove();
		}
	}

	@Test
	public void lambdaTest() {
		User a = createAliasProxy(User.class, "a");
		ctx.setAllowShowSQL(true);
		List<?> list = ctx.iQuery(new MapListHandler(), //
				"select a.*, ", (ACol) a::getAddress, ", ", (ACol) a::getName //
				, " from ", table(a), " where "//
				, (Col) a::getName, ">=?", param("Foo90") //
				, " and ", (Col) a::getAge, ">?", param(1) //
		);
		Assert.assertTrue(list.size() == 10);
	}
}