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
	public static String sqlBoxClassSuffix = "SqlBox";// NOSONAR

	/**
	 * defaultContext is a public static global SqlBoxContext instance, usually
	 * should be set at the beginning
	 */
	public static SqlBoxContext defaultContext = null;// NOSONAR

	/** Database's metaData, stored as virtual TableModel array */
	protected TableModel[] dbMetaTableModels;// Meta data of database

	/** The default EntityNetFactory instance */
	protected EntityNetFactory entityNetBuilder = TinyEntityNetFactory.instance;

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

	protected void setDbMetaTableModels(TableModel[] dbMetaTableModels) {
		this.dbMetaTableModels = dbMetaTableModels;
	}

	public EntityNetFactory getEntityNetBuilder() {
		return entityNetBuilder;
	}

	public void setEntityNetBuilder(EntityNetFactory entityNetBuilder) {
		this.entityNetBuilder = entityNetBuilder;
	}

}