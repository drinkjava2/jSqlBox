
package test.coverage_test;

import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxLogger;

public class SqlBoxLoggerTest {
	@Test
	public void doLogTest() {
		SqlBoxLogger log = SqlBoxLogger.getLog(SqlBoxLoggerTest.class);
		log.info("info");
		log.warn("warn");
		log.error("error");
	}
}
