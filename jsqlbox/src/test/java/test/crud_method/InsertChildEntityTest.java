package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.config.TestPrepare;
import test.config.po.User;
import test.config.po.User2;

public class InsertChildEntityTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void insertUser2() {
		User2 u = new User2();
		u.setUserName("User2");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.dao().insert();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User2")));
	}

	public static class U2 extends User {
		{
			this.box().configTable("users2");
		}
	}

	@Test
	public void insertU2() {
		U2 u = new U2();
		u.setPhoneNumber("111");
		u.dao().insert();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.phoneNumber(), "=", q("111")));
		u.box().configTable("users");
		u.setPhoneNumber("222");
		u.dao().insert();
		Assert.assertEquals(222, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.phoneNumber(), "=", q("222")));
	}
}