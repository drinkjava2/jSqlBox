package test.config;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.config.po.User;

/**
 * This Java class is a configuration file, equal to XML in Spring, see jBeanBox project
 *
 */
@Configuration
public class SpringConfig {

	@Before
	public void setup() {
		InitializeDatabase.recreateTables();
	}

	@Test
	public void doConfigTest() {
		AnnotationConfigApplicationContext springCtx = new AnnotationConfigApplicationContext(SpringConfig.class);
		SqlBoxContext sc = springCtx.getBean("sqlBoxCtxBean", SqlBoxContext.class);
		User u = sc.createBean(User.class);
		u.setUserName("Spring");
		u.dao().save();
		Assert.assertEquals("Spring", u.dao().queryForString(
				"select " + u.UserName() + " from " + u.Table() + " where " + u.UserName() + "=" + q("Spring")));
		springCtx.close();
	}

	@Bean
	public ComboPooledDataSource C3P0Bean() {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setUser("root");// set to your user name
		ds.setPassword("root888");// set to your password
		ds.setMaxPoolSize(30);
		ds.setCheckoutTimeout(5000);
		return ds;
	}

	@Bean
	public DataSource MySqlDataSourceBean() {
		ComboPooledDataSource ds = C3P0Bean();
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		try {
			ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		return ds;
	}

	@Bean(name = "sqlBoxCtxBean") // This is not good
	public SqlBoxContext sqlBoxCtxBean() {
		return new SqlBoxContext(MySqlDataSourceBean());
	}

}