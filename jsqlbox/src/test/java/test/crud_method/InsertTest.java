package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.TestPrepare;
import test.config.po.User;

public class InsertTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void insertUserA() {
		User u = new User();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAlive(true);
		u.insert();
		Assert.assertEquals(111, (int) SqlBox.queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User1")));
		Assert.assertTrue(u.getId() > 0);
		User u2 = SqlBox.load(User.class, u.getId());
		Assert.assertTrue(u2.getAlive());
	}

	@Test
	public void insertUserB() {
		User u = SqlBox.createBean(User.class);
		u.setUserName("User2");
		u.setAddress("Address2");
		u.setPhoneNumber("222");
		u.insert();
		Assert.assertEquals("222", SqlBox.queryForString("select ", u.phoneNumber(), " from ", u.table(), " where ",
				u.userName(), "=" + q("User2")));
		Assert.assertTrue(u.getId() > 0);
	}

	@Test
	public void insertUserC() {
		for (int i = 0; i < 10000; i++) {
			User u = SqlBox.createBean(User.class);
			u.setUserName("User2");
			u.setAddress("Address2");
			u.setPhoneNumber("222");
			Dao d = u.dao();
			if (d == null)
				System.out.println("null");
		}
	}

	@Test
	public void tx_insertUsers() {
		insertUserA();
		insertUserB();
	}

	@Test
	public void insertUsersWithinTransaction() {
		InsertTest t = BeanBox.getBean(InsertTest.class); // get Proxy bean
		t.tx_insertUsers(); // use Spring Declarative Transaction
	}

	public static void main(String[] args) {
		TestPrepare.dropAndRecreateTables();
		long oldtime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			SqlBoxContext ctx = SqlBoxContext.defaultSqlBoxContext;
			User u = new User();// SqlBox.createBean(User.class);
			u.setUserName("User2");
			u.setAddress("Address2");
			u.setPhoneNumber("222");
			Dao d = u.dao();
			if (d == null)
				System.out.println("null");
		}
		long newTime = System.currentTimeMillis();
		System.out.println("Time used=" + (newTime - oldtime));
	}
}