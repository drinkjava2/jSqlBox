package test.tinyjdbc;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

import test.config.JBeanBoxConfig.MsSqlServerDataSourceBox;
import test.config.JBeanBoxConfig.MySqlDataSourceBox;
import test.config.JBeanBoxConfig.OracleDataSourceBox;
import test.config.TestPrepare;

/**
 * This is to test TinyJDBC get meta data
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyJdbcGetMetaData {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDefaultContexts();
	}

	@Test
	public void getMysqlMetadata() {
		DatabaseType type = SqlBox.getDefaultDatabaseType();
		DataSource ds = null;
		if (type == DatabaseType.MYSQL)
			ds = BeanBox.getBean(MySqlDataSourceBox.class);
		if (type == DatabaseType.ORACLE)
			ds = BeanBox.getBean(OracleDataSourceBox.class);
		if (type == DatabaseType.MS_SQLSERVER)
			ds = BeanBox.getBean(MsSqlServerDataSourceBox.class);

		TinyDbMetaData meta = TinyJdbc.getMetaData(ds);
		System.out.println(meta.getJdbcDriverName());
	}

	@Test
	public void getOracleMetadata() {
		// DataSource ds = BeanBox.getBean(OracleDataSourceBox.class);
		// TinyDbMetaData meta = TinyJdbc.getMetaData(ds);
		// System.out.println(meta.getJdbcDriverName());
	}

}