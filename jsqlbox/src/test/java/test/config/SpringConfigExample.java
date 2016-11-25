package test.config;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This Java class is a configuration file, equal to XML in Spring, see jBeanBox project
 *
 */
@Configuration
public class SpringConfigExample {

	@Bean
	public ComboPooledDataSource C3P0Bean() {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setUser("root");// set to your user
		ds.setPassword("root888");// set to your password
		ds.setMinPoolSize(4);
		ds.setMaxPoolSize(30);
		ds.setCheckoutTimeout(5000);
		return ds;
	}

	@Bean
	public DataSource MySqlDataSource() {
		ComboPooledDataSource ds = C3P0Bean();
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		try {
			ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		return ds;
	}

	@Bean
	public SqlBoxContext CtxBean() {
		return new SqlBoxContext(MySqlDataSource());
	}

	@Bean
	public DataSourceTransactionManager TxManagerBean() {
		DataSourceTransactionManager dm = new DataSourceTransactionManager();
		dm.setDataSource(MySqlDataSource());
		return dm;
	}

}