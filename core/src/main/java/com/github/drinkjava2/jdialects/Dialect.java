/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.hibernatesrc.pagination.RowSelection;
import com.github.drinkjava2.jdialects.hibernatesrc.pagination.SQLServer2005LimitHandler;
import com.github.drinkjava2.jdialects.hibernatesrc.pagination.SQLServer2012LimitHandler;
import com.github.drinkjava2.jdialects.hibernatesrc.utils.StringHelper;
import com.github.drinkjava2.jdialects.id.IdGenerator;
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
	// Below dialects found on Internet
	SQLiteDialect, AccessDialect, ExcelDialect, TextDialect, ParadoxDialect, CobolDialect, XMLDialect, DbfDialect, // NOSONAR

	// Below dialects found in Hibernate 5.2.9

	/** Use Derby instead */
	@Deprecated
	DerbyDialect,

	/** Use Oracle8iDialect instead */
	@Deprecated
	OracleDialect,

	/** Use Oracle9i instead */
	@Deprecated
	Oracle9Dialect, //

	Cache71Dialect, CUBRIDDialect, DerbyTenFiveDialect, DataDirectOracle9Dialect, DB2Dialect, DB2390Dialect, DB2400Dialect, DerbyTenSevenDialect, DerbyTenSixDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, InformixDialect, Informix10Dialect, IngresDialect, Ingres10Dialect, Ingres9Dialect, InterbaseDialect, JDataStoreDialect, MariaDBDialect, MariaDB53Dialect, MckoiDialect, MimerSQLDialect, MySQLDialect, MySQL5Dialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5InnoDBDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle8iDialect, Oracle9iDialect, Oracle10gDialect, Oracle12cDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQLDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL9Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLServerDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SybaseDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE15Dialect, SybaseASE157Dialect, TeradataDialect, Teradata14Dialect, TimesTenDialect;

	/** If set true will allow use reserved words in DDL, default value is false */
	private static Boolean globalAllowReservedWords = false;

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
	private static final String SKIP_ROWS_PLUS1 = "$SKIP_ROWS_PLUS1";
	private static final String TOTAL_ROWS_PLUS1 = "$TOTAL_ROWS_PLUS1";
	private static final String DISTINCT_TAG = "($DISTINCT)";
	protected static final DialectLogger logger = DialectLogger.getLog(Dialect.class);
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
	 * Guess Dialect by given databaseName, major & minor version if have
	 * 
	 * @param databaseName
	 * @param majorVersionMinorVersion
	 * @return Dialect
	 */
	public static Dialect guessDialect(String databaseName, Object... majorVersionMinorVersion) {
		return GuessDialectUtils.guessDialect(databaseName, majorVersionMinorVersion);
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
	public String translateToDDLType(Type type, Integer... lengths) {// NOSONAR
		String value = this.typeMappings.get(type);
		if (StrUtils.isEmpty(value) || "N/A".equals(value) || "n/a".equals(value))
			DialectException.throwEX("Type \"" + type + "\" is not supported by dialect \"" + this + "\"");

		if (value.contains("|")) {
			// format example: varchar($l)<255|lvarchar($l)<32739|varchar($l)
			String[] mappings = StringHelper.split("|", value);

			for (String mapping : mappings) {
				if (mapping.contains("<")) {// varchar($l)<255
					String[] limitType = StringHelper.split("<", mapping);
					if (lengths.length > 0 && lengths[0] < Integer.parseInt(limitType[1]))// NOSONAR
						return putParamters(type, limitType[0], lengths);
				} else {// varchar($l)
					return putParamters(type, mapping, lengths);
				}
			}
		} else {
			if (value.contains("$")) {
				// always this order: $l, $p, $s
				return putParamters(type, value, lengths);
			} else
				return value;
		}
		return "";
	}

	// @formatter:off shut off eclipse's formatter

	// @formatter:on

	/**
	 * inside function
	 */
	private String putParamters(Type type, String value, Integer... lengths) {
		if (lengths.length < StrUtils.countMatches(value, '$'))
			DialectException.throwEX("In Dialect \"" + this + "\", Type \"" + type + "\" should have "
					+ StrUtils.countMatches(value, '$') + " parameters");
		int i = 0;
		String newValue = value;
		if (newValue.contains("$l"))
			newValue = StrUtils.replace(newValue, "$l", String.valueOf(lengths[i++]));
		if (newValue.contains("$p"))
			newValue = StrUtils.replace(newValue, "$p", String.valueOf(lengths[i++]));
		if (newValue.contains("$s"))
			newValue = StrUtils.replace(newValue, "$s", String.valueOf(lengths[i]));
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

	/**
	 * SQLServer is complex, don't want re-invent wheel, copy Hibernate's source
	 * code in this project to do the dirty job, that's why this project use LGPL
	 * license
	 */
	private static String processSQLServer(Dialect dialect, int pageNumber, int pageSize, String sql) {
		int skipRows = (pageNumber - 1) * pageSize;
		int totalRows = pageNumber * pageSize;

		RowSelection selection = new RowSelection(skipRows, totalRows);
		String result = null;
		switch (dialect) {
		case SQLServer2005Dialect:
		case SQLServer2008Dialect:
			result = new SQLServer2005LimitHandler().processSql(sql, selection);
			break;
		case SQLServer2012Dialect:
			result = new SQLServer2012LimitHandler().processSql(sql, selection);
			break;
		default:
		}
		result = StringHelper.replace(result, "__hibernate_row_nr__", "_ROW_NUM_");
		// Replace a special top tag
		result = StringHelper.replaceOnce(result, " $Top_Tag(?) ", " TOP(" + totalRows + ") ");
		result = StringHelper.replaceOnce(result, "_ROW_NUM_ >= ? AND _ROW_NUM_ < ?",
				"_ROW_NUM_ >= " + (skipRows + 1) + " AND _ROW_NUM_ < " + (totalRows + 1));
		result = StringHelper.replaceOnce(result, "offset ? rows fetch next ? rows only",
				"offset " + skipRows + " rows fetch next " + pageSize + " rows only");
		result = StringHelper.replaceOnce(result, "offset 0 rows fetch next ? rows only",
				"offset 0 rows fetch next " + pageSize + " rows only");

		if (StrUtils.isEmpty(result))
			DialectException.throwEX("Unexpected error, please report this bug");
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
		switch (this) {
		case SQLServer2005Dialect:
		case SQLServer2008Dialect:
		case SQLServer2012Dialect: {
			result = processSQLServer(this, pageNumber, pageSize, trimedSql);
			if (getGlobalAllowShowSql())
				logger.info("Paginated sql: " + result);
			return result;
		}
		default:
		}

		if (!StrUtils.startsWithIgnoreCase(trimedSql, "select "))
			return (String) DialectException.throwEX("SQL should start with \"select \".");
		String body = trimedSql.substring(7).trim();
		DialectException.assureNotEmpty(body, "SQL body can not be empty");

		int skipRows = (pageNumber - 1) * pageSize;
		int skipRowsPlus1 = skipRows + 1;
		int totalRows = pageNumber * pageSize;
		int totalRowsPlus1 = totalRows + 1;
		String useTemplate = this.sqlTemplate;

		// use simple limit ? template if offset is 0
		if (skipRows == 0)
			useTemplate = this.topLimitTemplate;

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
		result = StrUtils.replaceIgnoreCase(result, SKIP_ROWS_PLUS1, String.valueOf(skipRowsPlus1));
		result = StrUtils.replaceIgnoreCase(result, TOTAL_ROWS_PLUS1, String.valueOf(totalRowsPlus1));

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
		return DDLCreateUtils.toCreateDDL(this, TableModelUtils.entity2Models(entityClasses));
	}

	/**
	 * Transfer entity classes to create DDL
	 */
	public String[] toDropDDL(Class<?>... entityClasses) {
		return DDLDropUtils.toDropDDL(this, TableModelUtils.entity2Models(entityClasses));
	}

	/**
	 * Transfer entity classes to drop and create DDL String array
	 */
	public String[] toDropAndCreateDDL(Class<?>... entityClasses) {
		return toDropAndCreateDDL(TableModelUtils.entity2Models(entityClasses));
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
