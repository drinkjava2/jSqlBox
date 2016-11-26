package test.config;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This Java class is a configuration file, equal to XML in Spring, see jBeanBox
 * project
 *
 */
@Configuration
public class SpringConfig {
	public static void main(String[] args) {
		new SpringConfig().doConfigTest();
	}

	@Test
	public void doConfigTest() {
		// TODO: split SqlBox to 2 classes, one is runtime, another is config time
		// @SuppressWarnings("resource")
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		DataSource ds = ctx.getBean("MySqlDataSourceBean", DataSource.class);
		SqlBoxContext sbCTX = new SqlBoxContext(ds);
		SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setDataSource(ds);
		SqlBox sb = new SqlBox(sbCTX);
		System.out.println(sb.getContext().getDataSource());
		Dao dao = new Dao(sb);
		dao.execute("drop table if exists tmp_users");
		dao.execute("create table tmp_users (UserName Varchar (10))");
		dao.execute("insert into tmp_users (username) values('spring')");
		Assert.assertEquals("spring", dao.queryForString("select username from tmp_users where username='spring'"));
		// ctx.close(); //TODO: can not close it, will cause unit test fail need fix this problem
	}

	@Bean
	public ComboPooledDataSource C3P0Bean() {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setUser("root");// set to your user name
		ds.setPassword("root888");// set to your password
		ds.setMaxPoolSize(30);
		ds.setCheckoutTimeout(5000);
		return ds;
	}

	@Bean(name = "MySqlDataSourceBean") // This is not good
	public DataSource MySqlDataSourceBean() {
		ComboPooledDataSource ds = C3P0Bean();
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		try {
			ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		return ds;
	}

	@Bean
	public SqlBoxContext sqlBoxCtxBean() {
		return new SqlBoxContext(MySqlDataSourceBean());
	}

}