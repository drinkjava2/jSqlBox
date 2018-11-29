/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.function;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is unit test for jDialects.Dialect
 * 
 * @author Yong Z.
 *
 */
public class DialectTest {

	private static final String sql1 = "select distinct a.id, a.userName, a.userName as u2 from usertemp a where id>1 order by id, a.username";
	private static final String sql2 = "select * from users";
	private static final String sql3 = "select a.id, a.userName, a.userName as u2, b.c1 from usertemp a where id>? group by b.b1 order by id, a.username";
	private static final String sql4 = "select distinct top(?) * from users";

	@Test
	public void testPagination() {
		Dialect[] dialects = Dialect.values();
		for (Dialect dialect : dialects) {
			System.out.println("=========" + dialect + "==========");
			String result = "";
			try {
				System.out.println(dialect);
				result = dialect.pagin(1, 10, sql1);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			try {
				result = dialect.pagin(3, 10, sql1);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			try {
				result = dialect.pagin(1, 10, sql2);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			try {
				result = dialect.pagin(3, 10, sql2);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
		}
	}

	@Test
	public void testPagination2() {
		Dialect[] dialects = Dialect.values();
		for (Dialect dialect : dialects) {
			System.out.println("=========" + dialect + "==========");
			String result = "";
			try {
				result = dialect.pagin(1, 10, sql3);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
			try {
				result = dialect.pagin(3, 10, sql4);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
			try {
				result = dialect.pagin(1, 10, sql3);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
			try {
				result = dialect.pagin(3, 10, sql4);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
		}
	}

	// =======test guess dialects=======
	private static HikariDataSource buildH2Datasource() {
		HikariDataSource ds = new HikariDataSource();
		ds.addDataSourceProperty("cachePrepStmts", true);
		ds.addDataSourceProperty("prepStmtCacheSize", 250);
		ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		ds.addDataSourceProperty("useServerPrepStmts", true);
		ds.setMaximumPoolSize(3);
		ds.setConnectionTimeout(5000);
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}

	@Test
	public void testGuessDialectsByDatasource() {
		HikariDataSource ds = buildH2Datasource();
		String dialectName = Dialect.guessDialect(ds).toString();
		Assert.assertEquals("H2Dialect", dialectName);
		ds.close();
	}

	@Test
	public void testGuessDialectsByConnection() {
		HikariDataSource ds = buildH2Datasource();
		String dialectName = null;
		Connection con = null;
		try {
			con = ds.getConnection();
			dialectName = Dialect.guessDialect(con).toString();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals("H2Dialect", dialectName);
		ds.close();
	}
 
	@Test(expected = DialectException.class)
	public void testDDLTypeMapping4() {
		Dialect.setGlobalAllowReservedWords(false);
		Dialect.MySQL55Dialect.checkReservedWords("user");
	}

	@Test(expected = DialectException.class)
	public void testDDLTypeMapping5() {
		Dialect.setGlobalAllowReservedWords(false);
		Dialect.MySQL55Dialect.checkReservedWords("CHARACTER_LENGTH");
	}

	@Test
	public void testDDLTypeMapping6() {
		Dialect.SQLServer2005Dialect.checkReservedWords("MASTER_SSL_VERIFY_SERVER_CERT");
	}
	// =======test DDL Type Mapping method=======

	@Test
	public void dialectFamilyTest() {
		Assert.assertTrue(Dialect.DB2400Dialect.isDB2Family());
		Assert.assertFalse(Dialect.Oracle10gDialect.isDB2Family());

		Assert.assertTrue(Dialect.DerbyTenFiveDialect.isDerbyFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isDerbyFamily());

		Assert.assertTrue(Dialect.H2Dialect.isH2Family());
		Assert.assertFalse(Dialect.Oracle10gDialect.isH2Family());

		Assert.assertTrue(Dialect.MySQL5InnoDBDialect.isMySqlFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isMySqlFamily());

		Assert.assertTrue(Dialect.Oracle8iDialect.isOracleFamily());
		Assert.assertFalse(Dialect.SQLiteDialect.isOracleFamily());

		Assert.assertTrue(Dialect.PostgresPlusDialect.isPostgresFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isPostgresFamily());

		Assert.assertTrue(Dialect.SQLServer2005Dialect.isSQLServerFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isSQLServerFamily());

		Assert.assertTrue(Dialect.SybaseAnywhereDialect.isSybaseFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isSybaseFamily());
	}

	@Test
	public void testDropTable() {
		for (Dialect d : Dialect.values()) {
			String dropTable = d.dropTableDDL("testTable");
			System.out.println(dropTable);
			Assert.assertTrue(StrUtils.containsIgnoreCase(dropTable, "drop"));
			Assert.assertTrue(StrUtils.containsIgnoreCase(dropTable, "testTable"));
		}
	}
}
