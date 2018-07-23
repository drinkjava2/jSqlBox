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
import java.util.Set;

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
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
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
@SuppressWarnings("unchecked")
public class SqlBoxContext extends DbPro {// NOSONAR
	public static final String NO_GLOBAL_SQLBOXCONTEXT_FOUND = "No default global SqlBoxContext found, need use method SqlBoxContext.setGlobalSqlBoxContext() to set a global default SqlBoxContext instance at the beginning of appication.";

	protected static SqlBoxContext globalSqlBoxContext = null;

	/** Dialect of current SqlBoxContext, optional */
	protected Dialect dialect = SqlBoxContextConfig.globalNextDialect;

	/** In SqlMapper style, A guesser needed to guess and execute SQL methods */
	protected SqlMapperGuesser sqlMapperGuesser = SqlBoxContextConfig.globalNextSqlMapperGuesser;
	protected ShardingTool[] shardingTools = SqlBoxContextConfig.globalNextShardingTools;
	protected SnowflakeCreator snowflakeCreator = SqlBoxContextConfig.globalNextSnowflakeCreator;

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

	protected void miscMethods______________________________() {// NOSONAR
	}

	/** Reset all global SqlBox variants to default values */
	public static void resetGlobalVariants() {
		SqlBoxContextConfig.setGlobalNextAllowShowSql(false);
		SqlBoxContextConfig.setGlobalNextMasterSlaveOption(SqlOption.USE_AUTO);
		SqlBoxContextConfig.setGlobalNextConnectionManager(null);
		SqlBoxContextConfig.setGlobalNextSqlHandlers((SqlHandler[]) null);
		SqlBoxContextConfig.setGlobalNextLogger(DefaultDbProLogger.getLog(ImprovedQueryRunner.class));
		SqlBoxContextConfig.setGlobalNextBatchSize(300);
		SqlBoxContextConfig.setGlobalNextTemplateEngine(BasicSqlTemplate.instance());
		SqlBoxContextConfig.setGlobalNextDialect(null);
		SqlBoxContextConfig.setGlobalNextSqlMapperGuesser(SqlMapperDefaultGuesser.instance);
		SqlBoxContextConfig
				.setGlobalNextShardingTools(new ShardingTool[] { new ShardingModTool(), new ShardingRangeTool() });
		SqlBoxContextConfig.setGlobalNextIocTool(null);
		SqlBoxContextConfig.setGlobalNextSsModels(null);
		globalSqlBoxContext = null;
	}

