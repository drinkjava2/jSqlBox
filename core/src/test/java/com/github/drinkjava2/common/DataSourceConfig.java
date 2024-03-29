package com.github.drinkjava2.common;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.zaxxer.hikari.HikariDataSource;

/**
 * jSqlBox need pass unit tested on H2, MySql, MSSQL, Oracle
 * 
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
     * <pre>
     * ================================================================ 
     * Data source setting, change DataSourceBox extends which DataSource to do unit test
     * Now have below to choose:
     * 
     * H2DataSourceBox (H2 on HikariCP)
     * MySqlDataSourceUtcTimeBox (MySql on HikariCP, time is UTC)
     * MySqlDataSourceBox (MySql on HikariCP)
     * OracleDataSourceBox (Oracle on HikariCP)
     * MsSqlServerDataSourceBox (MsSqlServer on HikariCP)
     * BeeCPMySqlBox (MySql on BeeCP)
     * BeeCPH2Box (H2 on BeeCP)
     * ================================================================
     * <pre/>
     */
    public static class DataSourceBox extends H2DataSourceBox { //usually test on H2 or MySql
    }
 
	// H2Database memory database connection URL
	public static class H2DataSourceBox extends HikariCPBox {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0;DATABASE_TO_UPPER=false");
			injectValue("driverClassName", "org.h2.Driver");
			injectValue("username", "sa");
			injectValue("password", "");
		}
	}

	// MySql connection URL with UTC
	public static class MySqlDataSourceUtcTimeBox extends MySqlDataSourceBox {
		{
			injectValue("jdbcUrl",
					"jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false&serverTimezone=UTC");
		}
	}

	   // MySql connection URL
    public static class MySqlDataSourceBox extends HikariCPBox {
        {
            injectValue("jdbcUrl",
                    "jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false");
            injectValue("driverClassName", "com.mysql.cj.jdbc.Driver"); //for MySQL connection6 and above
            injectValue("username", "root");// change to your user & password
            injectValue("password", "root888");
        }
    }
    
	// Oracle connection URL
	public static class OracleDataSourceBox extends HikariCPBox {
		{
			injectValue("jdbcUrl", "jdbc:oracle:thin:@127.0.0.1:1521:XE");
			injectValue("driverClassName", "oracle.jdbc.OracleDriver");
			injectValue("username", "root");// change to your user & password
			injectValue("password", "root888");
		}
	}

	// MsSql Server connection URL
	public static class MsSqlServerDataSourceBox extends HikariCPBox {
		{
			injectValue("jdbcUrl", "jdbc:sqlserver://localhost:1433;databaseName=test");
			injectValue("driverClassName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
			injectValue("username", "sa");// change to your user & password
			injectValue("password", "root888");
		}
	}

	// HikariCP is a DataSource pool much quicker than C3P0
	public static class HikariCPBox extends BeanBox {
		// In jBeanBox, bean default is singleton
		public HikariDataSource create() {
			HikariDataSource ds = new HikariDataSource();
			ds.addDataSourceProperty("cachePrepStmts", true); //生产环境下，如果不动态增改列，要设成true以提高性能
			ds.addDataSourceProperty("prepStmtCacheSize", 250);
			ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
			ds.addDataSourceProperty("useServerPrepStmts", true);
			ds.setMaximumPoolSize(3);
			ds.setConnectionTimeout(5000); 
			this.setPreDestroy("close");// jBeanBox will close pool
			return ds;
		}
	}

	/*
    public static class BeeCPMySqlBox extends BeanBox {
        public BeeDataSource create() {
            BeeDataSourceConfig config = new BeeDataSourceConfig();
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false&serverTimezone=UTC");
            config.setUsername("root");
            config.setPassword("root888"); 
            config.setForceCloseUsingOnClear(true);
            config.setDelayTimeForNextClear(0);
            BeeDataSource ds = new BeeDataSource(config);
            this.setPreDestroy("close");// jBeanBox will close pool
            return ds;
        }
    }

    public static class BeeCPH2Box extends BeanBox {
        public BeeDataSource create() {
            BeeDataSourceConfig config = new BeeDataSourceConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
            config.setUsername("sa");
            config.setPassword("");
            config.setForceCloseUsingOnClear(true);
            config.setDelayTimeForNextClear(0);
            BeeDataSource ds = new BeeDataSource(config);
            this.setPreDestroy("close");// jBeanBox will close pool
            return ds;
        }
    } 
    */
}
