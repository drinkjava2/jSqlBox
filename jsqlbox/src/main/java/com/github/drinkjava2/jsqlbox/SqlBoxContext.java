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
import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jtinynet.TinyEntityNetFactory;
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
public class SqlBoxContext extends DbPro {// NOSONAR
	/** sqlBoxClassSuffix use to identify the SqlBox configuration class */
	public static final String sqlBoxClassSuffix = "SqlBox";// NOSONAR

	/**
	 * defaultContext is a public static global SqlBoxContext instance, usually
	 * should be set at the beginning
	 */
	public static SqlBoxContext defaultContext = null;// NOSONAR

	/** Database's metaData, stored as virtual TableModel array */
	protected TableModel[] dbMetaTableModels;// Meta data of database

	/** The default EntityNetFactory instance */
	protected EntityNetFactory entityNetBuilder = TinyEntityNetFactory.instance;

	/**
	 * Dialect of current ImprovedQueryRunner, default guessed from DataSource, can
	 * use setDialect() method to change to other dialect, to keep thread-safe, only
	 * subclass can access this variant
	 */
	protected Dialect dialect;

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(SqlTemplateEngine templateEngine) {
		super(templateEngine);
	}

	public SqlBoxContext(DataSource ds, Object... args) {
		super(ds);
		for (Object arg : args) {
			if (arg instanceof ConnectionManager)
				this.connectionManager = (ConnectionManager) arg;
			else if (arg instanceof SqlTemplateEngine)
				this.sqlTemplateEngine = (SqlTemplateEngine) arg;
			else if (arg instanceof Dialect)
				this.dialect = (Dialect) arg;
			else if (arg instanceof DbProLogger)
				this.logger = (DbProLogger) arg;
		}
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);
	}

	/** Refresh database's meta data based on current dataSource and dialect */
	public void refreshMetaData() {
		dbMetaTableModels = SqlBoxContextUtils.loadMetaTableModels(this, dialect);
	}

	protected TableModel getMetaTableModel(String tableName) {
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

	// ========== Dialect shortcut methods ===============
	private void assertDialectNotNull() {
		if (dialect == null)
			throw new DbProRuntimeException("Try use a dialect method but dialect is null");
	}

	/** Shortcut call to dialect.paginate method */
	public String paginate(int pageNumber, int pageSize, String sql) {
		assertDialectNotNull();
		return dialect.paginate(pageNumber, pageSize, sql);
	}

	/** Shortcut call to dialect.translate method */
	public String translate(String sql) {
		assertDialectNotNull();
		return dialect.translate(sql);
	}

	/** Shortcut call to dialect.paginAndTrans method */
	public String paginAndTrans(int pageNumber, int pageSize, String sql) {
		assertDialectNotNull();
		return dialect.paginAndTrans(pageNumber, pageSize, sql);
	}

	/** Shortcut call to dialect.toCreateDDL method */
	public String[] toCreateDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		return dialect.toCreateDDL(entityClasses);
	}

	/** Shortcut call to dialect.toDropDDL method */
	public String[] toDropDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		return dialect.toDropDDL(entityClasses);
	}

	/** Shortcut call to dialect.toDropAndCreateDDL method */
	public String[] toDropAndCreateDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		return dialect.toDropAndCreateDDL(entityClasses);
	}

	/** Shortcut call to dialect.toCreateDDL method */
	public String[] toCreateDDL(TableModel... tables) {
		assertDialectNotNull();
		return dialect.toCreateDDL(tables);
	}

	/** Shortcut call to dialect.toDropDDL method */
	public String[] toDropDDL(TableModel... tables) {
		assertDialectNotNull();
		return dialect.toDropDDL(tables);
	}

	/** Shortcut call to dialect.toDropAndCreateDDL method */
	public String[] toDropAndCreateDDL(TableModel... tables) {
		assertDialectNotNull();
		return dialect.toDropAndCreateDDL(tables);
	}

	// ================================================================
	/**
	 * Return an empty "" String and save a ThreadLocal netConfig object array in
	 * current thread, it will be used by SqlBoxContext's query methods.
	 */
	public static String netConfig(Object... netConfig) {
		getCurrentExplainers().add(new EntityNetSqlExplainer(netConfig));
		return "";
	}

	public static RowProcessor netProcessor(Object... netConfig) {
		getCurrentExplainers().add(new EntityNetSqlExplainer(netConfig));
		return new BasicRowProcessor();
	}

	/**
	 * Create a EntityNet by given configurations, load all columns
	 */
	public <T> T netLoad(Object... configObjects) {
		return entityNetBuilder.createEntityNet(this, false, configObjects);
	}

	/**
	 * Create a EntityNet instance but only load PKey and FKeys columns to improve
	 * loading speed
	 */
	public <T> T netLoadSketch(Object... configObjects) {
		return entityNetBuilder.createEntityNet(this, true, configObjects);
	}

	/** Create a EntityNet by given list and netConfigs */
	public <T> T netCreate(List<Map<String, Object>> listMap, Object... configObjects) {
		TableModel[] result = EntityNetUtils.joinConfigsModels(this, listMap, configObjects);
		if (result == null || result.length == 0)
			throw new SqlBoxException("No entity class config found");
		return entityNetBuilder.createEntityNet(listMap, result);
	}

	/** Join list and netConfigs to existed EntityNet */
	@SuppressWarnings("unchecked")
	public <T> T netJoinList(EntityNet net, List<Map<String, Object>> listMap, Object... configObjects) {
		try {
			TableModel[] result = EntityNetUtils.joinConfigsModels(this, listMap, configObjects);
			return (T) net.addMapList(listMap, result);
		} finally {
			EntityNetSqlExplainer.removeBindedTableModel(listMap);
		}
	}

	/** Add an entity to existed EntityNet */
	public void netAddEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.getBindedBox(entity);
		if (box == null)
			box = SqlBoxUtils.createSqlBox(this, entity.getClass());
		net.addEntity(entity, box.getTableModel());
	}

	/** Remove an entity from EntityNet */
	public void netRemoveEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.getBindedBox(entity);
		if (box == null)
			box = SqlBoxUtils.createSqlBox(this, entity.getClass());
		net.removeEntity(entity, box.getTableModel());
	}

	/** Update an entity in EntityNet */
	public void netUpdateEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.getBindedBox(entity);
		if (box == null)
			box = SqlBoxUtils.createSqlBox(this, entity.getClass());
		net.updateEntity(entity, box.getTableModel());
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

	public static SqlBoxContext getDefaultContext() {
		return defaultContext;
	}

	public static void setDefaultContext(SqlBoxContext defaultContext) {
		SqlBoxContext.defaultContext = defaultContext;
	}

	protected TableModel[] getDbMetaTableModels() {
		return dbMetaTableModels;
	}

	public EntityNetFactory getEntityNetBuilder() {
		return entityNetBuilder;
	}

	public Dialect getDialect() {
		return dialect;
	}

}