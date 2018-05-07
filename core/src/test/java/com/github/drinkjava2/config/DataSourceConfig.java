package com.github.drinkjava2.config;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is jBeanBox configuration classes, equal to XML in Spring <br/>
 * About jBeanBox project can google it, it's a small IOC/AOP tool, here used
 * only for prepare dataSource singleton.
 * 
 * @author Yong Zhu
 *
 * @since 1.0.0
 */
public class DataSourceConfig {
	/**
	 * ================================================================<br/>
	 * Data source setting, change "H2DataSourceBox" to MySqlDataSourceBox to
	 * test on MySql <br/>
	 * ================================================================<br/>
	 */
	public static class DataSourceBox extends H2DataSourceBox {
	}

	// H2Database memory database connection URL
	public static class H2DataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			setProperty("driverClassName", "org.h2.Driver");
			setProperty("username", "sa");
			setProperty("password", "");
		}
	}

	// MySql connection URL
	public static class MySqlDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClassName", "com.mysql.jdbc.Driver");
			setProperty("username", "root");// change to your user & password
			setProperty("password", "root888");
		}
	}

	// Oracle connection URL
	public static class OracleDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:oracle:thin:@127.0.0.1:1521:XE");
			setProperty("driverClassName", "oracle.jdbc.OracleDriver");
			setProperty("username", "root");// change to your user & password
			setProperty("password", "root888");
		}
	}

	// MsSql Server connection URL
	public static class MsSqlServerDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:sqlserver://localhost:1433;databaseName=test");
			setProperty("driverClassName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
			setProperty("username", "sa");// change to your user & password
			setProperty("password", "root888");
		}
	}

	// HikariCP is a DataSource pool much quicker than C3P0
	public static class HikariCPBox extends BeanBox {
		// In jBeanBox, bean default is singleton
		public HikariDataSource create() {
			HikariDataSource ds = new HikariDataSource();
			ds.addDataSourceProperty("cachePrepStmts", true);
			ds.addDataSourceProperty("prepStmtCacheSize", 250);
			ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
			ds.addDataSourceProperty("useServerPrepStmts", true);
			ds.setMaximumPoolSize(3);
			ds.setConnectionTimeout(5000);
			this.setPreDestory("close");// jBeanBox will close pool
			return ds;
		}
	}

}
