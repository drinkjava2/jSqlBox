package test.transaction;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.SpringConfig;
import test.config.TestPrepare;
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

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_doInsert() {
		User u = new User();
		tx_InsertUser1();
		int count = Dao.queryForInteger("select count(*) from ", u.table());
		System.out.println("Inserted " + count + " record into database");
		Assert.assertEquals(1, count);
		System.out.println(1 / 0);// throw a runtime exception
		tx_InsertUser2();
	}

	@Test
	public void doTest() {
		System.out
				.println("===============================Testing SpringTransactionTest===============================");
		TestPrepare.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		TestPrepare.closeDatasource_closeDefaultSqlBoxConetxt();

		AnnotationConfigApplicationContext springCTX = new AnnotationConfigApplicationContext(SpringConfig.class);
		SqlBoxContext.setDefaultSqlBoxContext(springCTX.getBean(SqlBoxContext.class));
		SpringTransactionTest tester = springCTX.getBean(SpringTransactionTest.class);
		boolean foundException = false;
		try {
			tester.tx_doInsert();
		} catch (Exception e) {
			foundException = true;
			User u = new User();
			int count = Dao.queryForInteger("select count(*) from ", u.table());
			System.out.println("After roll back, there is " + count + " record in database");
			Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		}
		Assert.assertEquals(foundException, true);

		SqlBoxContext.getDefaultSqlBoxContext().close();
		springCTX.close();
	}

}