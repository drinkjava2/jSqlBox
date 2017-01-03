package test;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;
import test.config.po.User;

public class HelloWorld {

	@Test
	public void doTest() {
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		Dao.execute("delete from users");
		Dao.execute("delete from users2");

		User u = new User();// default use users database table
		u.setUserName("James");
		u.insert();
		Assert.assertEquals("James", Dao.queryForString("select ", u.userName(), " from ", u.table()));

		u.box().configTable("users2");// to use users2 database table
		u.box().configColumnName("userName", u.address()); // to map userName field to address column
		u.setUserName("Tom");
		u.insert();
		Assert.assertEquals("Tom", Dao.queryForString("select ", u.address(), " from ", u.table()));

	}
}