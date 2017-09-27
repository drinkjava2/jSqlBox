/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import javax.sql.DataSource;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.utils.DialectUtils;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBoxContext extends DbPro {
	public static SqlBoxContext defaultContext = null;
	private Dialect dialect;

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		this.dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
		dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
	}

	public SqlBoxContext(DataSource ds, Dialect dialect, ConnectionManager cm) {
		super(ds, cm);
		this.dialect = dialect;
	}

	// =============CRUD methods=====
	public <T> T insert(Object entity) {
		return null;
	}

	public <T> T update(Object entity) {
		return null;
	}

	public <T> T delete(Object entity) {
		return null;
	}

	public <T> T load(Object entity, Object pkey) {
		return null;
	}

	// ========Utils methods=====
	public String[] toCreateDDL(Class<?> pojoClasses) {
		return dialect.toCreateDDL(DialectUtils.pojos2Models(pojoClasses));
	}

	// getter & setter =======

	public Dialect getDialect() {
		return dialect;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	public static SqlBoxContext getDefaultContext() {
		return defaultContext;
	}

	public static void setDefaultContext(SqlBoxContext defaultContext) {
		SqlBoxContext.defaultContext = defaultContext;
	}

}