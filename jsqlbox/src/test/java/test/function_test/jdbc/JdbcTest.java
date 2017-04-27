package test.function_test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questions;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.AopAround;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.JBeanBoxConfig.SpringTxInterceptorBox;
import test.config.po.User;

public class JdbcTest extends TestBase {

	/**
	 * Test Jdbc methods, SQL be automatically wrapped to preparedStatement
	 */
	@Test
	@AopAround(SpringTxInterceptorBox.class)
	public void tx_jdbcTest() {
		User u = new User();
		Dao.execute("insert into " + u.table() //
				+ " (" + u.ID() + empty(u.nextUUID())//
				+ ", " + u.USERNAME() + empty("user1")//
				+ ", " + u.ADDRESS() + empty("address1")//
				+ ", " + u.AGE() + empty("1")//
				+ ") values(?,?,?,?)");

		Dao.execute("insert into ", u.table(), //
				" (", u.ID(), empty(u.nextUUID()), //
				", ", u.USERNAME(), empty("user2"), //
				", ", u.ADDRESS(), empty("address2"), //
				", ", u.AGE(), empty("2"), //
				") values(?,?,?,?)");

		Dao.execute("insert into ", u.table(), //
				" (", u.ID(), empty(u.nextUUID()), //
				", ", u.USERNAME(), empty("user3"), //
				", ", u.ADDRESS(), empty("address2"), //
				", ", u.AGE(), empty("3"), //
				") values ", questions());

		Dao.execute("insert into ", u.table(), //
				" (", u.ID(u.nextUUID()), //
				", ", u.USERNAME("user3"), //
				", ", u.ADDRESS("address2"), //
				", ", u.AGE("4"), //
				") values ", questions());

		Assert.assertEquals(4, (int) Dao.queryForInteger("select count(*) from " + u.table()));

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

		Dao.execute("update ", u.table(), " set ", //
				u.USERNAME("Joe"), "=?,", //
				u.ADDRESS("Beijing"), " =?", //
				" where ", u.AGE(4), "=?");

		Assert.assertEquals(4, (int) Dao.queryForInteger("select count(*) from " + u.table() + " where ",
				u.USERNAME("John", "Jeffery", "Tom", "Joe"), " in ", questions()));
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