package test.crud_method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.InitializeDatabase;
import test.config.po.User;

public class RuntimeChangeConfig {

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	@Test
	public void normal() {
		User u = SqlBox.createBean(User.class);
		u.setUserName("Sam");
		u.dao().save();
		Assert.assertEquals(1, (int) Dao.dao().queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) Dao.dao().queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeTable() {
		User u = SqlBox.createBean(User.class);
		u.dao().box().configTable("users2");
		u.setUserName("Sam");
		u.dao().save();
		Assert.assertEquals(0, (int) Dao.dao().queryForInteger("select count(*) from users"));
		Assert.assertEquals(1, (int) Dao.dao().queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeColumnName() {
		User u = SqlBox.createBean(User.class);
		u.dao().box().configColumnName(User.UserName, u.Address());
		u.setUserName("Sam");
		u.dao().save();
		Assert.assertEquals(1,
				(int) Dao.dao().queryForInteger("select count(*) from users where ", u.Address(), "='Sam'"));
	}

	@Test
	public void changeTableAndColumnName() {
		User u = SqlBox.createBean(User.class);
		u.dao().box().configTable("users2");
		u.dao().box().configColumnName(User.UserName, u.Address());
		u.setUserName("Sam");
		u.dao().save();
		Assert.assertEquals(1,
				(int) Dao.dao().queryForInteger("select count(*) from users2 where ", u.Address(), "='Sam'"));
	}
}