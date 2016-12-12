package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.jpa.GenerationType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;
import test.config.po.User;

public class TableGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void insertUserInMysql() {
		if (Dao.dao().getContext().getDatabaseType() != DatabaseType.MYSQL)
			return;
		User u = new User();
		u.dao().executeQuiet("drop table t");
		u.dao().executeQuiet("create table t (pk varchar(5),v int(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		u.dao().getBox().configTableGenerator("creator1", "t", "pk", "pv", "v", 1, 50);
		u.dao().getBox().configGeneratedValue("age", GenerationType.TABLE, "creator1");
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.dao().insert();
		Assert.assertEquals(60, (int) u.dao().queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void insertUserInOracle() {
		if (Dao.dao().getContext().getDatabaseType() != DatabaseType.ORACLE)
			return;
		User u = new User();
		u.dao().executeQuiet("drop table T");
		if (u.dao().getContext().getDatabaseType() == DatabaseType.ORACLE)
			u.dao().executeQuiet("CREATE TABLE T (PK VARCHAR(5),V INTEGER) ");
		u.dao().getBox().configTableGenerator("creator1", "T", "PK", "PV", "V", 1, 50);
		u.dao().getBox().configGeneratedValue("id", GenerationType.TABLE, "creator1");
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.dao().insert();
		Assert.assertEquals(60, (int) u.dao().queryForInteger("select count(*) from ", u.table()));
	}

}