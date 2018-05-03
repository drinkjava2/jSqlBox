/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL Type definitions
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class TypeUtils {// NOSONAR
	public static final String BIGINT = "BIGINT";
	public static final String BINARY = "BINARY";
	public static final String BIT = "BIT";
	public static final String BLOB = "BLOB";
	public static final String BOOLEAN = "BOOLEAN";
	public static final String CHAR = "CHAR";
	public static final String CLOB = "CLOB";
	public static final String DATE = "DATE";
	public static final String DECIMAL = "DECIMAL";
	public static final String DOUBLE = "DOUBLE";
	public static final String FLOAT = "FLOAT";
	public static final String INTEGER = "INTEGER";
	public static final String JAVA_OBJECT = "JAVA_OBJECT";
	public static final String LONGNVARCHAR = "LONGNVARCHAR";
	public static final String LONGVARBINARY = "LONGVARBINARY";
	public static final String LONGVARCHAR = "LONGVARCHAR";
	public static final String NCHAR = "NCHAR";
	public static final String NCLOB = "NCLOB";
	public static final String NUMERIC = "NUMERIC";
	public static final String NVARCHAR = "NVARCHAR";
	public static final String OTHER = "OTHER";
	public static final String REAL = "REAL";
	public static final String SMALLINT = "SMALLINT";
	public static final String TIME = "TIME";
	public static final String TIMESTAMP = "TIMESTAMP";
	public static final String TINYINT = "TINYINT";
	public static final String VARBINARY = "VARBINARY";
	public static final String VARCHAR = "VARCHAR";

	private static final Map<Class<?>, Type> SQL_MAP_ABLE_TYPES = new HashMap<Class<?>, Type>();
	static {
		SQL_MAP_ABLE_TYPES.put(BigDecimal.class, Type.NUMERIC);
		SQL_MAP_ABLE_TYPES.put(BigInteger.class, Type.BIGINT);
		SQL_MAP_ABLE_TYPES.put(Boolean.class, Type.BOOLEAN);
		SQL_MAP_ABLE_TYPES.put(Byte.class, Type.TINYINT);
		SQL_MAP_ABLE_TYPES.put(Character.class, Type.CHAR);
		SQL_MAP_ABLE_TYPES.put(Date.class, Type.DATE);
		SQL_MAP_ABLE_TYPES.put(java.sql.Date.class, Type.DATE);
		SQL_MAP_ABLE_TYPES.put(java.sql.Time.class, Type.TIME);
		SQL_MAP_ABLE_TYPES.put(java.sql.Timestamp.class, Type.TIMESTAMP);
		SQL_MAP_ABLE_TYPES.put(java.sql.Clob.class, Type.CLOB);
		SQL_MAP_ABLE_TYPES.put(java.sql.Blob.class, Type.BLOB);
		SQL_MAP_ABLE_TYPES.put(Double.class, Type.DOUBLE);
		SQL_MAP_ABLE_TYPES.put(Float.class, Type.FLOAT);
		SQL_MAP_ABLE_TYPES.put(Integer.class, Type.INTEGER);
		SQL_MAP_ABLE_TYPES.put(Long.class, Type.BIGINT);
		SQL_MAP_ABLE_TYPES.put(Short.class, Type.SMALLINT);
		SQL_MAP_ABLE_TYPES.put(String.class, Type.VARCHAR);
	}

	/** Check if a class type can map to a SQL type */
	public static boolean canMapToSqlType(Class<?> clazz) {// NOSONAR
		if (clazz == null)
			return false;
		return SQL_MAP_ABLE_TYPES.containsKey(clazz);
	}

	/** Convert a Class type to Dialect's Type */
	public static Type toType(Class<?> clazz) {
		Type t = SQL_MAP_ABLE_TYPES.get(clazz);
		if (t == null)
			return Type.OTHER;
		else
			return t;
	}

	// @formatter:off shut off eclipse's formatter
	/**
	 * Convert column definition String to Dialect's Type
	 */
	public static Type toType(String columnDef) {
		if ("BIGINT".equalsIgnoreCase(columnDef))
			return Type.BIGINT;
		if ("BINARY".equalsIgnoreCase(columnDef))
			return Type.BINARY;
		if ("BIT".equalsIgnoreCase(columnDef))
			return Type.BIT;
		if ("BLOB".equalsIgnoreCase(columnDef))
			return Type.BLOB;
		if ("BOOLEAN".equalsIgnoreCase(columnDef))
			return Type.BOOLEAN;
		if ("CHAR".equalsIgnoreCase(columnDef))
			return Type.CHAR;
		if ("CLOB".equalsIgnoreCase(columnDef))
			return Type.CLOB;
		if ("DATE".equalsIgnoreCase(columnDef))
			return Type.DATE;
		if ("DECIMAL".equalsIgnoreCase(columnDef))
			return Type.DECIMAL;
		if ("DOUBLE".equalsIgnoreCase(columnDef))
			return Type.DOUBLE;
		if ("FLOAT".equalsIgnoreCase(columnDef))
			return Type.FLOAT;
		if ("INTEGER".equalsIgnoreCase(columnDef))
			return Type.INTEGER;
		if ("JAVA_OBJECT".equalsIgnoreCase(columnDef))
			return Type.JAVA_OBJECT;
		if ("LONGNVARCHAR".equalsIgnoreCase(columnDef))
			return Type.LONGNVARCHAR;
		if ("LONGVARBINARY".equalsIgnoreCase(columnDef))
			return Type.LONGVARBINARY;
		if ("LONGVARCHAR".equalsIgnoreCase(columnDef))
			return Type.LONGVARCHAR;
		if ("NCHAR".equalsIgnoreCase(columnDef))
			return Type.NCHAR;
		if ("NCLOB".equalsIgnoreCase(columnDef))
			return Type.NCLOB;
		if ("NUMERIC".equalsIgnoreCase(columnDef))
			return Type.NUMERIC;
		if ("NVARCHAR".equalsIgnoreCase(columnDef))
			return Type.NVARCHAR;
		if ("OTHER".equalsIgnoreCase(columnDef))
			return Type.OTHER;
		if ("REAL".equalsIgnoreCase(columnDef))
			return Type.REAL;
		if ("SMALLINT".equalsIgnoreCase(columnDef))
			return Type.SMALLINT;
		if ("TIME".equalsIgnoreCase(columnDef))
			return Type.TIME;
		if ("TIMESTAMP".equalsIgnoreCase(columnDef))
			return Type.TIMESTAMP;
		if ("TINYINT".equalsIgnoreCase(columnDef))
			return Type.TINYINT;
		if ("VARBINARY".equalsIgnoreCase(columnDef))
			return Type.VARBINARY;
		if ("VARCHAR".equalsIgnoreCase(columnDef))
			return Type.VARCHAR;
		// @formatter:on
		throw new DialectException("'" + columnDef + "' is not a legal SQL column definition name");
	}

	// @formatter:off shut off eclipse's formatter
	/**
	 * Convert java.sql.Types.xxx type to Dialect's Type
	 */
	public static Type javaSqlTypeToDialectType(int javaSqlType) {
		switch (javaSqlType) {
		case java.sql.Types.BIT:
			return Type.BIT;
		case java.sql.Types.TINYINT:
			return Type.TINYINT;
		case java.sql.Types.SMALLINT:
			return Type.SMALLINT;
		case java.sql.Types.INTEGER:
			return Type.INTEGER;
		case java.sql.Types.BIGINT:
			return Type.BIGINT;
		case java.sql.Types.FLOAT:
			return Type.FLOAT;
		case java.sql.Types.REAL:
			return Type.REAL;
		case java.sql.Types.DOUBLE:
			return Type.DOUBLE;
		case java.sql.Types.NUMERIC:
			return Type.NUMERIC;
		case java.sql.Types.DECIMAL:
			return Type.DECIMAL;
		case java.sql.Types.CHAR:
			return Type.CHAR;
		case java.sql.Types.VARCHAR:
			return Type.VARCHAR;
		case java.sql.Types.LONGVARCHAR:
			return Type.LONGVARCHAR;
		case java.sql.Types.DATE:
			return Type.DATE;
		case java.sql.Types.TIME:
			return Type.TIME;
		case java.sql.Types.TIMESTAMP:
			return Type.TIMESTAMP;
		case java.sql.Types.BINARY:
			return Type.BINARY;
		case java.sql.Types.VARBINARY:
			return Type.VARBINARY;
		case java.sql.Types.LONGVARBINARY:
			return Type.LONGVARBINARY;

		case java.sql.Types.OTHER:
			return Type.OTHER;
		case java.sql.Types.JAVA_OBJECT:
			return Type.JAVA_OBJECT;

		case java.sql.Types.BLOB:
			return Type.BLOB;
		case java.sql.Types.CLOB:
			return Type.CLOB;

		case java.sql.Types.BOOLEAN:
			return Type.BOOLEAN;

		case java.sql.Types.NCHAR:
			return Type.NCHAR;
		case java.sql.Types.NVARCHAR:
			return Type.NVARCHAR;
		case java.sql.Types.LONGNVARCHAR:
			return Type.LONGNVARCHAR;
		case java.sql.Types.NCLOB:
			return Type.NCLOB;
		// case java.sql.Types.SQLXML:return Type.UNSUPPORT;
		// case java.sql.Types.NULL:return Type.UNSUPPORT;
		// case java.sql.Types.ROWID:return Type.UNSUPPORT;
		// case java.sql.Types.DISTINCT:return Type.UNSUPPORT;
		// case java.sql.Types.STRUCT:return Type.UNSUPPORT;
		// case java.sql.Types.ARRAY:return Type.UNSUPPORT;
		// case java.sql.Types.REF:return Type.UNSUPPORT;
		// case java.sql.Types.DATALINK:return Type.UNSUPPORT;
		default:
			throw new DialectException("Not supported java.sql.Types value:" + javaSqlType);
		}
	}
}
