package test.coverage_test.id;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.UUID25Generator;
import com.github.drinkjava2.jsqlbox.id.UUIDAnyGenerator;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

import test.TestBase;
import test.config.po.User;

public class UUIDGeneratorTest extends TestBase {

	@Test
	public void testUUID() {
		User u = new User(); 
		u.box().configIdGenerator("userName", BeanBox.getBean(UUIDGenerator.class)); 
		u.insert();
		String username = Dao.queryForString("select ", u.USERNAME(), " from ", u.table()); 
		Assert.assertEquals(1 , (int) Dao.queryForInteger("select count(*) from ", u.table())); 
		Assert.assertEquals(32, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}
	
	@Test
	public void testUUIDAsEntityID() {
		User u = new User(); 
		u.box().configIdGenerator("userName", BeanBox.getBean(UUIDGenerator.class)); 
		u.box().configEntityIDs("userName");
		u.insert();
		String username = Dao.queryForString("select ", u.USERNAME(), " from ", u.table()); 
		Assert.assertEquals(1 , (int) Dao.queryForInteger("select count(*) from ", u.table())); 
		Assert.assertEquals(32, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void testUUID25() {
		User u = new User();
		u.box().configIdGenerator("userName", BeanBox.getBean(UUID25Generator.class));
		u.insert();
		String username = Dao.queryForString("select ", u.USERNAME(), " from ", u.table());
		Assert.assertEquals(25, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	private static class UUIDAnyGeneratorBox extends BeanBox {
		{
			this.setConstructor(UUIDAnyGenerator.class, 45);
		}
	}

	@Test
	public void testUUIDAny() {
		User u = new User();
		Dao.getDefaultContext().setShowSql(true);
		u.box().configIdGenerator("userName", BeanBox.getBean(UUIDAnyGeneratorBox.class));
		u.insert();
		String username = Dao.queryForString("select ", u.USERNAME(), " from ", u.table());
		Assert.assertEquals(45, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}
}