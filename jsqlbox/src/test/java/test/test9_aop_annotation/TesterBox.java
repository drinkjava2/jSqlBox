package test.test9_aop_annotation;

import java.util.Properties;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 *
 * @author Yong Zhu
 * @since 2.4
 */
public class TesterBox extends BeanBox {
	// H2Database memory database c3p0 DataSource pool setting
	public static class DSPoolBeanBox extends HikariCPBox {
		{
			setProperty("jdbcUrl", "jdbc:h2:~/test");
			setProperty("driverClassName", "org.h2.Driver");
			setProperty("username", "sa");
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

	public static class TxManagerBox extends BeanBox {
		{
			setClassOrValue(DataSourceTransactionManager.class);
			setProperty("dataSource", DSPoolBeanBox.class);
		}
	}

	public static class TxInterceptorBox extends BeanBox {// Advice
		{
			Properties props = new Properties();
			props.put("insert*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

	public static class JdbcTemplateBox extends BeanBox {
		{
			setConstructor(JdbcTemplate.class, DSPoolBeanBox.class);
		}
	}
}
