package com.jsqlboxdemo.init;

import java.sql.Connection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.junit.Assert;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.TX;
import com.github.drinkjava2.jdbpro.IocTool;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

import model.Team;

public class Initializer implements ServletContextListener {

	public static class DataSourceBox extends BeanBox {
		{ // H2 DataSource
			setProperty("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			setProperty("driverClassName", "org.h2.Driver");
			setProperty("username", "sa");
			setProperty("password", "");
		}

		public HikariDataSource create() {
			HikariDataSource ds = new HikariDataSource();
			ds.addDataSourceProperty("cachePrepStmts", true);
			ds.addDataSourceProperty("prepStmtCacheSize", 250);
			ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
			ds.addDataSourceProperty("useServerPrepStmts", true);
			ds.setMaximumPoolSize(10);
			ds.setConnectionTimeout(5000);
			this.setPreDestory("close");// jBeanBox will close pool
			return ds;
		}
	}

	public static class TxBox extends BeanBox {
		{
			this.setConstructor(TinyTx.class, BeanBox.getBean(DataSourceBox.class),
					Connection.TRANSACTION_READ_COMMITTED);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		// Register Transaction annotation
		BeanBox.regAopAroundAnnotation(TX.class, TxBox.class);
		//
		SqlBoxContextConfig config = new SqlBoxContextConfig();

		// Set transaction manager
		config.setConnectionManager(TinyTxConnectionManager.instance());

		// 这个仅当用到@Ioc注解时才需要配，通常可以不配
		config.setIocTool(new IocTool() {
			@Override
			public <T> T getBean(Class<?> configClass) {
				return BeanBox.getBean(configClass);
			}
		});
		SqlBoxContext ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class), config);
		SqlBoxContext.setGlobalSqlBoxContext(ctx); // 全局上下文

		// Initialize database
		String[] ddls = ctx.toDropAndCreateDDL(Team.class);
		for (String ddl : ddls)
			ctx.quiteExecute(ddl);

		for (int i = 0; i < 5; i++)
			new Team().put("name", "Team" + i, "rating", i * 10).insert();
		Assert.assertEquals(5, ctx.nQueryForLongValue("select count(*) from teams"));
		System.out.println("========== com.jsqlboxdemo.init.Initializer initialized=====");
	}

	@Override
	public void contextDestroyed(ServletContextEvent context) {
		SqlBoxContext.setGlobalSqlBoxContext(null);
		BeanBox.defaultContext.close();// close the dataSource
		System.out.println("========== com.jsqlboxdemo.init.Initializer destroyed=====");

	}

}
