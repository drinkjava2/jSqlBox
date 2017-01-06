package test.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

import test.config.po.DB;

/**
 * This is jBeanBox configuration classes, equal to XML in Spring <br/>
 * About jBeanBox project can google it, it's a small IOC/AOP tool to replace Spring
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JBeanBoxConfig {

	/**
	 * This is a SqlBoxContext setting, you can set up as many as possible contexts in one project, but for most
	 * projects usually only use one defaultSqlBox context
	 * 
	 */
	public static class DefaultSqlBoxContextBox extends BeanBox {
		public SqlBoxContext create() {
			SqlBoxContext ctx = new SqlBoxContext();
			ctx.setDataSource((DataSource) BeanBox.getBean(DataSourceBox.class));
			ctx.setDbClass(DB.class);
			return ctx;
		}
	}

	/**
	 * ==================================================================================================<br/>
	 * Data source setting, change "H2DataSourceBox" to MySqlDataSourceBox to test on MySql <br/>
	 * This project is already tested on H2 memory database & MySql5 and Oracle11g
	 * ==================================================================================================<br/>
	 */
	public static class DataSourceBox extends OracleDataSourceBox {
	}

	// H2Database memory database connection URL
	public static class H2DataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:h2:~/test");
			setProperty("driverClassName", "org.h2.Driver");
			setProperty("username", "sa");
			setProperty("password", "");
		}
	}

	// MySql connection URL
	public static class MySqlDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClassName", "com.mysql.jdbc.Driver");
			setProperty("username", "root");// change to your user & password
			setProperty("password", "root888");
		}
	}

	// Oracle connection URL
	public static class OracleDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:oracle:thin:@127.0.0.1:1521:xe");
			setProperty("driverClassName", "oracle.jdbc.OracleDriver");
			setProperty("username", "root");// change to your user & password
			setProperty("password", "root888");
		}
	}

	// MsSql Server connection URL, I haven't test MSSQL server
	public static class MsSqlServerDataSourceBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:sqlserver://localhost:1433;databaseName=test");
			setProperty("driverClassName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
			setProperty("username", "sa");// change to your user & password
			setProperty("password", "");
		}
	}

	// HikariCP is a DataSource pool much quicker than C3P0
	public static class HikariCPBox extends BeanBox {
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

	// Spring TxManager
	static class TxManagerBox extends BeanBox {
		{
			setClassOrValue(DataSourceTransactionManager.class);
			setProperty("dataSource", DataSourceBox.class);
		}
	}

	// Spring TransactionInterceptor
	public static class SpringTxInterceptorBox extends BeanBox {
		{
			Properties props = new Properties();
			props.put("*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

	public static class TxInterceptorBox2 extends BeanBox {
		{
			Properties props = new Properties();
			props.put("do*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

}
