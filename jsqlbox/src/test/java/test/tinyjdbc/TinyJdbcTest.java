package test.tinyjdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

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
		u.dao().execute("insert into ", u.table(), //
				" (", u.userName(), e("user1"), //
				", ", u.address(), e("address1"), //
				", ", u.age(), ")", e("10"), //
				SqlHelper.questionMarks());
		DataSource ds = BeanBox.getBean(MySqlDataSourceBox.class);
		TinyJdbc.execute(ds, Connection.TRANSACTION_READ_COMMITTED, "insert into users (age) values(?)", "20");
		Assert.assertEquals(20, (int) TinyJdbc.queryForInteger(ds, 2, "select age from users where age =?", "20"));
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
		TinyJdbcTest tester = BeanBox.getBean(TinyJdbcTest.class);
		boolean foundException = false;
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			foundException = true;
		}
		Assert.assertEquals(foundException, true);
	}

}