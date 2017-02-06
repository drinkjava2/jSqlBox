package test.examples;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

import test.config.PrepareTestContext;

public class HelloWorld3 {

	public static class User implements Entity {
		Integer id;
		String userName;
		String address;
		String phoneNumber;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

	}

	@Test
	public void doTest1() {
		System.out.println("===============================Testing HelloWorld3===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();

		User user = new User();
		SqlBox box = SqlBoxContext.getDefaultBox(user);
		Dao.executeQuiet("delete from ", box.realTable());
		user.setUserName("Sam");

		box.insert();
		Assert.assertEquals("Sam", Dao.queryForString("select USERNAME from users"));

		box.configTable("users2");
		box.configIdGenerator("phoneNumber", UUIDGenerator.INSTANCE);
		box.configColumnName("userName", "address");
		Dao.executeQuiet("delete from ", box.realTable());
		box.insert();
		Assert.assertEquals("Sam", Dao.queryForString("select ADDRESS from users2"));
		Assert.assertEquals(32, Dao.queryForString("select PHONE_NUMBER from users2").length());
	}

	public static class ChildUser extends User implements Entity {
	}

	@Test
	public void doTest2() {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		User user = new ChildUser();
		SqlBox box = SqlBoxContext.getDefaultBox(user);
		box.configTable("users2");
		Dao.executeQuiet("delete from ", box.realTable());
		user.setUserName("Sam");
		((Entity) user).insert();
		Assert.assertEquals("Sam", Dao.queryForString("select USERNAME from users2"));
	}

}