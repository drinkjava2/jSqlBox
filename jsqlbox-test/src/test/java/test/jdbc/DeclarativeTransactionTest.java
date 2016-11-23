package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.InitializeDatabase;
import test.config.po.User;

public class DeclarativeTransactionTest {
	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	public void tx_InsertUser1() {
		User u = new User();
		Dao.dao.execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user1"), //
				", ", u.Address(), e("address1"), //
				", ", u.Age(), ")", e("10"), //
				SqlHelper.questionMarks());
	}

	public void tx_InsertUser2() {
		User u = new User();
		Dao.dao.execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user2"), //
				", ", u.Address(), e("address2"), //
				", ", u.Age(), ")", e("20"), //
				SqlHelper.questionMarks());
	}

	public void tx_doInsert() {
		tx_InsertUser1();
		int i = Dao.dao.queryForInteger("select count(*) from users");
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
			int i = Dao.dao.queryForInteger("select count(*) from users");
			Assert.assertEquals(0, i);
			throw e;
		}
	}

}