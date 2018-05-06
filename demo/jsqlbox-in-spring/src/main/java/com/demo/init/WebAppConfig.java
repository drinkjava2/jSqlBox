package com.demo.init;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.ConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan("com.demo")
@EnableWebMvc
@EnableTransactionManagement
public class WebAppConfig {

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		HikariDataSource ds = new HikariDataSource();
		ds.addDataSourceProperty("cachePrepStmts", true);
		ds.addDataSourceProperty("prepStmtCacheSize", 250);
		ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		ds.addDataSourceProperty("useServerPrepStmts", true);
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(10);
		ds.setConnectionTimeout(5000);
		return ds;
	}

	/**
	 * Spring will think this is a special method? because it return a
	 * PlatformTransactionManager subclass instance
	 */
	@Bean
	public DataSourceTransactionManager createDm() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public UrlBasedViewResolver setupViewResolver() {
		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
		resolver.setPrefix("/WEB-INF/pages/");
		resolver.setSuffix(".jsp");
		resolver.setViewClass(JstlView.class);
		return resolver;
	}

	@Bean
	public SqlBoxContext createDefaultSqlBoxContext() {
		if (dataSource() != dataSource())
			throw new AssertionError("I found Spring weird");
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setConnectionManager(createMySpringConnectionMG());
		SqlBoxContext ctx = new SqlBoxContext(dataSource(), config);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		return ctx;
	}

	@Bean
	public MySpringConnectionManager createMySpringConnectionMG() {
		return new MySpringConnectionManager();
	}

	public static class MySpringConnectionManager implements ConnectionManager {

		@Override
		public Connection getConnection(DataSource dataSource) throws SQLException {
			return DataSourceUtils.getConnection(dataSource);
		}

		@Override
		public void releaseConnection(Connection conn, DataSource dataSource) throws SQLException {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}

		@Override
		public boolean isInTransaction(DataSource dataSource) {
			if (dataSource == null)
				return false;
			return null != TransactionSynchronizationManager.getResource(dataSource);
		}
	}
}
