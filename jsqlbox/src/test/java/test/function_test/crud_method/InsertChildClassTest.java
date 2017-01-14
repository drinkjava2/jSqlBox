package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.User;

public class InsertChildClassTest {

	@Before
	public void setup() {
		System.out.println("=============================Testing InsertChildClassTest=============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
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