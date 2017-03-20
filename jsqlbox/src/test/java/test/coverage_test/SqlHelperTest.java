
package test.coverage_test;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlAndParameters;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.TestBase;
import test.config.po.User;

public class SqlHelperTest extends TestBase {

	@Test
	public void prepareSQLandParameters() {
		User u = new User();
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters("select count(*) from ", u.table());
		int count = (Integer) Dao.getDefaultContext().getJdbc().queryForObject(sp.getSql(), sp.getParameters(),
				Integer.class);
		Assert.assertEquals(0, count);
	}

}
