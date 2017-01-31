package test.function_test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.User;

public class BatchInsertTest {
	@Before
	public void setup() {
		System.out.println("===============================Testing BatchInsertTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	public void tx_BatchInsertDemo() {
		User u = new User();
		for (int i = 0; i < 1000; i++)
			Dao.cacheSQL("insert into ", u.table(), " (", //
					u.USERNAME(), empty("user" + i), ",", //
					u.AGE(), empty("70"), //
					") ", valuesAndQuestions());
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