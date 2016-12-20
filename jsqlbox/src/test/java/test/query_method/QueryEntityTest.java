package test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class QueryEntityTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
		User u = SqlBox.createBean(User.class);
		SqlBox.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user1"), //
				", ", u.address(), empty("address1"), //
				", ", u.age(), ")", empty("1"), //
				questionMarks());
		SqlBox.execute("insert into ", u.table(), //
				" (", u.userName(), empty("user2"), //
				", ", u.address(), empty("address2"), //
				", ", u.age(), ")", empty("2"), //
				questionMarks());
		u.setUserName("user3");
		u.setAddress("address3");
		u.setAge(3);
		u.insert();
		Assert.assertEquals(3, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void queryUser() {
		//Assert.assertEquals(2, (int) SqlBox.queryForInteger("select count(*) from ", db.user.table()));
		// "select b.username as UNAME from users b"
	}

	public static void main(String[] args) {
		TestPrepare.dropAndRecreateTables();
		QueryEntityTest t = new QueryEntityTest();
		t.setup();
	}
}