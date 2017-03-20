package test.function_test.crud_method;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.po.User;

public class RuntimeChangeConfig extends TestBase {

	@Test
	public void normal() {
		User u = new User();
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeTable() {
		User u = new User();
		u.box().configTable("users2");
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeColumnName() {
		User u = new User();
		u.box().configColumnName(u.fieldID(u.USERNAME()), u.ADDRESS());
		u.box().configColumnName(u.fieldID(u.ADDRESS()), u.PHONENUMBER());
		u.setUserName("Sam");
		u.setPhoneNumber("111");
		u.insert();
		// below line, sql is "select Address from users"
		Assert.assertEquals("Sam", Dao.queryForString("select ", u.USERNAME(), " from ", u.table()));

		// below line sql is "select phoneNumber from users"
		Assert.assertEquals("111", Dao.queryForString("select ", u.ADDRESS(), " from ", u.table()));
	}

	@Test
	public void changeTableAndColumnName() {
		User u = new User();
		u.box().configTable("users2");
		u.box().configColumnName("userName", u.ADDRESS());
		u.setUserName("Sam");
		u.insert();
		// below line, sql is "select Address from users2"
		Assert.assertEquals("Sam", Dao.queryForString("select ", u.ADDRESS(), " from ", u.table()));

		// below line, sql is "select Address from users2"
		Assert.assertEquals("Sam", Dao.queryForString("select ", u.USERNAME(), " from ", u.table()));
	}
}