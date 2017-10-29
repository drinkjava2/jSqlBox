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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jentitynet.EntityNet;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * SqlBoxContext is extended from DbPro, DbPro is extended from QueryRunner, by
 * this way SqlBoxContext have all JDBC methods of QueryRunner and DbPro. <br/>
 * 
 * As a ORM tool, SqlBoxContext only focus on ORM methods like entity bean's
 * CRUD methods.
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

	private EntityNet loadKeyOrFullNet(boolean loadKeyOnly, Object... netConfigs) {
		if (netConfigs == null || netConfigs.length == 0)
			throw new SqlBoxException("LoadNet() does not support empty netConfigs parameter");
		TableModel[] models = EntityNetSqlExplainer.objectConfigsToModels(this, netConfigs);
		EntityNet net = new EntityNet();
		String starOrSharp = loadKeyOnly ? ".##" : ".**";
		for (TableModel t : models) {
			List<Map<String, Object>> mapList = null;
			String alias = t.getAlias();
			if (StrUtils.isEmpty(alias))
				alias = t.getTableName();
			try {
				mapList = this.nQuery(new MapListHandler(netProcessor(t)),
						"select " + alias + starOrSharp + " from " + t.getTableName() + " as " + alias);
			} finally {
				EntityNetSqlExplainer.netConfigBindToListCache.get().remove(mapList);
			}
			net.joinList(mapList, t);
		}
		return net;
	}

	/** Load EntityNet from database */
	public EntityNet loadKeyNet(Object... netConfigs) {
		return loadKeyOrFullNet(true, netConfigs);
	}

	/** Load EntityNet from database */
	public EntityNet loadNet(Object... netConfigs) {
		return loadKeyOrFullNet(false, netConfigs);
	}

	/** Build a EntityNet from given list and netConfigs */
	public EntityNet buildNet(List<Map<String, Object>> listMap, Object... netConfigs) {
		try {
			TableModel[] result = joinConfigsIntoModels(listMap, netConfigs);
			if(result==null||result.length==0)
				throw new SqlBoxException("No entity class config found");
			return new EntityNet(listMap, result);
		} finally {
			EntityNetSqlExplainer.netConfigBindToListCache.get().remove(listMap);
		}
	}

	private TableModel[] joinConfigsIntoModels(List<Map<String, Object>> listMap, Object... netConfigs) {
		// bindeds: tableModels entityClass and alias may be empty
		// setted: tableModels should have entityClass, alias may be null
		TableModel[] bindeds = (TableModel[]) EntityNetSqlExplainer.netConfigBindToListCache.get().get(listMap);
		if (bindeds == null || bindeds.length == 0)
			bindeds = new TableModel[0];

		TableModel[] setteds;
		if (netConfigs != null && netConfigs.length > 0)
			setteds = EntityNetSqlExplainer.objectConfigsToModels(this, netConfigs);
		else
			setteds = new TableModel[0];

		// check setted to avoid user set empty value to TableModel
		Map<String, TableModel> uses = new HashMap<String, TableModel>();
		for (TableModel tb : setteds) {
			SqlBoxException.assureNotNull(tb.getEntityClass(),
					"EntityClass setting can not be null for '" + tb.getTableName() + "'");
			SqlBoxException.assureNotEmpty(tb.getTableName(),
					"TableName setting can not be empty for '" + tb.getTableName() + "'");
			uses.put(tb.getTableName().toLowerCase(), tb);
		}

		for (TableModel tb : bindeds) {
			SqlBoxException.assureNotEmpty(tb.getTableName(),
					"TableName setting can not be empty for '" + tb.getTableName() + "'");
			TableModel exist = uses.get(tb.getTableName().toLowerCase());
			if (tb.getEntityClass() != null) {// it's binded by has entityClass
				if (exist == null)
					uses.put(tb.getTableName().toLowerCase(), tb);
				else // exist and current tb both can use, duplicated
					throw new SqlBoxException("Duplicated entityClass setting for '" + tb.getTableName() + "'");
			}
		}

		for (TableModel tb : bindeds) { // use alias to fill
			TableModel exist = uses.get(tb.getTableName().toLowerCase());
			if (exist != null && tb.getEntityClass() == null) {// it's binded by has entityClass
				String alias = tb.getAlias();
				if (!StrUtils.isEmpty(alias) && StrUtils.isEmpty(exist.getAlias()))
					exist.setAlias(alias);
			}
		}
		TableModel[] result = new TableModel[uses.size()];
		int i = 0;
		for (Entry<String, TableModel> entry : uses.entrySet()) {
			result[i++] = entry.getValue();
		}
		return result;
	}

	/** Join list and netConfigs to existed EntityNet */
	public EntityNet joinNet(EntityNet net, List<Map<String, Object>> listMap, Object... netConfigs) {
		try {
			TableModel[] result = joinConfigsIntoModels(listMap, netConfigs);
			return net.joinList(listMap, result);
		} finally {
			EntityNetSqlExplainer.netConfigBindToListCache.get().remove(listMap);
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