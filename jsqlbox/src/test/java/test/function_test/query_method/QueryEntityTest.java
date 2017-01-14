package test.function_test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.link;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlHelper.where;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.DB;
import test.config.PrepareTestContext;
import test.config.po.User;

public class QueryEntityTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing QueryEntityTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		User u = new User();
		for (int i = 0; i < 5; i++)
			Dao.execute("insert into ", u.table(), //
					" (", u.USERNAME(), empty("Sam"), //
					", ", u.ADDRESS(), empty("BeiJing"), //
					", ", u.PHONENUMBER(), empty("1234567"), //
					", ", u.AGE(), ")", empty("1"), //
					questionMarks());
		Assert.assertEquals(5, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void queryTest() {
		System.out.println("=====test1====");
		User u = new User();
		List<Map<String, Object>> list = Dao.queryForList(select(), u.ID(), ",", u.ADDRESS(), ",", u.AGE(), from(),
				u.table());
		for (Map<String, Object> map : list) {
			System.out.println(map.get(u.ID()));
			System.out.println(map.get(u.ADDRESS()));
		}

		System.out.println("=====test2====");
		User u1 = new User();
		User u2 = new User();
		u1.box().configTableAlias("a");
		u2.box().configTableAlias("b");
		List<Map<String, Object>> list2 = Dao.queryForList(select(),
				link(u1.ID(), u1.ADDRESS(), u1.AGE(), u2.ID(), u2.PHONENUMBER(), u2.ADDRESS(), u2.USERNAME()), from(),
				u1.table(), ", ", u2.table(), where(), u1.ID(), "=", u2.ID());
		for (Map<String, Object> map : list2) {
			System.out.println(map.get(u2.ID()));
			System.out.println(map.get(u2.PHONENUMBER()));
		}

		System.out.println("=====test3====");
		User u3 = new User();
		User u4 = new User();
		User u5 = new User();
		u3.box().configTableAlias("a");
		u4.box().configTableAlias("b");
		u5.box().configTableAlias("c");
		List<DB> list3 = Dao.queryForDbList(DB.class, select(),
				link(u3.ID(), u3.ADDRESS(), u3.AGE(), u4.ID(), u4.PHONENUMBER(), u4.ADDRESS(), u4.USERNAME(), u5.ID()),
				",", u5.ADDRESS(), from(), u3.table(), ", ", u4.table(), ", ", u5.table(), where(), u3.ID(), "=",
				u4.ID(), " and ", u3.ID(), "=", u5.ID());
		for (DB db : list3) {
			System.out.println("db.map().get(u4.ID())=" + db.map().get(u4.ID()));
			System.out.println("db.user().getAddress()=" + db.user().getAddress());
		}

	}

}