	/** Shortcut method equal to getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	public static SqlBoxContext getGlobalSqlBoxContext() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	/**
	 * Override DbPro's dealItem to deal SqlBoxContext's SqlItem
	 */
	@Override
	protected boolean dealOneSqlItem(boolean iXxxStyle, PreparedSQL ps, Object item) {// NOSONAR
		if (super.dealOneSqlItem(iXxxStyle, ps, item))
			return true; // if super class DbPro can deal it, let it do
		if (item instanceof SqlOption) {
			if (SqlOption.IGNORE_NULL.equals(item))
				ps.setIgnoreNull(true);
			else if (SqlOption.LEFT_JOIN_SQL.equals(item))
				SqlBoxContextUtils.appendLeftJoinSQL(ps);
			else
				return false;
		} else if (item instanceof TableModel) {
			TableModel t = (TableModel) item;
			SqlBoxException.assureNotNull(t.getEntityClass());
			ps.addModel(item);
			SqlBoxContextUtils.createLastAutoAliasName(ps);
		} else if (item instanceof Class) {
			ps.addModel(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			SqlBoxContextUtils.createLastAutoAliasName(ps);
		} else if (item instanceof SqlItem) {
			SqlItem sqItem = (SqlItem) item;
			SqlOption sqlItemType = sqItem.getType();
			if (SqlOption.SHARD_TABLE.equals(sqlItemType))
				handleShardTable(ps, sqItem);
			else if (SqlOption.SHARD_DATABASE.equals(sqlItemType))
				handleShardDatabase(ps, sqItem);
			else if (SqlOption.GIVE.equals(sqlItemType)) {
				Object[] o = ((SqlItem) item).getParameters();
				String[] s = new String[o.length];
				for (int i = 0; i < o.length; i++)
					s[i] = (String) o[i];
				ps.addGives(s);
			} else if (SqlOption.GIVE_BOTH.equals(sqlItemType)) {
				Object[] a = ((SqlItem) item).getParameters();
				ps.addGives(new String[] { (String) a[0], (String) a[1] });
				ps.addGives(new String[] { (String) a[1], (String) a[0] });
			} else if (SqlOption.ALIAS.equals(sqlItemType)) {
				if (sqItem.getParameters().length == 0)
					throw new SqlBoxException("alias method need parameter");
				ps.setLastAliases((String[]) sqItem.getParameters());// NOSONAR
			} else
				return false;
		} else if (item instanceof EntityNet) {
			ps.setEntityNet((EntityNet) item);
		} else
			return false;
		return true;
	}
	// =========getter & setter =======

	/**
	 * Create a subClass instance of a abstract ActiveRecordSupport class based on
	 * default global SqlBoxContext
	 */
	public static <T> T createMapper(Class<?> abstractClass) {
		Class<?> childClass = SqlMapperUtils.createChildClass(abstractClass);
		try {
			return (T) childClass.newInstance();
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
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

	protected String handleShardTable(PreparedSQL predSQL, SqlItem item) {
		Object[] params = item.getParameters();
		String table = null;
		if (predSQL.getModels() == null || predSQL.getModels().length == 0)
			throw new SqlBoxException("ShardTable not found model setting");
		TableModel model = (TableModel) predSQL.getModels()[0];
		if (params.length == 1)
			table = SqlBoxContextUtils.getShardedTB(this, model, params[0]);
		else if (params.length == 2)
			table = SqlBoxContextUtils.getShardedTB(this, model, params[0], params[1]);
		else
			throw new SqlBoxException("ShardTable need 1 or 2 parameters");
		if (table == null)
			throw new SqlBoxException("No ShardTable Tool found.");
		else
			predSQL.addSql(table);
		return table;
	}

	protected DbPro handleShardDatabase(PreparedSQL predSQL, SqlItem item) {
		Object[] params = item.getParameters();
		SqlBoxContext ctx = null;
		if (predSQL.getModels() == null || predSQL.getModels().length == 0)
			throw new SqlBoxException("ShardTable not found model setting");
		TableModel model = (TableModel) predSQL.getModels()[0];
		if (params.length == 1)
			ctx = SqlBoxContextUtils.getShardedDB(this, model, params[0]);
		else if (params.length == 2)
			ctx = SqlBoxContextUtils.getShardedDB(this, model, params[0], params[1]);
		else
			throw new SqlBoxException("ShardDatabase need 1 or 2 parameters");
		if (ctx == null)
			throw new SqlBoxException("No ShardDatabase Tool found.");
		else
			predSQL.setSwitchTo(ctx);
		return ctx;
	}

	public <T> List<T> iQueryForEntityList(Object... optionItems) {
		return this.iQuery(new EntityListHandler(), optionItems);
	}

	public <T> List<T> pQueryForEntityList(Object... optionItems) {
		return this.pQuery(new EntityListHandler(), optionItems);
	}

	public <T> List<T> tQueryForEntityList(Object... optionItems) {
		return this.tQuery(new EntityListHandler(), optionItems);
	}

	private static void checkOnlyOneRowAffected(int result, String curdType) {
		if (result <= 0)
			throw new SqlBoxException("No record found in database when do '" + curdType + "' operation.");
		if (result > 1)
			throw new SqlBoxException(
					"Affect more than 1 row record in database when do '" + curdType + "' operation.");
	}

	protected void crudMethods______________________________() {// NOSONAR
	}

	public <T> T entityInsert(T entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityTryInsert(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "insert");
		return entity;
	}

	/** Update an entity in database */
	public <T> T entityUpdate(Object entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityTryUpdate(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "update");
		return (T) entity;
	}

	/** Update an entity in database */
	public int entityTryUpdate(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityTryUpdate(this, entity, optionItems);
	}

	/** Delete an entity in database */
	public void entityDelete(Object entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityTryDelete(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "delete");
	}

	/** Delete an entity in database */
	public int entityTryDelete(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityTryDelete(this, entity, optionItems);
	}

	/** Delete an entity in database */
	public boolean entityExist(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityExist(this, entity, optionItems);
	}

	/** Delete an entity in database */
	public void entityDeleteById(Class<?> entityClass, Object id, Object... optionItems) {
		int result = SqlBoxContextUtils.entityTryDeleteById(this, entityClass, id, optionItems);
		checkOnlyOneRowAffected(result, "deleteById");
	}

	/** Delete an entity in database */
	public boolean entityExistById(Class<?> entityClass, Object id, Object... optionItems) {
		return SqlBoxContextUtils.entityExistById(this, entityClass, id, optionItems);
	}

	/** Delete an entity in database */
	public int entityTryDeleteById(Class<?> entityClass, Object id, Object... optionItems) {
		return SqlBoxContextUtils.entityTryDeleteById(this, entityClass, id, optionItems);
	}

	/** Load an entity from database */
	public <T> T entityLoad(T entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityTryLoad(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "insert");
		return entity;
	}

	/** Load an entity from database */
	public int entityTryLoad(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityTryLoad(this, entity, optionItems);
	}

	public int entityCountAll(Class<?> entityClass, Object... optionItems) {
		return SqlBoxContextUtils.entityCountAll(this, entityClass, optionItems);
	}

	/**
	 * Load an entity from database by entityId, entityId can be one object or a Map
	 */
	public <T> T entityLoadById(Class<T> entityClass, Object entityId, Object... optionItems) {
		T entity = SqlBoxContextUtils.entityTryLoadById(this, entityClass, entityId, optionItems);
		if (entity == null)
			throw new SqlBoxException("No record found in database when do 'LoadById' operation.");
		return entity;
	}

	/** Load all entity from database */
	public <T> List<T> entityFindByIds(Class<T> entityClass, Iterable<?> ids, Object... optionItems) {
		return SqlBoxContextUtils.entityFindByIds(this, entityClass, ids, optionItems);
	}

	/**
	 * Load an entity from database by entityId, entityId can be one object or a Map
	 */
	public <T> T entityTryLoadById(Class<T> entityClass, Object entityId, Object... optionItems) {
		return SqlBoxContextUtils.entityTryLoadById(this, entityClass, entityId, optionItems);
	}

	/** Load all entity from database */
	public <T> List<T> entityFindAll(Class<T> entityClass, Object... optionItems) {
		return SqlBoxContextUtils.entityFindAll(this, entityClass, optionItems);
	}

	/** Load all entity from database */
	public <T> List<T> entityFindBySQL(Class<T> entityClass, Object... optionItems) {
		return SqlBoxContextUtils.entityFindBySQL(this, entityClass, optionItems);
	}

	/** Load all entity from database */
	public <T> List<T> entityFindBySample(Object sampleBean, Object... optionItems) {
		return SqlBoxContextUtils.entityFindBySample(this, sampleBean, optionItems);
	}

	protected void ormQueryMethods__________________________() {// NOSONAR
	}

	/** Build a entityNet, only give both between start class and end classes */
	public EntityNet autoEntityNet(Class<?>... entityClass) {
		return SqlBoxContextUtils.autoEntityNet(this, entityClass);
	} 
	/** Find one related entity by given entity */
	public <E> E entityFindOneRelated(Object entity, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindOneRelated(this, entity, sqlItems);
	}

	/** Find related entity list for given entities ( entity or Iterable) */
	public <E> List<E> entityFindRelatedList(Object entity, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindRelatedList(this, entity, sqlItems);
	}

	/** Find related entity set for given entities ( entity or Iterable) */
	public <E> Set<E> entityFindRelatedSet(Object entity, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindRelatedSet(this, entity, sqlItems);
	}

	/** Find related entity map for given entities ( entity or Iterable) */
	public <E> Map<Object, E> entityFindRelatedMap(Object entity, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindRelatedMap(this, entity, sqlItems);
	}

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

	protected void getteSetters__________________________() {// NOSONAR
	}

	// =========getter & setter =======

	public Dialect getDialect() {
		return dialect;
	}

	/** This method is not thread safe, suggest only use at program starting */
	@Deprecated
	public void setDialect(Dialect dialect) {// NOSONAR
		this.dialect = dialect;
	}

	public SqlMapperGuesser getSqlMapperGuesser() {
		return sqlMapperGuesser;
	}

	/** This method is not thread safe, suggest only use at program starting */
	@Deprecated
	public void setSqlMapperGuesser(SqlMapperGuesser sqlMapperGuesser) {// NOSONAR
		this.sqlMapperGuesser = sqlMapperGuesser;
	}

	public ShardingTool[] getShardingTools() {
		return shardingTools;
	}

	/** This method is not thread safe, suggest only use at program starting */
	@Deprecated
	public void setShardingTools(ShardingTool[] shardingTools) {// NOSONAR
		this.shardingTools = shardingTools;
	}

	public SnowflakeCreator getSnowflakeCreator() {
		return snowflakeCreator;
	}

	/** This method is not thread safe, suggest only use at program starting */
	@Deprecated
	public void setSnowflakeCreator(SnowflakeCreator snowflakeCreator) {// NOSONAR
		this.snowflakeCreator = snowflakeCreator;
	}

	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		SqlBoxContext.globalSqlBoxContext = globalSqlBoxContext;
	}

}