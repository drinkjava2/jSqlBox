/*
 * Copyright 2016 the original author or authors.
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
package com.github.drinkjava2.jdbpro;

/**
 * SqlOption system how to explain a SqlItem, SqlItem like "Message" in windows,
 * SqlOption is the "Message" type.
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public enum SqlOption {
	// ----------Sql Items explained by DbPro ------------
	/** Append a SQL String piece */
	SQL, // Method

	/** Append a parameter or parameter array */
	PARAM, // Method

	/** Append a "?" String and append a parameter or parameter array */
	QUESTION_PARAM, // Method

	/** BIND Key-Values pairs "key1,value1, key2,value2..." for SqlTemplateEngine */
	BIND, // Method

	/**
	 * Usage: NOT_NUL("user_name=?", name), when name is null, nothing will be
	 * appended into SQL and parameters, otherwise return a "user_name=?" String and
	 * a SQL parameter
	 */
	NOT_NULL, // Method

	/** Append a " values(?,?,?....?)" String at end of SQL */
	VALUES_QUESTIONS, // Method

	// ----------Special Items ------------
	/** Switch to another DbPro or subClass (SqlBoxContext) to execute SQL */
	SWITCHTO, // Method

	/** Give one or more class as parameter, instance will created by IocTool */
	IOC, // Method

	/** Disable handles according given handlers' class */
	DISABLE_HANDLERS, // Method

	/** Disable handles according given handlers' class */
	ENABLE_HANDLERS, // Method

	/** Force use template style */
	USE_TEMPLATE, // Control Switch

	// ------Sql Operation type--------
	/** It's a EXECUTE type SQL */
	EXECUTE, // Control Switch

	/** It's a UPDATE type SQL */
	UPDATE, // Control Switch

	/** It's a INSERT type SQL */
	INSERT, // Control Switch

	/** It's a QUERY type SQL */
	QUERY, // Control Switch

	/** OTHER type SqlItem used to store some other items for user */
	OTHER,

	// ================================================================
	// Below items designed for jSqlBox or other projects to explain
	// ================================================================

	// ------Master_Slave Options-------
	/**
	 * Tell system to choose master or slave database automatically (write:master,
	 * read:if in Transaction use master otherwise use one slave)
	 */
	USE_AUTO, // Control Switch

	/** Tell system force use master database (write and read:master ) */
	USE_MASTER, // Control Switch

	/** Tell system force use slave database (write:all slaves, read:one slave) */
	USE_SLAVE, // Control Switch

	/**
	 * Tell system force use master and slave database (write: master + all slaves,
	 * read: master)
	 */
	USE_BOTH, // Control Switch

	// ------TableModel about options---
	/** Mark alias names for TableModels */
	ALIAS, // Method

	// ------- sharding items -----------
	/** Tell system this is a "SHARD_TABLE" item */
	SHARD_TABLE, // Method

	/** Tell system this is a "SHARD_DATABASE" item */
	SHARD_DATABASE, // Method

	/** GIVE, GIVE_BOTH option are designed for ORM Query */
	GIVE, // Method

	GIVE_BOTH, // Method

	// ------- Entity CURD items -----------
	/** This option is designed for ORM, mark a "IGNORE_NULL" item" */
	IGNORE_NULL // Control Switch

}