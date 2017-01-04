package test.transaction;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.TestPrepare;
import test.config.po.User;

/**
 * This is to test use Spring's Declarative Transaction but use jBeanBox replaced Spring's IOC/AOP core. <br/>
 * More detail please see jBeanBox project
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JBeanBoxTransactionTest {
	@Before
	public void setup() {
		System.out.println(
				"===============================Testing JBeanBoxTransactionTest===============================");
		TestPrepare.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	public void tx_InsertUser1() {
		User u = new User();
		Dao.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user1"), //
				", ", u.address(), empty("address1"), //
				", ", u.age(), ")", empty("10"), //
				questionMarks());
	}

	public void tx_InsertUser2() {
		User u = new User();
		Dao.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user2"), //
				", ", u.address(), empty("address2"), //
				", ", u.age(), ")", empty("20"), //
				questionMarks());
	}

	public static void insertAnother() {
		User u = new User();
		Dao.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user3"), //
				", ", u.address(), empty("address3"), //
				", ", u.age(), ")", empty("30"), //
				questionMarks());
	}

	public void tx_doInsert() {
		User u = new User();
		tx_InsertUser1();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		tx_InsertUser2();
		Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		insertAnother();
		Assert.assertEquals(3, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		int count = Dao.queryForInteger("select count(*) from ", u.table());
		System.out.println("Inserted " + count + " record into database");
		Assert.assertEquals(3, count);
		System.out.println(1 / 0);// throw a runtime exception
	}

	@Test
	public void doTest() {
		JBeanBoxTransactionTest tester = BeanBox.getBean(JBeanBoxTransactionTest.class);
		boolean foundException = false;
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			User u = new User();
			foundException = true;
			Assert.assertEquals(InvocationTargetException.class, e.getClass());
			int count = Dao.queryForInteger("select count(*) from ", u.table());
			System.out.println("After roll back, there is " + count + " record in database");
			Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		}
		Assert.assertEquals(foundException, true);
	}

}