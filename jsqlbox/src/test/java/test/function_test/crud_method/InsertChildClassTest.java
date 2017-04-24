package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.User;

public class InsertChildClassTest {

	@Before
	public void setup() {
		System.out.println("=============Testing " + this.getClass().getName() + "================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Dao.executeQuiet("drop table user2");
		Dao.execute(User2.ddl(Dao.getDialect()));
		Dao.refreshMetaData();
	}

	@After
	public void cleanUp() {
		Dao.execute("drop table user2");
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	public static class User2 extends User {
		{
			this.box().configEnable("active", false);
			// this.box().configEnable("age", false);
		}

		public static String ddl(Dialect d) {
			return "create table user2 " //
					+ "(id " + d.VARCHAR(32) //
					+ ", username " + d.VARCHAR(32) //
					+ ", Phone_Number " + d.VARCHAR(30) //
					+ ", Address  " + d.VARCHAR(32) //
					+ ", constraint users2_pk primary key (id)" //
					+ ")" + d.engine();
		}
	}

	@Test
	public void insertU2() {
		User2 u = new User2();
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