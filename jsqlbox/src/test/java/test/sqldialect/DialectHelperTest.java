package test.sqldialect;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
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
import org.hibernate.dialect.TypeNames;
import org.hibernate.dialect.function.AnsiTrimEmulationFunction;
import org.hibernate.dialect.function.AnsiTrimFunction;
import org.hibernate.dialect.function.CastFunction;
import org.hibernate.dialect.function.CharIndexFunction;
import org.hibernate.dialect.function.ConvertFunction;
import org.hibernate.dialect.function.DerbyConcatFunction;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.PositionSubstringFunction;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StaticPrecisionFspTimestampFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.springsrc.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.PrepareTestContext;

/**
 * ========================== will move this into a new project =============================== This is for test sql
 * dialect, not finished, SqlDialect will be a new tiny project
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings({ "deprecation", "unchecked" })
public class DialectHelperTest {
	@Before
	public void setup() {
		System.out.println("=============Testing " + this.getClass().getName() + "================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@Test
	public void doBuild() {
		testPreregisteredDialects();// first test if Dialects exist
		transferPagination();// Save Pagination into DB
		transferTypeNames();// Save TypeNames & HibernateTypeNames into DB
		transferFunctions();//Save registered functions into DB
	}

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

	public void transferPagination() {
		String createSQL = "create table tb_pagination ("//
				+ "dialect varchar(100),"//
				+ "SupoortLimit varchar(10),"//
				+ "supportsLimitOffset varchar(10),"//
				+ "pagination1 varchar(800),"//
				+ "pagination2 varchar(800),"//
				+ "pagination3 varchar(800) "//
				+ ")";
		Dao.executeQuiet("drop table tb_pagination");
		Dao.execute(createSQL);
		exportDialectPaginations();
	}

	private static Dialect buildDialectByName(Class<?> dialect) {
		BootstrapServiceRegistry bootReg = new BootstrapServiceRegistryBuilder()
				.applyClassLoader(DialectHelper.class.getClassLoader()).build();
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder(bootReg).build();
		DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
		dialectFactory.injectServices((ServiceRegistryImplementor) registry);
		final Map<String, String> configValues = new HashMap<>();
		configValues.put(Environment.DIALECT, dialect.getName());
		return dialectFactory.buildDialect(configValues, null);
	}

	public void exportDialectPaginations() {
		System.out.println("exportDialectPaginations========================");
		RowSelection r = new RowSelection();
		r.setFirstRow(90);
		r.setMaxRows(100);
		r.setFetchSize(5);
		r.setTimeout(1000);
		List<Class<? extends Dialect>> dialects = DialectHelper.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			LimitHandler l = dia.getLimitHandler();
			try {
				String dialect = class1.getSimpleName();
				String supoortLimit = l.supportsLimit() + "";
				String supportsLimitOffset = l.supportsLimitOffset() + "";
				String pagination1 = "N/A";
				try {
					pagination1 = l.processSql("select a from b", r);
				} catch (Exception e) {
				}

				String pagination2 = "N/A";
				try {
					pagination2 = l.processSql("select * from b order by b.id", r);
				} catch (Exception e) {
				}

				String pagination3 = "N/A";
				try {
					pagination3 = l.processSql(
							"select a.col1 as c1, a.col2 as c2, b.col3 as b1 from atb a, btb b order by a.col1, b.col4",
							r);
				} catch (Exception e) {
				}
				Dao.executeInsert("insert into tb_pagination ("//
						+ "dialect ," + empty(dialect)//
						+ "SupoortLimit ," + empty(supoortLimit)//
						+ "supportsLimitOffset ," + empty(supportsLimitOffset)//
						+ "pagination1  ," + empty(pagination1)//
						+ "pagination2  ," + empty(pagination2)//
						+ "pagination3) " + empty(pagination3)//
						+ valuesAndQuestions());
			} finally {
				SqlHelper.clear();
			}
		}
	}

	public void transferTypeNames() {
		String createSQL = "create table tb_typeNames ("//
				+ "line integer,"//
				+ "dialect varchar(100),"//
				+ "Types_BIT varchar(300),"//
				+ "Types_TINYINT varchar(300),"//
				+ "Types_SMALLINT varchar(300),"//
				+ "Types_INTEGER varchar(300),"//
				+ "Types_BIGINT varchar(300),"//
				+ "Types_FLOAT varchar(300),"//
				+ "Types_REAL varchar(300),"//
				+ "Types_DOUBLE varchar(300),"//
				+ "Types_NUMERIC varchar(300),"//
				+ "Types_DECIMAL varchar(300),"//
				+ "Types_CHAR varchar(300),"//
				+ "Types_VARCHAR varchar(300),"//
				+ "Types_LONGVARCHAR varchar(300),"//
				+ "Types_DATE varchar(300),"//
				+ "Types_TIME varchar(300),"//
				+ "Types_TIMESTAMP varchar(300),"//
				+ "Types_BINARY varchar(300),"//
				+ "Types_VARBINARY varchar(300),"//
				+ "Types_LONGVARBINARY varchar(300),"//
				+ "Types_NULL varchar(300),"//
				+ "Types_OTHER varchar(300),"//
				+ "Types_JAVA_OBJECT varchar(300),"//
				+ "Types_DISTINCT varchar(300),"//
				+ "Types_STRUCT varchar(300),"//
				+ "Types_ARRAY varchar(300),"//
				+ "Types_BLOB varchar(300),"//
				+ "Types_CLOB varchar(300),"//
				+ "Types_REF varchar(300),"//
				+ "Types_DATALINK varchar(300),"//
				+ "Types_BOOLEAN varchar(300),"//
				+ "Types_ROWID varchar(300),"//
				+ "Types_NCHAR varchar(300),"//
				+ "Types_NVARCHAR varchar(300),"//
				+ "Types_LONGNVARCHAR varchar(300),"//
				+ "Types_NCLOB varchar(300),"//
				+ "Types_SQLXML varchar(300),"//
				+ "Types_REF_CURSOR varchar(300),"//
				+ "Types_TIME_WITH_TIMEZONE varchar(300),"//
				+ "Types_TIMESTAMP_WITH_TIMEZONE varchar(300) "//
				+ ")";
		Dao.executeQuiet("drop table tb_typeNames");
		Dao.execute(createSQL);
		exportDialectTypeNames();
	}

	public void exportDialectTypeNames() {
		int line = 0;
		System.out.println("exportDialectTypeNames========================");
		List<Class<? extends Dialect>> dialects = DialectHelper.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			TypeNames t = (TypeNames) findFieldObject(dia, "typeNames");
			String insertSQL = "insert into tb_typeNames ("//
					+ "line," + empty(++line)//
					+ "dialect," + empty(dia.getClass().getSimpleName())//
					+ "Types_BIT," + empty(getTypeNameDefString(t, (Types.BIT)))//
					+ "Types_TINYINT," + empty(getTypeNameDefString(t, (Types.TINYINT)))//
					+ "Types_SMALLINT," + empty(getTypeNameDefString(t, (Types.SMALLINT)))//
					+ "Types_INTEGER," + empty(getTypeNameDefString(t, (Types.INTEGER)))//
					+ "Types_BIGINT," + empty(getTypeNameDefString(t, (Types.BIGINT)))//
					+ "Types_FLOAT," + empty(getTypeNameDefString(t, (Types.FLOAT)))//
					+ "Types_REAL," + empty(getTypeNameDefString(t, (Types.REAL)))//
					+ "Types_DOUBLE," + empty(getTypeNameDefString(t, (Types.DOUBLE)))//
					+ "Types_NUMERIC," + empty(getTypeNameDefString(t, (Types.NUMERIC)))//
					+ "Types_DECIMAL," + empty(getTypeNameDefString(t, (Types.DECIMAL)))//
					+ "Types_CHAR," + empty(getTypeNameDefString(t, (Types.CHAR)))//
					+ "Types_VARCHAR," + empty(getTypeNameDefString(t, (Types.VARCHAR)))//
					+ "Types_LONGVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGVARCHAR)))//
					+ "Types_DATE," + empty(getTypeNameDefString(t, (Types.DATE)))//
					+ "Types_TIME," + empty(getTypeNameDefString(t, (Types.TIME)))//
					+ "Types_TIMESTAMP," + empty(getTypeNameDefString(t, (Types.TIMESTAMP)))//
					+ "Types_BINARY," + empty(getTypeNameDefString(t, (Types.BINARY)))//
					+ "Types_VARBINARY," + empty(getTypeNameDefString(t, (Types.VARBINARY)))//
					+ "Types_LONGVARBINARY," + empty(getTypeNameDefString(t, (Types.LONGVARBINARY)))//
					+ "Types_NULL," + empty(getTypeNameDefString(t, (Types.NULL)))//
					+ "Types_OTHER," + empty(getTypeNameDefString(t, (Types.OTHER)))//
					+ "Types_JAVA_OBJECT," + empty(getTypeNameDefString(t, (Types.JAVA_OBJECT)))//
					+ "Types_DISTINCT," + empty(getTypeNameDefString(t, (Types.DISTINCT)))//
					+ "Types_STRUCT," + empty(getTypeNameDefString(t, (Types.STRUCT)))//
					+ "Types_ARRAY," + empty(getTypeNameDefString(t, (Types.ARRAY)))//
					+ "Types_BLOB," + empty(getTypeNameDefString(t, (Types.BLOB)))//
					+ "Types_CLOB," + empty(getTypeNameDefString(t, (Types.CLOB)))//
					+ "Types_REF," + empty(getTypeNameDefString(t, (Types.REF)))//
					+ "Types_DATALINK," + empty(getTypeNameDefString(t, (Types.DATALINK)))//
					+ "Types_BOOLEAN," + empty(getTypeNameDefString(t, (Types.BOOLEAN)))//
					+ "Types_ROWID," + empty(getTypeNameDefString(t, (Types.ROWID)))//
					+ "Types_NCHAR," + empty(getTypeNameDefString(t, (Types.NCHAR)))//
					+ "Types_NVARCHAR," + empty(getTypeNameDefString(t, (Types.NVARCHAR)))//
					+ "Types_LONGNVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGNVARCHAR)))//
					+ "Types_NCLOB," + empty(getTypeNameDefString(t, (Types.NCLOB)))//
					+ "Types_SQLXML," + empty(getTypeNameDefString(t, (Types.SQLXML)))//
					+ "Types_REF_CURSOR," + empty(getTypeNameDefString(t, (Types.REF_CURSOR)))//
					+ "Types_TIME_WITH_TIMEZONE," + empty(getTypeNameDefString(t, (Types.TIME_WITH_TIMEZONE)))//
					+ "Types_TIMESTAMP_WITH_TIMEZONE" + empty(getTypeNameDefString(t, (Types.TIMESTAMP_WITH_TIMEZONE)))//
					+ ")" //
					+ valuesAndQuestions();
			Dao.executeInsert(insertSQL);

			t = (TypeNames) findFieldObject(dia, "hibernateTypeNames");// Hibernate Type
			insertSQL = "insert into tb_typeNames ("//
					+ "line," + empty(++line)//
					+ "dialect," + empty("Hib_" + dia.getClass().getSimpleName())//
					+ "Types_BIT," + empty(getTypeNameDefString(t, (Types.BIT)))//
					+ "Types_TINYINT," + empty(getTypeNameDefString(t, (Types.TINYINT)))//
					+ "Types_SMALLINT," + empty(getTypeNameDefString(t, (Types.SMALLINT)))//
					+ "Types_INTEGER," + empty(getTypeNameDefString(t, (Types.INTEGER)))//
					+ "Types_BIGINT," + empty(getTypeNameDefString(t, (Types.BIGINT)))//
					+ "Types_FLOAT," + empty(getTypeNameDefString(t, (Types.FLOAT)))//
					+ "Types_REAL," + empty(getTypeNameDefString(t, (Types.REAL)))//
					+ "Types_DOUBLE," + empty(getTypeNameDefString(t, (Types.DOUBLE)))//
					+ "Types_NUMERIC," + empty(getTypeNameDefString(t, (Types.NUMERIC)))//
					+ "Types_DECIMAL," + empty(getTypeNameDefString(t, (Types.DECIMAL)))//
					+ "Types_CHAR," + empty(getTypeNameDefString(t, (Types.CHAR)))//
					+ "Types_VARCHAR," + empty(getTypeNameDefString(t, (Types.VARCHAR)))//
					+ "Types_LONGVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGVARCHAR)))//
					+ "Types_DATE," + empty(getTypeNameDefString(t, (Types.DATE)))//
					+ "Types_TIME," + empty(getTypeNameDefString(t, (Types.TIME)))//
					+ "Types_TIMESTAMP," + empty(getTypeNameDefString(t, (Types.TIMESTAMP)))//
					+ "Types_BINARY," + empty(getTypeNameDefString(t, (Types.BINARY)))//
					+ "Types_VARBINARY," + empty(getTypeNameDefString(t, (Types.VARBINARY)))//
					+ "Types_LONGVARBINARY," + empty(getTypeNameDefString(t, (Types.LONGVARBINARY)))//
					+ "Types_NULL," + empty(getTypeNameDefString(t, (Types.NULL)))//
					+ "Types_OTHER," + empty(getTypeNameDefString(t, (Types.OTHER)))//
					+ "Types_JAVA_OBJECT," + empty(getTypeNameDefString(t, (Types.JAVA_OBJECT)))//
					+ "Types_DISTINCT," + empty(getTypeNameDefString(t, (Types.DISTINCT)))//
					+ "Types_STRUCT," + empty(getTypeNameDefString(t, (Types.STRUCT)))//
					+ "Types_ARRAY," + empty(getTypeNameDefString(t, (Types.ARRAY)))//
					+ "Types_BLOB," + empty(getTypeNameDefString(t, (Types.BLOB)))//
					+ "Types_CLOB," + empty(getTypeNameDefString(t, (Types.CLOB)))//
					+ "Types_REF," + empty(getTypeNameDefString(t, (Types.REF)))//
					+ "Types_DATALINK," + empty(getTypeNameDefString(t, (Types.DATALINK)))//
					+ "Types_BOOLEAN," + empty(getTypeNameDefString(t, (Types.BOOLEAN)))//
					+ "Types_ROWID," + empty(getTypeNameDefString(t, (Types.ROWID)))//
					+ "Types_NCHAR," + empty(getTypeNameDefString(t, (Types.NCHAR)))//
					+ "Types_NVARCHAR," + empty(getTypeNameDefString(t, (Types.NVARCHAR)))//
					+ "Types_LONGNVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGNVARCHAR)))//
					+ "Types_NCLOB," + empty(getTypeNameDefString(t, (Types.NCLOB)))//
					+ "Types_SQLXML," + empty(getTypeNameDefString(t, (Types.SQLXML)))//
					+ "Types_REF_CURSOR," + empty(getTypeNameDefString(t, (Types.REF_CURSOR)))//
					+ "Types_TIME_WITH_TIMEZONE," + empty(getTypeNameDefString(t, (Types.TIME_WITH_TIMEZONE)))//
					+ "Types_TIMESTAMP_WITH_TIMEZONE" + empty(getTypeNameDefString(t, (Types.TIMESTAMP_WITH_TIMEZONE)))//
					+ ")" //
					+ valuesAndQuestions();
			Dao.executeInsert(insertSQL);

			Dao.executeInsert("insert into tb_typeNames (line)" + empty(++line) + valuesAndQuestions());
		}
	}

	private static String getTypeNameDefString(TypeNames t, int typeCode) {
		String s = "N/A";
		try {
			s = t.get(typeCode);
		} catch (Exception e) {
		}
		Map<Integer, Map<Long, String>> weighted = (Map<Integer, Map<Long, String>>) findFieldObject(t, "weighted");
		Map<Long, String> map = weighted.get(typeCode);
		if (map != null && map.size() > 0) {
			for (Map.Entry<Long, String> entry : map.entrySet()) {
				s += "|" + entry.getKey() + "," + entry.getValue();
			}
		}
		return s;
	}

	private static Object findFieldObject(Object obj, String fieldname) {
		try {
			Field field = ReflectionUtils.findField(obj.getClass(), fieldname);
			field.setAccessible(true);
			Object o = field.get(obj);
			return o;
		} catch (Exception e) {
			return null;
		}
	}

	public void transferFunctions() {
		String createSQL = "create table tb_functions ("//
				+ "fn_name varchar(200)  " + ", constraint const_fn_name primary key (fn_name))";
		Dao.executeQuiet("drop table tb_functions");
		Dao.execute(createSQL);
		exportDialectFunctions();
		// Map<String, SQLFunction> sqlFunctions
	}

	public void exportDialectFunctions() {
		System.out.println("exportDialectFunctions========================");
		List<Class<? extends Dialect>> dialects = DialectHelper.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			String diaName = dia.getClass().getSimpleName();
			Dao.execute("alter table tb_functions add  " + diaName + " varchar(200)");
			Dao.executeQuiet("insert into tb_functions (" + diaName + ", fn_name) values(?,?)", empty(diaName),
					empty("FUNCTIONS"));
			Map<String, SQLFunction> sqlFunctions = (Map<String, SQLFunction>) findFieldObject(dia, "sqlFunctions");

			for (Entry<String, SQLFunction> entry : sqlFunctions.entrySet()) {
				String fn_name = entry.getKey();
				Dao.executeQuiet("insert into tb_functions (" + diaName + ", fn_name) values(?,?)", empty("---"),
						empty(fn_name));

				SQLFunction fun = entry.getValue();
				@SuppressWarnings("rawtypes")
				Class funClass = fun.getClass();
				String sqlName = fun.toString();

				if (VarArgsSQLFunction.class.equals(funClass)) {
					sqlName = "" + findFieldObject(fun, "begin") + findFieldObject(fun, "sep")
							+ findFieldObject(fun, "end");
				} else if (NoArgSQLFunction.class.equals(funClass)) {
					sqlName = "" + findFieldObject(fun, "name");
					if ((Boolean) findFieldObject(fun, "hasParenthesesIfNoArguments"))
						sqlName += "()";
				} else if (ConvertFunction.class.equals(funClass)) {
					sqlName = "*convert";
				} else if (CastFunction.class.equals(funClass)) {
					sqlName = "*cast";
				} else if (NvlFunction.class.equals(funClass)) {
					sqlName = "*nul";
				} else if (AnsiTrimFunction.class.equals(funClass)) {
					sqlName = "*trim";
				} else if (DerbyConcatFunction.class.equals(funClass)) {
					sqlName = "*||";
				} else if (StaticPrecisionFspTimestampFunction.class.equals(funClass)) {
					sqlName = "*||";
				} else if (PositionSubstringFunction.class.equals(funClass)) {
					sqlName = "*position/substring";
				} else if (AnsiTrimEmulationFunction.class.equals(funClass)) {
					sqlName = "*TrimEmulation";
				} else if (CharIndexFunction.class.equals(funClass)) {
					sqlName = "*charindex";
				}
				Dao.execute("update tb_functions set " + diaName + "=? where fn_name=?", empty(sqlName),
						empty(fn_name));
			}
		}
	}

}