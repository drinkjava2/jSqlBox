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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jbeanbox.springsrc.StringUtils;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProLogger.DefaultDbProLogger;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetFactory;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetUtils;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;
import com.github.drinkjava2.jsqlbox.sharding.ShardingModTool;
import com.github.drinkjava2.jsqlbox.sharding.ShardingRangeTool;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;

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

	/** SQLBOX_SUFFIX use to identify the SqlBox configuration class */
	public static final String SQLBOX_SUFFIX = "SqlBox";// NOSONAR

	protected static SqlBoxContext globalSqlBoxContext = null;
	protected static Dialect globalNextDialect = null;
	protected static SqlMapperGuesser globalNextSqlMapperGuesser = SqlMapperDefaultGuesser.instance;
	protected static ShardingTool[] globalNextShardingTools = new ShardingTool[] { new ShardingModTool(),
			new ShardingRangeTool() };
	protected static SnowflakeCreator globalNextSnowflakeCreator = null;

	/** Dialect of current SqlBoxContext, optional */
	protected Dialect dialect = globalNextDialect;

	/** In SqlMapper style, A guesser needed to guess and execute SQL methods */
	protected SqlMapperGuesser sqlMapperGuesser = globalNextSqlMapperGuesser;
	protected ShardingTool[] shardingTools = globalNextShardingTools;
	protected SnowflakeCreator snowflakeCreator = globalNextSnowflakeCreator;

	public SqlBoxContext() {
		super();
		this.dialect = globalNextDialect;
		copyConfigs(null);
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		dialect = Dialect.guessDialect(ds);
		copyConfigs(null);
	}

	public SqlBoxContext(SqlBoxContextConfig config) {
		super(config);
		copyConfigs(config);
	}

	public SqlBoxContext(DataSource ds, SqlBoxContextConfig config) {
		super(ds, config);
		copyConfigs(config);
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);
	}

	private void copyConfigs(SqlBoxContextConfig config) {
		if (config == null) {
			this.sqlMapperGuesser = globalNextSqlMapperGuesser;
			this.shardingTools = globalNextShardingTools;
			this.snowflakeCreator = globalNextSnowflakeCreator;
		} else {
			this.dialect = config.getDialect();
			this.sqlMapperGuesser = config.getSqlMapperGuesser();
			this.shardingTools = config.getShardingTools();
			this.snowflakeCreator = globalNextSnowflakeCreator;
		}
	}

	protected void coreMethods______________________________() {// NOSONAR
	}

	// =========getter & setter =======

	public Dialect getDialect() {
		return dialect;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setDialect(Dialect dialect) {// NOSONAR
		this.dialect = dialect;
	}

	public SqlMapperGuesser getSqlMapperGuesser() {
		return sqlMapperGuesser;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSqlMapperGuesser(SqlMapperGuesser sqlMapperGuesser) {// NOSONAR
		this.sqlMapperGuesser = sqlMapperGuesser;
	}

	public ShardingTool[] getShardingTools() {
		return shardingTools;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setShardingTools(ShardingTool[] shardingTools) {
		this.shardingTools = shardingTools;
	}

	public SnowflakeCreator getSnowflakeCreator() {
		return snowflakeCreator;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSnowflakeCreator(SnowflakeCreator snowflakeCreator) {
		this.snowflakeCreator = snowflakeCreator;
	}

	/**
	 * Get the SqlBox instance binded to this entityBean, if no, create a new
	 * one and bind on entityBean
	 */
	public SqlBox getSqlBox(Object entityBean) {
		return SqlBoxUtils.findAndBindSqlBox(this, entityBean);
	}

	/**
	 * Create a subClass instance of a abstract ActiveRecordSupport class based
	 * on default global SqlBoxContext
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
	 * Create a subClass instance of a abstract ActiveRecordSupport class based
	 * on given SqlBoxContext
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

	public SqlItem shardTable(Object entityOrClass, Object... shardKey) {
		return new SqlItem(SqlOption.SHARD_TABLE, entityOrClass, shardKey, null);
	}

	public SqlItem shardTable(Object entityOrClass, Object shardKey1, Object shardKey2) {
		return new SqlItem(SqlOption.SHARD_TABLE, entityOrClass, shardKey1, shardKey2);
	}

	public SqlItem shardTableStr(Object entityOrClass, Object... shardKey) {
		return new SqlItem(SqlOption.SHARD_TABLE, entityOrClass, shardKey, null);
	}

	@Override
	protected void handleShardTable(PreparedSQL predSQL, StringBuilder sql, SqlItem item) {
 		String table = SqlBoxContextUtils.handleShardTable(this, item.getParameters()[0], item.getParameters()[1], item.getParameters()[2]);
		if (table == null)//TODO here
			xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
			throw new SqlBoxException(
					"No ShardingTool can handle target '" + Arrays.deepToString(item.getParameters()) + "'");
	}
 

	@Override
	protected void handleShardDatabase(PreparedSQL predSQL, StringBuilder sql, SqlItem item) {
		if (this.getMasters() == null || this.getMasters().length == 0)
			throw new SqlBoxException(
					"Current SqlBoxContext did not set masters property but try do shardDatabase opertation.");
		Object entityOrClass = item.getParameters()[0];
		Object shardKey1 = item.getParameters()[1];
		Object shardKey2 = item.getParameters()[2];

		SqlBoxContext ctx = null;
		for (ShardingTool sh : this.getShardingTools()) {
			SqlBoxContext[] result = sh.handleShardDatabase(this, entityOrClass, shardKey1, shardKey2);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException("Can not find master SqlBoxContext for '" + entityOrClass + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 SqlBoxContext tables for target '" + entityOrClass
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardDatabase search condition.");
				ctx = result[0];
				break;
			}
		}
		if (ctx == null)
			throw new SqlBoxException(
					"No ShardingTool can handle target '" + Arrays.deepToString(item.getParameters()) + "'");
		else predSQL.setSwitchTo(ctx);
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
	 * Create a EntityNet instance but only load PKey and FKeys columns to
	 * improve loading speed
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
	public static void resetGlobalVariants() {
		globalNextAllowShowSql = false;
		globalNextMasterSlaveSelect = SqlOption.USE_AUTO;
		globalNextConnectionManager = null;
		globalNextSqlHandlers = null;
		globalNextLogger = DefaultDbProLogger.getLog(ImprovedQueryRunner.class);
		globalNextBatchSize = 300;
		globalNextTemplateEngine = BasicSqlTemplate.instance();
		globalNextDialect = null;
		globalNextSpecialSqlItemPreparers = null;
		globalNextSqlMapperGuesser = SqlMapperDefaultGuesser.instance;
		globalNextShardingTools = new ShardingTool[] { new ShardingModTool(), new ShardingRangeTool() };
		globalSqlBoxContext = null;
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

	public static SnowflakeCreator getGlobalNextSnowflakeCreator() {
		return globalNextSnowflakeCreator;
	}

	public static void setGlobalNextSnowflakeCreator(SnowflakeCreator globalNextSnowflakeCreator) {
		SqlBoxContext.globalNextSnowflakeCreator = globalNextSnowflakeCreator;
	}

}