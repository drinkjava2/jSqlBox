
package test.coverage_test;

import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;

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
		User u = new User().configAlias("u");
		u.box().configColumnName(u.fieldID(u.USERNAME()), u.ADDRESS());
		u.setUserName("user2");
		u.insert();
		Assert.assertEquals("user2", Dao.queryForString(select(), u.ADDRESS(), from(), u.table()));

		List<Map<String, Object>> list = Dao.queryForList(select(), u.all(), from(), u.table());
		Map<String, Object> map = list.get(0);
		Assert.assertEquals("user2", map.get(u.alias(u.ADDRESS())));
	}

}
