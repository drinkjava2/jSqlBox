package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.DbPro.param;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.test.NoramlTest.UserDemo;
import com.zaxxer.hikari.HikariDataSource;

public class LambdaTest {

	public static interface COL {
		public Object get();
	}

	public static interface AS {
		public Object get();
	}

	@Test
	public void test2() {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		SqlBoxContext.setGlobalAllowShowSql(true); // Log output
		SqlBoxContext ctx = new SqlBoxContext(ds); // Here you go
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		ctx.setAllowShowSQL(true);

		String[] ddlArray = ctx.toDropAndCreateDDL(UserDemo.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);

		try {
			ctx.nBatchBegin();
			for (int i = 0; i < 100; i++) {
				UserDemo u = new UserDemo();
				u.setName("Foo" + i);
				u.setAge(i);
				u.insert();
			}
		} finally {
			ctx.nBatchEnd();
		}
		Assert.assertEquals(100, ctx.iQueryForLongValue("select count(*) from UserDemo"));

		//TODO at here
		UserDemo a = NoramlTest.aliasProxy(UserDemo.class, "a");
		List<?> list = ctx.iQuery(new MapListHandler(), //
				"select a.*, ", (AS) a::getAddress, ", ", (AS) a::getName //
				, " from UserDemo a where "//
				, (COL) a::getName, ">=?", param("Foo90") //
				, " and ", (COL) a::getAge, ">?", param(1) //
		);
		Assert.assertTrue(list.size() == 10);
		ds.close();
	}
}