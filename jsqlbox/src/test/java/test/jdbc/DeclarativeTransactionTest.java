package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.Config;

public class DeclarativeTransactionTest {
	@Before
	public void recreateDatabase() {
		Config.recreateDatabase();
	}

	public void tx_InsertUser1() {
		Dao.dao.execute("insert into user ", //
				" (username", e("user1"), //
				", address", e("address1"), //
				", age)", e("10"), //
				" values(?,?,?)");
	}

	public void tx_InsertUser2() {
		Dao.dao.execute("insert into user ", //
				" (username", e("user2"), //
				", address", e("address2"), //
				", age)", e("20"), // 0
				" values(?,?,?)");
	}

	public void tx_InsertGood() {
		tx_InsertUser1();
		tx_InsertUser2();
	}

	public void tx_InsertBad() {
		tx_InsertUser1();
		int i = 1 / 0;
		tx_InsertUser2();
	}

	@Test
	public void testGoodInsert() {
		DeclarativeTransactionTest tester = BeanBox.getBean(DeclarativeTransactionTest.class);
		tester.tx_InsertGood();
		int i = Dao.dao.getJdbc().queryForObject("select count(*) from user", Integer.class);
		Assert.assertEquals(2, i);
	}

	@Test(expected = InvocationTargetException.class)
	public void testBadInsert() {
		DeclarativeTransactionTest tester = BeanBox.getBean(DeclarativeTransactionTest.class);
		Exception e = null;
		try {
			tester.tx_InsertBad();
		} catch (Exception e1) {
			e = e1;
		}

		int i = Dao.dao.getJdbc().queryForObject("select count(*) from user", Integer.class);
		Assert.assertEquals(0, i);
	}

}