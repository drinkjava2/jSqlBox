package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.Config;
import test.crud_method.po.User;

public class QueryEntityTest {

	@Before
	public void setup() {
		Config.recreateTables();
		Dao.dao.execute("insert into user ", //
				"(username", e("user1"), //
				", address", e("address1"), //
				", age)", e("1"), //
				" values(?,?,?)");
		Dao.dao.execute("insert into ", User.Table, //
				" (", User.UserName, e("user2"), //
				", ", User.Address, e("address2"), //
				", ", User.Age, ")", e("2"), //
				SqlHelper.values());
		User u = new User();
		u.setUserName("user3");
		u.setAddress("address2");
		u.setAge(3);
		u.dao().save();
		Assert.assertEquals(3, (int) Dao.dao.queryForInteger("select count(*) from ", User.Table));
	}

	@Test
	public void queryUser() {
		User user = (User) Dao.dao.queryEntity(User.class, "select b.username as aa from user b").get(0);
		if (user == null) {// TODO need think about next step
		}
	}

}