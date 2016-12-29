package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.TestPrepare;
import test.config.po.User;

public class InsertChildClassTest {

	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
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
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.phoneNumber(), "=", q("111")));
		u.box().configTable("users");
		u.setPhoneNumber("222");
		u.insert();
		Assert.assertEquals(222, (int) Dao.queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.phoneNumber(), "=", q("222")));
	}
}