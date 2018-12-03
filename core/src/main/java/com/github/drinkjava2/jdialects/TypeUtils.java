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

import java.math.BigDecimal;
import java.math.BigInteger;
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
	public static final String UNKNOW = "UNKNOW";
	public static final String REAL = "REAL";
	public static final String SMALLINT = "SMALLINT";
	public static final String TIME = "TIME";
	public static final String TIMESTAMP = "TIMESTAMP";
	public static final String TINYINT = "TINYINT";
	public static final String VARBINARY = "VARBINARY";
	public static final String VARCHAR = "VARCHAR";

	private static final Map<Class<?>, Type> JAVA_TO_TYPE_MAP = new HashMap<Class<?>, Type>();
	private static final Map<Type, Class<?>> TYPE_TO_JAVA_MAP = new HashMap<Type, Class<?>>();
	static {
		JAVA_TO_TYPE_MAP.put(BigDecimal.class, Type.NUMERIC);
		JAVA_TO_TYPE_MAP.put(BigInteger.class, Type.BIGINT);
		JAVA_TO_TYPE_MAP.put(Boolean.class, Type.BOOLEAN);
		JAVA_TO_TYPE_MAP.put(Byte.class, Type.TINYINT);
		JAVA_TO_TYPE_MAP.put(Character.class, Type.CHAR);
		JAVA_TO_TYPE_MAP.put(java.util.Date.class, Type.DATE);
		JAVA_TO_TYPE_MAP.put(java.sql.Date.class, Type.DATE);
		JAVA_TO_TYPE_MAP.put(java.sql.Time.class, Type.TIME);
		JAVA_TO_TYPE_MAP.put(java.sql.Timestamp.class, Type.TIMESTAMP);
		JAVA_TO_TYPE_MAP.put(java.sql.Clob.class, Type.CLOB);
		JAVA_TO_TYPE_MAP.put(java.sql.Blob.class, Type.BLOB);
		JAVA_TO_TYPE_MAP.put(Double.class, Type.DOUBLE);
		JAVA_TO_TYPE_MAP.put(Float.class, Type.FLOAT);
		JAVA_TO_TYPE_MAP.put(Integer.class, Type.INTEGER);
		JAVA_TO_TYPE_MAP.put(Long.class, Type.BIGINT);
		JAVA_TO_TYPE_MAP.put(Short.class, Type.SMALLINT);
		JAVA_TO_TYPE_MAP.put(String.class, Type.VARCHAR);

		TYPE_TO_JAVA_MAP.put(Type.NUMERIC, BigDecimal.class);
		TYPE_TO_JAVA_MAP.put(Type.BIGINT, BigInteger.class);
		TYPE_TO_JAVA_MAP.put(Type.BOOLEAN, Boolean.class);
		TYPE_TO_JAVA_MAP.put(Type.TINYINT, Byte.class);
		TYPE_TO_JAVA_MAP.put(Type.CHAR, Character.class);
		TYPE_TO_JAVA_MAP.put(Type.DATE, java.util.Date.class);
		TYPE_TO_JAVA_MAP.put(Type.DATE, java.sql.Date.class);
		TYPE_TO_JAVA_MAP.put(Type.TIME, java.sql.Time.class);
		TYPE_TO_JAVA_MAP.put(Type.TIMESTAMP, java.sql.Timestamp.class);
		TYPE_TO_JAVA_MAP.put(Type.CLOB, java.sql.Clob.class);
		TYPE_TO_JAVA_MAP.put(Type.BLOB, java.sql.Blob.class);
		TYPE_TO_JAVA_MAP.put(Type.DOUBLE, Double.class);
		TYPE_TO_JAVA_MAP.put(Type.FLOAT, Float.class);
		TYPE_TO_JAVA_MAP.put(Type.INTEGER, Integer.class);
		TYPE_TO_JAVA_MAP.put(Type.BIGINT, Long.class);
		TYPE_TO_JAVA_MAP.put(Type.SMALLINT, Short.class);
		TYPE_TO_JAVA_MAP.put(Type.VARCHAR, String.class);

	}
 
	/** Check if a class type can map to a SQL type */
	public static boolean canMapToSqlType(Class<?> clazz) {// NOSONAR
		if (clazz == null)
			return false;
		return JAVA_TO_TYPE_MAP.containsKey(clazz);
	}


	public static Class<?> dialectTypeToJavaType(Type type) {// NOSONAR
		if (type == null)
			return null;
		return TYPE_TO_JAVA_MAP.get(type);
	}

	
	/** Convert a Class type to Dialect's Type */
	public static Type toType(Class<?> clazz) {
		Type t = JAVA_TO_TYPE_MAP.get(clazz);
		if (t == null)
			return Type.UNKNOW;
		else
			return t;
	}

	// @formatter:off shut off eclipse's formatter
	/**
	 * Convert column definition String to Dialect's Type
	 */
	public static Type toType(String columnDef) {
		if (BIGINT.equalsIgnoreCase(columnDef))
			return Type.BIGINT;
		if (BINARY.equalsIgnoreCase(columnDef))
			return Type.BINARY;
		if (BIT.equalsIgnoreCase(columnDef))
			return Type.BIT;
		if (BLOB.equalsIgnoreCase(columnDef))
			return Type.BLOB;
		if (BOOLEAN.equalsIgnoreCase(columnDef))
			return Type.BOOLEAN;
		if (CHAR.equalsIgnoreCase(columnDef))
			return Type.CHAR;
		if (CLOB.equalsIgnoreCase(columnDef))
			return Type.CLOB;
		if (DATE.equalsIgnoreCase(columnDef))
			return Type.DATE;
		if (DECIMAL.equalsIgnoreCase(columnDef))
			return Type.DECIMAL;
		if (DOUBLE.equalsIgnoreCase(columnDef))
			return Type.DOUBLE;
		if (FLOAT.equalsIgnoreCase(columnDef))
			return Type.FLOAT;
		if (INTEGER.equalsIgnoreCase(columnDef))
			return Type.INTEGER;
		if (JAVA_OBJECT.equalsIgnoreCase(columnDef))
			return Type.JAVA_OBJECT;
		if (LONGNVARCHAR.equalsIgnoreCase(columnDef))
			return Type.LONGNVARCHAR;
		if (LONGVARBINARY.equalsIgnoreCase(columnDef))
			return Type.LONGVARBINARY;
		if (LONGVARCHAR.equalsIgnoreCase(columnDef))
			return Type.LONGVARCHAR;
		if (NCHAR.equalsIgnoreCase(columnDef))
			return Type.NCHAR;
		if (NCLOB.equalsIgnoreCase(columnDef))
			return Type.NCLOB;
		if (NUMERIC.equalsIgnoreCase(columnDef))
			return Type.NUMERIC;
		if (NVARCHAR.equalsIgnoreCase(columnDef))
			return Type.NVARCHAR;
		if (UNKNOW.equalsIgnoreCase(columnDef))
			return Type.UNKNOW;
		if (REAL.equalsIgnoreCase(columnDef))
			return Type.REAL;
		if (SMALLINT.equalsIgnoreCase(columnDef))
			return Type.SMALLINT;
		if (TIME.equalsIgnoreCase(columnDef))
			return Type.TIME;
		if (TIMESTAMP.equalsIgnoreCase(columnDef))
			return Type.TIMESTAMP;
		if (TINYINT.equalsIgnoreCase(columnDef))
			return Type.TINYINT;
		if (VARBINARY.equalsIgnoreCase(columnDef))
			return Type.VARBINARY;
		if (VARCHAR.equalsIgnoreCase(columnDef))
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
			return Type.UNKNOW;
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
