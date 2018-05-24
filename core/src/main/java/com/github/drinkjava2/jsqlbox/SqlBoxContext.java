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
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlHandler;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetBuilder;
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

	/** Dialect of current SqlBoxContext, optional */
	protected Dialect dialect = SqlBoxContextConfig.globalNextDialect;

	/** In SqlMapper style, A guesser needed to guess and execute SQL methods */
	protected SqlMapperGuesser sqlMapperGuesser = SqlBoxContextConfig.globalNextSqlMapperGuesser;
	protected ShardingTool[] shardingTools = SqlBoxContextConfig.globalNextShardingTools;
	protected SnowflakeCreator snowflakeCreator = SqlBoxContextConfig.globalNextSnowflakeCreator;
	private EntityNetBuilder entityNetBuilder = new EntityNetBuilder(this);

	public SqlBoxContext() {
		super();
		this.dialect = SqlBoxContextConfig.globalNextDialect;
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
			this.sqlMapperGuesser = SqlBoxContextConfig.globalNextSqlMapperGuesser;
			this.shardingTools = SqlBoxContextConfig.globalNextShardingTools;
			this.snowflakeCreator = SqlBoxContextConfig.globalNextSnowflakeCreator;
		} else {
			this.dialect = config.getDialect();
			this.sqlMapperGuesser = config.getSqlMapperGuesser();
			this.shardingTools = config.getShardingTools();
			this.snowflakeCreator = config.getSnowflakeCreator();
		}
	}

	protected void coreMethods______________________________() {// NOSONAR
	}

	/** Reset all global SqlBox variants to its old default values */
	public static void resetGlobalVariants() {
		SqlBoxContextConfig.setGlobalNextAllowShowSql(false);
		SqlBoxContextConfig.setGlobalNextMasterSlaveSelect(SqlOption.USE_AUTO);
		SqlBoxContextConfig.setGlobalNextConnectionManager(null);
		SqlBoxContextConfig.setGlobalNextSqlHandlers((SqlHandler[]) null);
		SqlBoxContextConfig.setGlobalNextLogger(DefaultDbProLogger.getLog(ImprovedQueryRunner.class));
		SqlBoxContextConfig.setGlobalNextBatchSize(300);
		SqlBoxContextConfig.setGlobalNextTemplateEngine(BasicSqlTemplate.instance());
		SqlBoxContextConfig.setGlobalNextDialect(null);
		SqlBoxContextConfig.setGlobalNextSpecialSqlItemPreparers(null);
		SqlBoxContextConfig.setGlobalNextSqlMapperGuesser(SqlMapperDefaultGuesser.instance);
		SqlBoxContextConfig
				.setGlobalNextShardingTools(new ShardingTool[] { new ShardingModTool(), new ShardingRangeTool() });
		SqlBoxContextConfig.setGlobalNextIocTool(null);
		globalSqlBoxContext = null;
	}

	/** Shortcut method equal to getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	public static SqlBoxContext getGlobalSqlBoxContext() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	// =========getter & setter =======

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

	public String getShardedTB(Object entityOrClass, Object... shardvalues) {
		String table = SqlBoxContextUtils.getShardedTB(this, entityOrClass, shardvalues);
		if (table == null)
			throw new SqlBoxException("No found ShardingTool can handle target '" + entityOrClass + "' ");
		return table;
	}

	public SqlBoxContext getShardedDB(Object entityOrClass, Object... shardvalues) {
		SqlBoxContext ctx = SqlBoxContextUtils.getShardedDB(this, entityOrClass, shardvalues);
		if (ctx == null)
			throw new SqlBoxException("Not found ShardingTool can handle entity '" + entityOrClass + "' ");
		return ctx;
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
	public <T> T load(Object entityBean, Object... optionalSqlItems) {
		return SqlBoxContextUtils.load(this, entityBean, optionalSqlItems);
	}

	/** Load an entity from database by key, key can be one object or a Map */
	public <T> T loadById(Class<T> entityClass, Object idOrIdMap, Object... optionalSqlItems) {
		return SqlBoxContextUtils.loadById(this, entityClass, idOrIdMap, optionalSqlItems);
	}

	@Override
	protected String handleShardTable(PreparedSQL predSQL, StringBuilder sql, SqlItem item) {
		Object[] params = item.getParameters();
		String table = null;
		if (params.length == 1)
			table = SqlBoxContextUtils.getShardedTB(this, params[0]);
		else if (params.length == 2)
			table = SqlBoxContextUtils.getShardedTB(this, params[0], params[1]);
		else
			table = SqlBoxContextUtils.getShardedTB(this, params[0], params[1], params[2]);
		if (table == null)
			throw new SqlBoxException("No ShardingTool can handle target '" + params[0] + "'");
		else
			sql.append(table);
		return table;
	}

	@Override
	protected DbPro handleShardDatabase(PreparedSQL predSQL, StringBuilder sql, SqlItem item) {
		Object[] params = item.getParameters();
		SqlBoxContext ctx = null;
		if (params.length == 1)
			ctx = SqlBoxContextUtils.getShardedDB(this, params[0]);
		else if (params.length == 2)
			ctx = SqlBoxContextUtils.getShardedDB(this, params[0], params[1]);
		else
			ctx = SqlBoxContextUtils.getShardedDB(this, params[0], params[1], params[2]);
		if (ctx == null)
			throw new SqlBoxException("No ShardingTool can handle target '" + params[0] + "'");
		else
			predSQL.setSwitchTo(ctx);
		return ctx;
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
	protected void entityNetShortcutMethods__________________________() {// NOSONAR
	}

	/** Create a EntityNet by given configurations, load all columns */
	public EntityNet netLoad(Object... configObjects) {
		return entityNetBuilder.loadAll(configObjects);
	}

	/** Create a EntityNet instance but only load PKey and FKeys columns */
	public EntityNet netLoadSketch(Object... configObjects) {
		return entityNetBuilder.loadSketch(configObjects);
	}

	/** Create a EntityNet by given list and netConfigs */
	public EntityNet netCreate(List<Map<String, Object>> listMap, Object... configObjects) {
		return entityNetBuilder.create(listMap, configObjects);
	}

	/** Create a EntityNet by given MapListWrap */
	public EntityNet netCreate(MapListWrap mapListWrap) {
		return entityNetBuilder.create(mapListWrap);
	}

	/** Join list and netConfigs to existed EntityNet */
	public <T> T netJoin(EntityNet net, List<Map<String, Object>> listMap, Object... configObjects) {
		return entityNetBuilder.join(net, listMap, configObjects);
	}

	/** Join MapListWrap to existed EntityNet */
	public <T> T netJoin(EntityNet net, MapListWrap mapListWrap) {
		return entityNetBuilder.join(net, mapListWrap);
	}

	/** Add an entity to existed EntityNet */
	public void netAddEntity(EntityNet net, Object entity) {
		entityNetBuilder.addEntity(net, entity);
	}

	/** Remove an entity from EntityNet */
	public void netRemoveEntity(EntityNet net, Object entity) {
		entityNetBuilder.removeEntity(net, entity);
	}

	/** Update an entity in EntityNet */
	public void netUpdateEntity(EntityNet net, Object entity) {
		entityNetBuilder.updateEntity(net, entity);
	}

	/** Shortcut method, load all entities as list */
	public <T> List<T> netLoadAsEntityList(Class<T> entityClass) {
		return entityNetBuilder.loadAsEntityList(entityClass);
	}

	protected void getteSetters__________________________() {// NOSONAR
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

	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		SqlBoxContext.globalSqlBoxContext = globalSqlBoxContext;
	}

}