/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.functionstest;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectLogger;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class LoggerTest {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	DialectLogger logger = DialectLogger.getLog(LoggerTest.class);

	@Test
	public void doDialectLoggerTest() {
		Dialect.setGlobalAllowShowSql(true);
		Dialect.MySQL55Dialect.paginAndTrans(10, 10, "select * from sometable");
		logger.info("Logger test message1 output ok");
		System.out.println("Logger test message2 output ok");
		Dialect.setGlobalAllowShowSql(false);
	}

	@Test
	public void doSqlBoxLoggerTest() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		SqlBoxContext.setGlobalAllowShowSql(true);
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(LoggerTest.class))
			ctx.quiteExecute(ddl);
		LoggerTest t = new LoggerTest();
		t.setName("Tom");
		ctx.insert(t);
		SqlBoxContext.setGlobalAllowShowSql(false);

		SqlBoxContext.getGlobalLogger().info("Logger test message3 output ok");
		SqlBoxContext.getGlobalLogger().info("Logger test message4 output ok");
	}

}