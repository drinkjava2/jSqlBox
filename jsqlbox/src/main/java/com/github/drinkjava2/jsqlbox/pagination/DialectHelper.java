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

package com.github.drinkjava2.jsqlbox.pagination;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfoSource;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Use Hibernate's dialect to build a universal pagination tool to support all databases, shortage is imported lots
 * garbage libraries, advantage is no need re-invent wheels which Hibernate already have.
 * 
 * Support below dialects in Hibernate:
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
public class DialectHelper {
	private Dialect dialect;
	private StandardServiceRegistry registry;
	private DialectFactoryImpl dialectFactory;

	public DialectHelper(String dialectName) {
		final BootstrapServiceRegistry bootReg = new BootstrapServiceRegistryBuilder()
				.applyClassLoader(DialectHelper.class.getClassLoader()).build();
		registry = new StandardServiceRegistryBuilder(bootReg).build();
		dialectFactory = new DialectFactoryImpl();
		dialectFactory.injectServices((ServiceRegistryImplementor) registry);

		final Map<String, String> configValues = new HashMap<>();
		configValues.put(Environment.DIALECT, dialectName);

		dialect = dialectFactory.buildDialect(configValues, null);
	}

	public Dialect getDialect() {
		return dialect;
	}

	/**
	 * Guess database type and build Dialect
	 * 
	 * @param databaseName
	 * @param resolver
	 * @return
	 */
	public Dialect guessDialect(String databaseName, DialectResolver resolver) {
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
	public Dialect guessDialect(String databaseName, int databaseMajorVersion, DialectResolver resolver) {
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
	public Dialect guessDialect(final String databaseName, final int majorVersion, final int minorVersion,
			DialectResolver resolver) {
		dialectFactory.setDialectResolver(resolver);
		return dialectFactory.buildDialect(new Properties(), new DialectResolutionInfoSource() {// NOSONAR
			@Override
			public DialectResolutionInfo getDialectResolutionInfo() {
				return DefaultDialectResolutionInfo.forDatabaseInfo(databaseName, majorVersion, minorVersion);
			}
		});
	}

}
