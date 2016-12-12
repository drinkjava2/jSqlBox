package test.transaction;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;

import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlHelper;

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
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	public void tx_InsertUser1() {
		User u = new User();
		u.dao().execute("insert into ", u.table(), //
				" (", u.userName(), empty("user1"), //
				", ", u.address(), empty("address1"), //
				", ", u.age(), ")", empty("10"), //
				SqlHelper.questionMarks());
	}

	public void tx_InsertUser2() {
		User u = new User();
		u.dao().execute("insert into ", u.table(), //
				" (", u.userName(), empty("user2"), //
				", ", u.address(), empty("address2"), //
				", ", u.age(), ")", empty("20"), //
				SqlHelper.questionMarks());
	}

	public void tx_doInsert() {
		User u = new User();
		tx_InsertUser1();
		int i = u.dao().queryForInteger("select count(*) from ", u.table());
		Assert.assertEquals(1, i);
		System.out.println(i / 0);// throw a runtime exception
		tx_InsertUser2();
	}

	@Test
	public void doTest() {
		User u = new User();
		JBeanBoxTransactionTest tester = BeanBox.getBean(JBeanBoxTransactionTest.class);
		boolean foundException = false;
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			foundException = true;
			Assert.assertEquals(InvocationTargetException.class.getName(), e.getClass().getName());
			int i = u.dao().queryForInteger("select count(*) from users");
			Assert.assertEquals(0, i);
		}
		Assert.assertEquals(foundException, true);
	}

}