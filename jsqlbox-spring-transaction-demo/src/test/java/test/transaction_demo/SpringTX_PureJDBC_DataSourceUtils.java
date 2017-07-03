package test.transaction_demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jdialects.Dialect;
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
public class SpringTX_PureJDBC_DataSourceUtils {

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

		@Bean
		public SpringTX_PureJDBC tester() {
			return new SpringTX_PureJDBC();
		}
	}

	public Integer queryForInteger(DataSource ds, String sql) {
		Connection con = DataSourceUtils.getConnection(ds);
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closePST(pst);
			DataSourceUtils.releaseConnection(con, ds);
		}
		return null;
	}

	public void executeSql(DataSource ds, String sql) {
		Connection con = DataSourceUtils.getConnection(ds);
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement(sql);
			pst.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Exception found:", e);
		} finally {
			closePST(pst);
			DataSourceUtils.releaseConnection(con, ds);
		}
	}

	private void closePST(PreparedStatement pst) {
		try {
			if (pst != null)
				pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_Insert1(DataSource ds) {
		executeSql(ds, "insert into users (id) values('" + UUID.randomUUID() + "')");
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void tx_Insert2(DataSource ds) {
		executeSql(ds, "insert into users (id) values('" + UUID.randomUUID() + "')");
		Assert.assertEquals(2, (int) queryForInteger(ds, "select count(*) from users"));
		System.out.println("Now have 2 records in users table, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		System.out.println("============Testing: SpringTX_PureJDBC_DataSourceUtils============");
		AnnotationConfigApplicationContext springCTX = new AnnotationConfigApplicationContext(SpringConfig.class);
		DataSource ds = (DataSource) springCTX.getBean("HikariDataSourceBean");
		SpringTX_PureJDBC tester = springCTX.getBean(SpringTX_PureJDBC.class);

		try {
			tester.executeSql(ds, "drop table users");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		tester.executeSql(ds, Dialect.guessDialect(ds).toCreateDDL(User.model())[0]);
		Assert.assertEquals(0, (int) tester.queryForInteger(ds, "select count(*) from users"));
		try {
			tester.tx_Insert1(ds);// this one inserted 1 record
			tester.tx_Insert2(ds);// this one did not insert, roll back
		} catch (ArithmeticException e) {
			System.out.println("div/0 exception found, tx_Insert2  should roll back");
		}
		Assert.assertEquals(1, (int) tester.queryForInteger(ds, "select count(*) from users"));
		springCTX.close();// Release DataSource Pool
	}
}