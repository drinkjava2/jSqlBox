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

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.RowProcessor;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;

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

	/** The default EntityNetFactory instance */
	protected EntityNetFactory entityNetFactory = Config.getGlobalEntityNetFactory();

	/**
	 * Dialect of current ImprovedQueryRunner, default guessed from DataSource, can
	 * use setDialect() method to change to other dialect, to keep thread-safe, only
	 * subclass can access this variant
	 */
	protected Dialect dialect = Config.getGlobalDialect();

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(Config config) {
		super();
		this.connectionManager = config.getConnectionManager();
		this.dialect = config.getDialect();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.sqlInterceptors = config.getInterceptors();
	}

	public SqlBoxContext(DataSource ds, Config config) {
		super(ds);
		this.connectionManager = config.getConnectionManager();
		this.dialect = config.getDialect();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.sqlInterceptors = config.getInterceptors();
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);
	}

	/** Refresh database's meta data based on current dataSource and dialect */
	public TableModel[] queryDbMetaData() {
		return SqlBoxContextUtils.loadMetaTableModels(this, dialect);
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
	 * Return an empty "" String and save a ThreadLocal netConfig object array in
	 * current thread, it will be used by SqlBoxContext's query methods.
	 */
	public static String netConfig(Object... netConfig) {
		getThreadedSqlInterceptors().add(new EntityNetSqlExplainer(netConfig));
		return "";
	}

	public static RowProcessor netProcessor(Object... netConfig) {
		getThreadedSqlInterceptors().add(new EntityNetSqlExplainer(netConfig));
		return new BasicRowProcessor();
	}

	/**
	 * Create a EntityNet by given configurations, load all columns
	 */
	public <T> T netLoad(Object... configObjects) {
		return entityNetFactory.createEntityNet(this, false, configObjects);
	}

	/**
	 * Create a EntityNet instance but only load PKey and FKeys columns to improve
	 * loading speed
	 */
	public <T> T netLoadSketch(Object... configObjects) {
		return entityNetFactory.createEntityNet(this, true, configObjects);
	}

	/** Create a EntityNet by given list and netConfigs */
	public <T> T netCreate(List<Map<String, Object>> listMap, Object... configObjects) {
		TableModel[] result = EntityNetUtils.joinConfigsModels(this, listMap, configObjects);
		if (result == null || result.length == 0)
			throw new SqlBoxException("No entity class config found");
		return entityNetFactory.createEntityNet(listMap, result);
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

	// getter & setter =======
	public Dialect getDialect() {
		return dialect;
	}

}