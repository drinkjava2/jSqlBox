package test.transaction_demo;

import static com.github.drinkjava2.tinyjdbc.core.TinyJdbc.P0;

import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jbeanbox.AopAround;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.tinyjdbc.core.TinyDataSourceManager;
import com.github.drinkjava2.tinyjdbc.core.TinyJdbc;

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
public class JBeanBoxTX_TinyJDBC {
	TinyJdbc tiny = new TinyJdbc((DataSource) BeanBox.getBean(DataSourceBox.class),
			TinyDataSourceManager.springDataSourceManager());

	@AopAround(SpringTxInterceptorBox.class)
	public void tx_Insert1() {
		tiny.executeUpdate("insert into users (id) values(?)" + P0(UUID.randomUUID()));
	}

	@AopAround(SpringTxInterceptorBox.class)
	public void tx_Insert2() {
		tiny.executeUpdate("insert into users (id) values(?)" + P0(UUID.randomUUID()));
		Assert.assertEquals(2, (int) tiny.queryForInteger("select count(*) from users"));
		System.out.println("Now have 2 records in users table, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		System.out.println("============Testing: JBeanBoxTX_TinyJDBC============");
		JBeanBoxTX_TinyJDBC tester = BeanBox.getBean(JBeanBoxTX_TinyJDBC.class);

		try {
			tiny.executeUpdate("drop table users");
		} catch (Exception e1) {
		}

		tiny.executeUpdate(Dialect.guessDialect(tiny.getDataSource()).toCreateDDL(User.model())[0]);
		Assert.assertEquals(0, (int) tiny.queryForInteger("select count(*) from users"));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (Exception e) {
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1, (int) tiny.queryForInteger("select count(*) from users"));
		BeanBox.defaultContext.close();// Release DataSource Pool
	}
}