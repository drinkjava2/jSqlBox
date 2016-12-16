package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;

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
	public void insertUser1() {
		User u = new User();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.dao().insert();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User1")));
	}

	@Test
	public void insertUser2() {
		User u = SqlBox.createBean(User.class);
		u.setUserName("User2");
		u.setAddress("Address2");
		u.setPhoneNumber("222");
		u.dao().insert();
		Assert.assertEquals("222", u.dao().queryForString("select ", u.phoneNumber(), " from ", u.table(), " where ",
				u.userName(), "=" + q("User2")));
	}

	@Test
	public void tx_insertUsers() {
		insertUser1();
		insertUser2();
	}

	@Test
	public void insertUsersWithinTransaction() {
		InsertTest t = BeanBox.getBean(InsertTest.class); // get Proxy bean
		t.tx_insertUsers(); // use Spring Declarative Transaction
	}

	@Test
	public void insertUser() {
		User u = new User();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.dao().insert();
	}

	public static void main(String[] args) {
		TestPrepare.dropAndRecreateTables();
		for (int i = 0; i < 100000; i++) {
			System.out.print(i + "=");
			InsertTest t = new InsertTest();
			t.insertUser();
			System.out.println(Dao.dao().queryForInteger("select count(*) from users"));
		}

	}
}