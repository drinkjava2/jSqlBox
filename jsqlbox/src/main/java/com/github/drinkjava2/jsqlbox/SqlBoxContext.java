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

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.RowProcessor;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * SqlBoxContext is extended from DbPro, DbPro is extended from QueryRunner, by
 * this way SqlBoxContext have all JDBC methods of QueryRunner and DbPro. <br/>
 * 
 * As a ORM tool, SqlBoxContext focus on ORM methods like entity bean's CRUD
 * methods and EntityNet methods.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBoxContext extends DbPro {
	public static final SqlBoxLogger LOGGER = SqlBoxLogger.getLog(SqlBoxContext.class);
	public static String sqlBoxClassSuffix = "SqlBox";// NOSONAR
	public static SqlBoxContext defaultContext = null;// NOSONAR
	private Dialect dialect; // dialect
	private TableModel[] dbMetaTableModels;// Meta data of database

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		this.dialect = Dialect.guessDialect(ds);
		this.setPaginator(this.dialect);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
		dialect = Dialect.guessDialect(ds);
		this.setPaginator(this.dialect);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
		this.setPaginator(dialect);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, Dialect dialect, ConnectionManager cm) {
		super(ds, cm);
		this.dialect = dialect;
		this.setPaginator(dialect);
		refreshMetaData();
	}

	public void refreshMetaData() {
		dbMetaTableModels = SqlBoxContextUtils.loadMetaTableModels(this, dialect);
	}

	public TableModel getMetaTableModel(String tableName) {
		if (dbMetaTableModels == null)
			return null;
		for (TableModel model : dbMetaTableModels)
			if (tableName.equalsIgnoreCase(model.getTableName()))
				return model;
		return null;
	}

	/**
	 * create a box for class
	 */
	public SqlBox box(Class<?> clazz) {
		return SqlBoxUtils.createSqlBox(this, clazz);
	}

	// ================================================================
	// To support special in-line methods like net() methods which utilize
	// ThreadLocad variant, here have to override base class QueryRunner's 4
	// query methods, because some important methods in commons-DbUtils is
	// private, hope it can change to protected in future

	/**
	 * Return an empty "" String and save a ThreadLocal netConfig object array in
	 * current thread, it will be used by SqlBoxContext's query methods.
	 */
	public static String net(Object... netConfig) {
		getCurrentExplainers().add(new EntityNetSqlExplainer(netConfig));
		return "";
	}

	public static RowProcessor netProcessor(Object... netConfig) {
		getCurrentExplainers().add(new EntityNetSqlExplainer(netConfig));
		return new BasicRowProcessor();
	}

	/** Load EntityNet from database */
	public EntityNet loadKeyNet(Object... netConfigs) {
		return EntityNetUtils.loadKeyOrFullNet(this, true, netConfigs);
	}

	/** Load EntityNet from database */
	public EntityNet loadNet(Object... netConfigs) {
		return EntityNetUtils.loadKeyOrFullNet(this, false, netConfigs);
	}

	/** Build a EntityNet from given list and netConfigs */
	public EntityNet buildNet(List<Map<String, Object>> listMap, Object... netConfigs) {
		try {
			TableModel[] result = EntityNetUtils.joinConfigsIntoModels(this, listMap, netConfigs);
			if (result == null || result.length == 0)
				throw new SqlBoxException("No entity class config found");
			return new EntityNet(listMap, result);
		} finally {
			EntityNetSqlExplainer.removeBindedTableModel(listMap);
		}
	}

	/** Join list and netConfigs to existed EntityNet */
	public EntityNet joinList(EntityNet net, List<Map<String, Object>> listMap, Object... netConfigs) {
		try {
			TableModel[] result = EntityNetUtils.joinConfigsIntoModels(this, listMap, netConfigs);
			return net.joinList(listMap, result);
		} finally {
			EntityNetSqlExplainer.removeBindedTableModel(listMap);
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
		this.setPaginator(dialect);
	}

	public static SqlBoxContext getDefaultContext() {
		return defaultContext;
	}

	public static void setDefaultContext(SqlBoxContext defaultContext) {
		SqlBoxContext.defaultContext = defaultContext;
	}

	public TableModel[] getDbMetaTableModels() {
		return dbMetaTableModels;
	}

	public void setDbMetaTableModels(TableModel[] dbMetaTableModels) {
		this.dbMetaTableModels = dbMetaTableModels;
	}

}