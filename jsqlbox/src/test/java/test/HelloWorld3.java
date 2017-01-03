package test;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.IEntity;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;

public class HelloWorld3 {

	public static class User {
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
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));

		User user = new User();
		SqlBox box = SqlBox.getBox(user);
		Dao.executeQuiet("delete from ", box.table());
		user.setUserName("Sam");

		box.insert();
		Assert.assertEquals("Sam", Dao.queryForString("select USERNAME from users"));

		box.configTable("users2");
		box.configIdGenerator("phoneNumber", UUIDGenerator.INSTANCE);
		box.configColumnName("userName", "address");
		Dao.executeQuiet("delete from ", box.table());
		box.insert();
		Assert.assertEquals("Sam", Dao.queryForString("select ADDRESS from users2"));
		Assert.assertEquals(32, Dao.queryForString("select PHONE_NUMBER from users2").length());
	}

	public static class ChildUser extends User implements IEntity {
	}

	@Test
	public void doTest2() {
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		User user = new ChildUser();
		SqlBox box = SqlBox.getBox(user);
		box.configTable("users2");
		Dao.executeQuiet("delete from ", box.table());
		user.setUserName("Sam");
		((IEntity) user).insert();
		Assert.assertEquals("Sam", Dao.queryForString("select USERNAME from users2"));
	}

}