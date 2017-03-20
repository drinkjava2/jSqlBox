
package test.function_test.crud_method;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;

import test.TestBase;
import test.config.po.User;

/**
 * This is for test load method, and test composite Entiity ID
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class LoadTest extends TestBase {

	@Test
	public void loadSingleID() {
		User u = new User();
		// Default id is EntityID
		u.box().configIdGenerator(u.fieldID(u.ID()), AutoGenerator.INSTANCE);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();
		Assert.assertTrue(Dao.queryForInteger("select ", u.ID(), " from ", u.table()) > 0);
		Assert.assertTrue(u.getId() > 0);
		User u2 = Dao.load(User.class, u.getId());
		Assert.assertEquals("Address1", u2.getAddress());
		u2.delete();
		Assert.assertEquals("Address1", u2.getAddress());
		Assert.assertNull(u2.getId());
		Assert.assertNull(Dao.queryForString("select ", u.ID(), " from ", u.table()));
		Assert.assertTrue(Dao.queryForInteger("select count(*)   from ", u.table()) == 0);
	}

	@Test
	public void loadCompositeID() {
		Dao.getDefaultContext().setShowSql(true);
		User u = new User();
		u.box().configIdGenerator(u.fieldID(u.ID()), AutoGenerator.INSTANCE);
		u.box().configEntityIDs(u.fieldID(u.USERNAME()), u.fieldID(u.ADDRESS()));
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
		User u2 = Dao.load(User.class, u.box().getEntityID());
		Assert.assertEquals("Address1", u2.getAddress());
	}

	@Test
	public void loadCompositeIDbyMap() {
		User u = new User();
		u.box().configIdGenerator(u.fieldID(u.ID()), AutoGenerator.INSTANCE);
		u.box().configEntityIDs(u.fieldID(u.USERNAME()), u.fieldID(u.ADDRESS()));
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from ", u.table()));
		Assert.assertTrue(u.getId() > 0);
		Map<String, Object> entityID = new HashMap<>();
		entityID.put(u.USERNAME(), "User1");
		entityID.put(u.ADDRESS(), "Address1");
		User u2 = Dao.load(User.class, entityID);
		Assert.assertEquals("Address1", u2.getAddress());
	}

}