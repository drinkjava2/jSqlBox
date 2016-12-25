package test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.TestPrepare;
import test.config.po.DB;
import test.config.po.User;

/**
 * This is to test jSqlBoxContext class
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ContextTest {

	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
	}

	@Test
	public void insertUser1() {
		HikariDataSource ds = new HikariDataSource();// c3p0
		ds.setUsername("root");
		ds.setPassword("root888");
		ds.setJdbcUrl((String) new DataSourceBox().getProperty("jdbcUrl"));
		ds.setDriverClassName((String) new DataSourceBox().getProperty("driverClassName"));
		SqlBoxContext ctx = new SqlBoxContext(ds, DB.class);
		User u = ctx.createEntity(User.class);
		// Can not use User u=new User() here because default SqlBoxContext not configured
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.insert();
		Assert.assertEquals(111, (int) SqlBox.queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User1")));
	}

	// CtxBox is a SqlBoxContent singleton
	public static class AnotherSqlBoxContextBox extends BeanBox {
		public SqlBoxContext create() {
			SqlBoxContext ctx = new SqlBoxContext();
			ctx.setDataSource((DataSource) BeanBox.getBean(DataSourceBox.class));
			ctx.setDbClass(DB.class);
			return ctx;
		}
	}

	@Test
	public void insertUser2() {
		SqlBoxContext ctx = BeanBox.getBean(AnotherSqlBoxContextBox.class);
		User u = ctx.createEntity(User.class);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.insert();
		Assert.assertEquals(111, (int) SqlBox.queryForInteger("select ", u.phoneNumber(), " from ", u.table(),
				" where ", u.userName(), "=", q("User1")));
	}

	public static void main(String[] args) {
		ContextTest t = new ContextTest();
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
		t.insertUser1();
	}

}