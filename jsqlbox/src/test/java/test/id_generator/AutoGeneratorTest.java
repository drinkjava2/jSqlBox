package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;

import test.config.TestPrepare;
import test.config.po.User;

public class AutoGeneratorTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing AutoGeneratorTest===============================");
		TestPrepare.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_closeDefaultSqlBoxConetxt();
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

}