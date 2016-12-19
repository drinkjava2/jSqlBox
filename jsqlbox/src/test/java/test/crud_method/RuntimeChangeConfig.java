package test.crud_method;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class RuntimeChangeConfig {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void normal() {
		User u = SqlBox.createBean(User.class);
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) SqlBox.queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeTable() {
		User u = SqlBox.createBean(User.class);
		u.box().configTable("users2");
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(0, (int) SqlBox.queryForInteger("select count(*) from users"));
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeColumnName() {
		User u = SqlBox.createBean(User.class);
		u.box().configColumnName("userName", u.address());
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(1,
				(int) SqlBox.queryForInteger("select count(*) from users where ", u.address(), "='Sam'"));
	}

	@Test
	public void changeTableAndColumnName() {
		User u = SqlBox.createBean(User.class);
		u.box().configTable("users2");
		u.box().configColumnName("userName", u.address());
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(1,
				(int) SqlBox.queryForInteger("select count(*) from users2 where ", u.address(), "='Sam'"));
	}
}