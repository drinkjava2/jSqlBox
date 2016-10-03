package test.example1_basic_crud;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.BaseDao;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class TesterBox extends BeanBox {
	static {
		BaseDao.defaultContext.setDataSource((DataSource) BeanBox.getBean(DSPoolBeanBox.class));
		BeanBox.defaultContext.setAOPAround("test.example1_basic_crud.Test\\w*", "tx_\\w*", new TxInterceptorBox(),
				"invoke");
	}

	static class DSPoolBeanBox extends BeanBox {
		{
			setClassOrValue(ComboPooledDataSource.class);
			setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/test?user=root&password=root888");
			setProperty("driverClass", "com.mysql.jdbc.Driver");// change to your jdbc driver name
			setProperty("maxPoolSize", 10);
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
			props.put("insert*", "PROPAGATION_REQUIRED");
			setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
		}
	}

	public static class JdbcTemplateBox extends BeanBox {
		{
			setConstructor(JdbcTemplate.class, DSPoolBeanBox.class);
		}
	}
}
