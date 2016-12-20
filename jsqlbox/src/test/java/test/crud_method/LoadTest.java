package test.crud_method;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;

import test.config.TestPrepare;
import test.config.po.User;

public class LoadTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void loadSingleID() {
		User u = new User();
		u.box().configIdGenerator("id", AutoGenerator.INSTANCE);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();
		Assert.assertTrue(SqlBox.queryForInteger("select ", u.id(), " from ", u.table()) > 0);
		Assert.assertTrue(u.getId() > 0);
		User u2 = SqlBox.load(User.class, u.getId());
		Assert.assertEquals("Address1", u2.getAddress());
	}

	@Test
	public void loadCompositeID() {
		User u = new User();
		u.box().configIdGenerator("id", AutoGenerator.INSTANCE);
		u.box().configEntityIDs("userName", "address");
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
		User u2 = SqlBox.load(User.class, u.dao().getEntityID());
		Assert.assertEquals("Address1", u2.getAddress());
	}

	@Test
	public void loadCompositeIDbyMap() {
		User u = new User();
		u.box().configIdGenerator("id", AutoGenerator.INSTANCE);
		u.box().configEntityIDs("userName", "address");
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
		Map<String, Object> entityID = new HashMap<>();
		entityID.put("userName", "User1");
		entityID.put("address", "Address1");
		User u2 = SqlBox.load(User.class, entityID);
		Assert.assertEquals("Address1", u2.getAddress());
	}

	public static void main(String[] args) {
		BigDecimal b = new BigDecimal(13);
		System.out.println(b);
	}

}