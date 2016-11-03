package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.Config;

public class JdbcTest {
	@Before
	public void setup() {
		Config.recreateDatabase();
	}

	/**
	 * Test Jdbc methods, SQL be automatically wrapped to preparedStatement
	 */
	@Test
	public void tx_jdbcTest() {
		Dao.dao.execute(
				"insert user (username,address,age) values(" + q("user1") + "," + q("address1") + "," + q(10) + ")");
		Dao.dao.execute("insert user (username,address,age) values(", q("user2"), ",", q("address2"), ",", q(20), ")");
		Dao.dao.execute("insert user (username,address,age) values(" + q("user3", "address3", 30) + ")");
		Dao.dao.execute("insert user (username,address,age) values(?,?,?)" + e("user4") + e("address4") + e(40));
		Dao.dao.execute("insert user (username,address,age) values(?,?,?)" + e("user5", "address5", 50));
		Dao.dao.execute("insert user ", //
				" (username", e("user6"), //
				", address", e("address6"), //
				", age)", e("60"), //
				" values(?,?,?)");
		Dao.dao.execute("update user set username=?,address=? " + e("Sam", "BeiJing") + " where age=" + q(10));
		Dao.dao.execute("update user set username=", q("John"), ",address=", q("Shanghai"), " where age=", q(20));
		Dao.dao.execute("update user set", //
				" username=?", e("Tom"), //
				",address=? ", e("Nanjing"), //
				" where age=?", e(30));
		Dao.dao.execute("update user set", //
				" username=", q("Jeffery"), //
				",address=", q("Tianjing"), //
				" where age=", q(40));
		Assert.assertEquals(6, (int) Dao.dao.queryForInteger("select count(*) from user"));
	}

	/**
	 * Do test with Transaction
	 */
	@Test
	public void doTestWithTransaction() {
		JdbcTest t = BeanBox.getBean(JdbcTest.class); // get Proxy bean
		t.tx_jdbcTest(); // use Spring Declarative Transaction
	}

}