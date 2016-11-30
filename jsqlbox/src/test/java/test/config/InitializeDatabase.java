package test.config;

import java.io.InputStream;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import test.config.JBeanBoxConfig.DataSourceBox;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class InitializeDatabase {

	public static void dropAndRecreateTables() {
		SqlBoxContext.resetDefaultContext();
		JBeanBoxConfig.initialize();
		try {
			Dao.dao().execute("drop table users");
			Dao.dao().execute("drop table users2");
		} catch (Exception e) {
			System.out.println("Exception found when drop tables");
			SqlBoxException.eatException(e);
		}
		ComboPooledDataSource pool = (ComboPooledDataSource) BeanBox.getBean(DataSourceBox.class);
		String driverClassName = pool.getDriverClass().toLowerCase();
		if (driverClassName.indexOf("mysql") != -1)
			executeResourceSQLs("/CreateMysqlDatabase.sql");
		else if (driverClassName.indexOf("oracle") != -1)
			executeResourceSQLs("/CreateOracleDatabase.sql");
	}

	private static void executeResourceSQLs(String sqlResourceFile) {
		InputStream in = InitializeDatabase.class.getResourceAsStream(sqlResourceFile);
		if (in == null)
			throw new SqlBoxException("Can not find SQL resource file " + sqlResourceFile + " in resources folder");
		Scanner sc = new Scanner(in);
		try {
			while (sc.useDelimiter(";").hasNext())
				Dao.dao().execute(sc.next());
		} finally {
			sc.close();
		}
	}

	@Test
	public void testCreateTables() {
		dropAndRecreateTables();
		Assert.assertEquals(0, (int) Dao.dao().queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) Dao.dao().queryForInteger("select count(*) from users2"));
	}

	public static void main(String[] args) {
		dropAndRecreateTables();
	}

}
