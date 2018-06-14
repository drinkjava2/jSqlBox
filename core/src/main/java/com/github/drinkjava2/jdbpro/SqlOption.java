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
 * SqlOption is a property in SqlItem, this property tell system how to analyse
 * a SqlItem
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public enum SqlOption {
	/** A SQL String piece */
	SQL,

	/** SQL parameter or parameters, need Object types followed */
	PARAM,

	/** Append a "?" at end of SQL and append a parameter or parameter array */
	QUESTION_PARAM,
	/** BIND Key-Values pairs "key1,value1, key2,value2..." for SqlTemplateEngine */
	BIND, //

	/**
	 * Usage: NOT_NUL("user_name=?", name), when name is null, nothing will be
	 * appended into SQL and parameters
	 */
	NOT_NULL,

	/** Append a " values(?,?,?....?)" String at end of SQL */
	VALUES_QUESTIONS,

	// ----------Special Items------------
	/**
	 * Switch to another DbPro or subClass(like SqlBoxContext) instance to run the
	 * SQL
	 */
	SWITCHTO,

	/** Object will created by IocTool */
	IOC,

	/** Disable handles according given handlers' class */
	DISABLE_HANDLERS, //

	/** Disable handles according given handlers' class */
	ENABLE_HANDLERS,

	/** Tell system this is a "SHARD_TABLE" SqlItem */
	SHARD_TABLE,

	/** Tell system this is a "SHARD_DATABASE" SqlItem */
	SHARD_DATABASE,

	// =============Control option==================
	/** Force use template style */
	USE_TEMPLATE,

	// ------Master_Slave Options-------
	/**
	 * Tell system to choose master or slave database automatically (write:master,
	 * read:if in Transaction use master otherwise use on slave)
	 */
	USE_AUTO,
	/** Tell system force use master database (write:master, read:master) */
	USE_MASTER,

	/** Tell system force use slave database (write:all slaves, read:one slave) */
	USE_SLAVE, //

	/**
	 * Tell system force use master and slave database (write: master + all slaves,
	 * read: master)
	 */
	USE_BOTH,

	// ------Sql Operation type--------
	/** It's a EXECUTE type SQL */
	EXECUTE,

	/** It's a UPDATE type SQL */
	UPDATE,

	/** It's a INSERT type SQL */
	INSERT,

	/** It's a QUERY type SQL */
	QUERY
}