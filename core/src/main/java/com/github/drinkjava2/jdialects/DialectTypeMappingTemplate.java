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

import static com.github.drinkjava2.jdialects.Type.BIGINT;
import static com.github.drinkjava2.jdialects.Type.BINARY;
import static com.github.drinkjava2.jdialects.Type.BIT;
import static com.github.drinkjava2.jdialects.Type.BLOB;
import static com.github.drinkjava2.jdialects.Type.BOOLEAN;
import static com.github.drinkjava2.jdialects.Type.CHAR;
import static com.github.drinkjava2.jdialects.Type.CLOB;
import static com.github.drinkjava2.jdialects.Type.DATE;
import static com.github.drinkjava2.jdialects.Type.DECIMAL;
import static com.github.drinkjava2.jdialects.Type.DOUBLE;
import static com.github.drinkjava2.jdialects.Type.FLOAT;
import static com.github.drinkjava2.jdialects.Type.INTEGER;
import static com.github.drinkjava2.jdialects.Type.JAVA_OBJECT;
import static com.github.drinkjava2.jdialects.Type.LONGNVARCHAR;
import static com.github.drinkjava2.jdialects.Type.LONGVARBINARY;
import static com.github.drinkjava2.jdialects.Type.LONGVARCHAR;
import static com.github.drinkjava2.jdialects.Type.NCHAR;
import static com.github.drinkjava2.jdialects.Type.NCLOB;
import static com.github.drinkjava2.jdialects.Type.NUMERIC;
import static com.github.drinkjava2.jdialects.Type.NVARCHAR;
import static com.github.drinkjava2.jdialects.Type.REAL;
import static com.github.drinkjava2.jdialects.Type.SMALLINT;
import static com.github.drinkjava2.jdialects.Type.TIME;
import static com.github.drinkjava2.jdialects.Type.TIMESTAMP;
import static com.github.drinkjava2.jdialects.Type.TINYINT;
import static com.github.drinkjava2.jdialects.Type.VARBINARY;
import static com.github.drinkjava2.jdialects.Type.VARCHAR;

import java.util.Map;

