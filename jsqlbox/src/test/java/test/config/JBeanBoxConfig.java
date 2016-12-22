package test.config;

import java.util.Properties;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.config.po.DB;

/**
 * This is jBeanBox configuration file, equal to XML in Spring <br/>
 * About jBeanBox project can google it
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JBeanBoxConfig {

	// Data source pool setting
	public static class DataSourceBox extends MySqlDataSourceBox {
	}

	// Data source pool setting
	public static class C3P0Box extends BeanBox {
		{
			setClassOrValue(ComboPooledDataSource.class);
			setProperty("user", "root");// set to your user
			setProperty("password", "root888");// set to your password
			setProperty("minPoolSize", 3);
			setProperty("maxPoolSize", 10);
			setProperty("CheckoutTimeout", 5000);
			this.setPreDestory("close");// Close c3p0 DataSource pool if jBeanBox context close
		}
	}

	// MySql connection URL
	public static class MySqlDataSourceBox extends C3P0Box {
		{
			setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClass", "com.mysql.jdbc.Driver");
		}
	}

	// Oracle connection URL
	public static class OracleDataSourceBox extends C3P0Box {
		{
			setProperty("jdbcUrl", "jdbc:oracle:thin:@127.0.0.1:1521:xe");
			setProperty("driverClass", "oracle.jdbc.OracleDriver");
		}
	}

	// MsSql Server connection URL
	public static class MsSqlServerDataSourceBox extends C3P0Box {
		{
			setProperty("jdbcUrl", "jdbc:sqlserver://localhost:1433;databaseName=test");
			setProperty("driverClass", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}
	}

	// CtxBox is a SqlBoxContent singleton
	public static class DefaultSqlBoxContextBox extends BeanBox {
		{
			this.setConstructor(SqlBoxContext.class, DataSourceBox.class, DB.class);
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
	static class TxInterceptorBox extends BeanBox {
		{
			Properties props = new Properties();
			props.put("tx_*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

	public static class JdbcTemplateBox extends BeanBox {
		{
			setConstructor(JdbcTemplate.class, DataSourceBox.class);
		}
	}

}
