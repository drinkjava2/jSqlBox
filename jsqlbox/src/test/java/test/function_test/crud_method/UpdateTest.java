package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.po.User;

public class UpdateTest extends TestBase {

	@Test
	public void updateUser() {
		User u = new User(); 
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.insert();
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.ID(), "=", q(u.getId())));
		u.setPhoneNumber("333");
		u.update();
		Assert.assertEquals(333, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.ID(), "=", q(u.getId())));
	}

}