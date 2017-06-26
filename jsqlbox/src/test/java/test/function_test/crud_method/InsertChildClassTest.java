package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.entity.User;

public class InsertChildClassTest {

	@Before
	public void setup() {
		System.out.println("=============Testing " + this.getClass().getName() + "================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Dao.executeManyQuiet(Dao.getDialect().toDropDDL(User2.model()));
		Dao.executeMany(Dao.getDialect().toCreateDDL(User2.model()));
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

		public static Table model() {
			Table t = new Table("user2");
			t.column("id").VARCHAR(32).pkey();
			t.column("username").VARCHAR(32);
			t.column("Phone_Number").VARCHAR(32);
			t.column("Address").VARCHAR(32);
			return t;
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