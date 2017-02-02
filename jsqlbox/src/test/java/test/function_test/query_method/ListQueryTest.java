package test.function_test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.aliasBegin;
import static com.github.drinkjava2.jsqlbox.SqlHelper.aliasEnd;
import static com.github.drinkjava2.jsqlbox.SqlHelper.comma;
import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.User;

public class ListQueryTest {

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
					valuesAndQuestions());
		Assert.assertEquals(5, (int) Dao.queryForInteger("select count(*) from ", u.table()));
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void simpleQueryTest() {
		Dao.getDefaultContext().setShowSql(true).setFormatSql(true);
		System.out.println("=====test simple query====");
		User u = new User();
		List<Map<String, Object>> list = Dao.queryForList(select(), u.ID(), ",", u.ADDRESS(), ",", u.AGE(), from(),
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

		System.out.println("=====test alias, all, comma====");
		User u1 = new User().configAlias("a");
		User u2 = new User().configAlias("b");
		List<Map<String, Object>> list2 = Dao.queryForList(select(),
				comma(u1.all(), u2.ID(), u2.PHONENUMBER(), u2.ADDRESS(), u2.USERNAME()), from(),
				comma(u1.table(), u2.table()), " where ", u1.ID(), "=", u2.ID());
		Map<String, Object> map = list2.get(0);

		Assert.assertEquals(null, map.get(u1.USERNAME()));
		Assert.assertEquals("Sam", map.get(u1.alias(u1.USERNAME())));
		try {
			aliasBegin();
			Assert.assertEquals("Sam", map.get(u1.USERNAME()));
			Assert.assertEquals("BeiJing", map.get(u1.ADDRESS()));
			Assert.assertEquals("0", map.get(u1.PHONENUMBER()));
		} finally {
			aliasEnd();
		}
	}

}