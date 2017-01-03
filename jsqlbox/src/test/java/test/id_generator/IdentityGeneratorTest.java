package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;
import test.config.po.User;

public class IdentityGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
	}

	@Test
	public void insertUserNoConfig() {
		User u = new User();
		u.setUserName("User1");
		for (int i = 0; i < 10; i++) {
			u.insert();
		}
		Assert.assertEquals(10, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
	}

	@Test
	public void insertUser() {
		User u = new User();
		u.box().configIdGenerator("id", AutoGenerator.INSTANCE);
		u.setUserName("User1");
		for (int i = 0; i < 10; i++) {
			u.insert();
		}
		Assert.assertEquals(10, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
	}

	@Test
	public void insertUserForMysql() {
		if (SqlBoxContext.getDefaultSqlBoxContext().getDatabaseType() != DatabaseType.MYSQL)
			return;
		User u = new User();
		u.box().configIdGenerator("id", IdentityGenerator.INSTANCE);
		u.setUserName("User1");
		for (int i = 0; i < 10; i++) {
			u.insert();
		}
		Assert.assertEquals(10, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
	}

}