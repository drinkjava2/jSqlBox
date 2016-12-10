package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import java.beans.PropertyVetoException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.config.InitializeDatabase;
import test.config.JBeanBoxConfig.CtxBox;
import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.po.User;

/**
 * This is to test jSqlBoxContext class
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ContextTest {

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	@Test
	public void insertUser1() {
		ComboPooledDataSource ds = new ComboPooledDataSource();// c3p0
		ds.setUser("root");
		ds.setPassword("root888");
		ds.setJdbcUrl((String) new DataSourceBox().getProperty("jdbcUrl"));
		try {
			ds.setDriverClass((String) new DataSourceBox().getProperty("driverClass"));
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		SqlBoxContext ctx = new SqlBoxContext(ds);
		User u = ctx.createBean(User.class);
		// Can not use User u=new User() here because default global SqlBoxContext not configured
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().insert();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User1")));
	}

	@Test
	public void insertUser2() {
		SqlBoxContext ctx = BeanBox.getBean(CtxBox.class);
		User u = ctx.createBean(User.class);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().insert();
		Assert.assertEquals(111, (int) u.dao().queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User1")));
	}

	public static void main(String[] args) {
		ContextTest t = new ContextTest();
		InitializeDatabase.dropAndRecreateTables();
		t.insertUser1();
		// InitializeDatabase.dropAndRecreateTables();
		// t.insertUser2();
	}

}