package test.crud_method;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class RuntimeChangeConfig {

	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
		SqlBox.getDefaultContext().setShowSql(true);
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
	}

	@Test
	public void normal() {
		User u = SqlBox.createEntity(User.class);
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) SqlBox.queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeTable() {
		User u = SqlBox.createEntity(User.class);
		u.box().configTable("users2");
		u.setUserName("Sam");
		u.insert();
		Assert.assertEquals(0, (int) SqlBox.queryForInteger("select count(*) from users"));
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from users2"));
	}

	@Test
	public void changeColumnName() {
		User u = SqlBox.createEntity(User.class);
		u.box().configColumnName(u.fieldID(u.userName()), u.address());
		u.box().configColumnName(u.fieldID(u.address()), u.phoneNumber());
		u.setUserName("Sam");
		u.setPhoneNumber("111");
		u.insert();
		// below line, sql is "select Address from users"
		Assert.assertEquals("Sam", SqlBox.queryForString("select ", u.userName(), " from ", u.table()));

		// below line sql is "select phoneNumber from users"
		Assert.assertEquals("111", SqlBox.queryForString("select ", u.address(), " from ", u.table()));
	}

	@Test
	public void changeTableAndColumnName() {
		User u = SqlBox.createEntity(User.class);
		u.box().configTable("users2");
		u.box().configColumnName("userName", u.address());
		u.setUserName("Sam");
		u.insert();
		// below line, sql is "select Address from users2"
		Assert.assertEquals("Sam", SqlBox.queryForString("select ", u.address(), " from ", u.table()));

		// below line, sql is "select Address from users2"
		Assert.assertEquals("Sam", SqlBox.queryForString("select ", u.userName(), " from ", u.table()));
	}
}