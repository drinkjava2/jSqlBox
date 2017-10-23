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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBoxContext extends DbPro {
	public static final SqlBoxLogger LOGGER = SqlBoxLogger.getLog(SqlBoxContext.class);
	public static String sqlBoxClassSuffix = "SqlBox";// NOSONAR
	public static SqlBoxContext defaultContext = null;// NOSONAR
	protected Dialect dialect;
	private SqlBox[] dbMetaBoxes;

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		this.dialect = Dialect.guessDialect(ds);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
		dialect = Dialect.guessDialect(ds);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, Dialect dialect, ConnectionManager cm) {
		super(ds, cm);
		this.dialect = dialect;
		refreshMetaData();
	}

	public void refreshMetaData() {
		dbMetaBoxes = SqlBoxContextUtils.metaDataToModels(this, dialect);
	}

	public TableModel getMetaTableModel(String tableName) {
		if (StrUtils.isEmpty(tableName))
			return null;
		for (SqlBox box : dbMetaBoxes)
			if (tableName.equalsIgnoreCase(box.getTableModel().getTableName()))
				return box.getTableModel();
		return null;
	}

	/**
	 * create a box for class
	 */
	public SqlBox box(Class<?> clazz) {
		return SqlBoxUtils.createSqlBox(this, clazz);
	}
 
	// ================================================================
	// To support special in-line methods like pagin(), net() methods which utilize
	// ThreadLocad variant, here have to override base class QueryRunner's 4
	// query methods, because some important methods in commons-DbUtils is private,
	// hope it can change to protected in future

	/**
	 * Return a empty "" String and save a ThreadLocal pageNumber and pageSize
	 * array in current thread, it will be used by SqlBoxContext's query
	 * methods.
	 */
	public static String pagin(int pageNumber, int pageSize) {
		SpecialSqlUtils.paginationCache.set(new int[] { pageNumber, pageSize });
		return "";
	}

	/**
	 * Return a empty "" String and save a ThreadLocal netConfig object array in
	 * current thread, it will be used by SqlBoxContext's query methods.
	 */
	public static String net(Object... netConfig) {
		SpecialSqlUtils.netObjectConfigCache.set(netConfig);
		return "";
	}

	@Override
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		SpecialSqlUtils.cleanLastNetBoxCache();
		try {
			T t = super.query(conn, SpecialSqlUtils.explainSpecialSql(this, sql), rsh, params);
			SpecialSqlUtils.bindSqlBoxConfigToObject(t);
			return t;
		} finally {
			SpecialSqlUtils.cleanThreadLocalShouldOnlyUseOneTime();
		}
	}

	@Override
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		SpecialSqlUtils.cleanLastNetBoxCache();
		try {
			T t = super.query(conn, SpecialSqlUtils.explainSpecialSql(this, sql), rsh);
			SpecialSqlUtils.bindSqlBoxConfigToObject(t);
			return t;
		} finally {
			SpecialSqlUtils.cleanThreadLocalShouldOnlyUseOneTime();
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		SpecialSqlUtils.cleanLastNetBoxCache();
		try {
			T t = super.query(SpecialSqlUtils.explainSpecialSql(this, sql), rsh, params);
			SpecialSqlUtils.bindSqlBoxConfigToObject(t);
			return t;
		} finally {
			SpecialSqlUtils.cleanThreadLocalShouldOnlyUseOneTime();
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		SpecialSqlUtils.cleanLastNetBoxCache();
		try {
			T t = super.query(SpecialSqlUtils.explainSpecialSql(this, sql), rsh);
			SpecialSqlUtils.bindSqlBoxConfigToObject(t);
			return t;
		} finally {
			SpecialSqlUtils.cleanThreadLocalShouldOnlyUseOneTime();
		}
	}

	// =============CRUD methods=====
	public void insert(Object entity) {
		SqlBoxContextUtils.insert(this, entity);
	}

	public int update(Object entity) {
		return SqlBoxContextUtils.update(this, entity);
	}

	public void delete(Object entity) {
		SqlBoxContextUtils.delete(this, entity);
	}

	public <T> T load(Class<?> entityClass, Object pkey) {
		return SqlBoxContextUtils.load(this, entityClass, pkey);
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

	public SqlBox[] getDbMetaBoxes() {
		return dbMetaBoxes;
	}

	public void setDbMetaBoxes(SqlBox[] dbMetaBoxes) {
		this.dbMetaBoxes = dbMetaBoxes;
	}

}