/**
 * Initialize type mapping template for all dialects
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
@SuppressWarnings("all")
public class DialectTypeMappingTemplate {

    protected static void initTypeMappingTemplates() {

        //================SQLiteDialect family===============
        Map<Type, String> m = Dialect.SQLiteDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "blob");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float($p)");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "blob");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "datetime");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "blob");
        m.put(VARCHAR, "varchar($l)");

        //================AccessDialect family===============
        m = Dialect.AccessDialect.typeMappings;
        m.put(BIGINT, "integer");
        m.put(BINARY, "binary<255|N/A");
        m.put(BIT, "boolean");
        m.put(BLOB, "ole");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "longvarchar");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "java_object");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "timestamp");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary<255|bit varying($l)");
        m.put(VARCHAR, "varchar($l)");

        //================ExcelDialect family===============
        m = Dialect.ExcelDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "longvarchar");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "numeric(5,0)");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "numeric(3,0)");
        m.put(VARBINARY, "varbinary");
        m.put(VARCHAR, "varchar($l)");

        //================TextDialect family===============
        m = Dialect.TextDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "java_object");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "numeric(5,0)");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "numeric(3,0)");
        m.put(VARBINARY, "varbinary");
        m.put(VARCHAR, "varchar($l)");

        //================ParadoxDialect family===============
        m = Dialect.ParadoxDialect.typeMappings;
        m.put(BIGINT, "integer");
        m.put(BINARY, "binary");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "java_object");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary");
        m.put(VARCHAR, "varchar($l)");

        //================CobolDialect family===============
        m = Dialect.CobolDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "java_object");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary");
        m.put(VARCHAR, "varchar($l)");

        //================XMLDialect family===============
        m = Dialect.XMLDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "longvarchar");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "numeric(5,0)");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "numeric(3,0)");
        m.put(VARBINARY, "varbinary");
        m.put(VARCHAR, "varchar($l)");

        //================DbfDialect family===============
        m = Dialect.DbfDialect.typeMappings;
        m.put(BIGINT, "integer");
        m.put(BINARY, "binary<255|N/A");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "java_object");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary<255|bit varying($l)");
        m.put(VARCHAR, "varchar($l)");

        //================DamengDialect family===============
        m = Dialect.DamengDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary($l)");
        m.put(BIT, "bit");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "bit");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "text");
        m.put(LONGVARBINARY, "image");
        m.put(LONGVARCHAR, "text");
        m.put(NCHAR, "char(1)");
        m.put(NCLOB, "clob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "varchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "datetime");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary($l)");
        m.put(VARCHAR, "varchar($l)");

        //================GBaseDialect family===============
        m = Dialect.GBaseDialect.typeMappings;
        m.put(BIGINT, "number(19,0)");
        m.put(BINARY, "raw($l)<2000|long raw");
        m.put(BIT, "number(1,0)");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "number(1,0)");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "number($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "number(10,0)");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "long raw");
        m.put(LONGVARCHAR, "long");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "number($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "number(5,0)");
        m.put(TIME, "date");
        m.put(TIMESTAMP, "date");
        m.put(TINYINT, "number(3,0)");
        m.put(VARBINARY, "raw($l)<2000|long raw");
        m.put(VARCHAR, "varchar2($l)<4000|long");

        //================Cache71Dialect family===============
        m = Dialect.Cache71Dialect.typeMappings;
        m.put(BIGINT, "BigInt");
        m.put(BINARY, "varbinary($1)");
        m.put(BIT, "bit");
        m.put(BLOB, "longvarbinary");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "longvarchar");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "longvarbinary");
        m.put(VARCHAR, "varchar($l)");

        //================CUBRIDDialect family===============
        m = Dialect.CUBRIDDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "N/A");
        m.put(BIT, "bit(8)");
        m.put(BLOB, "bit varying(65535)");
        m.put(BOOLEAN, "bit(8)");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "string");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "int");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "double");
        m.put(SMALLINT, "short");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "short");
        m.put(VARBINARY, "bit varying($l)<2000|bit varying($l)");
        m.put(VARCHAR, "varchar($l)<255|varchar($l)<2000|string");

        //================DataDirectOracle9Dialect family===============
        m = Dialect.DataDirectOracle9Dialect.typeMappings;
        m.put(BIGINT, "number(19,0)");
        m.put(BINARY, "N/A");
        m.put(BIT, "number(1,0)");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1 char)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "number($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "number(10,0)");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "number($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "number(5,0)");
        m.put(TIME, "date");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "number(3,0)");
        m.put(VARBINARY, "raw($l)<2000|long raw");
        m.put(VARCHAR, "varchar2($l char)<4000|long");

        //================DB2Dialect family===============
        m = Dialect.DB2Dialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "char($l) for bit data<254|varchar($l) for bit data");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob($l)");
        m.put(BOOLEAN, "smallint");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob($l)");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "long varchar for bit data");
        m.put(LONGVARCHAR, "long varchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "decimal($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "varchar($l) for bit data");
        m.put(VARCHAR, "varchar($l)");

        m = Dialect.DB2390Dialect.typeMappings;
        m.putAll(Dialect.DB2Dialect.typeMappings);//extends from DB2Dialect

        m = Dialect.DB2390V8Dialect.typeMappings;
        m.putAll(Dialect.DB2Dialect.typeMappings);//extends from DB2Dialect

        m = Dialect.DB2400Dialect.typeMappings;
        m.putAll(Dialect.DB2Dialect.typeMappings);//extends from DB2Dialect

        m = Dialect.DB297Dialect.typeMappings;
        m.putAll(Dialect.DB2Dialect.typeMappings);//extends from DB2Dialect

        //================DerbyDialect family===============
        m = Dialect.DerbyDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "char($l) for bit data<254|varchar($l) for bit data");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "smallint");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob($l)");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "long varchar for bit data");
        m.put(LONGVARCHAR, "long varchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "decimal($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "varchar($l) for bit data");
        m.put(VARCHAR, "varchar($l)");

        m = Dialect.DerbyTenFiveDialect.typeMappings;
        m.putAll(Dialect.DerbyDialect.typeMappings);//extends from DerbyDialect

        m = Dialect.DerbyTenSevenDialect.typeMappings;
        m.putAll(Dialect.DerbyDialect.typeMappings);//extends from DerbyDialect
        m.put(BOOLEAN, "boolean");

        m = Dialect.DerbyTenSixDialect.typeMappings;
        m.putAll(Dialect.DerbyDialect.typeMappings);//extends from DerbyDialect

        //================FirebirdDialect family===============
        m = Dialect.FirebirdDialect.typeMappings;
        m.put(BIGINT, "numeric(18,0)");
        m.put(BINARY, "N/A");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "smallint");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "blob sub_type 1");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "blob");
        m.put(VARCHAR, "varchar($l)");

        //================FrontBaseDialect family===============
        m = Dialect.FrontBaseDialect.typeMappings;
        m.put(BIGINT, "longint");
        m.put(BINARY, "N/A");
        m.put(BIT, "bit");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "bit varying($l)");
        m.put(VARCHAR, "varchar($l)");

        //================H2Dialect family===============
        m = Dialect.H2Dialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary");
        m.put(BIT, "boolean");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "varchar(2147483647)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary($l)");
        m.put(VARCHAR, "varchar($l)");

        //================HANAColumnStoreDialect family===============
        m = Dialect.HANAColumnStoreDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "varbinary($l)<5000|blob");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "varchar(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p, $s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float($p)");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)<5000|nclob");
        m.put(LONGVARBINARY, "varbinary($l)<5000|blob");
        m.put(LONGVARCHAR, "varchar($l)<5000|clob");
        m.put(NCHAR, "nvarchar(1)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "decimal($p, $s)");
        m.put(NVARCHAR, "nvarchar($l)<5000|nclob");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "varbinary($l)<5000|blob");
        m.put(VARCHAR, "varchar($l)<5000|clob");

        m = Dialect.HANARowStoreDialect.typeMappings;
        m.putAll(Dialect.HANAColumnStoreDialect.typeMappings);//extends from HANAColumnStoreDialect

        //================HSQLDialect family===============
        m = Dialect.HSQLDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary($l)");
        m.put(BIT, "bit");
        m.put(BLOB, "longvarbinary");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char($l)");
        m.put(CLOB, "longvarchar");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "longvarbinary");
        m.put(LONGVARCHAR, "longvarchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "clob");
        m.put(NUMERIC, "numeric");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary($l)");
        m.put(VARCHAR, "varchar($l)");

        //================InformixDialect family===============
        m = Dialect.InformixDialect.typeMappings;
        m.put(BIGINT, "int8");
        m.put(BINARY, "byte");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal");
        m.put(DOUBLE, "float");
        m.put(FLOAT, "smallfloat");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "blob");
        m.put(LONGVARCHAR, "clob");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "decimal");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "smallfloat");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "datetime hour to second");
        m.put(TIMESTAMP, "datetime year to fraction(5)");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "byte");
        m.put(VARCHAR, "varchar($l)<255|lvarchar($l)<32739|varchar($l)");

        m = Dialect.Informix10Dialect.typeMappings;
        m.putAll(Dialect.InformixDialect.typeMappings);//extends from InformixDialect

        //================IngresDialect family===============
        m = Dialect.IngresDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "byte($l)<32000|long byte");
        m.put(BIT, "tinyint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char($l)<32000|char($l)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p, $s)");
        m.put(DOUBLE, "float");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "long byte");
        m.put(LONGVARCHAR, "long varchar");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "decimal($p, $s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time with time zone");
        m.put(TIMESTAMP, "timestamp with time zone");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbyte($l)<32000|long byte");
        m.put(VARCHAR, "varchar($l)<32000|long varchar");

        m = Dialect.Ingres10Dialect.typeMappings;
        m.putAll(Dialect.IngresDialect.typeMappings);//extends from IngresDialect
        m.put(BIT, "boolean");
        m.put(DATE, "ansidate");
        m.put(TIMESTAMP, "timestamp(9) with time zone");

        m = Dialect.Ingres9Dialect.typeMappings;
        m.putAll(Dialect.IngresDialect.typeMappings);//extends from IngresDialect
        m.put(DATE, "ansidate");
        m.put(TIMESTAMP, "timestamp(9) with time zone");

        //================InterbaseDialect family===============
        m = Dialect.InterbaseDialect.typeMappings;
        m.put(BIGINT, "numeric(18,0)");
        m.put(BINARY, "N/A");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "smallint");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "blob sub_type 1");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "blob");
        m.put(VARCHAR, "varchar($l)");

        //================JDataStoreDialect family===============
        m = Dialect.JDataStoreDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "N/A");
        m.put(BIT, "tinyint");
        m.put(BLOB, "varbinary");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "varchar");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p, $s)");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p, $s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary($l)");
        m.put(VARCHAR, "varchar($l)");

        //================MariaDBDialect family===============
        m = Dialect.MariaDBDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary($l)");
        m.put(BIT, "bit");
        m.put(BLOB, "longblob");
        m.put(BOOLEAN, "bit");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "longtext");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "mediumblob<16777215|longblob");
        m.put(LONGVARCHAR, "longtext");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "longtext");
        m.put(NUMERIC, "decimal($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "datetime");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "tinyblob<255|blob<65535|mediumblob<16777215|longblob");
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        m = Dialect.MariaDB53Dialect.typeMappings;
        m.putAll(Dialect.MariaDBDialect.typeMappings);//extends from MariaDBDialect
        m.put(TIMESTAMP, "datetime(6)");

        m = Dialect.MariaDB102Dialect.typeMappings;
        m.putAll(Dialect.MariaDBDialect.typeMappings);//extends from MariaDBDialect
        m.put(JAVA_OBJECT, "json");
        m.put(TIMESTAMP, "datetime(6)");

        m = Dialect.MariaDB103Dialect.typeMappings;
        m.putAll(Dialect.MariaDBDialect.typeMappings);//extends from MariaDBDialect
        m.put(JAVA_OBJECT, "json");
        m.put(TIMESTAMP, "datetime(6)");

        m = Dialect.MariaDB10Dialect.typeMappings;
        m.putAll(Dialect.MariaDBDialect.typeMappings);//extends from MariaDBDialect
        m.put(TIMESTAMP, "datetime(6)");

        //================MckoiDialect family===============
        m = Dialect.MckoiDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "N/A");
        m.put(BIT, "bit");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric");
        m.put(DOUBLE, "double");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary");
        m.put(VARCHAR, "varchar($l)");

        //================MimerSQLDialect family===============
        m = Dialect.MimerSQLDialect.typeMappings;
        m.put(BIGINT, "BIGINT");
        m.put(BINARY, "BINARY<2000|BLOB($1)");
        m.put(BIT, "ODBC.BIT");
        m.put(BLOB, "BLOB($l)");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "NCHAR(1)");
        m.put(CLOB, "NCLOB($l)");
        m.put(DATE, "DATE");
        m.put(DECIMAL, "NUMERIC(19, $l)");
        m.put(DOUBLE, "DOUBLE PRECISION");
        m.put(FLOAT, "FLOAT");
        m.put(INTEGER, "INTEGER");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "BLOB($1)");
        m.put(LONGVARCHAR, "CLOB($1)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "NUMERIC(19, $l)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "SMALLINT");
        m.put(TIME, "TIME");
        m.put(TIMESTAMP, "TIMESTAMP");
        m.put(TINYINT, "ODBC.TINYINT");
        m.put(VARBINARY, "BINARY VARYING($l)<2000|BLOB($1)");
        m.put(VARCHAR, "NATIONAL CHARACTER VARYING($l)<2000|NCLOB($l)");

        //================MySQLDialect family===============
        m = Dialect.MySQLDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "binary($l)");
        m.put(BIT, "bit");
        m.put(BLOB, "longblob");
        m.put(BOOLEAN, "bit");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "longtext");
        m.put(DATE, "date");
        m.put(DECIMAL, "decimal($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "mediumblob<16777215|longblob");
        m.put(LONGVARCHAR, "longtext");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "longtext");
        m.put(NUMERIC, "decimal($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "datetime");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "tinyblob<255|blob<65535|mediumblob<16777215|longblob");
        m.put(VARCHAR, "varchar($l)<255|longtext");

        m = Dialect.MySQL5Dialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        m = Dialect.MySQL55Dialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        m = Dialect.MySQL57Dialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect
        m.put(JAVA_OBJECT, "json");
        m.put(TIMESTAMP, "datetime(6)");
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        m = Dialect.MySQL57InnoDBDialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect
        m.put(JAVA_OBJECT, "json");
        m.put(TIMESTAMP, "datetime(6)");
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        m = Dialect.MySQL5InnoDBDialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        m = Dialect.MySQLInnoDBDialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect

        m = Dialect.MySQLMyISAMDialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect

        m = Dialect.MySQL8Dialect.typeMappings;
        m.putAll(Dialect.MySQLDialect.typeMappings);//extends from MySQLDialect
        m.put(JAVA_OBJECT, "json");
        m.put(TIMESTAMP, "datetime(6)");
        m.put(VARCHAR, "varchar($l)<65535|longtext");

        //================OracleDialect family===============
        m = Dialect.OracleDialect.typeMappings;
        m.put(BIGINT, "number(19,0)");
        m.put(BINARY, "N/A");
        m.put(BIT, "number(1,0)");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "number($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "number(10,0)");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "number($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "number(5,0)");
        m.put(TIME, "date");
        m.put(TIMESTAMP, "date");
        m.put(TINYINT, "number(3,0)");
        m.put(VARBINARY, "raw($l)<2000|long raw");
        m.put(VARCHAR, "varchar2($l)<4000|long");

        m = Dialect.Oracle10gDialect.typeMappings;
        m.putAll(Dialect.OracleDialect.typeMappings);//extends from OracleDialect
        m.put(BINARY, "raw($l)<2000|long raw");
        m.put(BOOLEAN, "number(1,0)");
        m.put(CHAR, "char(1 char)");
        m.put(LONGNVARCHAR, "nvarchar2($l)");
        m.put(LONGVARBINARY, "long raw");
        m.put(LONGVARCHAR, "long");
        m.put(NVARCHAR, "nvarchar2($l)");
        m.put(TIMESTAMP, "timestamp");
        m.put(VARCHAR, "varchar2($l char)<4000|long");

        m = Dialect.Oracle12cDialect.typeMappings;
        m.putAll(Dialect.OracleDialect.typeMappings);//extends from OracleDialect
        m.put(BINARY, "raw($l)<2000|long raw");
        m.put(BOOLEAN, "number(1,0)");
        m.put(CHAR, "char(1 char)");
        m.put(LONGNVARCHAR, "nvarchar2($l)");
        m.put(LONGVARBINARY, "long raw");
        m.put(LONGVARCHAR, "long");
        m.put(NVARCHAR, "nvarchar2($l)");
        m.put(TIMESTAMP, "timestamp");
        m.put(VARCHAR, "varchar2($l char)<4000|long");

        m = Dialect.Oracle8iDialect.typeMappings;
        m.putAll(Dialect.OracleDialect.typeMappings);//extends from OracleDialect
        m.put(BINARY, "raw($l)<2000|long raw");
        m.put(BOOLEAN, "number(1,0)");
        m.put(LONGVARBINARY, "long raw");
        m.put(LONGVARCHAR, "long");

        m = Dialect.Oracle9Dialect.typeMappings;
        m.putAll(Dialect.OracleDialect.typeMappings);//extends from OracleDialect
        m.put(CHAR, "char(1 char)");
        m.put(TIMESTAMP, "timestamp");
        m.put(VARCHAR, "varchar2($l char)<4000|long");

        m = Dialect.Oracle9iDialect.typeMappings;
        m.putAll(Dialect.OracleDialect.typeMappings);//extends from OracleDialect
        m.put(BINARY, "raw($l)<2000|long raw");
        m.put(BOOLEAN, "number(1,0)");
        m.put(CHAR, "char(1 char)");
        m.put(LONGNVARCHAR, "nvarchar2($l)");
        m.put(LONGVARBINARY, "long raw");
        m.put(LONGVARCHAR, "long");
        m.put(NVARCHAR, "nvarchar2($l)");
        m.put(TIMESTAMP, "timestamp");
        m.put(VARCHAR, "varchar2($l char)<4000|long");

        //================PointbaseDialect family===============
        m = Dialect.PointbaseDialect.typeMappings;
        m.put(BIGINT, "bigint");
        m.put(BINARY, "N/A");
        m.put(BIT, "smallint");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "blob($l)");
        m.put(VARCHAR, "varchar($l)");

        //================PostgreSQLDialect family===============
        m = Dialect.PostgreSQLDialect.typeMappings;
        m.put(BIGINT, "int8");
        m.put(BINARY, "bytea");
        m.put(BIT, "bool");
        m.put(BLOB, "oid");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "text");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p, $s)");
        m.put(DOUBLE, "float8");
        m.put(FLOAT, "float4");
        m.put(INTEGER, "int4");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bytea");
        m.put(LONGVARCHAR, "text");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p, $s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "int2");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "int2");
        m.put(VARBINARY, "bytea");
        m.put(VARCHAR, "varchar($l)");

        m = Dialect.PostgresPlusDialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect

        m = Dialect.PostgreSQL81Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect

        m = Dialect.PostgreSQL82Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect

        m = Dialect.PostgreSQL9Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect

        m = Dialect.PostgreSQL91Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect

        m = Dialect.PostgreSQL92Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect
        m.put(JAVA_OBJECT, "json");

        m = Dialect.PostgreSQL93Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect
        m.put(JAVA_OBJECT, "json");

        m = Dialect.PostgreSQL94Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect
        m.put(JAVA_OBJECT, "json");

        m = Dialect.PostgreSQL95Dialect.typeMappings;
        m.putAll(Dialect.PostgreSQLDialect.typeMappings);//extends from PostgreSQLDialect
        m.put(JAVA_OBJECT, "json");

        //================ProgressDialect family===============
        m = Dialect.ProgressDialect.typeMappings;
        m.put(BIGINT, "numeric");
        m.put(BINARY, "N/A");
        m.put(BIT, "bit");
        m.put(BLOB, "blob");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "character(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "real");
        m.put(INTEGER, "integer");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "tinyint");
        m.put(VARBINARY, "varbinary($l)");
        m.put(VARCHAR, "varchar($l)");

        //================RDMSOS2200Dialect family===============
        m = Dialect.RDMSOS2200Dialect.typeMappings;
        m.put(BIGINT, "NUMERIC(21,0)");
        m.put(BINARY, "N/A");
        m.put(BIT, "SMALLINT");
        m.put(BLOB, "BLOB($l)");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "CHARACTER(1)");
        m.put(CLOB, "clob");
        m.put(DATE, "DATE");
        m.put(DECIMAL, "NUMERIC(21,$l)");
        m.put(DOUBLE, "DOUBLE PRECISION");
        m.put(FLOAT, "FLOAT");
        m.put(INTEGER, "INTEGER");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "NUMERIC(21,$l)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "REAL");
        m.put(SMALLINT, "SMALLINT");
        m.put(TIME, "TIME");
        m.put(TIMESTAMP, "TIMESTAMP");
        m.put(TINYINT, "SMALLINT");
        m.put(VARBINARY, "bit varying($l)");
        m.put(VARCHAR, "CHARACTER($l)");

        //================SAPDBDialect family===============
        m = Dialect.SAPDBDialect.typeMappings;
        m.put(BIGINT, "fixed(19,0)");
        m.put(BINARY, "N/A");
        m.put(BIT, "boolean");
        m.put(BLOB, "long byte");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "long varchar");
        m.put(DATE, "date");
        m.put(DECIMAL, "fixed($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "int");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "fixed($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "timestamp");
        m.put(TINYINT, "fixed(3,0)");
        m.put(VARBINARY, "long byte");
        m.put(VARCHAR, "varchar($l)");

        //================SQLServerDialect family===============
        m = Dialect.SQLServerDialect.typeMappings;
        m.put(BIGINT, "numeric(19,0)");
        m.put(BINARY, "binary($l)");
        m.put(BIT, "tinyint");
        m.put(BLOB, "image");
        m.put(BOOLEAN, "bit");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "text");
        m.put(DATE, "datetime");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "int");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "image");
        m.put(LONGVARCHAR, "text");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "datetime");
        m.put(TIMESTAMP, "datetime");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "varbinary($l)<8000|image");
        m.put(VARCHAR, "varchar($l)");

        m = Dialect.SQLServer2005Dialect.typeMappings;
        m.putAll(Dialect.SQLServerDialect.typeMappings);//extends from SQLServerDialect
        m.put(BIGINT, "bigint");
        m.put(BIT, "bit");
        m.put(BLOB, "varbinary(MAX)");
        m.put(CLOB, "varchar(MAX)");
        m.put(LONGVARBINARY, "varbinary(MAX)");
        m.put(LONGVARCHAR, "varchar(MAX)");
        m.put(NCLOB, "nvarchar(MAX)");
        m.put(VARBINARY, "varbinary($l)<8000|varbinary(MAX)");
        m.put(VARCHAR, "varchar($l)<8000|varchar(MAX)");

        m = Dialect.SQLServer2008Dialect.typeMappings;
        m.putAll(Dialect.SQLServerDialect.typeMappings);//extends from SQLServerDialect
        m.put(BIGINT, "bigint");
        m.put(BIT, "bit");
        m.put(BLOB, "varbinary(MAX)");
        m.put(CLOB, "varchar(MAX)");
        m.put(DATE, "date");
        m.put(LONGVARBINARY, "varbinary(MAX)");
        m.put(LONGVARCHAR, "varchar(MAX)");
        m.put(NCLOB, "nvarchar(MAX)");
        m.put(NVARCHAR, "nvarchar($l)<4000|nvarchar(MAX)");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "datetime2");
        m.put(VARBINARY, "varbinary($l)<8000|varbinary(MAX)");
        m.put(VARCHAR, "varchar($l)<8000|varchar(MAX)");

        m = Dialect.SQLServer2012Dialect.typeMappings;
        m.putAll(Dialect.SQLServerDialect.typeMappings);//extends from SQLServerDialect
        m.put(BIGINT, "bigint");
        m.put(BIT, "bit");
        m.put(BLOB, "varbinary(MAX)");
        m.put(CLOB, "varchar(MAX)");
        m.put(DATE, "date");
        m.put(LONGVARBINARY, "varbinary(MAX)");
        m.put(LONGVARCHAR, "varchar(MAX)");
        m.put(NCLOB, "nvarchar(MAX)");
        m.put(NVARCHAR, "nvarchar($l)<4000|nvarchar(MAX)");
        m.put(TIME, "time");
        m.put(TIMESTAMP, "datetime2");
        m.put(VARBINARY, "varbinary($l)<8000|varbinary(MAX)");
        m.put(VARCHAR, "varchar($l)<8000|varchar(MAX)");

        //================SybaseDialect family===============
        m = Dialect.SybaseDialect.typeMappings;
        m.put(BIGINT, "numeric(19,0)");
        m.put(BINARY, "binary($l)");
        m.put(BIT, "tinyint");
        m.put(BLOB, "image");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "char(1)");
        m.put(CLOB, "text");
        m.put(DATE, "datetime");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(DOUBLE, "double precision");
        m.put(FLOAT, "float");
        m.put(INTEGER, "int");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "numeric($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "smallint");
        m.put(TIME, "datetime");
        m.put(TIMESTAMP, "datetime");
        m.put(TINYINT, "smallint");
        m.put(VARBINARY, "varbinary($l)");
        m.put(VARCHAR, "varchar($l)");

        m = Dialect.Sybase11Dialect.typeMappings;
        m.putAll(Dialect.SybaseDialect.typeMappings);//extends from SybaseDialect

        m = Dialect.SybaseAnywhereDialect.typeMappings;
        m.putAll(Dialect.SybaseDialect.typeMappings);//extends from SybaseDialect
        m.put(BOOLEAN, "bit");

        m = Dialect.SybaseASE15Dialect.typeMappings;
        m.putAll(Dialect.SybaseDialect.typeMappings);//extends from SybaseDialect
        m.put(BIGINT, "bigint");
        m.put(BOOLEAN, "tinyint");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(LONGVARBINARY, "image");
        m.put(LONGVARCHAR, "text");
        m.put(TIME, "time");

        m = Dialect.SybaseASE157Dialect.typeMappings;
        m.putAll(Dialect.SybaseDialect.typeMappings);//extends from SybaseDialect
        m.put(BIGINT, "bigint");
        m.put(BOOLEAN, "tinyint");
        m.put(DATE, "date");
        m.put(DECIMAL, "numeric($p,$s)");
        m.put(LONGVARBINARY, "image");
        m.put(LONGVARCHAR, "text");
        m.put(TIME, "time");

        //================TeradataDialect family===============
        m = Dialect.TeradataDialect.typeMappings;
        m.put(BIGINT, "NUMERIC(18,0)");
        m.put(BINARY, "BYTEINT");
        m.put(BIT, "BYTEINT");
        m.put(BLOB, "BLOB");
        m.put(BOOLEAN, "BYTEINT");
        m.put(CHAR, "CHAR(1)");
        m.put(CLOB, "CLOB");
        m.put(DATE, "DATE");
        m.put(DECIMAL, "DECIMAL");
        m.put(DOUBLE, "DOUBLE PRECISION");
        m.put(FLOAT, "FLOAT");
        m.put(INTEGER, "INTEGER");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "LONG VARCHAR");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "NUMERIC($p,$s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "SMALLINT");
        m.put(TIME, "TIME");
        m.put(TIMESTAMP, "TIMESTAMP");
        m.put(TINYINT, "BYTEINT");
        m.put(VARBINARY, "VARBYTE($l)");
        m.put(VARCHAR, "VARCHAR($l)");

        m = Dialect.Teradata14Dialect.typeMappings;
        m.putAll(Dialect.TeradataDialect.typeMappings);//extends from TeradataDialect
        m.put(BIGINT, "BIGINT");
        m.put(BINARY, "VARBYTE(100)");
        m.put(LONGVARBINARY, "VARBYTE(32000)");
        m.put(LONGVARCHAR, "VARCHAR(32000)");

        //================TimesTenDialect family===============
        m = Dialect.TimesTenDialect.typeMappings;
        m.put(BIGINT, "BIGINT");
        m.put(BINARY, "N/A");
        m.put(BIT, "TINYINT");
        m.put(BLOB, "VARBINARY(4000000)");
        m.put(BOOLEAN, "boolean");
        m.put(CHAR, "CHAR(1)");
        m.put(CLOB, "VARCHAR(4000000)");
        m.put(DATE, "DATE");
        m.put(DECIMAL, "DECIMAL($p, $s)");
        m.put(DOUBLE, "DOUBLE");
        m.put(FLOAT, "FLOAT");
        m.put(INTEGER, "INTEGER");
        m.put(JAVA_OBJECT, "N/A");
        m.put(LONGNVARCHAR, "nvarchar($l)");
        m.put(LONGVARBINARY, "bit varying($l)");
        m.put(LONGVARCHAR, "varchar($l)");
        m.put(NCHAR, "nchar($l)");
        m.put(NCLOB, "nclob");
        m.put(NUMERIC, "DECIMAL($p, $s)");
        m.put(NVARCHAR, "nvarchar($l)");
        m.put(REAL, "real");
        m.put(SMALLINT, "SMALLINT");
        m.put(TIME, "TIME");
        m.put(TIMESTAMP, "TIMESTAMP");
        m.put(TINYINT, "TINYINT");
        m.put(VARBINARY, "VARBINARY($l)");
        m.put(VARCHAR, "VARCHAR($l)");
    }

}
