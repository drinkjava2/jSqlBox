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
 * SqlItemType type is an enum type
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public enum SqlOption {
	SQL, // Build a SQL String piece, need String type followed

	PARAM, // Build SQL parameter or parameters, need Object types followed

	PUT, // Append [key1,value1, key2,value2...] parameters array (for SqlTemplateEngine)

	QUESTION_PARAM, // Append a "?" at end of SQL and append a parameter or parameter array

	NOT_NULL, // Usage: NOT_NUL("user_name=?", name), when name is null, nothing will be
				// appended into SQL and parameters

	VALUES_QUESTIONS, // Append a " values(?,?,?....?)" String at end of SQL

	SWITCHTO, // Switch to another DbPro or subClass instance to run the SQL

	// ------Master_Slave Options-------
	USE_AUTO, // Tell system to choose master or slave database automatically (write:master,
				// read:if in Transaction use master otherwise use on slave)

	USE_MASTER, // Tell system force use master database (write:master, read:master)

	USE_SLAVE, // Tell system force use slave database (write:all slaves, read:one slave)

	USE_BOTH, // Tell system force use master and slave database (write: master + all slaves,
				// read: master)

	// -------Sharding Options--------
	SHARD_TABLE, // Tell system this is a "SHARD_TABLE" SqlItem

	SHARD_DATABASE // Tell system this is a "SHARD_DATABASE" SqlItem
}