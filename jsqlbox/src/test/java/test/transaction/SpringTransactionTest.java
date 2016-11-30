package test.transaction;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.InitializeDatabase;
import test.config.SpringConfig;
import test.config.po.User;

/**
 * This is to test use Spring's Declarative Transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Transactional(propagation = Propagation.REQUIRED)
public class SpringTransactionTest {
	private static SqlBoxContext defaultSqlBoxContext = null;

	public static SqlBoxContext getDefaultCTX() {
		return defaultSqlBoxContext;
	}

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	public void tx_InsertUser1() {
		User u = new User();
		u.dao().execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user1"), //
				", ", u.Address(), e("address1"), //
				", ", u.Age(), ")", e("10"), //
				SqlHelper.questionMarks());
	}

	public void tx_InsertUser2() {
		User u = new User();
		u.dao().execute("insert into ", u.Table(), //
				" (", u.UserName(), e("user2"), //
				", ", u.Address(), e("address2"), //
				", ", u.Age(), ")", e("20"), //
				SqlHelper.questionMarks());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_doInsert() {
		User u = new User();
		tx_InsertUser1();
		int i = u.dao().queryForInteger("select count(*) from ", u.Table());
		Assert.assertEquals(1, i);
		System.out.println(i / 0);// throw a runtime exception
		tx_InsertUser2();
	}

	@Test
	public void doTest() {
		AnnotationConfigApplicationContext springCTX = new AnnotationConfigApplicationContext(SpringConfig.class);
		SqlBoxContext sc = springCTX.getBean("sqlBoxCtxBean", SqlBoxContext.class);
		defaultSqlBoxContext = sc;
		SqlBoxContext.configDefaultContext(SpringTransactionTest.class.getName(), "getDefaultCTX");
		SpringTransactionTest tester = springCTX.getBean(SpringTransactionTest.class);
		boolean foundException = false;
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			foundException = true;
			User u = new User();
			int i = u.dao().queryForInteger("select count(*) from ", u.Table());
			Assert.assertEquals(0, i);
		}
		Assert.assertEquals(foundException, true);
		springCTX.close();
	}

}