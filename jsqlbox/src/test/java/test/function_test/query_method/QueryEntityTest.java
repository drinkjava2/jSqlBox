package test.function_test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.comma;
import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.questionMarks;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;

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
					", ", u.PHONENUMBER(), empty(i), //
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
		System.out.println("=====test1 simple query====");
		User u = new User();
		List<Map<String, Object>> list = Dao.queryForList("select ", u.ID(), ",", u.ADDRESS(), ",", u.AGE(), " from ",
				u.table());
		for (Map<String, Object> map : list) {
			Assert.assertNotNull(map.get(u.ID()));
			Assert.assertEquals("BeiJing", map.get(u.ADDRESS()));
		}
		list = Dao.queryForList("select ", u.all(), " from ", u.table());
		for (Map<String, Object> map : list) {
			Assert.assertNotNull(map.get(u.ID()));
			Assert.assertEquals("BeiJing", map.get(u.ADDRESS()));
		}

		System.out.println("=====test2 alias and comma====");
		User u1 = new User();
		User u2 = new User();
		u1.box().configAlias("a");
		u2.box().configAlias("b");
		List<Map<String, Object>> list2 = Dao.queryForList(select(),
				comma(u1.ID(), u1.ADDRESS(), u1.AGE(), u2.ID(), u2.PHONENUMBER(), u2.ADDRESS(), u2.USERNAME()), from(),
				u1.table(), ", ", u2.table(), " where ", u1.ID(), "=", u2.ID());
		Map<String, Object> map = list2.get(0);
		System.out.println(map);
		Assert.assertNotNull(map.get(u2.alias(u1.ID())));
		Assert.assertEquals("BeiJing", map.get(u1.alias(u2.ADDRESS())));
		Assert.assertEquals("0", map.get(u2.alias(u2.PHONENUMBER())));

		System.out.println("=====test3  inner join====");
		User a = new User();
		User b = new User();
		User c = new User();
		a.configAlias("a").addNode(b.addNode(c));
		b.configAlias("b");
		c.configAlias("c");
		Dao.getDefaultContext().setShowSql(true);
		List<Entity> aList = Dao.queryForEntityList(select(), comma(a.all(), b.all(), c.USERNAME()), from(),
				comma(a.table(), b.table(), c.table()), //
				" where ", a.ID(), "=", b.ID(), " and ", a.ID(), "=", c.ID()//
		);

		System.out.println(aList);

		for (Entity aItem : aList) {
			List<Entity> bList = aItem.getList(0);
			for (Entity bItem : bList) {
				System.out.println(bItem);
			}
			List<Entity> cList = aItem.getList(1);
			for (Entity cItem : bList) {
				System.out.println(cItem);
			}
		}

	}

}