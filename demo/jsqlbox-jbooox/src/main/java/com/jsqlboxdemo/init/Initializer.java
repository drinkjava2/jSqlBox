package com.jsqlboxdemo.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.junit.Assert;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxAOP;
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
			this.setPreDestroy("close");// jBeanBox will close pool
			return ds;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface TX {
		public Class<?> value() default TinyTxAOP.class;
	}
  
	@Override
	public void contextInitialized(ServletContextEvent context) {
		DbContext.setGlobalNextAllowShowSql(true);
		DbContext ctx = new DbContext(JBEANBOX.getBean(DataSourceBox.class)); 
		ctx.setConnectionManager(TinyTxConnectionManager.instance());
		DbContext.setGlobalDbContext(ctx); // 全局上下文

		// Initialize database
		String[] ddls = ctx.toDropAndCreateDDL(Team.class);
		for (String ddl : ddls)
			ctx.quiteExecute(ddl);

		for (int i = 0; i < 5; i++)
			new Team().putField("name", "Team" + i, "rating", i * 10).insert();
		Assert.assertEquals(5, ctx.nQueryForLongValue("select count(*) from teams"));
		System.out.println("========== com.jsqlboxdemo.init.Initializer initialized=====");
	}

	@Override
	public void contextDestroyed(ServletContextEvent context) {
		DbContext.setGlobalDbContext(null);
		JBEANBOX.close();// close the dataSource
		System.out.println("========== com.jsqlboxdemo.init.Initializer destroyed=====");

	}

}
