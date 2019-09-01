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
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.log.DialectLog;
import com.github.drinkjava2.jdialects.log.DialectLogFactory;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

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
public enum Dialect implements CommonDialect {
	/** Use Derby instead */
	@Deprecated
	DerbyDialect,

	/** Use Oracle8iDialect instead */
	@Deprecated
	OracleDialect,

	/** Use Oracle9i instead */
	@Deprecated
	Oracle9Dialect, //

	//below added by hand
	DamengDialect,// equal to InformixDialect
	GBaseDialect,// equal to Oracle8iDialect
	
	// Below dialects found on Internet
	AccessDialect, //
	CobolDialect, //
	DbfDialect, //
	ExcelDialect, //
	ParadoxDialect, //
	SQLiteDialect, //
	TextDialect, //
	XMLDialect, //

	// Below dialects imported from Hibernate
	Cache71Dialect,//
	CUBRIDDialect,//
	DataDirectOracle9Dialect,//
	DB2390Dialect,//
	DB2390V8Dialect,//
	DB2400Dialect,//
	DB297Dialect,//
	DB2Dialect,//
	DerbyTenFiveDialect,//
	DerbyTenSevenDialect,//
	DerbyTenSixDialect,//
	FirebirdDialect,//
	FrontBaseDialect,//
	H2Dialect,//
	HANAColumnStoreDialect,//
	HANARowStoreDialect,//
	HSQLDialect,//
	Informix10Dialect,//
	InformixDialect,//
	Ingres10Dialect,//
	Ingres9Dialect,//
	IngresDialect,//
	InterbaseDialect,//
	JDataStoreDialect,//
	MariaDB102Dialect,//
	MariaDB103Dialect,//
	MariaDB10Dialect,//
	MariaDB53Dialect,//
	MariaDBDialect,//
	MckoiDialect,//
	MimerSQLDialect,//
	MySQL55Dialect,//
	MySQL57Dialect,//
	MySQL57InnoDBDialect,//
	MySQL5Dialect,//
	MySQL5InnoDBDialect,//
	MySQL8Dialect,//
	MySQLDialect,//
	MySQLInnoDBDialect,//
	MySQLMyISAMDialect,//
	Oracle10gDialect,//
	Oracle12cDialect,//
	Oracle8iDialect,//
	Oracle9iDialect,//
	PointbaseDialect,//
	PostgresPlusDialect,//
	PostgreSQL81Dialect,//
	PostgreSQL82Dialect,//
	PostgreSQL91Dialect,//
	PostgreSQL92Dialect,//
	PostgreSQL93Dialect,//
	PostgreSQL94Dialect,//
	PostgreSQL95Dialect,//
	PostgreSQL9Dialect,//
	PostgreSQLDialect,//
	ProgressDialect,//
	RDMSOS2200Dialect,//
	SAPDBDialect,//
	SQLServer2005Dialect,//
	SQLServer2008Dialect,//
	SQLServer2012Dialect,//
	SQLServerDialect,//
	Sybase11Dialect,//
	SybaseAnywhereDialect,//
	SybaseASE157Dialect,//
	SybaseASE15Dialect,//
	SybaseDialect,//
	Teradata14Dialect,//
	TeradataDialect,//
	TimesTenDialect;//


	/** If set true will allow use reserved words in DDL, default value is false */
	private static Boolean globalAllowReservedWords = false;

	private static final DialectLog logger = DialectLogFactory.getLog(Dialect.class);

	/**
	 * If set true will output log for each paginate, translate, paginAndTranslate,
	 * toCreateDDL, toDropAndCreateDDL, toDropDDL method call, default value is
	 * false
	 */
	private static Boolean globalAllowShowSql = false;

	/** The SQL function prefix String, default value is null */
	private static String globalSqlFunctionPrefix = null;

	public static final String NOT_SUPPORT = "NOT_SUPPORT";
	private static final String SKIP_ROWS = "$SKIP_ROWS";
	private static final String PAGESIZE = "$PAGESIZE";
	private static final String TOTAL_ROWS = "$TOTAL_ROWS";
	private static final String DISTINCT_TAG = "($DISTINCT)";
	private String sqlTemplate = null;
	private String topLimitTemplate = null;
	protected final Map<Type, String> typeMappings = new EnumMap<Type, String>(Type.class);
	protected final Map<String, String> functions = new HashMap<String, String>();
	protected final DDLFeatures ddlFeatures = new DDLFeatures();// NOSONAR

	static {
		for (Dialect d : Dialect.values()) {
			d.sqlTemplate = DialectPaginationTemplate.initializePaginSQLTemplate(d);
			d.topLimitTemplate = DialectPaginationTemplate.initializeTopLimitSqlTemplate(d);
			DDLFeatures.initDDLFeatures(d, d.ddlFeatures);
		}
		DialectTypeMappingTemplate.initTypeMappings();
		DialectFunctionTemplate.initFunctionTemplates();
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
	 * Transfer com.github.drinkjava2.jdialects.Type to a real dialect's type
	 * definition DDL String, lengths is optional for some types
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

	// @formatter:off shut off eclipse's formatter

	// @formatter:on

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

	@Override
	public String trans(String... sql) {
		StringBuilder sb = new StringBuilder();
		for (String str : sql)
			sb.append(str);
		return DialectFunctionTranslator.instance.doTranslate(this, sb.toString());
	}

	@Override
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
		if (skipRows == 0) {
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

	/**
	 * @return true if is MySql family
	 */
	public boolean isMySqlFamily() {
		return this.toString().startsWith("MySQL");
	}

	/**
	 * @return true if is Infomix family
	 */
	public boolean isInfomixFamily() {
		return this.toString().startsWith("Infomix");
	}

	/**
	 * @return true if is Oracle family
	 */
	public boolean isOracleFamily() {
		return this.toString().startsWith("Oracle");
	}

	/**
	 * @return true if is SQL Server family
	 */
	public boolean isSQLServerFamily() {
		return this.toString().startsWith("SQLServer");
	}

	/**
	 * @return true if is H2 family
	 */
	public boolean isH2Family() {
		return H2Dialect.equals(this);
	}

	/**
	 * @return true if is Postgres family
	 */
	public boolean isPostgresFamily() {
		return this.toString().startsWith("Postgres");
	}

	/**
	 * @return true if is Sybase family
	 */
	public boolean isSybaseFamily() {
		return this.toString().startsWith("Sybase");
	}

	/**
	 * @return true if is DB2 family
	 */
	public boolean isDB2Family() {
		return this.toString().startsWith("DB2");
	}

	/**
	 * @return true if is Derby family
	 */
	public boolean isDerbyFamily() {
		return this.toString().startsWith("Derby");
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
	/** Get Type mapping features key-value Map of current dialect */
	public Map<Type, String> getTypeMappings() {
		return typeMappings;
	}

	/** Get DDL features of current dialect */
	public Map<String, String> getFunctions() {
		return functions;
	}

	/** Get DDL features of current dialect */
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

}
