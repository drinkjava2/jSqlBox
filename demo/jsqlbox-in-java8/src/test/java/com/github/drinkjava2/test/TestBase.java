package com.github.drinkjava2.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.github.drinkjava2.jdbpro.SpecialSqlItemPreparer;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.test.refactor_sql.LambdSqlItemPreparer;
import com.zaxxer.hikari.HikariDataSource;

public class TestBase {
	protected SqlBoxContext ctx = null;
	protected HikariDataSource ds = null;

	/**
	 * This is for Java6 and 7 how to use method name to support refactor
	 */
	@Before
	public void init() {
		ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		SqlBoxContext.resetGlobalVariants();
		// LambdSqlItemPreparer only for Java8
		SqlBoxContextConfig.setGlobalNextSpecialSqlItemPreparers(new SpecialSqlItemPreparer[] { new LambdSqlItemPreparer() });
		// SqlBoxContext.setGlobalNextAllowShowSql(true);
		ctx = new SqlBoxContext(ds);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		prepareTestData();
	}

	@After
	public void cleanup() {
		deleteTestData();
		ds.close();
	}

	public void prepareTestData() {
		String[] ddlArray = ctx.toCreateDDL(User.class);
		for (String ddl : ddlArray)
			ctx.iExecute(ddl);

		try {
			ctx.nBatchBegin();
			for (int i = 0; i < 100; i++) {
				User u = new User();
				u.setName("Foo" + i);
				u.setAge(i);
				u.insert();
			}
		} finally {
			ctx.nBatchEnd();
		}
		Assert.assertEquals(100, ctx.iQueryForLongValue("select count(*) from usertb"));
	}

	public void deleteTestData() {
		String[] ddlArray = ctx.toDropDDL(User.class);
		for (String ddl : ddlArray)
			ctx.iExecute(ddl);
	}
}