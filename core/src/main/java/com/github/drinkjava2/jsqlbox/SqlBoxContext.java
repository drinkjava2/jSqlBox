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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProException;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlHandler;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.TableModelUtilsOfDb;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
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

	protected static Dialect globalNextDialect = null;
	protected static ShardingTool[] globalNextShardingTools = new ShardingTool[] { new ShardingModTool(),
			new ShardingRangeTool() };
	protected static SnowflakeCreator globalNextSnowflakeCreator = null;
	protected static Object[] globalNextSsModels = null;

	public static final String NO_GLOBAL_SQLBOXCONTEXT_FOUND = "No default global SqlBoxContext found, need use method SqlBoxContext.setGlobalSqlBoxContext() to set a global default SqlBoxContext instance at the beginning of appication.";

	protected static SqlBoxContext globalSqlBoxContext = null;

	/** Dialect of current SqlBoxContext, optional */
	protected Dialect dialect = globalNextDialect;

	protected ShardingTool[] shardingTools = globalNextShardingTools;
	protected SnowflakeCreator snowflakeCreator = globalNextSnowflakeCreator;
	protected TableModel[] tailModels; // TableModels loaded from DB, only used for tail mode

	public SqlBoxContext() {
		super();
		this.dialect = globalNextDialect;
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		if (globalNextDialect != null)
			dialect = globalNextDialect;
		else
			dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
	}

	protected void miscMethods______________________________() {// NOSONAR
	}

	/** Reset all global SqlBox variants to default values */
	public static void resetGlobalVariants() {
		setGlobalNextAllowShowSql(false);
		setGlobalNextMasterSlaveOption(SqlOption.USE_AUTO);
		setGlobalNextConnectionManager(null);
		setGlobalNextSqlHandlers((SqlHandler[]) null);
		setGlobalNextBatchSize(300);
		setGlobalNextTemplateEngine(BasicSqlTemplate.instance());
		setGlobalNextDialect(null);
		setGlobalNextShardingTools(new ShardingTool[] { new ShardingModTool(), new ShardingRangeTool() });
		globalSqlBoxContext = null;
	}

	/** Shortcut method equal to getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	/** Get the global static SqlBoxContext instance */
	public static SqlBoxContext getGlobalSqlBoxContext() {
		return SqlBoxContext.globalSqlBoxContext;
	}

	/**
	 * Override DbPro's dealOneSqlItem method to deal SqlBoxContext's SqlItem
	 */
	@Override
	protected boolean dealOneSqlItem(boolean iXxxStyle, PreparedSQL ps, Object item) {// NOSONAR
		if (super.dealOneSqlItem(iXxxStyle, ps, item))
			return true; // if super class DbPro can deal it, let it do
		if (item instanceof SqlOption) {
			if (SqlOption.IGNORE_NULL.equals(item))
				ps.setIgnoreNull(true);
			else if (SqlOption.AUTO_SQL.equals(item))
				SqlBoxContextUtils.appendLeftJoinSQL(ps);
			else
				return false;
		} else if (item instanceof TableModel) {
			ps.addModel(item);
			SqlBoxContextUtils.createLastAutoAliasName(ps);
		} else if (item instanceof Class) {
			ps.addModel(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			SqlBoxContextUtils.createLastAutoAliasName(ps);
			return true;
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
			} else if (SqlOption.TAIL.equals(sqlItemType)) {
				return true; // do nothing
			} else
				return false;
		} else if (item instanceof EntityNet) {
			ps.setEntityNet((EntityNet) item);
			ps.addHandler(new EntityNetHandler());
		} else
			return false;
		return true;
	}

	/** Get the sharded table name by given shard values */
	public String getShardedTB(Object entityOrClass, Object... shardvalues) {
		String table = SqlBoxContextUtils.getShardedTB(this, entityOrClass, shardvalues);
		if (table == null)
			throw new SqlBoxException("No found ShardingTool can handle target '" + entityOrClass + "' ");
		return table;
	}

	/** Get the sharded DB(=SqlBoxContext) instance by given shard values */
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

	private static void checkOnlyOneRowAffected(int result, String curdType) {
		if (result <= 0)
			throw new SqlBoxException("No record found in database when do '" + curdType + "' operation.");
		if (result > 1)
			throw new SqlBoxException(
					"Affect more than 1 row record in database when do '" + curdType + "' operation.");
	}

	/** Use i style to query for an entity list */
	public <T> List<T> iQueryForEntityList(Object... optionItems) {
		return this.iQuery(new EntityListHandler(), optionItems);
	}

	/** Use p style to query for an entity list */
	public <T> List<T> pQueryForEntityList(Object... optionItems) {
		return this.pQuery(new EntityListHandler(), optionItems);
	}

	/** Use t style to query for an entity list */
	public <T> List<T> tQueryForEntityList(Object... optionItems) {
		return this.tQuery(new EntityListHandler(), optionItems);
	}

	/** Build a entityNet, only give both between start class and end classes */
	public EntityNet autoNet(Class<?>... entityClass) {
		return SqlBoxContextUtils.entityAutoNet(this, entityClass);
	}

	/** If dbModels not loaded, loaded from database */
	public void ensureTailModelLoaded() {
		if (tailModels != null)
			return;
		reloadTailModels();
	}

	/**
	 * Manually call this method to reload tail TableModels when database structure
	 * be changed by DDL command
	 */
	public synchronized void reloadTailModels() {
		DataSource ds = getDataSource();
		SqlBoxException.assureNotNull(ds, "Can not load tail TableModels when datasource is null");
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			tailModels = TableModelUtilsOfDb.db2Models(conn, dialect);
		} catch (SQLException e) {
			throw new SqlBoxException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					SqlBoxException.eatException(e);
				}
		}
	}

	protected void entityCrudMethods______________________________() {// NOSONAR
	}

	/** Insert entity to database, if not 1 row updated, throw SqlBoxException */
	public <T> T eInsert(T entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityInsertTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "insert");
		return entity;
	}

	/** Update entity in database, if not 1 row updated, throw SqlBoxException */
	public <T> T eUpdate(Object entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityUpdateTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "update");
		return (T) entity;
	}

	/** Update entity in database, return how many rows affected */
	public int eUpdateTry(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityUpdateTry(this, entity, optionItems);
	}

	/** Delete entity in database, if not 1 row deleted, throw SqlBoxException */
	public void eDelete(Object entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityDeleteTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "delete");
	}

	/** Delete entity in database, return how many rows affected */
	public int eDeleteTry(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityDeleteTry(this, entity, optionItems);
	}

	/** Delete entity by given id, if not 1 row deleted, throw SqlBoxException */
	public void eDeleteById(Class<?> entityClass, Object id, Object... optionItems) {
		int result = SqlBoxContextUtils.entityDeleteByIdTry(this, entityClass, id, optionItems);
		checkOnlyOneRowAffected(result, "deleteById");
	}

	/** Delete entity by given id, return how many rows deleted */
	public int eDeleteByIdTry(Class<?> entityClass, Object id, Object... optionItems) {
		return SqlBoxContextUtils.entityDeleteByIdTry(this, entityClass, id, optionItems);
	}

	/** Check if entity exist by its id */
	public boolean eExist(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityExist(this, entity, optionItems);
	}

	/** Check if entity exist by given id */
	public boolean eExistById(Class<?> entityClass, Object id, Object... optionItems) {
		return SqlBoxContextUtils.entityExistById(this, entityClass, id, optionItems);
	}

	/** Return how many records for current entity class */
	public int eCountAll(Class<?> entityClass, Object... optionItems) {
		return SqlBoxContextUtils.entityCountAll(this, entityClass, optionItems);
	}

	/** Load entity according its id, if not 1 row round, throw SqlBoxException */
	public <T> T eLoad(T entity, Object... optionItems) {
		int result = SqlBoxContextUtils.entityLoadTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "insert");
		return entity;
	}

	/** Load entity according its id, return how many rows found */
	public int eLoadTry(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.entityLoadTry(this, entity, optionItems);
	}

	/** Load entity by given id, if not 1 row found, throw SqlBoxException */
	public <T> T eLoadById(Class<T> entityClass, Object entityId, Object... optionItems) {
		T entity = SqlBoxContextUtils.entityLoadByIdTry(this, entityClass, entityId, optionItems);
		if (entity == null)
			throw new SqlBoxException("No record found in database when do 'LoadById' operation.");
		return entity;
	}

	/** Load one entity according SQL, if not found, return null */
	public <T> T eLoadBySQL(Object... optionItems) {
		List<T> entities = iQueryForEntityList(optionItems);
		if (entities == null || entities.isEmpty())
			throw new SqlBoxException("No record found in database when try to load entity.");
		if (entities.size() > 1)
			throw new SqlBoxException("More than 1 record found when try to load 1 entity.");
		return entities.get(0);
	}

	/** Load entity by given id, if not found, return null */
	public <T> T eLoadByIdTry(Class<T> entityClass, Object entityId, Object... optionItems) {
		return SqlBoxContextUtils.entityLoadByIdTry(this, entityClass, entityId, optionItems);
	}

	/**
	 * Find all entity of given entity class as List, if not found, return empty
	 * list
	 */
	public <T> List<T> eFindAll(Class<T> entityClass, Object... optionItems) {
		return SqlBoxContextUtils.entityFindAll(this, entityClass, optionItems);
	}

	/** Find entity according SQL, if not found, return empty list */
	public <T> List<T> eFindBySQL(Object... optionItems) {
		return iQueryForEntityList(optionItems);
	}

	/**
	 * Find entity according a sample bean, ignore null fields, if not found, return
	 * empty list
	 */
	public <T> List<T> eFindBySample(Object sampleBean, Object... optionItems) {
		return SqlBoxContextUtils.entityFindBySample(this, sampleBean, optionItems);
	}

	/** Find one related entity by given entity */
	public <E> E eFindRelatedOne(Object entity, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindRelatedOne(this, entity, sqlItems);
	}

	/** Find related entity list by given entity or Iterable */
	public <E> List<E> eFindRelatedList(Object entityOrIterable, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindRelatedList(this, entityOrIterable, sqlItems);
	}

	/** Find related entity set by given entity or Iterable */
	public <E> Set<E> eFindRelatedSet(Object entity, Object... sqlItems) {
		return SqlBoxContextUtils.entityFindRelatedSet(this, entity, sqlItems);
	}

	/** Find related entity map(key is entityID) by given entity or Iterable */
	public <E> Map<Object, E> eFindRelatedMap(Object entity, Object... sqlItems) {
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
			throw new DbProException("Try use a dialect method but dialect is null");
	}

	protected void getteSetters__________________________() {// NOSONAR
	}

	// static global variants setting
	protected void staticGlobalNextMethods______________________() {// NOSONAR
	}

	public static Dialect getGlobalNextDialect() {
		return globalNextDialect;
	}

	public static void setGlobalNextDialect(Dialect dialect) {
		globalNextDialect = dialect;
	}

	public static ShardingTool[] getGlobalNextShardingTools() {
		return globalNextShardingTools;
	}

	public static void setGlobalNextShardingTools(ShardingTool[] shardingTools) {
		globalNextShardingTools = shardingTools;
	}

	public static SnowflakeCreator getGlobalNextSnowflakeCreator() {
		return globalNextSnowflakeCreator;
	}

	public static void setGlobalNextSnowflakeCreator(SnowflakeCreator snowflakeCreator) {
		globalNextSnowflakeCreator = snowflakeCreator;
	}

	// =========getter & setter =======

	public Dialect getDialect() {
		return dialect;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setDialect(Dialect dialect) {// NOSONAR
		this.dialect = dialect;
	}

	public ShardingTool[] getShardingTools() {
		return shardingTools;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setShardingTools(ShardingTool[] shardingTools) {// NOSONAR
		this.shardingTools = shardingTools;
	}

	public SnowflakeCreator getSnowflakeCreator() {
		return snowflakeCreator;
	}

	/** This method is not thread safe, suggest only use at program starting */
	public void setSnowflakeCreator(SnowflakeCreator snowflakeCreator) {// NOSONAR
		this.snowflakeCreator = snowflakeCreator;
	}

	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		SqlBoxContext.globalSqlBoxContext = globalSqlBoxContext;
	}

	public TableModel[] getTailModels() {
		return tailModels;
	}

	public void setTailModels(TableModel[] tailModels) {
		this.tailModels = tailModels;
	}

}