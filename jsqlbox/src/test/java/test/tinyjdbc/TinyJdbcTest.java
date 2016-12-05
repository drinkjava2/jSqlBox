package test.tinyjdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlHelper;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

import test.config.InitializeDatabase;
import test.config.JBeanBoxConfig.MySqlDataSourceBox;
import test.config.po.User;

/**
 * This is to test TinyJDBC use its own Transaction not related to Spring<br/>
 * An exception will causer Spring rollback but will not affect TinyJDBC
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyJdbcTest {
	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	@Test
	public void tx_InsertUser1() {
		User u = new User();
		u.dao().execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user1"), //
				", ", u.Address(), e("address1"), //
				", ", u.Age(), ")", e("10"), //
				SqlHelper.questionMarks());
		DataSource ds = BeanBox.getBean(MySqlDataSourceBox.class);
		TinyJdbc.execute(ds, Connection.TRANSACTION_READ_COMMITTED, "insert into users (username) values(?)",
				"TinyJdbc");
		Assert.assertEquals(2, (int) TinyJdbc.queryForInteger(ds, 2, "select count(*) from users"));
		Assert.assertEquals("TinyJdbc",
				TinyJdbc.queryForString(ds, 2, "select username from users where username =?", "TinyJdbc"));
		Assert.assertEquals("TinyJdbc",
				TinyJdbc.queryForObject(ds, 2, "select username from users where username =?", "TinyJdbc"));
	}

	public void tx_InsertUser2() {
		System.out.println(1 / 0);// throw a runtime exception
	}

	public void tx_doInsert() {
		tx_InsertUser1();
		tx_InsertUser2();
	}

	@Test
	public void doTest() {
		User u = new User();
		TinyJdbcTest tester = BeanBox.getBean(TinyJdbcTest.class);
		boolean foundException = false;
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			foundException = true;
			Assert.assertEquals(InvocationTargetException.class.getName(), e.getClass().getName());
			int i = u.dao().queryForInteger("select count(*) from users");
			Assert.assertEquals(1, i);
		}
		Assert.assertEquals(foundException, true);
	}

}