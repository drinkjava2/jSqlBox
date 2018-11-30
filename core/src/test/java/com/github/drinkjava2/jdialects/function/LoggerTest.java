/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectLogger;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class LoggerTest extends JdialectsTestBase {
	DialectLogger logger = DialectLogger.getLog(LoggerTest.class);

	@Test
	public void doLoggerTest() {
		Dialect.setGlobalAllowShowSql(true);
		Dialect.MySQL55Dialect.pagin(10, 10, "select * from sometable");
		logger.info("Message1 output");
		Systemout.println("Message2 output");
	}
}