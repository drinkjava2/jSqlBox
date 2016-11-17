package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.InitializeDatabase;
import test.crud_method.po.User;

public class InsertTest {

	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	@Test
	public void insertUser1() {
		User u = new User();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().save();
		Assert.assertEquals(111, (int) Dao.dao.queryForInteger("select ", u.PhoneNumber(), " from ", u.Table(),
				" where ", u.UserName(), "=", q("User1")));
	}

	@Test
	public void insertUser2() {
		User u = new User();
		u.dao().getSqlBox().setColumnName(User.PhoneNumber, null);
		u.setUserName("User2");
		u.setAddress("Address2");
		u.setPhoneNumber("222");// this phone number will not write to table
		u.setAge(20);
		u.dao().save();
		Assert.assertEquals(null, Dao.dao.queryForString("select ", u.PhoneNumber(), " from ", u.Table(), " where ",
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
		InitializeDatabase.recreateTables();
		InsertTest t = new InsertTest();
		t.insertUser1();
	}
}