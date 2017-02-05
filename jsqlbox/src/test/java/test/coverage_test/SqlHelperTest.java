
package test.coverage_test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlAndParameters;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.PrepareTestContext;
import test.config.po.User;

public class SqlHelperTest {
	@Before
	public void setup() {
		System.out.println("=============================Testing SqlBoxTest=============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}
	
	@Test
	public void prepareSQLandParameters() {
		User u = new User();
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters("select count(*) from ", u.table());
		int count = (Integer) Dao.getDefaultContext().getJdbc().queryForObject(sp.getSql(), sp.getParameters(),
				Integer.class);
		Assert.assertEquals(0, count);
	}

 

}
