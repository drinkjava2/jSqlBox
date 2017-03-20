package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.po.User;

public class InsertTest extends TestBase {

	@Test
	public void insertUserA() {
		User u = new User();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setActive(true);
		u.insert();
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.USERNAME(), "=", q("User1")));
		Assert.assertTrue(u.getId() > 0);
		User u2 = Dao.load(User.class, u.getId());
		Assert.assertTrue(u2.getActive());
	}

	@Test
	public void insertUserB() {
		User u = new User();
		u.setUserName("User2");
		u.setAddress("Address2");
		u.setPhoneNumber("222");
		u.insert();
		Assert.assertEquals("222", Dao.queryForString("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.USERNAME(), "=" + q("User2")));
		Assert.assertTrue(u.getId() > 0);
	}

	@Test
	public void tx_insertUsers() {
		insertUserA();
		insertUserB();
	}

	@Test
	public void insertUsersWithinTransaction() {
		InsertTest t = BeanBox.getBean(InsertTest.class); // get Proxy bean
		t.tx_insertUsers(); // use Spring Declarative Transaction
	}

}