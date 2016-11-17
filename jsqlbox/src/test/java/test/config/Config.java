package test.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This Java class is a configuration file, equal to XML in Spring, see jBeanBox project
 *
 */
public class Config {
	static void initialize() {
		SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setDataSource((DataSource) BeanBox.getBean(DSPoolBeanBox.class));
		// SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setShowSql(true);
		BeanBox.defaultContext.setAOPAround("test.\\w*.\\w*", "tx_\\w*", new TxInterceptorBox(), "invoke");
	}

	static class MySqlConfigBox extends BeanBox {
		{// Change to your url & driver
			setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClass", "com.mysql.jdbc.Driver");
		}
	}

	static class OracleConfigBox extends BeanBox {
		{ // Change to your url & driver
			setProperty("jdbcUrl", "jdbc:oracle:thin:@127.0.0.1:1521:xe");
			setProperty("driverClass", "oracle.jdbc.OracleDriver");
		}
	}

	static class DSPoolBeanBox extends OracleConfigBox {// change base class to switch Database
		{
			setClassOrValue(ComboPooledDataSource.class);
			setProperty("user", "root");// change to your user
			setProperty("password", "root888");// change to your password
			setProperty("minPoolSize", 4);
			setProperty("maxPoolSize", 30);
			setProperty("CheckoutTimeout", 5000);
		}
	}

	static class TxManagerBox extends BeanBox {
		{
			setClassOrValue(DataSourceTransactionManager.class);
			setProperty("dataSource", DSPoolBeanBox.class);
		}
	}

	static class TxInterceptorBox extends BeanBox {// Advice
		{
			Properties props = new Properties();
			props.put("tx_*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

	public static class JdbcTemplateBox extends BeanBox {
		{
			setConstructor(JdbcTemplate.class, DSPoolBeanBox.class);
		}
	}

}
