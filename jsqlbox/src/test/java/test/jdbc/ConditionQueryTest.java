package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class ConditionQueryTest {
	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
		User u = new User(); // use default SqlBoxContext
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setAge(10);
		u.insert();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
	}

	public int conditionQuery(int condition, Object parameter) {
		User u = SqlBox.createBean(User.class);
		String sql = "Select count(*) from " + u.table() + " where ";
		if (condition == 1 || condition == 3)
			sql = sql + u.userName() + "=" + q(parameter) + " and " + u.address() + "=" + q("Address1");

		if (condition == 2)
			sql = sql + u.userName() + "=" + q(parameter);

		if (condition == 3)
			sql = sql + " or " + u.age() + "=" + q(parameter);

		return SqlBox.queryForInteger(sql);
	}

	@Test
	public void doJdbcConditionQuery() {
		Assert.assertEquals(1, conditionQuery(1, "User1"));
		Assert.assertEquals(0, conditionQuery(2, "User does not exist"));
		Assert.assertEquals(1, conditionQuery(3, 10));
		Assert.assertEquals(0, conditionQuery(3, 20));
	}
}