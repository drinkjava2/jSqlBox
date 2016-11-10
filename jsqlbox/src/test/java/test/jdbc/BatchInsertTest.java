package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.Config;

public class BatchInsertTest {
	@Before
	public void setup() {
		Config.recreateTables();
	}

	public void tx_BatchInsertDemo() {
		for (int i = 0; i < 10000; i++)
			Dao.dao.cacheSQL("insert user (username", e("user" + i), ",age", e("70"), ") values(?,?)");
		Dao.dao.executeCachedSQLs();
	}

	@Test
	public void doTest() {
		BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
		t.tx_BatchInsertDemo();
		Assert.assertEquals(10000, (int) Dao.dao.queryForInteger("select count(*) from user"));
	}

}