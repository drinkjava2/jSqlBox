package com.github.drinkjava2.jsqlbox.function;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;

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

	DbContext ctx = null;

	{
		DbContext.resetGlobalVariants();

		// Change to MySqlDataSourceBox to Test!
		ctx = new DbContext((DataSource) JBEANBOX.getBean(DataSourceBox.class));// !MySQL
		DbContext.setGlobalNextBatchSize(BUFFER_SIZE);
		
		DbContext.setGlobalDbContext(ctx);
	}

	@Before
	public void setupDB() {
		ctx.quiteExecute("drop table batch_test_tb");
		ctx.quiteExecute("create table batch_test_tb (name varchar(40), address varchar(40))");
	}

	@After
	public void cleanUp() {
		ctx.quiteExecute("drop table batch_test_tb");
		JBEANBOX.close();
	}

	@Table(name = "batch_test_tb")
	public static class User extends ActiveRecord<User> {
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
		Systemout.println("\r\n\r\n\r\n*********Above are warm up, below are real test********\r\n\r\n\r\n");
		INSERT_AMOUNT = OLD_INSERT_AMOUNT;
		doTestBatch();
	}

	public void doTestBatch() {
		User user = new User();
		user.setName("Sam");
		user.setAddress("Canada");
		long start, end;
		String timeused;

		Systemout.println("=======================nBatch method begin=====================");
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
		Systemout.println(String.format(
				"nBatch(Sql, List<Object[])) method execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		Systemout.println("=======================nBatch method end=====================\n\n");

		Systemout.println("=======================DbUtils batch method begin=====================");
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
					Systemout.println("Exception found: " + e.getMessage());
				}
			}
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		Systemout.println(String
				.format("batch(Sql, Object[][]) method execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		Systemout.println("=======================DbUtils batch method end=====================\n\n");

		Systemout.println("===============nBatchBegin()/nBatchEnd() switch methods ==============");
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
		Systemout.println(
				String.format("nBatchBegin/nBatchEnd execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		Systemout.println("=========================================================\n\n");

		Systemout.println("=======================Normal insert begin=====================");
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
		Systemout.println(String.format("Non-Batch execute " + INSERT_AMOUNT + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(INSERT_AMOUNT, ctx.nQueryForLongValue("select  count(*) from batch_test_tb"));
		ctx.nExecute("delete from batch_test_tb");
		Systemout.println("=======================Normal insert  end=====================\n\n");
	}

}
