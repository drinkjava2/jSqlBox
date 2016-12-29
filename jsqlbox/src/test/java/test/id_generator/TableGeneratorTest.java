package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.TableGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;
import test.config.po.User;

public class TableGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
	}

	public static class TableGeneratorBox extends BeanBox {
		{
			this.setConstructor(TableGenerator.class, "T", "PK", "PV", "V", 1, 50);
		}
	}

	@Test
	public void insertUserInMysql() {
		if (Dao.getDefaultDatabaseType() != DatabaseType.MYSQL)
			return;
		User u = new User();
		Dao.executeQuiet("drop table t");
		Dao.executeQuiet("create table t (pk varchar(5),v int(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		u.box().configIdGenerator("age", BeanBox.getBean(TableGeneratorBox.class));
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(60, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void insertUserInOracle() {
		if (Dao.getDefaultDatabaseType() != DatabaseType.ORACLE)
			return;
		User u = new User();
		Dao.executeQuiet("drop table T");
		Dao.executeQuiet("CREATE TABLE T (PK VARCHAR(5),V INTEGER) ");
		u.box().configIdGenerator("age", BeanBox.getBean(TableGeneratorBox.class));
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(60, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

}