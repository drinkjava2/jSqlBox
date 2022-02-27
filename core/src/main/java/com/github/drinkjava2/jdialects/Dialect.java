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

import java.sql.Connection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.converter.BasicJavaConverter;
import com.github.drinkjava2.jdialects.converter.JavaConverter;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jlogs.Log;
import com.github.drinkjava2.jlogs.LogFactory;

/**
 * jDialects is a small Java tool collect all databases' dialect, most data are
 * extracted from Hibernate, usually jDialects is used for build pagination SQL
 * and DDL SQL for cross-databases developing. Currently jDialects support ~70
 * database dialects. It has no any 3rd party dependency, run on JDK1.6 or
 * above.
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class Dialect {
	/** Use Derby other dialects instead */
	@Deprecated
	public static final Dialect DerbyDialect = new Dialect("DerbyDialect");

	/** Use Oracle8iDialect instead */
	@Deprecated
	public static final Dialect OracleDialect = new Dialect("OracleDialect");

	/** Use Oracle9i instead */
	@Deprecated
	public static final Dialect Oracle9Dialect = new Dialect("Oracle9Dialect");

	// below added by hand
	public static final Dialect DamengDialect = new Dialect("DamengDialect");
	public static final Dialect GBaseDialect = new Dialect("GBaseDialect");

	// Below dialects found on Internet
	public static final Dialect AccessDialect = new Dialect("AccessDialect");
	public static final Dialect CobolDialect = new Dialect("CobolDialect");
	public static final Dialect DbfDialect = new Dialect("DbfDialect");
	public static final Dialect ExcelDialect = new Dialect("ExcelDialect");
	public static final Dialect ParadoxDialect = new Dialect("ParadoxDialect");
	public static final Dialect SQLiteDialect = new Dialect("SQLiteDialect");
	public static final Dialect TextDialect = new Dialect("TextDialect");
	public static final Dialect XMLDialect = new Dialect("XMLDialect");

	// Below dialects imported from Hibernate
	public static final Dialect Cache71Dialect = new Dialect("Cache71Dialect");
	public static final Dialect CUBRIDDialect = new Dialect("CUBRIDDialect");
	public static final Dialect DataDirectOracle9Dialect = new Dialect("DataDirectOracle9Dialect");
	public static final Dialect DB2390Dialect = new Dialect("DB2390Dialect");
	public static final Dialect DB2390V8Dialect = new Dialect("DB2390V8Dialect");
	public static final Dialect DB2400Dialect = new Dialect("DB2400Dialect");
	public static final Dialect DB297Dialect = new Dialect("DB297Dialect");
	public static final Dialect DB2Dialect = new Dialect("DB2Dialect");
	public static final Dialect DerbyTenFiveDialect = new Dialect("DerbyTenFiveDialect");
	public static final Dialect DerbyTenSevenDialect = new Dialect("DerbyTenSevenDialect");
	public static final Dialect DerbyTenSixDialect = new Dialect("DerbyTenSixDialect");
	public static final Dialect FirebirdDialect = new Dialect("FirebirdDialect");
	public static final Dialect FrontBaseDialect = new Dialect("FrontBaseDialect");
	public static final Dialect H2Dialect = new Dialect("H2Dialect");
	public static final Dialect HANAColumnStoreDialect = new Dialect("HANAColumnStoreDialect");
	public static final Dialect HANARowStoreDialect = new Dialect("HANARowStoreDialect");
	public static final Dialect HSQLDialect = new Dialect("HSQLDialect");
	public static final Dialect Informix10Dialect = new Dialect("Informix10Dialect");
	public static final Dialect InformixDialect = new Dialect("InformixDialect");
	public static final Dialect Ingres10Dialect = new Dialect("Ingres10Dialect");
	public static final Dialect Ingres9Dialect = new Dialect("Ingres9Dialect");
	public static final Dialect IngresDialect = new Dialect("IngresDialect");
	public static final Dialect InterbaseDialect = new Dialect("InterbaseDialect");
	public static final Dialect JDataStoreDialect = new Dialect("JDataStoreDialect");
	public static final Dialect MariaDB102Dialect = new Dialect("MariaDB102Dialect");
	public static final Dialect MariaDB103Dialect = new Dialect("MariaDB103Dialect");
	public static final Dialect MariaDB10Dialect = new Dialect("MariaDB10Dialect");
	public static final Dialect MariaDB53Dialect = new Dialect("MariaDB53Dialect");
	public static final Dialect MariaDBDialect = new Dialect("MariaDBDialect");
	public static final Dialect MckoiDialect = new Dialect("MckoiDialect");
	public static final Dialect MimerSQLDialect = new Dialect("MimerSQLDialect");
	public static final Dialect MySQL55Dialect = new Dialect("MySQL55Dialect");
	public static final Dialect MySQL57Dialect = new Dialect("MySQL57Dialect");
	public static final Dialect MySQL57InnoDBDialect = new Dialect("MySQL57InnoDBDialect");
	public static final Dialect MySQL5Dialect = new Dialect("MySQL5Dialect");
	public static final Dialect MySQL5InnoDBDialect = new Dialect("MySQL5InnoDBDialect");
	public static final Dialect MySQL8Dialect = new Dialect("MySQL8Dialect");
	public static final Dialect MySQLDialect = new Dialect("MySQLDialect");
	public static final Dialect MySQLInnoDBDialect = new Dialect("MySQLInnoDBDialect");
	public static final Dialect MySQLMyISAMDialect = new Dialect("MySQLMyISAMDialect");
	public static final Dialect Oracle10gDialect = new Dialect("Oracle10gDialect");
	public static final Dialect Oracle12cDialect = new Dialect("Oracle12cDialect");
	public static final Dialect Oracle8iDialect = new Dialect("Oracle8iDialect");
	public static final Dialect Oracle9iDialect = new Dialect("Oracle9iDialect");
	public static final Dialect PointbaseDialect = new Dialect("PointbaseDialect");
	public static final Dialect PostgresPlusDialect = new Dialect("PostgresPlusDialect");
	public static final Dialect PostgreSQL81Dialect = new Dialect("PostgreSQL81Dialect");
	public static final Dialect PostgreSQL82Dialect = new Dialect("PostgreSQL82Dialect");
	public static final Dialect PostgreSQL91Dialect = new Dialect("PostgreSQL91Dialect");
	public static final Dialect PostgreSQL92Dialect = new Dialect("PostgreSQL92Dialect");
	public static final Dialect PostgreSQL93Dialect = new Dialect("PostgreSQL93Dialect");
	public static final Dialect PostgreSQL94Dialect = new Dialect("PostgreSQL94Dialect");
	public static final Dialect PostgreSQL95Dialect = new Dialect("PostgreSQL95Dialect");
	public static final Dialect PostgreSQL9Dialect = new Dialect("PostgreSQL9Dialect");
	public static final Dialect PostgreSQLDialect = new Dialect("PostgreSQLDialect");
	public static final Dialect ProgressDialect = new Dialect("ProgressDialect");
	public static final Dialect RDMSOS2200Dialect = new Dialect("RDMSOS2200Dialect");
	public static final Dialect SAPDBDialect = new Dialect("SAPDBDialect");
	public static final Dialect SQLServer2005Dialect = new Dialect("SQLServer2005Dialect");
	public static final Dialect SQLServer2008Dialect = new Dialect("SQLServer2008Dialect");
	public static final Dialect SQLServer2012Dialect = new Dialect("SQLServer2012Dialect");
	public static final Dialect SQLServerDialect = new Dialect("SQLServerDialect");
	public static final Dialect Sybase11Dialect = new Dialect("Sybase11Dialect");
	public static final Dialect SybaseAnywhereDialect = new Dialect("SybaseAnywhereDialect");
	public static final Dialect SybaseASE157Dialect = new Dialect("SybaseASE157Dialect");
	public static final Dialect SybaseASE15Dialect = new Dialect("SybaseASE15Dialect");
	public static final Dialect SybaseDialect = new Dialect("SybaseDialect");
	public static final Dialect Teradata14Dialect = new Dialect("Teradata14Dialect");
	public static final Dialect TeradataDialect = new Dialect("TeradataDialect");
	public static final Dialect TimesTenDialect = new Dialect("TimesTenDialect");

	/** If set true will allow use reserved words in DDL, default value is false */
	private static Boolean globalAllowReservedWords = false;

	private static final Log logger = LogFactory.getLog(Dialect.class);

	/**
	 * If set true will output log for each paginate, translate, paginAndTranslate,
	 * toCreateDDL, toDropAndCreateDDL, toDropDDL method call, default value is
	 * false
	 */
	private static Boolean globalAllowShowSql = false;

	/** The SQL function prefix String, default value is null */
	private static String globalSqlFunctionPrefix = null;

	/** If disable, will use same SqlTemplate for first page pagination query */
	private static Boolean globalEnableTopLimitPagin = true;
	
	/** if not null, will use this JavaConverter to convert jdbc type to Java type */
	public static JavaConverter globalJdbcTypeConverter=new BasicJavaConverter();
	
	/** if not null, will use this NamingRule to convert entity name to database table and entity field to database column */
    public static NamingConversion globalNamingConversion = null;	

	public static final String NOT_SUPPORT = "NOT_SUPPORT";
	private static final String SKIP_ROWS = "$SKIP_ROWS";
	private static final String PAGESIZE = "$PAGESIZE";
	private static final String TOTAL_ROWS = "$TOTAL_ROWS";
	private static final String DISTINCT_TAG = "($DISTINCT)";
	public String sqlTemplate;
	public String topLimitTemplate;
	public String name;
	public DialectType type; // To support java6 switch
	public Map<Type, String> typeMappings = new EnumMap<Type, String>(Type.class);
	public Map<String, String> functions = new HashMap<String, String>();
	public DDLFeatures ddlFeatures = new DDLFeatures();// NOSONAR

    static {
        DialectTypeMappingTemplate.initTypeMappings();
        DialectFunctionTemplate.initFunctionTemplates();
    }
	   
    public Dialect(String name) {
        this.name = name;
        try {
            this.type = DialectType.valueOf(name);
        } catch (Exception e) {
            this.type = DialectType.Customized;
        }
        this.sqlTemplate = DialectPaginationTemplate.initializePaginSQLTemplate(this);
        this.topLimitTemplate = DialectPaginationTemplate.initializeTopLimitSqlTemplate(this);
        DDLFeatures.initDDLFeatures(this);
    }

	public static Dialect[] dialects = new Dialect[] { DerbyDialect, OracleDialect, Oracle9Dialect, DamengDialect,
	            GBaseDialect, AccessDialect, CobolDialect, DbfDialect, ExcelDialect, ParadoxDialect, SQLiteDialect,
	            TextDialect, XMLDialect, Cache71Dialect, CUBRIDDialect, DataDirectOracle9Dialect, DB2390Dialect,
	            DB2390V8Dialect, DB2400Dialect, DB297Dialect, DB2Dialect, DerbyTenFiveDialect, DerbyTenSevenDialect,
	            DerbyTenSixDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect,
	            HANARowStoreDialect, HSQLDialect, Informix10Dialect, InformixDialect, Ingres10Dialect, Ingres9Dialect,
	            IngresDialect, InterbaseDialect, JDataStoreDialect, MariaDB102Dialect, MariaDB103Dialect, MariaDB10Dialect,
	            MariaDB53Dialect, MariaDBDialect, MckoiDialect, MimerSQLDialect, MySQL55Dialect, MySQL57Dialect,
	            MySQL57InnoDBDialect, MySQL5Dialect, MySQL5InnoDBDialect, MySQL8Dialect, MySQLDialect, MySQLInnoDBDialect,
	            MySQLMyISAMDialect, Oracle10gDialect, Oracle12cDialect, Oracle8iDialect, Oracle9iDialect, PointbaseDialect,
	            PostgresPlusDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect,
	            PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, PostgreSQL9Dialect, PostgreSQLDialect,
	            ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLServer2005Dialect, SQLServer2008Dialect,
	            SQLServer2012Dialect, SQLServerDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE157Dialect,
	            SybaseASE15Dialect, SybaseDialect, Teradata14Dialect, TeradataDialect, TimesTenDialect };
	   
    static {
        DialectFunctionTemplate.initExtraFunctionTemplates();
        
        //Manual fix bugs some special dialect bugs
        H2Dialect.ddlFeatures.supportsIdentityColumns=false; //H2 from 2.x version does not support Identity function
    } 

	/** Use Dialect.dialects directly */
	@Deprecated
	public static Dialect[] values() {
		return dialects;
	}

	/**
	 * Guess Dialect by given connection, note:this method does not close connection
	 * 
	 * @param con
	 *            The JDBC Connection
	 * @return Dialect The Dialect intance, if can not guess out, return null
	 */
	public static Dialect guessDialect(Connection connection) {
		return GuessDialectUtils.guessDialect(connection);
	}

	/**
	 * Guess Dialect by given data source
	 * 
	 * @param datasource
	 * @return Dialect
	 */
	public static Dialect guessDialect(DataSource datasource) {
		return GuessDialectUtils.guessDialect(datasource);
	}

	/**
	 * Check if is current dialect or ANSI reserved word, if yes throw exception. if
	 * is other database's reserved word, log output a warning.
	 */
	private void checkIfReservedWord(String word, String... tableName) {
		if (ReservedDBWords.isReservedWord(word)) {
			String inTable = tableName.length > 0 ? "In table " + tableName[0] + ", " : "";
			String reservedForDB = ReservedDBWords.reservedForDB(word);
			if (ReservedDBWords.isReservedWord(this, word)) {
				if (Dialect.globalAllowReservedWords)
					logger.warn(inTable + "\"" + word + "\" is a reserved word of \"" + reservedForDB
							+ "\", should not use it as table, column, unique or index name");
				else
					DialectException.throwEX(inTable + "\"" + word + "\" is a reserved word of \"" + reservedForDB
							+ "\", should not use it as table, column, unique or index name. "
							+ "if you really want use this reserved word, call Dialect.setGlobalAllowReservedWords() at application starting.");
			} else {
				logger.warn(inTable + "\"" + word + "\" is a reserved word of other database \"" + reservedForDB
						+ "\", not recommend be used as table, column, unique or index name");
			}
		}
	}

	/**
	 * Check if a word or word array include current dialect or ANSI-SQL's reserved
	 * word, if yes throw exception. if belong to other database's reserved word,
	 * log output a warning. Otherwise return word itself or first word if is array
	 */
	public String checkReservedWords(String... words) {
		if (words == null || words.length == 0)
			return null;
		for (String word : words)
			checkIfReservedWord(word);
		return words[0];
	}

	/**
	 * Check if a word is current dialect or ANSI-SQL's reserved word, if yes throw
	 * exception. if is other database's reserved word, log output a warning.
	 * Otherwise return word itself.
	 */
	public String checkNotEmptyReservedWords(String word, String type, String tableName) {
		if (StrUtils.isEmpty(word))
			DialectException.throwEX(type + " can not be empty");
		checkIfReservedWord(word, tableName);
		return word;
	}

	/**
	 * Transfer columnModel to a real dialect's DDL definition String, lengths is
	 * optional for some types
	 */
	public String translateToDDLType(ColumnModel col) {// NOSONAR
		Type type = col.getColumnType();
		String value = this.typeMappings.get(type);
		if (StrUtils.isEmpty(value) || "N/A".equals(value) || "n/a".equals(value))
			DialectException.throwEX("Type \"" + type + "\" is not supported by dialect \"" + this + "\"");

		if (value.contains("|")) {
			// format example: varchar($l)<255|lvarchar($l)<32739|varchar($l)
			String[] typeTempls = StrUtils.split("|", value);
			for (String templ : typeTempls) {
				if (templ.contains("<")) {// varchar($l)<255
					String[] limitType = StrUtils.split("<", templ);
					if (col.getLength() > 0 && col.getLength() < Integer.parseInt(limitType[1]))// NOSONAR
						return replacePlaceHolders(type, limitType[0], col);
				} else {// varchar($l)
					return replacePlaceHolders(type, templ, col);
				}
			}
			return (String) DialectException
					.throwEX("Type \"" + type + "\" is not supported by dialect \"" + this + "\" of template:" + value);
		} else if (value.contains("$"))
			return replacePlaceHolders(type, value, col);
		else
			return value;
	}

	/**
	 * inside function
	 */
	private String replacePlaceHolders(Type type, String value, ColumnModel col) {
		String newValue = StrUtils.replace(value, "$l", String.valueOf(col.getLength()));
		if (newValue.contains("$p"))
			newValue = StrUtils.replace(newValue, "$p", String.valueOf(col.getPrecision()));
		if (newValue.contains("$s"))
			newValue = StrUtils.replace(newValue, "$s", String.valueOf(col.getScale()));
		return newValue;
	}

	/**
	 * An example tell users how to use a top limit SQL for a dialect
	 */
	private static String aTopLimitSqlExample(String template) {
		String result = StrUtils.replaceIgnoreCase(template, "$SQL", "select * from users order by userid");
		result = StrUtils.replaceIgnoreCase(result, "$BODY", "* from users order by userid");
		result = StrUtils.replaceIgnoreCase(result, " " + DISTINCT_TAG, "");
		result = StrUtils.replaceIgnoreCase(result, SKIP_ROWS, "0");
		result = StrUtils.replaceIgnoreCase(result, PAGESIZE, "10");
		result = StrUtils.replaceIgnoreCase(result, TOTAL_ROWS, "10");
		return result;
	}

	// ====================================================
	// ====================================================

	/** Paginate and Translate a SQL */
	public String paginAndTrans(int pageNumber, int pageSize, String... sql) {
		return pagin(pageNumber, pageSize, trans(sql));
	}

	public String trans(String... sql) {
		StringBuilder sb = new StringBuilder();
		for (String str : sql)
			sb.append(str);
		return DialectFunctionTranslator.instance.doTranslate(this, sb.toString());
	}

	public String pagin(int pageNumber, int pageSize, String sql) {// NOSONAR
		String result = null;
		DialectException.assureNotNull(sql, "sql string can not be null");
		String trimedSql = sql.trim();
		DialectException.assureNotEmpty(trimedSql, "sql string can not be empty");

		if (!StrUtils.startsWithIgnoreCase(trimedSql, "select "))
			return (String) DialectException.throwEX("SQL should start with \"select \".");
		String body = trimedSql.substring(7).trim();
		DialectException.assureNotEmpty(body, "SQL body can not be empty");

		int skipRows = (pageNumber - 1) * pageSize;
		int skipRowsPlus1 = skipRows + 1;
		int totalRows = pageNumber * pageSize;
		int totalRowsPlus1 = totalRows + 1;
		String useTemplate;
		if (globalEnableTopLimitPagin && skipRows == 0) {
			useTemplate = topLimitTemplate;
			if (SQLServer2012Dialect.equals(this) && !StrUtils.containsIgnoreCase(trimedSql, "order by "))
				useTemplate = SQLServer2005Dialect.topLimitTemplate;
		} else {
			useTemplate = sqlTemplate;
			if (SQLServer2012Dialect.equals(this) && !StrUtils.containsIgnoreCase(trimedSql, "order by "))
				useTemplate = SQLServer2005Dialect.sqlTemplate;
		}

		if (Dialect.NOT_SUPPORT.equals(useTemplate)) {
			if (!Dialect.NOT_SUPPORT.equals(this.topLimitTemplate))
				return (String) DialectException
						.throwEX("Dialect \"" + this + "\" only support top limit SQL, for example: \""
								+ aTopLimitSqlExample(this.topLimitTemplate) + "\"");
			return (String) DialectException.throwEX("Dialect \"" + this + "\" does not support physical pagination");
		}

		if (useTemplate.contains(DISTINCT_TAG)) {
			// if distinct template use non-distinct sql, delete distinct tag
			if (!StrUtils.startsWithIgnoreCase(body, "distinct "))
				useTemplate = StrUtils.replace(useTemplate, DISTINCT_TAG, "");
			else {
				// if distinct template use distinct sql, use it
				useTemplate = StrUtils.replace(useTemplate, DISTINCT_TAG, "distinct");
				body = body.substring(9);
			}
		}

		// if have $XXX tag, replaced by real values
		result = StrUtils.replaceIgnoreCase(useTemplate, SKIP_ROWS, String.valueOf(skipRows));
		result = StrUtils.replaceIgnoreCase(result, PAGESIZE, String.valueOf(pageSize));
		result = StrUtils.replaceIgnoreCase(result, TOTAL_ROWS, String.valueOf(totalRows));

		// now insert the customer's real full SQL here
		result = StrUtils.replace(result, "$SQL", trimedSql);

		// or only insert the body without "select "
		result = StrUtils.replace(result, "$BODY", body);
		if (getGlobalAllowShowSql())
			logger.info("Paginated sql: " + result);
		return result;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Dialect) obj).name);
	}

	/**
     * @return true if is MySql family
     */
    public boolean isFamily(String databaseName) {
        return StrUtils.startsWithIgnoreCase(this.toString(), databaseName);
    }
	
	/**
	 * @return true if is MySql family
	 */
	public boolean isMySqlFamily() {
		return isFamily("MySQL");
	}

	/**
	 * @return true if is Infomix family
	 */
	public boolean isInfomixFamily() {
		return isFamily("Infomix");
	}

	/**
	 * @return true if is Oracle family
	 */
	public boolean isOracleFamily() {
		return isFamily("Oracle");
	}

	/**
	 * @return true if is SQL Server family
	 */
	public boolean isSQLServerFamily() {
		return isFamily("SQLServer");
	}

	/**
	 * @return true if is H2 family
	 */
	public boolean isH2Family() {
		return isFamily("H2");
	}

	/**
	 * @return true if is Postgres family
	 */
	public boolean isPostgresFamily() {
		return isFamily("Postgres");
	}

	/**
	 * @return true if is Sybase family
	 */
	public boolean isSybaseFamily() {
		return isFamily("Sybase");
	}

	/**
	 * @return true if is DB2 family
	 */
	public boolean isDB2Family() {
		return isFamily("DB2");
	}

	/**
	 * @return true if is Derby family
	 */
	public boolean isDerbyFamily() {
		return isFamily("Derby");
	}

	// ===============================================
	// Below are new DDL methods
	// ===============================================

	/**
	 * Transfer entity classes to create DDL
	 */
	public String[] toCreateDDL(Class<?>... entityClasses) {
		return DDLCreateUtils.toCreateDDL(this, TableModelUtils.entity2ReadOnlyModels(entityClasses));
	}

	/**
	 * Transfer entity classes to create DDL
	 */
	public String[] toDropDDL(Class<?>... entityClasses) {
		return DDLDropUtils.toDropDDL(this, TableModelUtils.entity2ReadOnlyModels(entityClasses));
	}

	/**
	 * Transfer entity classes to drop and create DDL String array
	 */
	public String[] toDropAndCreateDDL(Class<?>... entityClasses) {
		return toDropAndCreateDDL(TableModelUtils.entity2ReadOnlyModels(entityClasses));
	}

	/**
	 * Transfer tables to create DDL
	 */
	public String[] toCreateDDL(TableModel... tables) {
		return DDLCreateUtils.toCreateDDL(this, tables);
	}

	/**
	 * Transfer tables to drop DDL
	 */
	public String[] toDropDDL(TableModel... tables) {
		return DDLDropUtils.toDropDDL(this, tables);
	}

	/**
	 * Transfer tables to drop and create DDL String array
	 */
	public String[] toDropAndCreateDDL(TableModel... tables) {
		String[] drop = DDLDropUtils.toDropDDL(this, tables);
		String[] create = DDLCreateUtils.toCreateDDL(this, tables);
		return StrUtils.joinStringArray(drop, create);
	}

	/**
	 * Build a "drop table xxxx " like DDL String according this dialect
	 */
	public String dropTableDDL(String tableName) {
		return ddlFeatures.dropTableString.replaceFirst("_TABLENAME", tableName);
	}

	/**
	 * Build a "drop sequence xxxx " like DDL String according this dialect
	 */
	public String dropSequenceDDL(String sequenceName) {
		if (DDLFeatures.isValidDDLTemplate(ddlFeatures.dropSequenceStrings))
			return StrUtils.replace(ddlFeatures.dropSequenceStrings, "_SEQNAME", sequenceName);
		else
			return (String) DialectException.throwEX("Dialect \"" + this
					+ "\" does not support drop sequence ddl, on sequence \"" + sequenceName + "\"");
	}

	/**
	 * Build a "alter table tableName drop foreign key fkeyName " like DDL String
	 * according this dialect
	 */
	public String dropFKeyDDL(String tableName, String fkeyName) {
		if (DDLFeatures.isValidDDLTemplate(ddlFeatures.dropForeignKeyString))
			return "alter table " + tableName + " " + ddlFeatures.dropForeignKeyString + " " + fkeyName;
		else
			return (String) DialectException.throwEX(
					"Dialect \"" + this + "\" does not support drop foreign key, on foreign key \"" + fkeyName + "\"");
	}

	/**
	 * Return next ID by given IdGenerator and NormalJdbcStyle instance
	 */
	public Object getNexID(IdGenerator idGenerator, NormalJdbcTool jdbc, Type dataType) {
		return idGenerator.getNextID(jdbc, this, dataType);
	}

	// getter & setter====
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DDLFeatures getDdlFeatures() {
		return ddlFeatures;
	}

	public static Boolean getGlobalAllowReservedWords() {
		return globalAllowReservedWords;
	}

	/** Note! this is a global method to set globalAllowReservedWords */
	public static void setGlobalAllowReservedWords(Boolean ifAllowReservedWords) {
		Dialect.globalAllowReservedWords = ifAllowReservedWords;
	}

	public static Boolean getGlobalAllowShowSql() {
		return globalAllowShowSql;
	}

	/** Note! this is a global method to set globalAllowShowSql */
	public static void setGlobalAllowShowSql(Boolean ifAllowShowSql) {
		Dialect.globalAllowShowSql = ifAllowShowSql;
	}

	public static String getGlobalSqlFunctionPrefix() {
		return globalSqlFunctionPrefix;
	}

	/** Note! this is a global method to set globalSqlFunctionPrefix */
	public static void setGlobalSqlFunctionPrefix(String sqlFunctionPrefix) {
		Dialect.globalSqlFunctionPrefix = sqlFunctionPrefix;
	}

	public static Boolean getGlobalEnableTopLimitPagin() {
		return globalEnableTopLimitPagin;
	}

	/** Note! this is a global method to set globalEnableTopLimitPagin */
	public static void setGlobalEnableTopLimitPagin(Boolean globalEnableTopLimitPagin) {
		Dialect.globalEnableTopLimitPagin = globalEnableTopLimitPagin;
	}

	public static JavaConverter getGlobalJdbcTypeConverter() {
		return globalJdbcTypeConverter;
	}

	/** Note! this is a global method to set globalJdbcTypeConverter */
	public static void setGlobalJdbcTypeConverter(JavaConverter globalJdbcTypeConverter) {
		Dialect.globalJdbcTypeConverter = globalJdbcTypeConverter;
	}

    public static NamingConversion getGlobalNamingConversion() {
        return globalNamingConversion;
    }

    /** Note! this is a global method to set globalNamingConversion */
    public static void setGlobalNamingConversion(NamingConversion globalNamingConversion) {
        Dialect.globalNamingConversion = globalNamingConversion;
    } 

}
