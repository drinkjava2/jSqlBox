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
	public void setup() {
		Config.recreateTables();
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
				", age)", e("20"), //
				" values(?,?,?)");
	}

	public void tx_doInsert() {
		tx_InsertUser1();
		int i = Dao.dao.queryForInteger("select count(*) from user");
		Assert.assertEquals(1, i);
		System.out.println(i / 0);// throw a runtime exception
		tx_InsertUser2();
	}

	@Test(expected = InvocationTargetException.class)
	public void doTest() {
		DeclarativeTransactionTest tester = BeanBox.getBean(DeclarativeTransactionTest.class);
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			Assert.assertEquals(InvocationTargetException.class.getName(), e.getClass().getName());
			int i = Dao.dao.queryForInteger("select count(*) from user");
			Assert.assertEquals(0, i);
			throw e;
		}
	}

}