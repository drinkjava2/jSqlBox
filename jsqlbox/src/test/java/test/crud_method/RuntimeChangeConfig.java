package test.crud_method;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.InitializeDatabase;
import test.config.po.User;

public class RuntimeChangeConfig {

	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	@Test
	public void changeRuntimeTablename() {
		User u2 = SqlBox.createBean(User.class);
		u2.dao().setTableName("users2");
		u2.dao().setColumnName(User.Address, u2.PhoneNumber());
		u2.setUserName("user2");
		u2.setAddress("address2");
		u2.dao().save();

		User u1 = u2.dao().createBean();
		u1.setUserName("user1");
		u1.setAddress("address1");
		u1.dao().save();

	}
}