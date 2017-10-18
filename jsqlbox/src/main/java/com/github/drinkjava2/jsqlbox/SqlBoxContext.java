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
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.StrUtils;
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
	protected static ThreadLocal<int[]> paginationCache = new ThreadLocal<int[]>();
	protected static ThreadLocal<Object[]> netConfigCache = new ThreadLocal<Object[]>();

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

	// === Rewrite all query methods to support special Threadlocal variants
	// explain ==
	/**
	 * Return a empty "" String and save a Threadlocal pageNumber and pageSize
	 * array in current thread, it will be used by SqlBoxContext's query
	 * methods.
	 */
	public static String pagin(int pageNumber, int pageSize) {
		paginationCache.set(new int[] { pageNumber, pageSize });
		return "";
	}

	/**
	 * Return a empty "" String and save a Threadlocal netConfig object array in
	 * current thread, it will be used by SqlBoxContext's query methods.
	 */
	public static String net(Object... netConfig) {
		netConfigCache.set(netConfig);
		return "";
	}

	@Override
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			return super.query(conn, sql, rsh, params);
		} finally {
			paginationCache.set(null);
			netConfigCache.set(null);
		}
	}

	@Override
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			return super.query(conn, sql, rsh);
		} finally {
			paginationCache.set(null);
			netConfigCache.set(null);
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) {
		try {
			return super.nQuery(SqlBoxContextUtils.explainThreadLocal(this, sql), rsh, params);
		} finally {
			paginationCache.set(null);
			netConfigCache.set(null);
		}
	}

	@Override
	public <T> T nQuery(String sql, ResultSetHandler<T> rsh, Object... params) {
		try {
			return super.nQuery(SqlBoxContextUtils.explainThreadLocal(this, sql), rsh, params);
		} finally {
			paginationCache.set(null);
			netConfigCache.set(null);
		}
	}

	@Override
	public <T> T iQuery(ResultSetHandler<T> rsh, String... inlineSQL) {
		try {
			return super.iQuery(rsh, SqlBoxContextUtils.explainThreadLocal(this, inlineSQL));
		} finally {
			paginationCache.set(null);
			netConfigCache.set(null);
		}
	}

	@Override
	public <T> T tQuery(ResultSetHandler<T> rsh, String... templateSQL) {
		try {
			return super.tQuery(rsh, SqlBoxContextUtils.explainThreadLocal(this, templateSQL));
		} finally {
			paginationCache.set(null);
			netConfigCache.set(null);
		}
	}

	// =============CRUD methods=====
	public void insert(Object entity) {
		SqlBoxContextUtils.insert(entity, SqlBoxUtils.findAndBindSqlBox(this, entity));
	}

	public int update(Object entity) {
		return SqlBoxContextUtils.update(entity, SqlBoxUtils.findAndBindSqlBox(this, entity));
	}

	public void delete(Object entity) {
		SqlBoxContextUtils.delete(entity, SqlBoxUtils.findAndBindSqlBox(this, entity));
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