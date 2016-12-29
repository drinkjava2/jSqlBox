/**
 * Copyright (C) 2016 Yong Zhu.
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

package com.github.drinkjava2.jsqlbox;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class Dao {

	private Dao() {
		// Hide default constructor
	}

	// == shortcut methods, just copy some common public static method here======
	public static Integer queryForInteger(String... sql) {
		return SqlBox.box().queryForInteger(sql);
	}

	public static String queryForString(String... sql) {
		return SqlBox.box().queryForString(sql);
	}

	public static <T> T queryForObject(Class<?> clazz, String... sql) {
		return SqlBox.box().queryForObject(clazz, sql);
	}

	public static void cacheSQL(String... sql) {
		SqlBox.box().cacheSQL(sql);
	}

	public static Integer execute(String... sql) {
		return SqlBox.box().execute(sql);
	}

	public static Integer executeInsert(String... sql) {
		return SqlBox.box().executeInsert(sql);
	}

	public static Integer executeQuiet(String... sql) {
		return SqlBox.box().executeQuiet(sql);
	}

	public static void executeCachedSQLs() {
		SqlBox.box().executeCachedSQLs();
	}

	public static JdbcTemplate getDefaultJdbc() {
		return SqlBox.box().getJdbc();
	}

	public static SqlBoxContext getDefaultContext() {
		return SqlBox.box().getContext();
	}

	public static DatabaseType getDefaultDatabaseType() {
		return SqlBox.box().getDatabaseType();
	}

	public static void refreshMetaData() {
		SqlBox.box().refreshMetaData();
	}

	public static <T> T load(Class<?> entityOrBoxClass, Object entityID) {
		T bean = (T) SqlBoxContext.getDefaultSqlBoxContext().createEntity(entityOrBoxClass);
		SqlBox box = SqlBox.getBox(bean);
		return box.load(entityID);
	}

	public static <T> List<T> queryForList(Class<?> dbClass, String... sql) {
		return SqlBox.box().queryForList(dbClass, sql);
	}

	public static <T> T createEntity(Class<?> beanOrSqlBoxClass) {
		return SqlBoxContext.getDefaultSqlBoxContext().createEntity(beanOrSqlBoxClass);
	}

}
