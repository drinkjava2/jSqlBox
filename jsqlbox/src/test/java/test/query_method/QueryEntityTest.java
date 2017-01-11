package test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.link;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.TestPrepare;
import test.config.po.DB;
import test.config.po.User;

public class QueryEntityTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing QueryEntityTest===============================");
		TestPrepare.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		User u = new User();
		for (int i = 0; i < 5; i++)
			Dao.execute("insert into ", u.table(), //
					" (", u.USERNAME(), empty("user1"), //
					", ", u.ADDRESS(), empty("address1"), //
					", ", u.AGE(), ")", empty("1"), //
					questionMarks());
		Assert.assertEquals(5, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void queryTest() {
		User u = new User();
		User u2 = new User();
		u.box().configTableAlias("a");
		u2.box().configTableAlias("b");
		List<DB> list = Dao.queryForList(DB.class, "select ",
				link(u.ID(), u.ADDRESS(), u.AGE(), u2.ID() + " as id2", u2.PHONENUMBER(), u2.ADDRESS(), u2.USERNAME()),
				" from ", u.alias(), ", ", u2.alias(), " where ", u.ID(), "=", u2.ID());
	}

}