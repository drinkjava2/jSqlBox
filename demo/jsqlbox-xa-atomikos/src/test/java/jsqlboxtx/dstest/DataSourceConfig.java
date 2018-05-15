package jsqlboxtx.dstest;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

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

	// MySql connection URL
	public static class MySqlDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl",
					"jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClassName", "com.mysql.jdbc.Driver");
			setProperty("username", "root");// change to your user & password
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

	// H2Database memory database
	public static class H2DataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			setProperty("driverClassName", "org.h2.Driver");
			setProperty("username", "sa");
			setProperty("password", "");
		}
	}

	public static class AnotherH2DataSource extends BeanBox {
		public JdbcDataSource create() {
			JdbcDataSource ds = new JdbcDataSource();
			ds.setUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			ds.setUser("sa");
			ds.setPassword("");
			return ds;
		}
	}

	public static class AnotherH2Pool extends BeanBox {
		{
			this.setPreDestory("dispose");
		}

		public JdbcConnectionPool create() {
			JdbcConnectionPool cp = JdbcConnectionPool
					.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
			return cp;
		}
	}

}
