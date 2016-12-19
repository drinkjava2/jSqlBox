package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class BatchInsertTest {
	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	public void tx_BatchInsertDemo() {
		User u = SqlBox.createBean(User.class);
		for (int i = 0; i < 1000; i++)
			SqlBox.cacheSQL("insert into ", u.table(), " (", u.userName(), empty("user" + i), ",", u.age(),
					empty("70"), ") ", questionMarks());
		SqlBox.executeCachedSQLs();
	}

	@Test
	public void doTest() {
		User u = SqlBox.createBean(User.class);
		BatchInsertTest t = BeanBox.getBean(BatchInsertTest.class);
		t.tx_BatchInsertDemo();
		Assert.assertEquals(1000, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
	}

}