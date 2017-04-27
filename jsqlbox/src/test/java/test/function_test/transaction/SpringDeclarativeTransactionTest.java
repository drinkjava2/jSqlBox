package test.function_test.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.po.User;

/**
 * This is to test use Spring's Declarative Transaction to control transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */

@Transactional(propagation = Propagation.REQUIRED)
public class SpringDeclarativeTransactionTest {

	@EnableTransactionManagement
	@Configuration
	public static class SpringConfig {
		@Bean(destroyMethod = "close")
		public HikariDataSource HikariDataSourceBean() {
			HikariDataSource ds = new HikariDataSource();
			DataSourceBox dsBox = new DataSourceBox();// Copy begin
			ds.setUsername((String) dsBox.getProperty("username"));
			ds.setPassword((String) dsBox.getProperty("password"));
			ds.setJdbcUrl((String) dsBox.getProperty("jdbcUrl"));
			ds.setDriverClassName((String) dsBox.getProperty("driverClassName"));
			return ds;
		}

		@Bean // This is not good, if have 2 same type Bean, will have to use
				// String method as ID
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
		public SpringDeclarativeTransactionTest tester() {
			return new SpringDeclarativeTransactionTest();
		}

	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_doInsert() {
		User u = new User();
		u.setId(u.nextUUID());
		u.insert();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from users"));
		Dao.execute("A bad sql Gramma");
	}

	@Test
	public void doTest() {
		System.out.println("============Testing: SpringDeclarativeTransactionTest============");
		AnnotationConfigApplicationContext springCTX = new AnnotationConfigApplicationContext(SpringConfig.class);
		SqlBoxContext.setDefaultSqlBoxContext(springCTX.getBean("sqlBoxCtxBean"));

		Dao.executeQuiet("drop table users");// auto commit mode
		Dao.execute(User.ddl(Dao.getDialect()));// auto commit mode
		Dao.refreshMetaData();

		SpringDeclarativeTransactionTest tester = springCTX.getBean(SpringDeclarativeTransactionTest.class);
		try {
			tester.tx_doInsert();
		} catch (BadSqlGrammarException e) {// should roll back
			Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		}
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		springCTX.close();// don't forget close CTX to avoid Datasource leak
	}

}