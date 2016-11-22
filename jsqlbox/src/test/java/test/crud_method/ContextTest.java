package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.Config.DSPoolBeanBox;
import test.config.InitializeDatabase;
import test.config.po.User;

public class ContextTest {
//	@Before
//	public void setup() {
//		InitializeDatabase.recreateTables();
//	}
//
//	@Test
//	public void insertUser() {
//		SqlBoxContext ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DSPoolBeanBox.class));
//		SqlBox sb = ctx.findAndBuildSqlBox(User.class);
//		Dao dao = new Dao(sb);
//		User u = new User();
//		u.putDao(dao);
//		u.setUserName("User1");
//		u.setAddress("Address1");
//		u.setPhoneNumber("111");
//		u.setAge(10);
//		u.dao().save();
//		Assert.assertEquals(111, (int) Dao.dao.queryForInteger("select ", u.PhoneNumber(), " from ", u.Table(),
//				" where ", u.UserName(), "=", q("User1")));
//	}
//
//	public static void main(String[] args) {
//		new ContextTest().insertUser();
//	}

}