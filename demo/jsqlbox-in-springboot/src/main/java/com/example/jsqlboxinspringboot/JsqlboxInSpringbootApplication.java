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
	DataSource ds;

	public static void main(String[] args) {
		SpringApplication.run(JsqlboxInSpringbootApplication.class, args);
		String[] ddls = gctx().toCreateDDL(Customer.class);
		for (String ddl : ddls)
			gctx().iExecute(ddl);
	}

	@Bean
	public SqlBoxContext createDefaultSqlBoxContext() {
		SqlBoxContext ctx = new SqlBoxContext(ds);
		ctx.setConnectionManager(new MySpringConnectionManager());
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		return ctx;
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
