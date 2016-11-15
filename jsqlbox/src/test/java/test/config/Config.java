package test.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.crud_method.po.User;

public class Config {
	static {
		SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setDataSource((DataSource) BeanBox.getBean(DSPoolBeanBox.class));
		// SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setShowSql(true);
		BeanBox.defaultContext.setAOPAround("test.\\w*.\\w*", "tx_\\w*", new TxInterceptorBox(), "invoke");
	}

	static class MySqlConfigBox extends BeanBox {
		{ // Change to your schema, username & password
			setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClass", "com.mysql.jdbc.Driver");
		}
	}

	static class OracleConfigBox extends BeanBox {
		{ // Change to your schema, username & password
			setProperty("jdbcUrl", "jdbc:oracle:thin:@127.0.0.1:1521:xe");
			setProperty("driverClass", "oracle.jdbc.OracleDriver");
		}
	}

	static class DSPoolBeanBox extends MySqlConfigBox {// change here to switch Test Database
		{
			setClassOrValue(ComboPooledDataSource.class);
			setProperty("user", "root");
			setProperty("password", "root888");
			setProperty("minPoolSize", 4);
			setProperty("maxPoolSize", 30);
			setProperty("CheckoutTimeout", 5000);
		}
	}

	static class TxManagerBox extends BeanBox {
		{
			setClassOrValue(DataSourceTransactionManager.class);
			setProperty("dataSource", DSPoolBeanBox.class);
		}
	}

	static class TxInterceptorBox extends BeanBox {// Advice
		{
			Properties props = new Properties();
			props.put("tx_*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

	public static class JdbcTemplateBox extends BeanBox {
		{
			setConstructor(JdbcTemplate.class, DSPoolBeanBox.class);
		}
	}

	public static void recreateTables() {
		try {
			Dao.dao.execute("drop table USERS");
			Dao.dao.execute("drop table USERS2");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			SqlBoxUtils.eatException(e);
		}
		ComboPooledDataSource pool = (ComboPooledDataSource) BeanBox.getBean(DSPoolBeanBox.class);
		String driverClassName = pool.getDriverClass().toLowerCase();
		if (driverClassName.indexOf("mysql") != -1)
			executeResourceSQLs("/CreateMysqlDatabase.sql");
		else if (driverClassName.indexOf("oracle") != -1)
			executeResourceSQLs("/CreateOracleDatabase.sql");
	}

	private static void executeResourceSQLs(String sqlResourceFile) {
		InputStream in = Config.class.getResourceAsStream(sqlResourceFile);
		if (in == null)
			throw new SqlBoxException("Can not find SQL resource file " + sqlResourceFile + " in resources folder");
		Scanner sc = new Scanner(in);
		try {
			while (sc.useDelimiter(";").hasNext())
				Dao.dao.execute(sc.next());
		} finally {
			sc.close();
		}
	}

	@Test
	public void testCreateTables() {
		recreateTables();
		Assert.assertEquals((Integer) 0, Dao.dao.queryForInteger("select count(*) from ", User.Table));
		Assert.assertEquals((Integer) 0, Dao.dao.queryForInteger("select count(*) from ", User.Table));
	}

	public static void main(String[] args) {
		recreateTables();
	}

}
