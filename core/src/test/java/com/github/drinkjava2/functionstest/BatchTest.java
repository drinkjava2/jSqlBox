package com.github.drinkjava2.functionstest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;

/**
 * This is Batch operation function test<br/>
 * note: only test on MySql, not on H2
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class BatchTest {
	int BUFFER_SIZE = 300;
	int INSERT_AMOUNT = BUFFER_SIZE * 1; // Change 1 to 1000 try

	SqlBoxContext ctx = null;

	{
		SqlBoxContext.resetGlobalVariants();
		// SqlBoxContext.setGlobalAllowShowSql(true);

		SqlBoxContextConfig.setGlobalNextBatchSize(BUFFER_SIZE);

		// Change to MySqlDataSourceBox to Test!
		ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class));// !MySQL
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
	}

	@Before
	public void setupDB() {
		ctx.quiteExecute("drop table batch_test_tb");
		ctx.quiteExecute("create table batch_test_tb (name varchar(40), address varchar(40))");
	}

	@After
	public void cleanUp() {
		ctx.quiteExecute("drop table batch_test_tb");
		BeanBox.defaultContext.close();
	}

	@Table(name = "batch_test_tb")
	public static class User extends ActiveRecord {
		String name;
		String address;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	@Test
	public void testBatch() {
		int OLD_INSERT_AMOUNT = INSERT_AMOUNT;
		INSERT_AMOUNT = BUFFER_SIZE * 2;
		doTestBatch();
		System.out.println("\r\n\r\n\r\n*********Above are warm up, below are real test********\r\n\r\n\r\n");
		INSERT_AMOUNT = OLD_INSERT_AMOUNT;
		doTestBatch();
	}

	public void doTestBatch() {
		User user = new User();
		user.setName("Sam");
		user.setAddress("Canada");
		long start, end;
		String timeused;

		System.out.println("=======================nBatch method begin=====================");
		Assert.assertEquals(0, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		start = System.currentTimeMillis();
		List<Object[]> params = new ArrayList<Object[]>();
		for (long i = 0; i < INSERT_AMOUNT; i++) {
			params.add(new Object[] { "Name" + i, "Address" + i });
			if (i % BUFFER_SIZE == (BUFFER_SIZE - 1)) {
				ctx.nBatch("insert into batch_test_tb (name, address) values(?,?)", params);
				params.clear();
			}
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format(
				"nBatch(Sql, List<Object[])) method execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		System.out.println("=======================nBatch method end=====================\n\n");

		System.out.println("=======================DbUtils batch method begin=====================");
		Assert.assertEquals(0, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		start = System.currentTimeMillis();
		Object[][] paramsArray = new Object[BUFFER_SIZE][2];
		int j = 0;
		for (int i = 0; i < INSERT_AMOUNT; i++) {
			paramsArray[j][0] = "Name" + i;
			paramsArray[j][1] = "Address" + i;
			j++;
			if (i % BUFFER_SIZE == (BUFFER_SIZE - 1)) {
				try {
					ctx.batch("insert into batch_test_tb (name, address) values(?,?)", paramsArray);
					j = 0;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String
				.format("batch(Sql, Object[][]) method execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		System.out.println("=======================DbUtils batch method end=====================\n\n");

		System.out.println("===============nBatchBegin()/nBatchEnd() switch methods ==============");
		Assert.assertEquals(0, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		start = System.currentTimeMillis();
		try {
			ctx.nBatchBegin();
			for (long i = 0; i < INSERT_AMOUNT; i++) {
				user.setName("Name" + i);
				user.setAddress("Address" + i);
				user.insert();
			}
		} finally {
			ctx.nBatchEnd();
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(
				String.format("nBatchBegin/nBatchEnd execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		System.out.println("=========================================================\n\n");

		System.out.println("=======================Normal insert begin=====================");
		ctx.nExecute("delete from batch_test_tb");
		Assert.assertEquals(0, ctx.nQueryForLongValue("select count(*) from  batch_test_tb"));
		start = System.currentTimeMillis();
		for (long i = 0; i < INSERT_AMOUNT; i++) {
			user.setName("Name" + i);
			user.setAddress("Address" + i);
			user.insert();
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("Non-Batch execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select  count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		System.out.println("=======================Normal insert  end=====================\n\n");
	}

}
