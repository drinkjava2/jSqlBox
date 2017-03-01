package test.function_test.transaction;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.AopAround;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.JBeanBoxConfig.SpringTxInterceptorBox;
import test.config.PrepareTestContext;
import test.config.po.User;

/**
 * This is to test use Spring's Declarative Transaction but use jBeanBox replaced Spring's IOC/AOP core. <br/>
 * More detail please see jBeanBox project
 *
 * @author Yong Zhu
 *
 * @since 1.0.0
 */
public class JBeanBoxTransactionTest {
	@Before
	public void setup() {
		System.out.println("=========================Testing JBeanBoxTransactionTest=========================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	public void tx_InsertUser1() {
		User u = new User();
		Dao.execute("insert into ", u.table(), //
				" (", u.USERNAME(), empty("user1"), //
				", ", u.ADDRESS(), empty("address1"), //
				", ", u.AGE(), ")", empty("10"), //
				valuesAndQuestions());
	}

	public void tx_InsertUser2() {
		User u = new User();
		Dao.execute("insert into ", u.table(), //
				" (", u.USERNAME(), empty("user2"), //
				", ", u.ADDRESS(), empty("address2"), //
				", ", u.AGE(), ")", empty("20"), //
				valuesAndQuestions());
	}

	public void tx_doInsert() {
		User u = new User();
		tx_InsertUser1();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		tx_InsertUser2();
		Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		int count = Dao.queryForInteger("select count(*) from ", u.table());
		System.out.println("Inserted " + count + " record into database");
		Assert.assertEquals(2, count);
		System.out.println(1 / 0);// throw a runtime exception
	}

	@Test
	public void doTest() {
		BeanBox.defaultContext.setAOPAround("test.\\w*.\\w*.\\w*", "tx_\\w*", new SpringTxInterceptorBox(), "invoke");
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

	@AopAround(SpringTxInterceptorBox.class)
	public void insertButUseAopAroundAnnotation() {
		User u = new User();
		Dao.execute("insert into ", u.table(), " (", //
				u.USERNAME() + empty("user3"), ", ", //
				u.AGE() + empty("30"), //
				")", valuesAndQuestions());
		int count = Dao.queryForInteger("select count(*) from ", u.table());
		System.out.println("Inserted " + count + " record into database");
		Assert.assertEquals(1, count);
		System.out.println(1 / 0);// throw a runtime exception
	}

	@Test
	public void doAopAroundTest() {
		JBeanBoxTransactionTest tester = BeanBox.getBean(JBeanBoxTransactionTest.class);
		boolean foundException = false;
		try {
			tester.insertButUseAopAroundAnnotation();
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