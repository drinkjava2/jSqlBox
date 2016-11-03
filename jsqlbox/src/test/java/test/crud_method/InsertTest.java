package test.crud_method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.Config;
import test.crud_method.po.User;

public class InsertTest {

	@Before
	public void setup() {
		Config.recreateDatabase();
	}

	@Test
	public void insert() {
		User user = new User();
		user.setUserName("User1");
		user.setAddress("Address1");
		user.setPhoneNumber("11111");
		user.setAge(10);
		user.dao().save();

		User user2 = new User();
		user2.dao().getSqlBox().setColumnName(User.PhoneNumber, null);
		user2.setUserName("User2");
		user2.setAddress("Address2");
		user2.setPhoneNumber("22222");// this phone number will not write to table
		user2.setAge(20);
		user2.dao().save();

		Assert.assertEquals(2, (int) Dao.dao.queryForInteger("select count(*) from user"));
	}

}