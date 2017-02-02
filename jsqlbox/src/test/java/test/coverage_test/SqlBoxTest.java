
package test.coverage_test;

import static com.github.drinkjava2.jsqlbox.SqlHelper.aliasBegin;
import static com.github.drinkjava2.jsqlbox.SqlHelper.aliasEnd;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.User;

public class SqlBoxTest {
	@Before
	public void setup() {
		System.out.println("=============================Testing SqlBoxTest=============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	/**
	 * Test if no alias config the column name
	 */
	@Test
	public void configColumnNameTest() {
		Dao.getDefaultContext().setShowSql(true);
		User u = new User();
		u.box().configColumnName(u.fieldID(u.USERNAME()), u.ADDRESS());
		u.setUserName("user1");
		u.insert();
		Assert.assertEquals("user1", Dao.queryForString(select(), u.USERNAME(), from(), u.table()));

		List<Map<String, Object>> list = Dao.queryForList(select(), u.all(), from(), u.table());
		Map<String, Object> map = list.get(0);
		Assert.assertEquals("user1", map.get(u.ADDRESS()));
	}

	/**
	 * Test if have alias config the column name
	 */
	@Test
	public void aliasConfigColumnNameTest() {
		Dao.getDefaultContext().setShowSql(true);
		User u = new User();
		String userNameFieldID = u.fieldID(u.USERNAME());
		u.configAlias("u");
		try {
			aliasBegin();
			// fieldID() method should always return right fieldID value
			Assert.assertEquals(userNameFieldID, u.fieldID(u.USERNAME()));
		} finally {
			aliasEnd();
		}

		u.box().configColumnName(u.fieldID(u.USERNAME()), u.ADDRESS());
		Assert.assertTrue(u.USERNAME().equals(u.ADDRESS()));
		System.out.println(u.USERNAME());

		u.setUserName("user2");
		u.insert();
		Assert.assertEquals("user2", Dao.queryForString(select(), u.ADDRESS(), from(), u.table()));

		List<Map<String, Object>> list = Dao.queryForList(select(), u.all(), from(), u.table());
		Map<String, Object> map = list.get(0);
		Assert.assertEquals("user2", map.get(u.alias(u.ADDRESS())));

		try {
			aliasBegin();
			Assert.assertTrue(u.USERNAME().equals(u.ADDRESS()));
			System.out.println(u.USERNAME());
			Assert.assertEquals("user2", map.get(u.USERNAME()));
			Assert.assertEquals("user2", map.get(u.ADDRESS()));
		} finally {
			aliasEnd();
		}
	}

	@Test
	public void automaticQuerySQL() {
		Dao.getDefaultContext().setShowSql(true);
		User u = new User();
		u.setUserName("user3");
		u.insert();
		List<Map<String, Object>> result2 = Dao.queryForList(u.automaticQuerySQL());
		for (Map<String, Object> map : result2) {
			System.out.println(map);
			Assert.assertEquals("user3", map.get(u.USERNAME()));
		}
	}

	@Test
	public void automaticQuerySQLwithAlias() {
		Dao.getDefaultContext().setShowSql(true);
		User u = new User().configAlias("u");
		u.setUserName("user4");
		u.insert();
		List<Map<String, Object>> result2 = Dao.queryForList(u.automaticQuerySQL());
		for (Map<String, Object> map : result2) {
			System.out.println(map);
			Assert.assertEquals("user4", map.get(u.alias(u.USERNAME())));
		}
	}

}
