package test.function_test.pagination;

import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.DerbyTenSixDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.PostgresPlusDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseASE15Dialect;
import org.hibernate.dialect.SybaseAnywhereDialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.pagination.DialectHelper;

/**
 * This is for test Hibernate Dialects
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class DialectHelperTest {

	@Test
	public void testDialectHelper() {
		DialectHelper d = new DialectHelper("SQLServer2005");
		System.out.println(d.getDialect());
		testPreregisteredDialects(d);
	}

	public void testPreregisteredDialects(DialectHelper d) {
		DialectResolver resolver = StandardDialectResolver.INSTANCE;
		Assert.assertEquals(HSQLDialect.class, d.guessDialect("HSQL Database Engine", resolver).getClass());
		Assert.assertEquals(H2Dialect.class, d.guessDialect("H2", resolver).getClass());
		Assert.assertEquals(MySQLDialect.class, d.guessDialect("MySQL", resolver).getClass());
		Assert.assertEquals(MySQL5Dialect.class, d.guessDialect("MySQL", 5, 0, resolver).getClass());
		Assert.assertEquals(PostgreSQL81Dialect.class, d.guessDialect("PostgreSQL", resolver).getClass());
		Assert.assertEquals(PostgreSQL82Dialect.class, d.guessDialect("PostgreSQL", 8, 2, resolver).getClass());
		Assert.assertEquals(PostgreSQL9Dialect.class, d.guessDialect("PostgreSQL", 9, 0, resolver).getClass());
		Assert.assertEquals(PostgresPlusDialect.class, d.guessDialect("EnterpriseDB", 9, 2, resolver).getClass());
		Assert.assertEquals(DerbyDialect.class, d.guessDialect("Apache Derby", 10, 4, resolver).getClass());
		Assert.assertEquals(DerbyTenFiveDialect.class, d.guessDialect("Apache Derby", 10, 5, resolver).getClass());
		Assert.assertEquals(DerbyTenSixDialect.class, d.guessDialect("Apache Derby", 10, 6, resolver).getClass());
		Assert.assertEquals(DerbyTenSevenDialect.class, d.guessDialect("Apache Derby", 11, 5, resolver).getClass());
		Assert.assertEquals(IngresDialect.class, d.guessDialect("Ingres", resolver).getClass());
		Assert.assertEquals(IngresDialect.class, d.guessDialect("ingres", resolver).getClass());
		Assert.assertEquals(IngresDialect.class, d.guessDialect("INGRES", resolver).getClass());
		Assert.assertEquals(SQLServerDialect.class,
				d.guessDialect("Microsoft SQL Server Database", resolver).getClass());
		Assert.assertEquals(SQLServerDialect.class, d.guessDialect("Microsoft SQL Server", resolver).getClass());
		Assert.assertEquals(SybaseASE15Dialect.class, d.guessDialect("Sybase SQL Server", resolver).getClass());
		Assert.assertEquals(SybaseASE15Dialect.class,
				d.guessDialect("Adaptive Server Enterprise", resolver).getClass());
		Assert.assertEquals(SybaseAnywhereDialect.class,
				d.guessDialect("Adaptive Server Anywhere", resolver).getClass());
		Assert.assertEquals(InformixDialect.class, d.guessDialect("Informix Dynamic Server", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/NT", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/LINUX", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/6000", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/HPUX", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/SUN", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/LINUX390", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, d.guessDialect("DB2/AIX64", resolver).getClass());
		Assert.assertEquals(DB2400Dialect.class, d.guessDialect("DB2 UDB for AS/400", resolver).getClass());
		Assert.assertEquals(Oracle8iDialect.class, d.guessDialect("Oracle", 8, resolver).getClass());
		Assert.assertEquals(Oracle9iDialect.class, d.guessDialect("Oracle", 9, resolver).getClass());
		Assert.assertEquals(Oracle10gDialect.class, d.guessDialect("Oracle", 10, resolver).getClass());
		Assert.assertEquals(Oracle10gDialect.class, d.guessDialect("Oracle", 11, resolver).getClass());

	}
}