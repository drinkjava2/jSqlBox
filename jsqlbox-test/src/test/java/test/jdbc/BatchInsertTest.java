package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.InitializeDatabase;
import test.config.po.User;

public class BatchInsertTest {
	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	public void tx_BatchInsertDemo() {
		User u = new User();
		for (int i = 0; i < 100000; i++)
			Dao.dao.cacheSQL("insert ", u.Table(), " (", u.UserName(), e("user" + i), ",", u.Age(), e("70"), ") ",
					SqlHelper.questionMarks());
		Dao.dao.executeCachedSQLs();
	}

	@Test
	public void doTest() {
		BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
		t.tx_BatchInsertDemo();
		Assert.assertEquals(10000, (int) Dao.dao.queryForInteger("select count(*) from ", User.Table));
	}

}