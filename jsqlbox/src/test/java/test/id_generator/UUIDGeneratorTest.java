package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.id.UUID25Generator;
import com.github.drinkjava2.jsqlbox.id.UUIDAnyGenerator;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

import test.config.TestPrepare;
import test.config.po.User;

public class UUIDGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void testUUID() {
		User u = new User();
		u.box().configIdGenerator("userName", (IdGenerator) BeanBox.getBean(UUIDGenerator.class));
		u.insert();
		String username = SqlBox.queryForString("select ", u.userName(), " from ", u.table());
		Assert.assertEquals(32, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void testUUID25() {
		User u = new User();
		u.box().configIdGenerator("userName", (IdGenerator) BeanBox.getBean(UUID25Generator.class));
		u.insert();
		String username = SqlBox.queryForString("select ", u.userName(), " from ", u.table());
		Assert.assertEquals(25, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
	}

	private static class UUIDAnyGeneratorBox extends BeanBox {
		{
			this.setConstructor(UUIDAnyGenerator.class, 45);
		}
	}

	@Test
	public void testUUIDAny() {
		User u = new User();
		u.box().configIdGenerator("userName", (IdGenerator) BeanBox.getBean(UUIDAnyGeneratorBox.class));
		u.insert();
		String username = SqlBox.queryForString("select ", u.userName(), " from ", u.table());
		Assert.assertEquals(45, username.length());
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(61, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
	}
}