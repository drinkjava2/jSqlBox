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
	SQL,

	/** Append a parameter or parameter array */
	PARAM,

	/** Append a "?" String and append a parameter or parameter array */
	QUESTION_PARAM,

	/** BIND Key-Values pairs "key1,value1, key2,value2..." for SqlTemplateEngine */
	BIND,

	/**
	 * Usage: NOT_NUL("user_name=?", name), when name is null, nothing will be
	 * appended into SQL and parameters, otherwise return a "user_name=?" String and
	 * a SQL parameter
	 */
	NOT_NULL,

	/** Append a " values(?,?,?....?)" String at end of SQL */
	VALUES_QUESTIONS,

	// ----------Special Items ------------
	/** Switch to another DbPro or subClass (SqlBoxContext) to execute SQL */
	SWITCHTO,

	/** Give one or more class as parameter, instance will created by IocTool */
	IOC,

	/** Disable handles according given handlers' class */
	DISABLE_HANDLERS,

	/** Disable handles according given handlers' class */
	ENABLE_HANDLERS,

	/** Force use template style */
	USE_TEMPLATE,

	// ------Sql Operation type--------
	/** It's a EXECUTE type SQL */
	EXECUTE,

	/** It's a UPDATE type SQL */
	UPDATE,

	/** It's a INSERT type SQL */
	INSERT,

	/** It's a QUERY type SQL */
	QUERY,

	/** OTHER type SqlItem used to store some other items */
	OTHER,

	// ================================================================
	// Below items designed for jSqlBox or other projects to explain
	// ================================================================

	// ------Master_Slave Options-------
	/**
	 * Tell system to choose master or slave database automatically (write:master,
	 * read:if in Transaction use master otherwise use one slave)
	 */
	USE_AUTO,

	/** Tell system force use master database (write and read:master ) */
	USE_MASTER,

	/** Tell system force use slave database (write:all slaves, read:one slave) */
	USE_SLAVE,

	/**
	 * Tell system force use master and slave database (write: master + all slaves,
	 * read: master)
	 */
	USE_BOTH,

	// ------TableModel about options---
	/** Mark alias names for TableModels */
	ALIAS,

	// ------- sharding items -----------
	/** Tell system this is a "SHARD_TABLE" item */
	SHARD_TABLE,

	/** Tell system this is a "SHARD_DATABASE" item */
	SHARD_DATABASE,

	/** GIVE, GIVE_BOTH option are designed for ORM query */
	GIVE,

	GIVE_BOTH,

	// ------- Entity CURD items -----------
	/**
	 * This option is designed for ORM insert and update CURD method, mark a
	 * "IGNORE_NULL" item"
	 */
	IGNORE_NULL,

	// ------- Entity CURD items -----------
	/** This option is designed for ORM, will be parsed as a left join SQL */
	AUTO_SQL
}