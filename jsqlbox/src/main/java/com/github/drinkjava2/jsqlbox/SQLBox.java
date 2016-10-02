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

/**
 * For developers: this project is set to each line 120 characters.  
 */

package com.github.drinkjava2.jsqlbox;

import java.util.HashMap;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 * @update 2016-09-28
 */
@SuppressWarnings("unchecked")
public class SQLBox {
	public static final String SQLBoxIdentity = "Box";
	private Class<?> entityClass;
	private String sql;
	private String tablename;
	private HashMap<Object, Column> fields = new HashMap<Object, Column>();
	public static final SQLBoxContext defaultSQLBoxContext = new SQLBoxContext();
	public SQLBoxContext context = defaultSQLBoxContext;

	public static ThreadLocal<HashMap<Object, Object>> poCache = new ThreadLocal<HashMap<Object, Object>>() {

		protected HashMap<Object, Object> initialValue() {
			return new HashMap<Object, Object>();
		}
	};

	// ====================== Utils methods begin======================
	public Object findID(Object o, Class<?> poClass) {
		return null;
	}
	// ====================== Utils methods end======================

	// ====================== CRUD methods begin======================

	public static <T> T create(Class<?> clazz) {
		return defaultSQLBoxContext.create(clazz);
	}

	public <T> T create() {
		return (T) SQLBoxUtils.createProxyPO(entityClass, this, this.context);
	}

	public static <T> T get(Object id) {
		return null;
	}

	public <T> T save() {
		return null;
	}

	public <T> T update() {
		return null;
	}

	public <T> T delete() {
		return null;
	}

	// ====================== CRUD methods end======================

	// ====================== Getter & Setter methods begin======================
	public Class<?> getEntityClass() {
		return entityClass;
	}

	public SQLBox setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
		return this;
	}

	public String getSql() {
		return sql;
	}

	public SQLBox setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public String getTablename() {
		return tablename;
	}

	public SQLBox setTablename(String tablename) {
		this.tablename = tablename;
		return this;
	}

	public SQLBox setField(Object field, Column column) {
		System.out.println(field);
		fields.put(field, column);
		return this;
	}

	public SQLBoxContext getContext() {
		return context;
	}

	public SQLBox setContext(SQLBoxContext context) {
		this.context = context;
		return this;
	}
	// ====================== Getter & Setter methods end======================
}
