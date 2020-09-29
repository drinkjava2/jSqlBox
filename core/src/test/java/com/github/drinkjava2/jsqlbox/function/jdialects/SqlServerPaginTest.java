package com.github.drinkjava2.jsqlbox.function.jdialects;

import static com.github.drinkjava2.jsqlbox.DB.qryEntityList;
import static com.github.drinkjava2.jsqlbox.DB.pagin;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Test SqlServer Pagination, this test need run in SQL Server DataSource
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class SqlServerPaginTest extends TestBase {
	String sql;
	List<Demo> demos;
	{
		this.regTables(Demo.class);
	}

	@Before
	public void init() {
		super.init();
		Systemout.println(ctx.getDialect());
		for (int i = 1; i <= 50; i++) {
			new Demo().putField("name", "Foo" + i, "age", i).insert();
		}
	}

	@Test
	public void sqlServerPaginTest() {
		if (!ctx.getDialect().isSQLServerFamily())
			return;
		Systemout.println("========================sqlServerPaginTest===========");
		// ctx.setDialect(Dialect.SQLServerDialect);
		demos = qryEntityList(Demo.class, "select * from demo order by age", pagin(1, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			Systemout.print(demo.getAge() + ", ");
		Systemout.println();
	}

	@Test
	public void sqlServer20052008PaginTest() {
		if (!ctx.getDialect().isSQLServerFamily())
			return;
		Systemout.println("========================sqlServer2012PaginTest===========");
		// ctx.setDialect(Dialect.SQLServer2005Dialect);
		demos = qryEntityList(Demo.class, "select * from demo order by age", pagin(1, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			Systemout.print(demo.getAge() + ", ");
		Systemout.println();

		demos = qryEntityList(Demo.class, "select * from demo order by age", pagin(3, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			Systemout.print(demo.getAge() + ", ");
		Systemout.println();

		demos = qryEntityList(Demo.class, "select * from demo ", pagin(3, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			Systemout.print(demo.getAge() + ", ");
		Systemout.println();
	}

	@Test
	public void sqlServer2012PaginTest() {
		if (!ctx.getDialect().isSQLServerFamily())
			return;
		Systemout.println("========================sqlServer2012PaginTest===========");
		// ctx.setDialect(Dialect.SQLServer2012Dialect);
		demos = qryEntityList(Demo.class, "select * from demo order by age", pagin(1, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			Systemout.print(demo.getAge() + ", ");
		Systemout.println();

		demos = qryEntityList(Demo.class, "select * from demo order by age", pagin(3, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			Systemout.print(demo.getAge() + ", ");
		Systemout.println();
	}
}