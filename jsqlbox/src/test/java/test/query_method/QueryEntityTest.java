package test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.DB;
import test.config.po.User;

public class QueryEntityTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
		User u = new User();
		for (int i = 0; i < 5; i++)
			SqlBox.execute("insert into ", u.table(), //
					" (", u.userName(), empty("user1"), //
					", ", u.address(), empty("address1"), //
					", ", u.age(), ")", empty("1"), //
					questionMarks());
		Assert.assertEquals(5, (int) SqlBox.queryForInteger("select count(*) from ", u.table()));
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDefaultContexts();
	}

	@Test
	public void queryTest() {
		User u = new User();
		List<DB> list = SqlBox.queryForList("select ", u.star(), " from ", u.table());
		for (DB db : list) {
			System.out.println(db.map);
		}

	}

}