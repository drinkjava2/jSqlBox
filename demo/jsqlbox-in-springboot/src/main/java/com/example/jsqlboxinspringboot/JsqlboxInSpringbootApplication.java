package com.example.jsqlboxinspringboot;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.jsqlboxinspringboot.entity.Customer;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.ConnectionManager;

@SpringBootApplication
public class JsqlboxInSpringbootApplication {
	@Autowired
	DataSource ds; // SpringBoot会自动注入H2数据源

	public static void main(String[] args) {
		SpringApplication.run(JsqlboxInSpringbootApplication.class, args);

		// 第一次运行要建表，利用jSqlBox来做
		String[] ddls = gctx().toCreateDDL(Customer.class);
		for (String ddl : ddls)
			gctx().iExecute(ddl);
	}

	// 只要不加@Lazy标记，SpringBoot在启动时会创建一个Bean单例
	@Bean
	public SqlBoxContext createDefaultSqlBoxContext() {
		SqlBoxContext ctx = new SqlBoxContext(ds);
		// 设定连接管理器，这是事务设置生效的前题
		ctx.setConnectionManager(new MySpringConnectionManager());
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// 静态全局上下文，用于单数据源场合
		return ctx;
	}

	/*
	 * jTransactions是一个独立的声明式事务工具，但是这次不用它的，改用Spring的声明式事务。
	 * 声明式事务有3个关键：1.IOC/AOP工具，这里就用Spring自带的。 2.声明式事务切面处理器，因为这个项目pom.xml中有一个H2的依赖，
	 * 所以Spring在后台已经悄悄地自动创建了一个DataSourceTransactionManager单例，并将H2数据源注给它了。
	 * 3.连接管理器，它的作用是告诉DAO工具连接的获取和关闭方式，这里手工生成一个，调用Spring的DataSourceUtils中的静态方法。
	 * 将来这个小类可能做成插件发布，但现在吗多打几行也不算太麻烦，毕竟一个项目只要配置一次就行了。
	 */
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
