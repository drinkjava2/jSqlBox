package test.id_generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.id.SortedUUIDGenerator;
import com.github.drinkjava2.jsqlbox.id.TableGenerator;
import com.github.drinkjava2.jsqlbox.id.UUIDAnyGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;
import test.config.po.User;

public class SortedUUIDGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	// @After
	// public void cleanUp() {
	// TestPrepare.closeBeanBoxContext();
	// }

	public static class TableGeneratorBox extends BeanBox {
		{
			this.setConstructor(TableGenerator.class, "T", "PK", "PV", "V", 1, 50);
		}
	}

	public static class UUIDAnyGeneratorBox extends BeanBox {
		{
			this.setConstructor(UUIDAnyGenerator.class, 20);
		}
	}

	public static class SortedUUIDBox extends BeanBox {
		{
			this.setConstructor(SortedUUIDGenerator.class, TableGeneratorBox.class, UUIDAnyGeneratorBox.class,29);
		}
	}

	@Test
	public void insertUserInMysql() {
		if (Dao.dao().getDatabaseType() != DatabaseType.MYSQL)
			return;
		User u = new User();
		u.dao().executeQuiet("drop table t");
		u.dao().executeQuiet("create table t (pk varchar(5),v int(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		u.box().configIdGenerator("userName", (IdGenerator) BeanBox.getBean(SortedUUIDBox.class));
		for (int i = 0; i < 60; i++)
			u.dao().insert();
		Assert.assertEquals(60, (int) u.dao().queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void insertUserInOracle() {
		if (Dao.dao().getDatabaseType() != DatabaseType.ORACLE)
			return;
		User u = new User();
		u.dao().executeQuiet("drop table T");
		u.dao().executeQuiet("CREATE TABLE T (PK VARCHAR(5),V INTEGER) ");
		u.box().configIdGenerator("userName", (IdGenerator) BeanBox.getBean(SortedUUIDBox.class));
		for (int i = 0; i < 60; i++)
			u.dao().insert();
		Assert.assertEquals(60, (int) u.dao().queryForInteger("select count(*) from ", u.table()));
	}

}