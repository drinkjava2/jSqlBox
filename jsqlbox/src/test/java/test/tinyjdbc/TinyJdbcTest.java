package test.tinyjdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

import test.config.TestPrepare;
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
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void tx_InsertUser1() {
		User u = new User();
		SqlBox.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user1"), //
				", ", u.address(), empty("address1"), //
				", ", u.age(), ")", empty("10"), //
				questionMarks());
		DataSource ds = SqlBox.getDefaultContext().getDataSource();
		TinyJdbc.execute(ds, TinyJdbc.TRANSACTION_READ_COMMITTED, "insert into users (age) values(?)", "20");
		Assert.assertEquals(20, (int) TinyJdbc.queryForInteger(ds, 2, "select age from users where age =?", "20"));
	}

	public void tx_InsertUser2() {
		System.out.println(1 / 0);// throw a runtime exception
	}

	public void tx_doInsert() {
		tx_InsertUser1();
		tx_InsertUser2();
	}

	//@Test
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