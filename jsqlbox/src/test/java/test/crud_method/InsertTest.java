package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.InitializeDatabase;
import test.config.po.User;

public class InsertTest {

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	@Test
	public void insertUser1() {
		User u = new User();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.dao().save();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.PhoneNumber(), " from ", u.Table(),
				" where ", u.UserName(), "=", q("User1")));
	}

	@Test
	public void insertUser2() {
		User u = SqlBox.createBean(User.class);
		u.setUserName("User2");
		u.setAddress("Address2");
		u.setPhoneNumber("222");
		u.dao().save();
		Assert.assertEquals("222", u.dao().queryForString("select ", u.PhoneNumber(), " from ", u.Table(), " where ",
				u.UserName(), "=" + q("User2")));
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

	public static void main(String[] args) {
		InitializeDatabase.dropAndRecreateTables();
		InsertTest t = new InsertTest();
		t.insertUser2();
	}
}