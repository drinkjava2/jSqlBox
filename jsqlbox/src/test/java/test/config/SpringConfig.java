package test.config;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

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

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.po.User;
import test.function_test.transaction.SpringTransactionTest;

/**
 * This is traditional Spring configuration
 *
 */
@EnableTransactionManagement
@Configuration
public class SpringConfig {
	@Before
	public void setup() {
		System.out.println("===============================Testing SpringConfig===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	/**
	 * Here I copied jdbcURL, driverClass, username, password settings from jBeainBoxConfig, buy you can change to your
	 * database settings
	 */
	@Bean
	public HikariDataSource HikariDataSourceBean() {
		HikariDataSource ds = new HikariDataSource();
		DataSourceBox dsSetting = new DataSourceBox();
		ds.setUsername((String) dsSetting.getProperty("username"));
		ds.setPassword((String) dsSetting.getProperty("password"));
		ds.setJdbcUrl((String) dsSetting.getProperty("jdbcUrl"));
		ds.setDriverClassName((String) dsSetting.getProperty("driverClassName"));
		ds.setMaximumPoolSize(10);
		ds.setConnectionTimeout(5000);
		return ds;
	}

	@Bean // This is not good
	public SqlBoxContext sqlBoxCtxBean() {
		return new SqlBoxContext(HikariDataSourceBean());
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(HikariDataSourceBean());
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
		// Can not use User u=new User() here it use default global SqlBoxContext
		Assert.assertEquals(sc, u.box().getSqlBoxContext());
		Assert.assertNotEquals(Dao.getDefaultContext(), u.box().getSqlBoxContext());

		Dao.execute("delete from " + u.table());
		u.setUserName("Spring");
		u.insert();
		Assert.assertEquals("Spring", Dao.queryForString(
				"select " + u.USERNAME() + " from " + u.table() + " where " + u.USERNAME() + "=" + q("Spring")));

		User u2 = sc.load(User.class, u.getId());
		Assert.assertEquals(sc, u2.box().getSqlBoxContext());
		Assert.assertNotEquals(Dao.getDefaultContext(), u2.box().getSqlBoxContext());
		springCtx.close();
	}
}