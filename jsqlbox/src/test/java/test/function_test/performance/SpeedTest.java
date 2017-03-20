package test.function_test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.PrepareTestContext;
import test.config.po.User;

public class SpeedTest {

	@Before
	public void setup() {
		System.out.println("===============================Testing SpeedTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	/**
	 * Test jSqlBox its self, do not write to database, only test configuring time
	 */
	@Test
	public void doSpeedTest() {
		long oldTime = System.currentTimeMillis();
		for (int i = 0; i < 500; i++) {
			SqlBoxContext.getDefaultSqlBoxContext();
			User u = new User();
			u.setUserName("User2");
			u.setAddress("Address2");
			u.setPhoneNumber("222");
			u.box();
			if (Dao.getDefaultDatabaseType().isH2())
				u.insert();
		}
		long newTime = System.currentTimeMillis();
		System.out.println("Time used for 500 times:" + (newTime - oldTime) + "ms");
		Assert.assertTrue((newTime - oldTime) < 1000);
	}

	/**
	 * GC Test
	 * 
	 * <pre>
	 * public void doGCTest() {
	 * 	SqlBoxContext.getDefaultSqlBoxContext().setShowSql(true);
	 * 	long oldTime = System.currentTimeMillis();
	 * 	for (int i = 0; i < 1000000; i++) {
	 * 		User u = new User();
	 * 		u.setUserName("User2");
	 * 		u.setPhoneNumber("222");
	 * 		SqlBox b = u.box();
	 * 		Assert.assertNotNull(b);
	 * 		Assert.assertEquals(u, b.getEntityBean());
	 * 	}
	 * 	long newTime = System.currentTimeMillis();
	 * 	System.out.println("Time used for 1000000 times:" + (newTime - oldTime) + "ms");
	 * }
	 * 
	 * public static void main(String[] args) {
	 * 	PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	 * 	SpeedTest t = new SpeedTest();
	 * 	t.doGCTest();
	 * }
	 * </pre>
	 */

}