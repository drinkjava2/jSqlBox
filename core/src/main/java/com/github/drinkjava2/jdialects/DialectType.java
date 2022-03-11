/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects;

/**
 * DialectType is a enum list all dialects, if customized dialect, use
 * "Customized" type.
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public enum DialectType { //这个类没啥用，主要用于方便使用case遍历
	Customized, //
	DerbyDialect, OracleDialect, Oracle9Dialect, DamengDialect, GBaseDialect, AccessDialect, CobolDialect, DbfDialect, ExcelDialect, ParadoxDialect, SQLiteDialect, TextDialect, XMLDialect, Cache71Dialect, CUBRIDDialect, DataDirectOracle9Dialect, DB2390Dialect, DB2390V8Dialect, DB2400Dialect, DB297Dialect, DB2Dialect, DerbyTenFiveDialect, DerbyTenSevenDialect, DerbyTenSixDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, Informix10Dialect, InformixDialect, Ingres10Dialect, Ingres9Dialect, IngresDialect, InterbaseDialect, JDataStoreDialect, MariaDB102Dialect, MariaDB103Dialect, MariaDB10Dialect, MariaDB53Dialect, MariaDBDialect, MckoiDialect, MimerSQLDialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5Dialect, MySQL5InnoDBDialect, MySQL8Dialect, MySQLDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle10gDialect, Oracle12cDialect, Oracle8iDialect, Oracle9iDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, PostgreSQL9Dialect, PostgreSQLDialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SQLServerDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE157Dialect, SybaseASE15Dialect, SybaseDialect, Teradata14Dialect, TeradataDialect, TimesTenDialect;
	
    public static DialectType getDialectType(Dialect d) {
        DialectType diaType = null;
        try {
            diaType = DialectType.valueOf(d.getName());
        } catch (Exception e) {
            diaType = DialectType.Customized;
        }
        return diaType;
    }
}
