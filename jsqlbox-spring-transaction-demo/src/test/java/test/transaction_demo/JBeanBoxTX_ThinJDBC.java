package test.transaction_demo;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jbeanbox.AopAround;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.thinjdbc.DataSourceManager;
import com.github.drinkjava2.thinjdbc.dao.DataAccessException;
import com.github.drinkjava2.thinjdbc.jdbc.core.JdbcTemplate;

import test.config.DataSourceConfig.DataSourceBox;
import test.config.DataSourceConfig.SpringTxInterceptorBox;
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
public class JBeanBoxTX_ThinJDBC {
	JdbcTemplate jdbc = new JdbcTemplate(BeanBox.getBean(DataSourceBox.class),
			DataSourceManager.springDataSourceManager());

	@AopAround(SpringTxInterceptorBox.class)
	public void tx_Insert1() {
		jdbc.update("insert into users (id) values(?)", UUID.randomUUID());
	}

	@AopAround(SpringTxInterceptorBox.class)
	public void tx_Insert2() {
		jdbc.update("insert into users (id) values(?)", UUID.randomUUID());
		Assert.assertEquals(2, (int) jdbc.queryForObject("select count(*) from users", Integer.class));
		System.out.println("Now have 2 records in users table, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		System.out.println("============Testing: JBeanBoxTX_ThinJDBC============");
		JBeanBoxTX_ThinJDBC tester = BeanBox.getBean(JBeanBoxTX_ThinJDBC.class);

		try {
			jdbc.update("drop table users");
		} catch (DataAccessException e1) {
		}
		jdbc.update(Dialect.guessDialect(jdbc.getDataSource()).toCreateDDL(User.model())[0]);
		Assert.assertEquals(0, (int) jdbc.queryForObject("select count(*) from users", Integer.class));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (Exception e) {
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1, (int) jdbc.queryForObject("select count(*) from users", Integer.class));
		BeanBox.defaultContext.close();// Release DataSource Pool
	}
}