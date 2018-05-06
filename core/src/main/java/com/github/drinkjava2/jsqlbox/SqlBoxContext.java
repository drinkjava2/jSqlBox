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
import com.github.drinkjava2.jdbpro.DbProLogger.DefaultDbProLogger;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.ShardingTool;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlItemType;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.ColumnModel;
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
	protected static SqlMapperGuesser globalNextSqlMapperGuesser = SqlMapperDefaultGuesser.instance;
	protected static ShardingTool[] globalNextShardingTools = new ShardingTool[] { ShardingModTool.instance,
			ShardingRangeTool.instance };

	/**
	 * Dialect of current SqlBoxContext, optional
	 */
	protected Dialect dialect;

	/** In SqlMapper style, A guesser needed to guess and execute SQL methods */
	protected SqlMapperGuesser sqlMapperGuesser;

	public SqlBoxContext() {
		super();
		this.dialect = globalNextDialect;
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		dialect = Dialect.guessDialect(ds);
		this.sqlMapperGuesser = globalNextSqlMapperGuesser;
		this.shardingTools = globalNextShardingTools;
	}

	public SqlBoxContext(SqlBoxContextConfig config) {
		super(config);
		this.dialect = config.getDialect();
		this.sqlMapperGuesser = config.getSqlMapperGuesser();
		this.shardingTools = config.getShardingTools();
	}

	public SqlBoxContext(DataSource ds, SqlBoxContextConfig config) {
		super(ds, config);
		this.dialect = config.getDialect();
		this.sqlMapperGuesser = config.getSqlMapperGuesser();
		this.shardingTools = config.getShardingTools();
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);
	}

	protected void coreMethods______________________________() {// NOSONAR
	}

	// =========getter & setter =======

	public Dialect getDialect() {
		return dialect;
	}

	/**
	 * This method is not thread safe, so put a "$" at method end to reminder, but
	 * sometimes need use it to change dialect setting
	 */
	public void setDialect$(Dialect dialect) {// NOSONAR
		this.dialect = dialect;
	}

	public SqlMapperGuesser getSqlMapperGuesser() {
		return sqlMapperGuesser;
	}

	@Override
	public ShardingTool[] getShardingTools() {
		return shardingTools;
	}

	@Override
	public void setShardingTools$(ShardingTool[] shardingTools) {
		this.shardingTools = shardingTools;
	}

	/**
	 * This method is not thread safe, so put a "$" at method end to reminder, but
	 * sometimes need use it to change sqlMapperGuesser setting
	 */
	public void setSqlMapperGuesser$(SqlMapperGuesser sqlMapperGuesser) {// NOSONAR
		this.sqlMapperGuesser = sqlMapperGuesser;
	}

	@Override
	protected String getTableNameByShardingSqlItem(SqlItem sqlItem) {
		if (sqlItem == null || sqlItem.getParameters() == null || sqlItem.getParameters().length < 2)
			throw new SqlBoxException("Wrong format of sharding SqlItem '" + sqlItem + "'");
		Object entityOrClass = sqlItem.getParameters()[1];
		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(this, entityOrClass);
		ColumnModel col = t.getShardingColumn();
		if (col == null)
			throw new SqlBoxException("No sharding column setting found for table '" + t.getTableName() + "'");
		String[] setting = col.getShardingSetting();
		if (setting == null || setting.length < 2)
			throw new SqlBoxException("Wrong format of sharding setting of column '" + col.getColumnName()
					+ "' in Table '" + t.getTableName() + "'");
		if (this.getShardingTools() == null)
			throw new SqlBoxException("Want do sharding operation but shardingTools not set, please read user manual.");
		ShardingTool tool = null;
		for (ShardingTool tl : this.getShardingTools())
			if (tl.getStrategyName() != null && tl.getStrategyName().equalsIgnoreCase(setting[0])) {
				tool = tl;
				break;
			}
		if (tool == null)
			throw new SqlBoxException("Try do sharding operation but can not find shardingTool to treate '" + setting[0]
					+ "' type sharding SqlItem.");
		String[] tables = tool.doSharding(this, sqlItem, setting);
		if (tables == null || tables.length == 0)
			throw new SqlBoxException("Did not find sharding table");
		if (tables.length != 1)
			throw new SqlBoxException(
					"Found multiple tables but curren jSqlBox version does not support auto-join, you need manually solve this problem in program.");
		return tables[0];
	}

	/**
	 * Get the SqlBox instance binded to this entityBean, if no, create a new one
	 * and bind on entityBean
	 */
	public SqlBox getSqlBox(Object entityBean) {
		return SqlBoxUtils.findAndBindSqlBox(this, entityBean);
	}

	/**
	 * Create a subClass instance of a abstract ActiveRecordSupport class based on
	 * default global SqlBoxContext
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createMapper(Class<?> abstractClass) {
		Class<?> childClass = SqlMapperUtils.createChildClass(abstractClass);
		try {
			return (T) childClass.newInstance();
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
	}

	/**
	 * Create a subClass instance of a abstract ActiveRecordSupport class based on
	 * given SqlBoxContext
	 */
	public static <T> T createMapper(SqlBoxContext ctx, Class<?> abstractClass) {
		T entity = createMapper(abstractClass);
		SqlBoxUtils.findAndBindSqlBox(ctx, entity);
		return entity;
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
	public void insert(Object entity, Object... optionalSqlItems) {
		SqlBoxContextUtils.insert(this, entity, optionalSqlItems);
	}

	/** Update an entity in database by its ID columns */
	public int update(Object entity, Object... optionalSqlItems) {
		return SqlBoxContextUtils.update(this, entity, optionalSqlItems);
	}

	/** Delete an entity in database by its ID columns */
	public void delete(Object entity, Object... optionalSqlItems) {
		SqlBoxContextUtils.delete(this, entity, optionalSqlItems);
	}

	/** Load an entity from database by key, key can be one object or a Map */
	public <T> T load(Class<?> entityClass, Object pkey, Object... optionalSqlItems) {
		return SqlBoxContextUtils.load(this, entityClass, pkey, optionalSqlItems);
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

	/** Reset all global SqlBox variants to its old default values */
	public static void resetGlobalSqlBoxVariants() {
		globalNextAllowShowSql = false;
		globalNextMasterSlaveSelect = SqlItemType.USE_AUTO;
		globalNextConnectionManager = null;
		globalNextSqlHandlers = null;
		globalNextLogger = DefaultDbProLogger.getLog(ImprovedQueryRunner.class);
		globalNextBatchSize = 300;
		globalNextTemplateEngine = BasicSqlTemplate.instance();
		globalNextDialect = null;
		globalNextSpecialSqlItemPreparers = null;
		globalSqlBoxContext = null;
		globalNextSqlMapperGuesser = SqlMapperDefaultGuesser.instance;
	}

	public static SqlBoxContext getGlobalSqlBoxContext() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	/** Shortcut method equal to getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		SqlBoxContext.globalSqlBoxContext = globalSqlBoxContext;
	}

	/** Return "SqlBox" String */
	public static String getGlobalSqlBoxSuffix() {
		return SqlBoxContext.globalSqlBoxSuffix;
	}

	public static Dialect getGlobalNextDialect() {
		return SqlBoxContext.globalNextDialect;
	}

	public static SqlMapperGuesser getGlobalNextSqlMapperGuesser() {
		return SqlBoxContext.globalNextSqlMapperGuesser;
	}

	public static void setGlobalNextSqlMapperGuesser(SqlMapperGuesser sqlMapperGuesser) {
		SqlBoxContext.globalNextSqlMapperGuesser = sqlMapperGuesser;
	}

	public static void setGlobalNextDialect(Dialect dialect) {
		SqlBoxContext.globalNextDialect = dialect;
	}

	public static ShardingTool[] getGlobalNextShardingTools() {
		return globalNextShardingTools;
	}

	public static void setGlobalNextShardingTools(ShardingTool[] globalNextShardingTools) {
		SqlBoxContext.globalNextShardingTools = globalNextShardingTools;
	}

}