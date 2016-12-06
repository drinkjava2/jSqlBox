package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.InitializeDatabase;
import test.config.po.User;

public class QueryEntityTest {

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
		User u = SqlBox.createBean(User.class);
		u.dao().execute("insert into ", u.table(), //
				" (", u.userName(), e("user1"), //
				", ", u.address(), e("address1"), //
				", ", u.age(), ")", e("1"), //
				SqlHelper.questionMarks());
		u.dao().execute("insert into ", u.table(), //
				" (", u.userName(), e("user2"), //
				", ", u.address(), e("address2"), //
				", ", u.age(), ")", e("2"), //
				SqlHelper.questionMarks());
		u.setUserName("user3");
		u.setAddress("address3");
		u.setAge(3);
		u.dao().insert();
		Assert.assertEquals(3, (int) Dao.dao().queryForInteger("select count(*) from ", u.table()));
	}

	@Test
	public void queryUser() {
		// Assert.assertEquals(2, (int) Dao.dao.queryForInteger("select count(*) from ", User.Table));
		// User user = (User) Dao.dao.queryEntity(User.class, "select b.username as UNAME from users b").get(0);
		// if (user == null) {// TODO need think about next step
		// }
	}

	public static void main(String[] args) {
		InitializeDatabase.dropAndRecreateTables();
		QueryEntityTest t = new QueryEntityTest();
		t.setup();
	}
}