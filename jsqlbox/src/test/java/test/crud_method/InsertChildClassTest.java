package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class InsertChildClassTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDefaultContexts();
	}

	public static class U2 extends User {
		private static final long serialVersionUID = 1014969353551282130L;

		{
			this.box().configTable("users2");
		}
	}

	@Test
	public void insertU2() {
		U2 u = new U2();
		u.setPhoneNumber("111");
		u.insert();
		Assert.assertEquals(111, (int) SqlBox.queryForInteger("select ", u.phoneNumber(), " from ", "users2", " where ",
				u.phoneNumber(), "=", q("111")));
		u.box().configTable("users");
		u.setPhoneNumber("222");
		u.insert();
		Assert.assertEquals(222, (int) SqlBox.queryForInteger("select ", u.phoneNumber(), " from ", "users", " where ",
				u.phoneNumber(), "=", q("222")));
	}
}