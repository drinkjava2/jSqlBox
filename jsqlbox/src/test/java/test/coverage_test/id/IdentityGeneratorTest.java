package test.coverage_test.id;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;

import test.config.PrepareTestContext;
import test.config.po.User;

public class IdentityGeneratorTest {

	@Before
	public void setup() {
		System.out
				.println("===============================Testing IdentityGeneratorTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
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
		if (!(Dao.getDefaultDatabaseType().isMySql() || Dao.getDefaultDatabaseType().isH2()))
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