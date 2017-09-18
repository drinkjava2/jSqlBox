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
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBoxContext {
	private DbPro db;
	private Dialect dialect;

	public SqlBoxContext() {
		db = new DbPro();
	}

	public SqlBoxContext(DataSource ds) {
		db = new DbPro(ds);
	}

	public SqlBoxContext(DataSource ds, ConnectionManager cm) {
		db = new DbPro(ds, cm);
	}

	// getter & setter =======
	public DbPro getDb() {
		return db;
	}

	public void setDb(DbPro db) {
		this.db = db;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

}