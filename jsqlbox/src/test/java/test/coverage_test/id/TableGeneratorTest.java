package test.coverage_test.id;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.TableGenerator;

import test.TestBase;
import test.config.po.User;

public class TableGeneratorTest extends TestBase {

	public static class TableGeneratorBox extends BeanBox {
		{
			this.setConstructor(TableGenerator.class, "T", "PK", "PV", "V", 1, 50);
		}
	}

	public static class TableGeneratorBox2 extends BeanBox {
		{
			this.setConstructor(TableGenerator.class, "T", "PK", "PV", "V", 1, 50);
		}
	}

	@Test
	public void insertUserInMysql() {
		Dialect d = Dao.getDialect();
		if (!(d.isMySqlFamily() || d.isH2Family()))
			return;
		User u = new User();
		Dao.executeQuiet("drop table t");
		Dao.executeQuiet("create table t (pk varchar(5),v int(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		u.box().configIdGenerator("age", BeanBox.getBean(TableGeneratorBox.class));
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(60, (int) Dao.queryForInteger("select count(*) from ", u.table()));

		u.box().configIdGenerator("age", BeanBox.getBean(TableGeneratorBox2.class));
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(120, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void insertUserInOracle() {
		if (!Dao.getDialect().isOracleFamily())
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