package test.coverage_test;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.DBMetaData;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.DatabaseType;

import test.config.JBeanBoxConfig.H2DataSourceBox;
import test.config.JBeanBoxConfig.MsSqlServerDataSourceBox;
import test.config.JBeanBoxConfig.MySqlDataSourceBox;
import test.config.JBeanBoxConfig.OracleDataSourceBox;
import test.config.PrepareTestContext;

/**
 * This is to test TinyJDBC get meta data
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DBMetaDataTest {

	@Before
	public void setup() {
		System.out.println("=============================Testing DBMetaDataTest=============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void tinyJdbcGetMetadataTest() {
		DatabaseType type = Dao.getDefaultDatabaseType();
		DataSource ds = null;
		if (type.isH2())
			ds = BeanBox.getBean(H2DataSourceBox.class);
		else if (type.isMySql())
			ds = BeanBox.getBean(MySqlDataSourceBox.class);
		else if (type.isOracle())
			ds = BeanBox.getBean(OracleDataSourceBox.class);
		else if (type.isMsSQLSERVER())
			ds = BeanBox.getBean(MsSqlServerDataSourceBox.class);
		Assert.assertNotNull(ds);
		DBMetaData meta = Dao.getDefaultContext().getMetaData();
		System.out.println(meta.getJdbcDriverName());
	}

}