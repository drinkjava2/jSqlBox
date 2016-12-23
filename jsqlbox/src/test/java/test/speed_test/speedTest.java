package test.speed_test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.TestPrepare;
import test.config.po.User;

public class speedTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDefaultContexts();
	}

	/**
	 * Test jSqlBox its self, do not write to database, only test configuring time
	 */
	@Test
	public void doSpeedTest() {
		long oldTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			SqlBoxContext.defaultSqlBoxContext();
			User u = SqlBox.createBean(User.class);
			u.setUserName("User2");
			u.setAddress("Address2");
			u.setPhoneNumber("222");
			u.box().buildRealColumns();
			// u.insert();
		}
		long newTime = System.currentTimeMillis();
		System.out.println("Time used for 10000 times:" + (newTime - oldTime) + "ms");
		Assert.assertTrue((newTime - oldTime) < 1000);
	}

}