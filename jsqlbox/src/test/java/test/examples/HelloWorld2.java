package test.examples;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

import test.config.PrepareTestContext;
import test.config.po.User;

public class HelloWorld2 {

	@Before
	public void setup() {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void doTest() {
		System.out.println("===============================Testing HelloWorld2===============================");
		User user = new User();
		System.out.println(user.table());
		Dao.executeQuiet("delete from ", user.table());
		user.setUserName("Sam");
		user.insert();
		Assert.assertEquals("Sam", Dao.queryForString("select USERNAME from users"));

		user.box().configTable("users2");
		user.box().configIdGenerator("phoneNumber", UUIDGenerator.INSTANCE);
		user.box().configColumnName("userName", "address");
		Dao.executeQuiet("delete from ", user.table());
		user.setUserName("Tom");
		user.insert();
		Assert.assertEquals("Tom", Dao.queryForString("select ADDRESS from users2"));
		Assert.assertEquals(32, Dao.queryForString("select PHONE_NUMBER from users2").length());
	}

	public static class ChildUser extends User {
	}

	@Test
	public void doTest2() {
		User user = new ChildUser();
		SqlBox box = SqlBoxContext.getDefaultBox(user);
		box.configTable("users2");
		Dao.executeQuiet("delete from ", box.realTable());
		user.setUserName("Sam");
		((Entity) user).insert();
		Assert.assertEquals("Sam", Dao.queryForString("select USERNAME from users2"));
	}

}