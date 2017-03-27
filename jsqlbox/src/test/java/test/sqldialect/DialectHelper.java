/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.sqldialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.Cache71Dialect;
import org.hibernate.dialect.DB2390Dialect;
import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DataDirectOracle9Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.DerbyTenSixDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.FirebirdDialect;
import org.hibernate.dialect.FrontBaseDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HANAColumnStoreDialect;
import org.hibernate.dialect.HANARowStoreDialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.Informix10Dialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.Ingres10Dialect;
import org.hibernate.dialect.Ingres9Dialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.InterbaseDialect;
import org.hibernate.dialect.JDataStoreDialect;
import org.hibernate.dialect.MariaDB53Dialect;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MckoiDialect;
import org.hibernate.dialect.MimerSQLDialect;
import org.hibernate.dialect.MySQL55Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.MySQL57InnoDBDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.hibernate.dialect.MySQLMyISAMDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PointbaseDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.PostgreSQL91Dialect;
import org.hibernate.dialect.PostgreSQL92Dialect;
import org.hibernate.dialect.PostgreSQL93Dialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.PostgresPlusDialect;
import org.hibernate.dialect.ProgressDialect;
import org.hibernate.dialect.RDMSOS2200Dialect;
import org.hibernate.dialect.SAPDBDialect;
import org.hibernate.dialect.SQLServer2005Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.Sybase11Dialect;
import org.hibernate.dialect.SybaseASE157Dialect;
import org.hibernate.dialect.SybaseASE15Dialect;
import org.hibernate.dialect.SybaseAnywhereDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.Teradata14Dialect;
import org.hibernate.dialect.TeradataDialect;
import org.hibernate.dialect.TimesTenDialect;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfoSource;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Transfer Hibernate's dialect to build a universal and tiny pagination tool
 * 
 * dialects in Hibernate5.2.9:
 * 
 * <pre>
 * Cache71
 * CUBRID
 * DataDirectOracle9
 * DB2
 * DB2390
 * DB2400
 * Derby
 * DerbyTenFive
 * DerbyTenSeven
 * DerbyTenSix
 * Firebird
 * FrontBase
 * H2
 * HANAColumnStore
 * HANARowStore
 * HSQL
 * Informix
 * Informix10
 * Ingres
 * Ingres10
 * Ingres9
 * Interbase
 * JDataStore
 * MariaDB
 * MariaDB53
 * Mckoi
 * MimerSQL
 * MySQL
 * MySQL5
 * MySQL55
 * MySQL57
 * MySQL57InnoDB
 * MySQL5InnoDB
 * MySQLInnoDB
 * MySQLMyISAM
 * Oracle
 * Oracle10g
 * Oracle12c
 * Oracle8i
 * Oracle9
 * Oracle9i
 * Pointbase
 * PostgresPlus
 * PostgreSQL
 * PostgreSQL81
 * PostgreSQL82
 * PostgreSQL9
 * PostgreSQL91
 * PostgreSQL92
 * PostgreSQL93
 * PostgreSQL94
 * PostgreSQL95
 * Progress
 * RDMSOS2200
 * SAPDB
 * SQLServer
 * SQLServer2005
 * SQLServer2008
 * SQLServer2012
 * Sybase
 * Sybase11
 * SybaseAnywhere
 * SybaseASE15
 * SybaseASE157
 * Teradata
 * Teradata14
 * TimesTen
 * </pre>
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class DialectHelper {
	public static List<Class<? extends Dialect>> SUPPORTED_DIALECTS = new ArrayList<>();
	static {
		SUPPORTED_DIALECTS.add(Cache71Dialect.class);
		SUPPORTED_DIALECTS.add(CUBRIDDialect.class);
		SUPPORTED_DIALECTS.add(DataDirectOracle9Dialect.class);
		SUPPORTED_DIALECTS.add(DB2Dialect.class);
		SUPPORTED_DIALECTS.add(DB2390Dialect.class);
		SUPPORTED_DIALECTS.add(DB2400Dialect.class);
		SUPPORTED_DIALECTS.add(DerbyDialect.class);
		SUPPORTED_DIALECTS.add(DerbyTenFiveDialect.class);
		SUPPORTED_DIALECTS.add(DerbyTenSevenDialect.class);
		SUPPORTED_DIALECTS.add(DerbyTenSixDialect.class);
		SUPPORTED_DIALECTS.add(FirebirdDialect.class);
		SUPPORTED_DIALECTS.add(FrontBaseDialect.class);
		SUPPORTED_DIALECTS.add(H2Dialect.class);
		SUPPORTED_DIALECTS.add(HANAColumnStoreDialect.class);
		SUPPORTED_DIALECTS.add(HANARowStoreDialect.class);
		SUPPORTED_DIALECTS.add(HSQLDialect.class);
		SUPPORTED_DIALECTS.add(InformixDialect.class);
		SUPPORTED_DIALECTS.add(Informix10Dialect.class);
		SUPPORTED_DIALECTS.add(IngresDialect.class);
		SUPPORTED_DIALECTS.add(Ingres10Dialect.class);
		SUPPORTED_DIALECTS.add(Ingres9Dialect.class);
		SUPPORTED_DIALECTS.add(InterbaseDialect.class);
		SUPPORTED_DIALECTS.add(JDataStoreDialect.class);
		SUPPORTED_DIALECTS.add(MariaDBDialect.class);
		SUPPORTED_DIALECTS.add(MariaDB53Dialect.class);
		SUPPORTED_DIALECTS.add(MckoiDialect.class);
		SUPPORTED_DIALECTS.add(MimerSQLDialect.class);
		SUPPORTED_DIALECTS.add(MySQLDialect.class);
		SUPPORTED_DIALECTS.add(MySQL5Dialect.class);
		SUPPORTED_DIALECTS.add(MySQL55Dialect.class);
		SUPPORTED_DIALECTS.add(MySQL57Dialect.class);
		SUPPORTED_DIALECTS.add(MySQL57InnoDBDialect.class);
		SUPPORTED_DIALECTS.add(MySQL5InnoDBDialect.class);
		SUPPORTED_DIALECTS.add(MySQLInnoDBDialect.class);
		SUPPORTED_DIALECTS.add(MySQLMyISAMDialect.class);
		SUPPORTED_DIALECTS.add(OracleDialect.class);
		SUPPORTED_DIALECTS.add(Oracle10gDialect.class);
		SUPPORTED_DIALECTS.add(Oracle12cDialect.class);
		SUPPORTED_DIALECTS.add(Oracle8iDialect.class);
		SUPPORTED_DIALECTS.add(Oracle9Dialect.class);
		SUPPORTED_DIALECTS.add(Oracle9iDialect.class);
		SUPPORTED_DIALECTS.add(PointbaseDialect.class);
		SUPPORTED_DIALECTS.add(PostgresPlusDialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQLDialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL81Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL82Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL9Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL91Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL92Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL93Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL94Dialect.class);
		SUPPORTED_DIALECTS.add(PostgreSQL95Dialect.class);
		SUPPORTED_DIALECTS.add(ProgressDialect.class);
		SUPPORTED_DIALECTS.add(RDMSOS2200Dialect.class);
		SUPPORTED_DIALECTS.add(SAPDBDialect.class);
		SUPPORTED_DIALECTS.add(SQLServerDialect.class);
		SUPPORTED_DIALECTS.add(SQLServer2005Dialect.class);
		SUPPORTED_DIALECTS.add(SQLServer2008Dialect.class);
		SUPPORTED_DIALECTS.add(SQLServer2012Dialect.class);
		SUPPORTED_DIALECTS.add(SybaseDialect.class);
		SUPPORTED_DIALECTS.add(Sybase11Dialect.class);
		SUPPORTED_DIALECTS.add(SybaseAnywhereDialect.class);
		SUPPORTED_DIALECTS.add(SybaseASE15Dialect.class);
		SUPPORTED_DIALECTS.add(SybaseASE157Dialect.class);
		SUPPORTED_DIALECTS.add(TeradataDialect.class);
		SUPPORTED_DIALECTS.add(Teradata14Dialect.class);
		SUPPORTED_DIALECTS.add(TimesTenDialect.class);
	}

	/**
	 * Guess database type and build Dialect
	 * 
	 * @param databaseName
	 * @param resolver
	 * @return
	 */
	public static Dialect guessDialect(String databaseName, DialectResolver resolver) {
		return guessDialect(databaseName, -9999, resolver);
	}

	/**
	 * Guess database type and build Dialect
	 * 
	 * @param databaseName
	 * @param databaseMajorVersion
	 * @param resolver
	 * @return
	 */
	public static Dialect guessDialect(String databaseName, int databaseMajorVersion, DialectResolver resolver) {
		return guessDialect(databaseName, databaseMajorVersion, -9999, resolver);
	}

	/**
	 * Guess database type and build Dialect
	 * 
	 * @param databaseName
	 * @param majorVersion
	 * @param minorVersion
	 * @param resolver
	 * @return
	 */
	public static Dialect guessDialect(final String databaseName, final int majorVersion, final int minorVersion,
			DialectResolver resolver) {
		BootstrapServiceRegistry bootReg = new BootstrapServiceRegistryBuilder()
				.applyClassLoader(DialectHelper.class.getClassLoader()).build();
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder(bootReg).build();
		DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
		dialectFactory.injectServices((ServiceRegistryImplementor) registry);
		dialectFactory.setDialectResolver(resolver);
		return dialectFactory.buildDialect(new Properties(), new DialectResolutionInfoSource() {// NOSONAR
			@Override
			public DialectResolutionInfo getDialectResolutionInfo() {
				return DefaultDialectResolutionInfo.forDatabaseInfo(databaseName, majorVersion, minorVersion);
			}
		});
	}

}
