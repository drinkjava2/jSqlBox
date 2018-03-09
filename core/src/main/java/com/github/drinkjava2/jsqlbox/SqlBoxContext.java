/*
 * Copyright 2016 the original author or authors.
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

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetFactory;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetUtils;

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

	/** globalSqlBoxSuffix use to identify the SqlBox configuration class */
	private static String globalSqlBoxSuffix = "SqlBox";// NOSONAR
	private static SqlBoxContext globalSqlBoxContext = null;
	private static Dialect globalDialect = null;

	/**
	 * Dialect of current ImprovedQueryRunner, default guessed from DataSource, can
	 * use setDialect() method to change to other dialect, to keep thread-safe, only
	 * subclass can access this variant
	 */
	protected Dialect dialect;

	public SqlBoxContext() {
		super();
		this.dialect = globalDialect;

	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(SqlBoxContextConfig config) {
		super(); 
		this.connectionManager = config.getConnectionManager();
		this.dialect = config.getDialect();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.handlers = config.getHandlers();
	}

	public SqlBoxContext(DataSource ds, SqlBoxContextConfig config) {
		super(ds);
		this.connectionManager = config.getConnectionManager();
		this.dialect = config.getDialect();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.handlers = config.getHandlers();
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);
	}

	// ========== Dialect shortcut methods ===============
	private void assertDialectNotNull() {
		if (dialect == null)
			throw new DbProRuntimeException("Try use a dialect method but dialect is null");
	}

	/** Shortcut call to dialect.pagin method */
	public String pagin(int pageNumber, int pageSize, String sql) {
		assertDialectNotNull();
		return dialect.pagin(pageNumber, pageSize, sql);
	}

	/** Shortcut call to dialect.trans method */
	public String trans(String sql) {
		assertDialectNotNull();
		return dialect.trans(sql);
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
	 * Create a EntityNet by given configurations, load all columns
	 */
	public EntityNet netLoad(Object... configObjects) {
		return EntityNetFactory.createEntityNet(this, false, configObjects);
	}

	/**
	 * Create a EntityNet instance but only load PKey and FKeys columns to improve
	 * loading speed
	 */
	public EntityNet netLoadSketch(Object... configObjects) {
		return EntityNetFactory.createEntityNet(this, true, configObjects);
	}

	/** Create a EntityNet by given list and netConfigs */
	public EntityNet netCreate(List<Map<String, Object>> listMap, Object... configObjects) {
		TableModel[] result = EntityNetUtils.joinConfigsModels(this, listMap, configObjects);
		if (result == null || result.length == 0)
			throw new SqlBoxException("No entity class config found");
		return EntityNetFactory.createEntityNet(listMap, result);
	}

	/** Join list and netConfigs to existed EntityNet */
	@SuppressWarnings("unchecked")
	public <T> T netJoinList(EntityNet net, List<Map<String, Object>> listMap, Object... configObjects) {
		try {
			TableModel[] result = EntityNetUtils.joinConfigsModels(this, listMap, configObjects);
			return (T) net.addMapList(listMap, result);
		} finally {
			EntityNetUtils.removeBindedTableModel(listMap);
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

	/** Create a new instance and bind current SqlBoxContext to it */
	public void create(Class<?> entityClass) {
		Object entity = null;
		try {
			entity = entityClass.newInstance();
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
		SqlBoxUtils.findAndBindSqlBox(this, entity);
	}

	/**
	 * Get the SqlBox instance binded to this entityBean, if no, create a new one
	 * and bind on entityBean
	 */
	public SqlBox getSqlBox(Object entityBean) {
		return SqlBoxUtils.findAndBindSqlBox(this, entityBean);
	}

	/** Insert an entity to database */
	public void insert(Object entity) {
		SqlBoxContextUtils.insert(this, entity);
	}

	/** Update an entity in database by its ID columns */
	public int update(Object entity) {
		return SqlBoxContextUtils.update(this, entity);
	}

	/** Delete an entity in database by its ID columns */
	public void delete(Object entity) {
		SqlBoxContextUtils.delete(this, entity);
	}

	/** Load an entity from database by key, key can be one object or a Map */
	public <T> T load(Class<?> entityClass, Object pkey) {
		return SqlBoxContextUtils.load(this, entityClass, pkey);
	}

	/** Shortcut method, load all entities as list */
	public <T> List<T> nLoadAllEntityList(Class<T> entityClass) {
		return this.netLoad(entityClass).getAllEntityList(entityClass);
	}

	// =========getter & setter =======
	public Dialect getDialect() {
		return dialect;
	}

	// ==========global getter & setter =======

	public static Dialect getGlobalDialect() {
		return globalDialect;
	}

	public static void setGlobalDialect(Dialect globalDialect) {
		SqlBoxContext.globalDialect = globalDialect;
	}

	public static SqlBoxContext getGlobalSqlBoxContext() {
		return globalSqlBoxContext;
	}

	/** Shortcut method equal to SqlBoxContext.getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		return globalSqlBoxContext;
	}

	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		SqlBoxContext.globalSqlBoxContext = globalSqlBoxContext;
	}

	public static String getGlobalsqlboxsuffix() {
		return globalSqlBoxSuffix;
	}

}