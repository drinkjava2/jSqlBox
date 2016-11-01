package test.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class Config {
	static {
		SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setDataSource((DataSource) BeanBox.getBean(DSPoolBeanBox.class));
		BeanBox.defaultContext.setAOPAround("test.\\w*.\\w*", "tx_\\w*", new TxInterceptorBox(), "invoke");
	}

	public static void recreateDatabase() {
		try {
			Dao.dao.execute("drop table user");
			Dao.dao.execute("drop tables user2");
		} catch (Exception e) {
			System.out.println("Exception found when drop table.");
		}
		Dao.dao.execute("create table user", //
				"( ID integer auto_increment ,", //
				"constraint const1 primary key (ID),", //
				"UserName Varchar  (50) ,", //
				"PhoneNumber Varchar  (50) ,", //
				"Address Varchar  (50) ,", //
				"Alive Boolean, ", //
				"Age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		Dao.dao.execute("create table user2", //
				"( id integer auto_increment ,", //
				"constraint const1 primary key (ID),", //
				"user_name Varchar  (50) ,", //
				"phone_number Varchar  (50) ,", //
				"address Varchar  (50) ,", //
				"age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	static class DSPoolBeanBox extends BeanBox {
		{
			setClassOrValue(ComboPooledDataSource.class);
			setProperty("jdbcUrl",
					"jdbc:mysql://127.0.0.1:3306/test?user=root&password=root888&rewriteBatchedStatements=true&useSSL=false");
			setProperty("driverClass", "com.mysql.jdbc.Driver");
			setProperty("maxPoolSize", 100);
			setProperty("CheckoutTimeout", 2000);
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

}
