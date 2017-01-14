package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;

import test.config.PrepareTestContext;
import test.config.po.User;

public class UpdateTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing UpdateTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void updateUser() {
		User u = new User();
		u.box().configIdGenerator("id", AutoGenerator.INSTANCE);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.insert();
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(),
				" where ", u.ID(), "=", q(u.getId())));
		u.setPhoneNumber("333");
		u.update();
		Assert.assertEquals(333, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(),
				" where ", u.ID(), "=", q(u.getId())));
	}

}