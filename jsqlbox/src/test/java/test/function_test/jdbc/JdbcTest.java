package test.function_test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.User;

public class JdbcTest {
	@Before
	public void setup() {
		System.out.println("===============================Testing JdbcTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	/**
	 * Test Jdbc methods, SQL be automatically wrapped to preparedStatement
	 */
	@Test
	public void tx_jdbcTest() {
		User u = new User();
		Dao.execute("insert into " + u.table() //
				+ " (" + u.USERNAME() + empty("user1")//
				+ ", " + u.ADDRESS() + empty("address1")//
				+ ", " + u.AGE() + empty("1")//
				+ ") values(?,?,?)");

		Dao.execute("insert into ", u.table(), //
				" (", u.USERNAME(), empty("user2"), //
				", ", u.ADDRESS(), empty("address2"), //
				", ", u.AGE(), empty("2"), //
				") values(?,?,?)");

		Dao.execute("insert into ", u.table(), //
				" (", u.USERNAME(), empty("user3"), //
				", ", u.ADDRESS(), empty("address3"), //
				", ", u.AGE(), empty("3"), //
				")", valuesAndQuestions());

		Dao.execute("update " + u.table() + " set " + u.USERNAME() + "=" + q("John") + "," + u.ADDRESS() + "="
				+ q("Shanghai") + " where " + u.AGE() + "=" + q(1));

		Dao.execute("update ", u.table(), " set ", //
				u.USERNAME(), "=", q("Jeffery"), ",", //
				u.ADDRESS(), " =", q("Tianjing"), //
				" where ", u.AGE(), "=", q(2));

		Dao.execute("update ", u.table(), " set ", //
				u.USERNAME(), "=?", empty("Tom"), ",", //
				u.ADDRESS(), " =?", empty("Nanjing"), //
				" where ", u.AGE(), "=?", empty(3));

		Assert.assertEquals(3, (int) Dao.queryForInteger("select count(*) from " + u.table()));
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