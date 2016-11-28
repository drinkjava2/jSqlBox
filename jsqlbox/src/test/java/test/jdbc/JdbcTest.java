package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.InitializeDatabase;
import test.config.po.User;

public class JdbcTest {
	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	/**
	 * Test Jdbc methods, SQL be automatically wrapped to preparedStatement
	 */
	@Test
	public void tx_jdbcTest() {
		User u = SqlBox.createBean(User.class);
		Dao.dao().execute("insert into " + u.Table() + " (" + u.UserName() + e("user1") + ", " + u.Address()
				+ e("address1") + ", " + u.Age() + e("1") + ") values(?,?,?)");

		Dao.dao().execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user2"), //
				", ", u.Address(), e("address2"), //
				", ", u.Age(), e("2"), //
				") values(?,?,?)");

		Dao.dao().execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user3"), //
				", ", u.Address(), e("address3"), //
				", ", u.Age(), e("3"), //
				")", SqlHelper.questionMarks());

		Dao.dao().execute("update " + u.Table() + " set " + u.UserName() + "=" + q("John") + "," + u.Address() + "="
				+ q("Shanghai") + " where " + u.Age() + "=" + q(1));

		Dao.dao().execute("update ", u.Table(), " set ", //
				u.UserName(), "=", q("Jeffery"), ",", //
				u.Address(), " =", q("Tianjing"), //
				" where ", u.Age(), "=", q(2));

		Dao.dao().execute("update ", u.Table(), " set ", //
				u.UserName(), "=?", e("Tom"), ",", //
				u.Address(), " =?", e("Nanjing"), //
				" where ", u.Age(), "=?", e(3));

		Assert.assertEquals(3, (int) Dao.dao().queryForInteger("select count(*) from " + u.Table()));
	}

	/**
	 * Do test within transaction
	 */
	@Test
	public void doTestWithTransaction() {
		JdbcTest t = BeanBox.getBean(JdbcTest.class); // get Proxy bean
		t.tx_jdbcTest(); // use Spring Declarative Transaction
	}

}