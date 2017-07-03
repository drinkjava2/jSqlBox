package test.transaction_demo;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.thinjdbc.DataSourceManager;

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
public class JBeanBoxTX_JSQLBox2 {

	public static class SqlBoxContextBean extends BeanBox {
		public SqlBoxContext create() {
			return new SqlBoxContext(BeanBox.getBean(DataSourceBox.class), DataSourceManager.springDataSourceManager());
		}
	}

	public void tx_Insert1() {
		new User().insert();
	}

	public void tx_Insert2() {
		Dao.execute("insert into users (id) values('" + UUID.randomUUID() + "')");
		Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from users"));
		System.out.println("Now have 2 records in users table, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		System.out.println("============Testing: JBeanBoxTX_JSQLBox2============");
		BeanBox.defaultContext.setAOPAround(JBeanBoxTX_JSQLBox2.class.getName(), "tx_\\w*",
				new SpringTxInterceptorBox(), "invoke");
		JBeanBoxTX_JSQLBox2 tester = BeanBox.getBean(JBeanBoxTX_JSQLBox2.class);
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(SqlBoxContextBean.class));

		Dao.executeManyQuiet(Dao.getDialect().toDropAndCreateDDL(User.model()));
		Dao.refreshMetaData();

		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		try {
			tester.tx_Insert1();// this one inserted 1 record
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (Exception e) {
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from users"));
		BeanBox.defaultContext.close();// Release DataSource Pool
	}
}