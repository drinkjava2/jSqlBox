package test.function_test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questions;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.PrepareTestContext;
import test.config.po.User;

public class BatchInsertTest extends TestBase {

	public void tx_BatchInsertDemo() {
		User u = new User();
		for (int i = 0; i < 1000; i++)
			Dao.cacheSQL("insert into ", u.table(), " (", //
					u.ID(), empty(i), ",", //
					u.USERNAME(), empty("user" + i), ",", //
					u.AGE(), empty("70"), //
					") values ", questions());
		Dao.executeCachedSQLs();
	}

	@Test
	public void doTest() {
		User u = new User();
		BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
		t.tx_BatchInsertDemo();
		Assert.assertEquals(1000, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	public static void main(String[] args) {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		User u = new User();
		long old = System.currentTimeMillis();
		BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
		t.tx_BatchInsertDemo();
		System.out.println(System.currentTimeMillis() - old);
		Assert.assertEquals(1000, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

}