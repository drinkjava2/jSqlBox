package com.github.drinkjava2.functionstest.jdialects;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.iQueryForEntityList;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.pagin;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;

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
		System.out.println(ctx.getDialect());
		for (int i = 1; i <= 50; i++) {
			new Demo().putField("name", "Foo" + i, "age", i).insert();
		}
	}

	@Test
	public void sqlServerPaginTest() {
		System.out.println("========================sqlServerPaginTest===========");
		// ctx.setDialect(Dialect.SQLServerDialect);
		demos = iQueryForEntityList(Demo.class, "select * from demo order by age", pagin(1, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			System.out.print(demo.getAge() + ", ");
		System.out.println();
	}

	@Test
	public void sqlServer20052008PaginTest() {
		System.out.println("========================sqlServer2012PaginTest===========");
		// ctx.setDialect(Dialect.SQLServer2005Dialect);
		demos = iQueryForEntityList(Demo.class, "select * from demo order by age", pagin(1, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			System.out.print(demo.getAge() + ", ");
		System.out.println();

		demos = iQueryForEntityList(Demo.class, "select * from demo order by age", pagin(3, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			System.out.print(demo.getAge() + ", ");
		System.out.println();

		demos = iQueryForEntityList(Demo.class, "select * from demo ", pagin(3, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			System.out.print(demo.getAge() + ", ");
		System.out.println();
	}

	@Test
	public void sqlServer2012PaginTest() {
		System.out.println("========================sqlServer2012PaginTest===========");
		// ctx.setDialect(Dialect.SQLServer2012Dialect);
		demos = iQueryForEntityList(Demo.class, "select * from demo order by age", pagin(1, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			System.out.print(demo.getAge() + ", ");
		System.out.println();

		demos = iQueryForEntityList(Demo.class, "select * from demo order by age", pagin(3, 10), new PrintSqlHandler());
		for (Demo demo : demos)
			System.out.print(demo.getAge() + ", ");
		System.out.println();
	}
}