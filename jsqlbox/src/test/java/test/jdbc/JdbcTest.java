package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class JdbcTest {
	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDefaultContexts();
	}

	/**
	 * Test Jdbc methods, SQL be automatically wrapped to preparedStatement
	 */
	@Test
	public void tx_jdbcTest() {
		User u = SqlBox.createBean(User.class);
		SqlBox.execute("insert into " + u.table() + " (" + u.userName() + empty("user1") + ", " + u.address()
				+ empty("address1") + ", " + u.age() + empty("1") + ") values(?,?,?)");

		SqlBox.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user2"), //
				", ", u.address(), empty("address2"), //
				", ", u.age(), empty("2"), //
				") values(?,?,?)");

		SqlBox.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user3"), //
				", ", u.address(), empty("address3"), //
				", ", u.age(), empty("3"), //
				")", questionMarks());

		SqlBox.execute("update " + u.table() + " set " + u.userName() + "=" + q("John") + "," + u.address() + "="
				+ q("Shanghai") + " where " + u.age() + "=" + q(1));

		SqlBox.execute("update ", u.table(), " set ", //
				u.userName(), "=", q("Jeffery"), ",", //
				u.address(), " =", q("Tianjing"), //
				" where ", u.age(), "=", q(2));

		SqlBox.execute("update ", u.table(), " set ", //
				u.userName(), "=?", empty("Tom"), ",", //
				u.address(), " =?", empty("Nanjing"), //
				" where ", u.age(), "=?", empty(3));

		Assert.assertEquals(3, (int)SqlBox.queryForInteger("select count(*) from " + u.table()));
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