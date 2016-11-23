package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import java.beans.PropertyVetoException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.config.Config.CtxBox;
import test.config.InitializeDatabase;
import test.config.po.User;

public class ContextTest {
	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	@Test
	public void insertUser1() {
		ComboPooledDataSource ds = new ComboPooledDataSource();// c3p0
		ds.setUser("root");
		ds.setPassword("root888");
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		try {
			ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		SqlBoxContext ctx = new SqlBoxContext(ds);
		User u = ctx.create(User.class);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().save();
		Assert.assertEquals(111, (int) Dao.dao.queryForInteger("select ", u.PhoneNumber(), " from ", u.Table(),
				" where ", u.UserName(), "=", q("User1")));
	}

	@Test
	public void insertUser2() {
		SqlBoxContext ctx = BeanBox.getBean(CtxBox.class); 
		User u = ctx.create(User.class);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().save();
		Assert.assertEquals(111, (int) Dao.dao.queryForInteger("select ", u.PhoneNumber(), " from ", u.Table(),
				" where ", u.UserName(), "=", q("User1")));
	}

	public static void main(String[] args) {
		ContextTest t = new ContextTest();
		InitializeDatabase.recreateTables();
		t.insertUser1();
		InitializeDatabase.recreateTables();
		t.insertUser2();
	}

}