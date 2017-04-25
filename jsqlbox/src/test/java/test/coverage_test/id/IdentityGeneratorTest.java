package test.coverage_test.id;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;

import test.TestBase;

/**
 * IdentityGenerator Test, only run on MySQL and H2, did not test other
 * databases which support Identity type
 *
 * @author Yong Zhu
 * @since 1.0.0
 */
public class IdentityGeneratorTest extends TestBase {
	public static class UserTemp implements Entity {

		Integer id;
		String userName;
		{
			this.box().configIdGenerator("id", IdentityGenerator.INSTANCE);
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}
	}

	@Before
	public void setup() {
		super.setup();
		if (!(Dao.getDialect().isMySqlFamily() || Dao.getDialect().isH2Family()))
			return;
		Dao.executeQuiet("Drop table UserTemp");
		Dao.execute("create table UserTemp(id int auto_increment,user_name varchar(20),primary key(id))");
		Dao.refreshMetaData();
	}

	@After
	public void cleanUp() {
		Dao.executeQuiet("Drop table UserTemp");
		super.cleanUp();
	}

	@Test
	public void insertUserTemp() {
		UserTemp u = new UserTemp();
		u.setUserName("User1");
		for (int i = 0; i < 5; i++)
			u.insert();
		Assert.assertEquals(5, (int) Dao.queryForInteger("select count(*) from  ", u.table()));
		Assert.assertTrue(u.getId() > 0);
	}

}