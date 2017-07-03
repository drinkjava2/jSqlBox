package test.transaction_demo;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.thinjdbc.DataSourceManager;
import com.zaxxer.hikari.HikariDataSource;

import test.config.entity.User;

/**
 * This is to test use Spring's Declarative Transaction to control transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */

@Transactional(propagation = Propagation.REQUIRED)
public class SpringTX_JSQLBox {
	@EnableTransactionManagement
	@Configuration
	public static class SpringConfig {

		@Bean(destroyMethod = "close")
		public HikariDataSource HikariDataSourceBean() {
			HikariDataSource ds = new HikariDataSource();
			ds.setUsername("root");
			ds.setPassword("root888");
			ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
			ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setMaximumPoolSize(3);
			ds.setConnectionTimeout(5000);
			return ds;
		}

		@Bean
		public PlatformTransactionManager transactionManager() {
			DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
			transactionManager.setDataSource(HikariDataSourceBean());
			return transactionManager;
		}

		@Bean // Bad design to use method name as Bean's ID
		public SqlBoxContext sqlBoxContextBean() {
			return new SqlBoxContext(HikariDataSourceBean(), DataSourceManager.springDataSourceManager());
		}

		@Bean
		public SpringTX_JSQLBox tester() {
			return new SpringTX_JSQLBox();
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_Insert1() {
		new User().insert();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_Insert2() {
		Dao.execute("insert into users (id) values('" + UUID.randomUUID() + "')");
		Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from users"));
		System.out.println("Now have 2 records in users table, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		System.out.println("============Testing: SpringTX_JSQLBox============");
		AnnotationConfigApplicationContext springCTX = new AnnotationConfigApplicationContext(SpringConfig.class);
		SpringTX_JSQLBox tester = springCTX.getBean(SpringTX_JSQLBox.class);
		SqlBoxContext.setDefaultSqlBoxContext(springCTX.getBean("sqlBoxContextBean"));

		Dao.executeManyQuiet(Dao.getDialect().toDropAndCreateDDL(User.model()));
		Dao.refreshMetaData();

		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		try {
			tester.tx_Insert1();// this one inserted 1 record
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (ArithmeticException e) {
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from users"));
		springCTX.close();// Release DataSource Pool
	}
}