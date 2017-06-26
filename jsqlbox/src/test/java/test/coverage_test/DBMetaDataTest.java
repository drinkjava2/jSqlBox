package test.coverage_test;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;
import test.config.DataSourceConfig.H2DataSourceBox;
import test.config.DataSourceConfig.MsSqlServerDataSourceBox;
import test.config.DataSourceConfig.MySqlDataSourceBox;
import test.config.DataSourceConfig.OracleDataSourceBox;

/**
 * This is to test TinyJDBC get meta data
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DBMetaDataTest extends TestBase {

	@Test
	public void tinyJdbcGetMetadataTest() {
		DataSource ds = null;
		if (Dao.getDialect().isH2Family())
			ds = BeanBox.getBean(H2DataSourceBox.class);
		else if (Dao.getDialect().isMySqlFamily())
			ds = BeanBox.getBean(MySqlDataSourceBox.class);
		else if (Dao.getDialect().isOracleFamily())
			ds = BeanBox.getBean(OracleDataSourceBox.class);
		else if (Dao.getDialect().isMySqlFamily())
			ds = BeanBox.getBean(MsSqlServerDataSourceBox.class);
		Assert.assertNotNull(ds);
	}

}