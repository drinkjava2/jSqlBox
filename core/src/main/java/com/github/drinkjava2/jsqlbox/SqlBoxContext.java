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

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProLogger.DefaultDbProLogger;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetFactory;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetUtils;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;

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
	public static final String NO_GLOBAL_SQLBOXCONTEXT_FOUND = "No default global SqlBoxContext found, need use method SqlBoxContext.setGlobalSqlBoxContext() to set a global default SqlBoxContext instance at the beginning of appication.";

	/** globalSqlBoxSuffix use to identify the SqlBox configuration class */
	protected static String globalSqlBoxSuffix = "SqlBox";// NOSONAR

	protected static SqlBoxContext globalSqlBoxContext = null;
	protected static Dialect globalNextDialect = null;
	protected static Guesser globalNextGuesser = Guesser.simpleGuesserInstance;

	/**
	 * Dialect of current SqlBoxContext, optional
	 */
	protected Dialect dialect;

	/** In SqlMapper style, A guesser needed to guess and execute SQL methods */
	protected Guesser guesser = globalNextGuesser;

	public SqlBoxContext() {
		super();
		this.dialect = globalNextDialect;
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(SqlBoxContextConfig config) {
		super();
		this.connectionManager = config.getConnectionManager();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.sqlHandlers = config.getSqlHandlers();
		this.guesser = config.getGuesser();
		this.dialect = config.getDialect();
	}

	public SqlBoxContext(DataSource ds, SqlBoxContextConfig config) {
		super(ds);
		this.connectionManager = config.getConnectionManager();
		this.sqlTemplateEngine = config.getTemplateEngine();
		this.allowShowSQL = config.getAllowSqlSql();
		this.logger = config.getLogger();
		this.batchSize = config.getBatchSize();
		this.sqlHandlers = config.getSqlHandlers();
		this.guesser = config.getGuesser();
		this.iocTool = config.getIocTool();
		this.dialect = config.getDialect();
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);

	}

	protected void coreMethods______________________________() {// NOSONAR
	}

	// =========getter & setter =======
	public Dialect getDialect() {
		return dialect;
	}

	public Guesser getGuesser() {
		return guesser;
	}

	/**
	 * Get the SqlBox instance binded to this entityBean, if no, create a new one
	 * and bind on entityBean
	 */
	public SqlBox getSqlBox(Object entityBean) {
		return SqlBoxUtils.findAndBindSqlBox(this, entityBean);
	}

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

	// ========== Dialect shortcut methods ===============
	// ================================================================
	protected void dialectShortcutMethods__________________________() {// NOSONAR
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

	private void assertDialectNotNull() {
		if (dialect == null)
			throw new DbProRuntimeException("Try use a dialect method but dialect is null");
	}

	// ================================================================
	protected void entityNetAboutMethods__________________________() {// NOSONAR
	}

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
		TableModel[] result = EntityNetUtils.objectConfigsToModels(this, configObjects);
		if (result == null || result.length == 0)
			throw new SqlBoxException("No entity class config found");
		return EntityNetFactory.createEntityNet(listMap, result);
	}

	/** Create a EntityNet by given list and netConfigs */
	public EntityNet netCreate(MapListWrap mapListWrap) {
		if (mapListWrap.getConfig() == null || mapListWrap.getConfig().length == 0)
			throw new SqlBoxException("No entity class config found");
		return EntityNetFactory.createEntityNet(mapListWrap.getMapList(), mapListWrap.getConfig());
	}

	/** Join list and netConfigs to existed EntityNet */
	@SuppressWarnings("unchecked")
	public <T> T netJoinList(EntityNet net, List<Map<String, Object>> listMap, Object... configObjects) {
		TableModel[] result = EntityNetUtils.objectConfigsToModels(this, configObjects);
		return (T) net.addMapList(listMap, result);
	}

	@SuppressWarnings("unchecked")
	public <T> T netJoinList(EntityNet net, MapListWrap mapListWrap) {
		return (T) net.addMapList(mapListWrap.getMapList(), mapListWrap.getConfig());
	}

	/** Add an entity to existed EntityNet */
	public void netAddEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(this, entity);
		net.addEntity(entity, box.getTableModel());
	}

	/** Remove an entity from EntityNet */
	public void netRemoveEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(this, entity);
		net.removeEntity(entity, box.getTableModel());
	}

	/** Update an entity in EntityNet */
	public void netUpdateEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(this, entity);
		net.updateEntity(entity, box.getTableModel());
	}

	/** Shortcut method, load all entities as list */
	public <T> List<T> netLoadAllEntityList(Class<T> entityClass) {
		return this.netLoad(entityClass).getAllEntityList(entityClass);
	}

	protected void publicStaticMethods_____________________() {// NOSONAR
	}

	public static Dialect getGlobalNextDialect() {
		return globalNextDialect;
	}

	public static Guesser getGlobalNextGuesser() {
		return globalNextGuesser;
	}

	public static void setGlobalNextGuesser(Guesser globalNextGuesser) {
		SqlBoxContext.globalNextGuesser = globalNextGuesser;
	}

	public static void setGlobalNextDialect(Dialect dialect) {
		SqlBoxContext.globalNextDialect = dialect;
	}

	public static SqlBoxContext getGlobalSqlBoxContext() {
		return globalSqlBoxContext;
	}

	/** Shortcut method equal to getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		if (globalSqlBoxContext == null)
			throw new SqlBoxException(NO_GLOBAL_SQLBOXCONTEXT_FOUND);
		return globalSqlBoxContext;
	}

	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		SqlBoxContext.globalSqlBoxContext = globalSqlBoxContext;
	}

	/** Return "SqlBox" String */
	public static String getGlobalSqlBoxSuffix() {
		return globalSqlBoxSuffix;
	}

	/** Reset all global SqlBox variants to its old default values */
	public static void resetGlobalSqlBoxVariants() {
		globalNextAllowShowSql = false;
		globalNextConnectionManager = null;
		globalNextSqlHandlers = null;
		globalNextLogger = DefaultDbProLogger.getLog(ImprovedQueryRunner.class);
		globalNextBatchSize = 300;
		globalNextTemplateEngine = BasicSqlTemplate.instance();
		globalNextDialect = null;
		globalNextSpecialSqlItemPreparer = null;
		globalSqlBoxContext = null;

	}

	//@formatter:off
	protected void gxXxxxStylePublicStaticMethods_____________________() {}// NOSONAR 
	public static <T> T giQuery(Object... inlineSQL) {return gctx().iQuery(inlineSQL);}
	public static <T> T giQueryForObject(Object... inlineSQL) {return gctx().iQueryForObject(inlineSQL);}
	public static long giQueryForLongValue(Object... inlineSQL) {return gctx().iQueryForLongValue(inlineSQL);}
	public static String giQueryForString(Object... inlineSQL) {return gctx().iQueryForString(inlineSQL);}
	public static List<Map<String, Object>> giQueryForMapList(Object... items) {return gctx().iQueryForMapList(items);}
	public static int giUpdate(Object... inlineSQL) {return gctx().iUpdate(inlineSQL);}
	public static int giInsert(Object... inlineSQL) {return gctx().iInsert(inlineSQL);}
	public static <T> T giExecute(Object... inlineSQL) {return gctx().iExecute(inlineSQL); }
	 
	public static <T> T gpQuery(Object... inlineSQL) {return gctx().pQuery(inlineSQL);}
	public static <T> T gpQueryForObject(Object... inlineSQL) {return gctx().pQueryForObject(inlineSQL);}
	public static long gpQueryForLongValue(Object... inlineSQL) {return gctx().pQueryForLongValue(inlineSQL);}
	public static String gpQueryForString(Object... inlineSQL) {return gctx().pQueryForString(inlineSQL);}
	public static List<Map<String, Object>> gpQueryForMapList(Object... items) {return gctx().pQueryForMapList(items);}
	public static int gpUpdate(Object... inlineSQL) {return gctx().pUpdate(inlineSQL);}
	public static int gpInsert(Object... inlineSQL) {return gctx().pInsert(inlineSQL);}
	public static <T> T gpExecute(Object... inlineSQL) {return gctx().pExecute(inlineSQL); } 

	public static <T> T gtQuery(Object... items) {return gctx().tQuery(items);}
	public static <T> T gtQueryForObject(Object... items) {return gctx().tQueryForObject(items);}
	public static long gtQueryForLongValue(Object... items) {return gctx().tQueryForLongValue(items);}
	public static String gtQueryForString(Object... items) {return gctx().tQueryForString(items);}
	public static List<Map<String, Object>> gtQueryForMapList(Object... items) {return gctx().tQueryForMapList(items);}
	public static int gtUpdate(Object... items) {return gctx().tUpdate(items);}
	public static int gtInsert(Object... items) {return gctx().tInsert(items);}
	public static <T> T gtExecute(Object... items) {return gctx().tExecute(items);}
	
	public static <T> T gnQuery(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nQuery(conn, rsh, sql, params);}
	public static <T> T gnQueryForObject(Connection conn, String sql, Object... params) {return gctx().nQueryForObject(conn, sql, params);}
	public static String gnQueryForString(Connection conn, String sql, Object... params) {return gctx().nQueryForString(conn, sql, params);}
	public static long gnQueryForLongValue(Connection conn, String sql, Object... params) {return gctx().nQueryForLongValue(conn, sql, params);}
	public static List<Map<String, Object>> gnQueryForMapList(Connection conn, String sql, Object... params) {return gctx().nQueryForMapList(conn, sql, params);}
	public static int gnUpdate(Connection conn, String sql, Object... params) {return gctx().nUpdate(conn, sql, params);}
	public static <T> T gnInsert(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nInsert(conn, rsh, sql, params);}
	public static int gnExecute(Connection conn, String sql, Object... params) {return gctx().nExecute(conn, sql, params);}
	public static <T> List<T> gnExecute(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nExecute(conn, rsh, sql, params);}
	public static <T> T gnQuery(ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nQuery(rsh, sql, params);}
	public static <T> T gnQueryForObject(String sql, Object... params) {return gctx().nQueryForObject(sql, params);}
	public static String gnQueryForString(String sql, Object... params) {return gctx().nQueryForString(sql, params);}
	public static long gnQueryForLongValue(String sql, Object... params) {return gctx().nQueryForLongValue(sql, params);}
	public static List<Map<String, Object>> gnQueryForMapList(String sql, Object... params) {return gctx().nQueryForMapList(sql, params);}
	public static int gnUpdate(String sql, Object... params) {return gctx().nUpdate(sql, params);}
	public static <T> T gnInsert(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... params) {return gctx().nInsert(rsh, sql, params);}
	public static int gnExecute(String sql, Object... params) {return gctx().nExecute(sql, params);}
	public static <T> List<T> gnExecute(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... params) {return gctx().nExecute(rsh, sql, params);}


}