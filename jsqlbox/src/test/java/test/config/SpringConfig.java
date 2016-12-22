package test.config;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.po.DB;
import test.config.po.User;
import test.transaction.SpringTransactionTest;

/**
 * This is traditional Spring configuration
 *
 */
@EnableTransactionManagement
@Configuration
public class SpringConfig {
	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Bean
	public ComboPooledDataSource C3P0Bean() {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setUser("root");// change to set your user name
		ds.setPassword("root888");// change to set your password
		ds.setMaxPoolSize(30);
		ds.setCheckoutTimeout(5000);
		return ds;
	}

	/**
	 * Here I copied jdbcURL, driverClass, username, password settings from jBeainBoxConfig, buy you can change to your
	 * database settings
	 */
	@Bean
	public DataSource MySqlDataSourceBean() {
		ComboPooledDataSource ds = C3P0Bean();
		ds.setJdbcUrl((String) new DataSourceBox().getProperty("jdbcUrl"));// change to set your jdbcURL
		try {
			ds.setDriverClass((String) new DataSourceBox().getProperty("driverClass"));// set your driverClass
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		return ds;
	}

	@Bean(name = "sqlBoxCtxBean") // This is not good
	public SqlBoxContext sqlBoxCtxBean() {
		return new SqlBoxContext(MySqlDataSourceBean(), DB.class);
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(MySqlDataSourceBean());
		return transactionManager;
	}

	@Bean
	public SpringTransactionTest springTransactionTest() {
		return new SpringTransactionTest();
	}

	@Test
	public void doConfigTest() {
		AnnotationConfigApplicationContext springCtx = new AnnotationConfigApplicationContext(SpringConfig.class);
		SqlBoxContext sc = springCtx.getBean("sqlBoxCtxBean", SqlBoxContext.class);
		User u = sc.createEntity(User.class);
		// Can not use User u=new User() here because default global SqlBoxContext not configured
		SqlBox.execute("delete from " + u.table());
		u.setUserName("Spring");
		u.insert();
		Assert.assertEquals("Spring", SqlBox.queryForString(
				"select " + u.userName() + " from " + u.table() + " where " + u.userName() + "=" + q("Spring")));
		springCtx.close();
	}
}