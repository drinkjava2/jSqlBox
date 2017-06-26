package test.config;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.DataSourceConfig.DefaultSqlBoxContextBox;
import test.config.entity.User;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class PrepareTestContext {

	/**
	 * Drop and rebuild all tables
	 */
	public static void prepareDatasource_setDefaultSqlBoxConetxt_recreateTables() {
		BeanBox.defaultContext.close();
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		System.out.println("Drop and re-create a demo \"users\" table for next unit test ...");
		Dao.executeManyQuiet(Dao.getDialect().toDropDDL(User.model()));
		Dao.executeMany(Dao.getDialect().toCreateDDL(User.model()));
		Dao.refreshMetaData();
	}

	/**
	 * Close BeanBox Context
	 */
	public static void closeDatasource_closeDefaultSqlBoxConetxt() {
		/**
		 * This will close HikariDataSource because preDestroy method set to
		 * "Close"
		 */
		BeanBox.defaultContext.close();
		SqlBoxContext.getDefaultSqlBoxContext().close();
	}

	@Test
	public void testCreateTables() {
		System.out.println("===============================Testing TestPrepare===============================");
		prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		closeDatasource_closeDefaultSqlBoxConetxt();
	}

}
