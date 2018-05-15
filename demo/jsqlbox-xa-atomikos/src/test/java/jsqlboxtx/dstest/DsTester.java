package jsqlboxtx.dstest;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import jsqlboxtx.dstest.DataSourceConfig.AnotherH2DataSource;
import jsqlboxtx.dstest.DataSourceConfig.AnotherH2Pool;
import jsqlboxtx.dstest.DataSourceConfig.H2DataSourceBox;

/**
 * This is to test TinyTx Declarative Transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DsTester {
	SqlBoxContext ctx1;
	SqlBoxContext ctx2;
	SqlBoxContext ctx3;
	{

		ctx1 = new SqlBoxContext((DataSource) BeanBox.getBean(H2DataSourceBox.class));
		ctx2 = new SqlBoxContext((DataSource) BeanBox.getBean(AnotherH2DataSource.class));
		ctx3 = new SqlBoxContext((DataSource) BeanBox.getBean(AnotherH2Pool.class));
	}

	@After
	public void cleanupDS() {
		BeanBox.defaultContext.close();
	}

	@Test
	public void doTest() {
		ctx1.nExecute("create table user_tb (id varchar(40)) engine=InnoDB");
		Assert.assertEquals(0, ctx1.nQueryForLongValue("select count(*) from user_tb "));
		Assert.assertEquals(0, ctx2.nQueryForLongValue("select count(*) from user_tb "));
		Assert.assertEquals(0, ctx3.nQueryForLongValue("select count(*) from user_tb "));

		ctx1.nExecute("insert into user_tb (id) values('456')");
		Assert.assertEquals(1, ctx1.nQueryForLongValue("select count(*) from user_tb "));
		Assert.assertEquals(1, ctx2.nQueryForLongValue("select count(*) from user_tb "));
		Assert.assertEquals(1, ctx3.nQueryForLongValue("select count(*) from user_tb "));

		ctx1.nExecute("drop table user_tb  ");
	}

}