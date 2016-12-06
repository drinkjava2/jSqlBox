package test.config;

import java.io.InputStream;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.DatabaseType;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class InitializeDatabase {

	public static void dropAndRecreateTables() {
		SqlBoxContext.configDefaultContext(SqlBoxConfig.class.getName(), "getSqlBoxContext");
		JBeanBoxConfig.initialize();

		Dao.dao().executeQuiet("drop table users");
		Dao.dao().executeQuiet("drop table users2");

		if (Dao.dao().getDatabaseType() == DatabaseType.MYSQL)
			executeResourceSQLs("/CreateMysqlDatabase.sql");
		if (Dao.dao().getDatabaseType() == DatabaseType.ORACLE)
			executeResourceSQLs("/CreateOracleDatabase.sql");
		Dao.dao().refreshMetaData();
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
