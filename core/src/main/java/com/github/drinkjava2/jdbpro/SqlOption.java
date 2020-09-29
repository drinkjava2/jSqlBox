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
	/** Mark a SQL SqlItem, i.e. a Sql String piece */
	SQL,

	/** Mark a SQL parameter SqlItem */
	PARAM,

	/** Mark a QUESTION_PARAM SqlItem, i.e. a ? String and a parameter */
	QUESTION_PARAM,

	/**
	 * Mark a Mark a BIND SqlItem, bind Key-Values pairs "key1,value1,
	 * key2,value2..." for SqlTemplateEngine
	 */
	BIND,

	/**
	 * Mark a NOT_NULL SqlItem, Usage: NOT_NUL("user_name=?", name), when name is
	 * null, nothing will be appended into SQL and parameters, otherwise return a
	 * "user_name=?" String and a SQL parameter
	 */
	NOT_NULL,

	/**
	 * Mark a VALUES_QUESTIONS SqlItem, append a " values(?,?,?....?)" String at end
	 * of SQL
	 */
	VALUES_QUESTIONS,

	// ----------Special Items ------------
	/**
	 * Mark a ENABLE_HANDLERS SqlOption, switch to another DbPro or subClass
	 * (DbContext) to execute SQL
	 */
	SWITCHTO,

	/** Mark a ENABLE_HANDLERS SqlOption, disable given handlers */
	DISABLE_HANDLERS,

	/** Mark a ENABLE_HANDLERS SqlOption, enable given handlers */
	ENABLE_HANDLERS,

	// ------Sql Operation type--------
	/** Mark a EXECUTE SqlOption */
	EXECUTE,

	/** Mark a (entity) LOAD SqlOption */
	LOAD,

	/** Mark a (entity) DELETE SqlOption */
	DELETE,

	/** Mark a UPDATE SqlOption */
	UPDATE,

	/** Mark a INSERT SqlOption */
	INSERT,

	/** Mark a QUERY SqlOption */
	QUERY,

	/** Mark a OTHER SqlItem, used to store some other items */
	OTHER,

	// =================================
	// Below items designed for jSqlBox
	// =================================

	// ------Master_Slave Options-------
	/**
	 * Mark a USE_SLAVE SqlOption, tell system to choose master or slave database
	 * automatically (write:master, read:if in Transaction use master otherwise use
	 * one slave)
	 */
	USE_AUTO,

	/** Mark a USE_SLAVE SqlOption, tell system force use master database */
	USE_MASTER,

	/** Mark a USE_SLAVE SqlOption, tell system force use slave database */
	USE_SLAVE,

	/** Mark a USE_BOTH SqlOption, tell system force use master and slave */
	USE_BOTH,

	// ------TableModel about options---
	/** Mark a SqlItem stored alias name(s) for latest TableModel in SQL */
	ALIAS,

	// ------- Sharding about options -----------
	/** Mark a SHARD_TABLE SqlOption */
	SHARD_TABLE,

	/** Mark a SHARD_DATABASE SqlOption */
	SHARD_DATABASE,

	/** Mark a GIVE SqlItem for ORM purpose */
	GIVE,

	/** Mark a GIVE_BOTH SqlItem for ORM purpose */
	GIVE_BOTH,

	// ------- Entity CURD options -----------
	/**
	 * Mark a IGNORE_NULL SqlOption, if true in ORM insert and update method will
	 * ignore null fields
	 */
	IGNORE_NULL,

	/**
	 * Mark a IGNORE_EMPTY SqlOption, if true in ORM insert and update method will
	 * ignore null Object and empty String fields
	 */
	IGNORE_EMPTY,

	/** Mark a AUTO_SQL SqlOption, tell ORM to create left join SQL automatically */
	AUTO_SQL,

	/** Mark a TAIL SqlItem, tell ORM to use this model do CRUD */
	TAIL
}