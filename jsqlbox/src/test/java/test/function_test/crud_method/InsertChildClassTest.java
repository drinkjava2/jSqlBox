package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.po.User;

public class InsertChildClassTest extends TestBase {

	public static class U2 extends User {
		{
			this.box().configTable("users2");
		}
	}

	@Test
	public void insertU2() {
		U2 u = new U2();
		u.setPhoneNumber("111");
		u.insert();
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.PHONENUMBER(), "=", q("111")));
		u.box().configTable("users");
		u.setPhoneNumber("222");
		u.insert();
		Assert.assertEquals(222, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.PHONENUMBER(), "=", q("222")));
	}
}