package test;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.thinjdbc.DataSourceManager;

import test.config.DataSourceConfig.DataSourceBox;
import test.config.entity.User;

/**
 * This is the base class for all test cases which need prepare a database for
 * test, default using H2 memory database, see configurations in
 * DataSourceConfig.java
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TestBase {

	/**
	 * Set up DataSource pool, default SqlBoxContext, insert test data into
	 * database
	 */
	@Before
	public void setup() {
		System.out.println("=============Testing " + this.getClass().getName() + "================");
		cleanUp();
		DataSource ds = BeanBox.getBean(DataSourceBox.class);
		SqlBoxContext.setDefaultSqlBoxContext(new SqlBoxContext(ds, DataSourceManager.springDataSourceManager()));

		System.out.println("Drop and re-create an empty \"users\" table for next unit test ...");
		Dao.executeManyQuiet(Dao.getDialect().toDropAndCreateDDL(User.model()));
		Dao.refreshMetaData();
	}

	/**
	 * Clean up, close default SqlBoxContext, release DataSource pool
	 */
	@After
	public void cleanUp() {		 
		SqlBoxContext.getDefaultSqlBoxContext().close();
		// This will release DataSource because preDestroy method set to "Close"
		BeanBox.defaultContext.close();

	}

}