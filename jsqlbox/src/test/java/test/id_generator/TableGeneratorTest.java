package test.id_generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.jpa.GenerationType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.InitializeDatabase;
import test.config.po.User;

public class TableGeneratorTest {

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	@Test
	public void insertUser1() {
		User u = new User();
		u.dao().executeQuiet("drop table t");
		if (u.dao().getContext().getDatabaseType() == DatabaseType.MYSQL)
			u.dao().executeQuiet("create table t (pk varchar(5),v int(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		if (u.dao().getContext().getDatabaseType() == DatabaseType.ORACLE)
			u.dao().executeQuiet("create table t (pk varchar(5),v integer) ");
		u.dao().getBox().configTableGenerator("creator1", "t", "pk", "pv", "v", 1, 50);
		u.dao().getBox().configGeneratedValue("age", GenerationType.TABLE, "creator1");
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.dao().insert();
		Assert.assertEquals(60, (int) u.dao().queryForInteger("select count(*) from ", u.table()));
	}

}