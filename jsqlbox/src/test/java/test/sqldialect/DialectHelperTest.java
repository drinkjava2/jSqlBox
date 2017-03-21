package test.sqldialect;

import java.util.List;

import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.DerbyTenSixDialect;
import org.hibernate.dialect.Dialect;
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
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.engine.spi.RowSelection;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.sqldialect.DialectHelper;

/**
 * This is for test sql dialect, not finished, SqlDialect will be a new tiny project
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class DialectHelperTest {

	@Test
	public void testDialectCodeBuilder() {
		System.out.println("testDialectHelper========================");
		RowSelection r = new RowSelection();
		r.setFirstRow(90);
		r.setMaxRows(100);
		r.setFetchSize(5);
		r.setTimeout(1000);

		List<Class<? extends Dialect>> dialects = DialectHelper.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = DialectHelper.buildDialectByName(class1);
			LimitHandler l = dia.getLimitHandler();
			try {
				System.out.println("Dialect=" + class1);
				String sql = l.processSql(
						"select a.col1 as c1, a.col2 as c2, b.col3 as b1 from atb a, btb b order by a.col1, b.col4", r);
				System.out.println("SQL=" + sql);
			} catch (Exception e) {
				System.out.println("ExceptionL=" + e.getMessage());
			}
		}
	}

	@Test
	public void testPreregisteredDialects() {
		System.out.println("testDialectHelper=======================");
		DialectResolver resolver = StandardDialectResolver.INSTANCE;
		Assert.assertEquals(HSQLDialect.class, DialectHelper.guessDialect("HSQL Database Engine", resolver).getClass());
		Assert.assertEquals(H2Dialect.class, DialectHelper.guessDialect("H2", resolver).getClass());
		Assert.assertEquals(MySQLDialect.class, DialectHelper.guessDialect("MySQL", resolver).getClass());
		Assert.assertEquals(MySQL5Dialect.class, DialectHelper.guessDialect("MySQL", 5, 0, resolver).getClass());
		Assert.assertEquals(PostgreSQL81Dialect.class, DialectHelper.guessDialect("PostgreSQL", resolver).getClass());
		Assert.assertEquals(PostgreSQL82Dialect.class,
				DialectHelper.guessDialect("PostgreSQL", 8, 2, resolver).getClass());
		Assert.assertEquals(PostgreSQL9Dialect.class,
				DialectHelper.guessDialect("PostgreSQL", 9, 0, resolver).getClass());
		Assert.assertEquals(PostgresPlusDialect.class,
				DialectHelper.guessDialect("EnterpriseDB", 9, 2, resolver).getClass());
		Assert.assertEquals(DerbyDialect.class, DialectHelper.guessDialect("Apache Derby", 10, 4, resolver).getClass());
		Assert.assertEquals(DerbyTenFiveDialect.class,
				DialectHelper.guessDialect("Apache Derby", 10, 5, resolver).getClass());
		Assert.assertEquals(DerbyTenSixDialect.class,
				DialectHelper.guessDialect("Apache Derby", 10, 6, resolver).getClass());
		Assert.assertEquals(DerbyTenSevenDialect.class,
				DialectHelper.guessDialect("Apache Derby", 11, 5, resolver).getClass());
		Assert.assertEquals(IngresDialect.class, DialectHelper.guessDialect("Ingres", resolver).getClass());
		Assert.assertEquals(IngresDialect.class, DialectHelper.guessDialect("ingres", resolver).getClass());
		Assert.assertEquals(IngresDialect.class, DialectHelper.guessDialect("INGRES", resolver).getClass());
		Assert.assertEquals(SQLServerDialect.class,
				DialectHelper.guessDialect("Microsoft SQL Server Database", resolver).getClass());
		Assert.assertEquals(SQLServerDialect.class,
				DialectHelper.guessDialect("Microsoft SQL Server", resolver).getClass());
		Assert.assertEquals(SybaseASE15Dialect.class,
				DialectHelper.guessDialect("Sybase SQL Server", resolver).getClass());
		Assert.assertEquals(SybaseASE15Dialect.class,
				DialectHelper.guessDialect("Adaptive Server Enterprise", resolver).getClass());
		Assert.assertEquals(SybaseAnywhereDialect.class,
				DialectHelper.guessDialect("Adaptive Server Anywhere", resolver).getClass());
		Assert.assertEquals(InformixDialect.class,
				DialectHelper.guessDialect("Informix Dynamic Server", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/NT", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/LINUX", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/6000", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/HPUX", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/SUN", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/LINUX390", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, DialectHelper.guessDialect("DB2/AIX64", resolver).getClass());
		Assert.assertEquals(DB2400Dialect.class, DialectHelper.guessDialect("DB2 UDB for AS/400", resolver).getClass());
		Assert.assertEquals(Oracle8iDialect.class, DialectHelper.guessDialect("Oracle", 8, resolver).getClass());
		Assert.assertEquals(Oracle9iDialect.class, DialectHelper.guessDialect("Oracle", 9, resolver).getClass());
		Assert.assertEquals(Oracle10gDialect.class, DialectHelper.guessDialect("Oracle", 10, resolver).getClass());
		Assert.assertEquals(Oracle10gDialect.class, DialectHelper.guessDialect("Oracle", 11, resolver).getClass());

	}
}