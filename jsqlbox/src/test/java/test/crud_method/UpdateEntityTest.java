package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;

import test.config.TestPrepare;
import test.config.po.User;

public class UpdateEntityTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void updateUser() {
		User u = new User();
		u.box().configIdGenerator("id", (IdGenerator) new BeanBox(IdentityGenerator.class).getBean());
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.dao().insert();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.id(), "=", q(u.getId())));
		u.setPhoneNumber("333");
		u.dao().update();
		Assert.assertEquals(333, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.id(), "=", q(u.getId())));
	}

}