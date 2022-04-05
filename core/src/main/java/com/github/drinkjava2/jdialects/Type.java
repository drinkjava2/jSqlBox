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
 * Virtual SQL Type definitions
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public enum Type {
	BIGINT//
	, BINARY//
	, BIT//
	, BLOB//
	, BOOLEAN//
	, CHAR//
	, CLOB//
	, DATE//
	, DECIMAL//
	, DOUBLE//
	, FLOAT//
	, INTEGER//
	, JAVA_OBJECT//
	, LONGNVARCHAR//
	, LONGVARBINARY//
	, LONGVARCHAR//
	, NCHAR//
	, NCLOB//
	, NUMERIC//
	, NVARCHAR//
	, UNKNOW//
	, REAL//
	, SMALLINT//
	, TIME//
	, TIMESTAMP//
	, TINYINT//
	, VARBINARY//
	, VARCHAR
	//mysql
	, DATETIME
	, MEDIUMINT
	, INT
	, TINYBLOB
	, TINYTEXT
	, TEXT
	, MEDIUMBLOB
	, MEDIUMTEXT
	, LONGBLOB
	, LONGTEXT
	, YEAR
	, JSON
	//oracle
	, BINARY_FLOAT
	, DOUBLE_PRECISION
	, BINARY_DOUBLE
	, TIMESTAMP_WITH_TIME_ZONE
	, TIMESTAMP_WITH_LOCAL_TIME_ZONE
	, VARCHAR2
	,INTERVAL_YEAR_TO_MONTH
	,INTERVAL_DAY_TO_SECOND
	;
	
	//TODO 此处需要考虑数据类型多对一的情况
	public static Type getByTypeName(String typeName) {
        for (Type val : Type.values()) {
            if (val.name().equalsIgnoreCase(typeName)) {
                return val;
            }
        }
        throw new DialectException("'" + typeName + "' can not be map to a dialect type");
    }

}
