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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.StatementConfiguration;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProException;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlHandler;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.TableModelUtilsOfDb;
import com.github.drinkjava2.jdialects.converter.BasicJavaConverter;
import com.github.drinkjava2.jdialects.converter.JavaConverter;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ClassUtils;
import com.github.drinkjava2.jdialects.springsrc.utils.CollectionUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
import com.github.drinkjava2.jsqlbox.gtx.GtxInfo;
import com.github.drinkjava2.jsqlbox.gtx.GtxUtils;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.sharding.ShardingModTool;
import com.github.drinkjava2.jsqlbox.sharding.ShardingRangeTool;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * DbContext is extended from DbPro, DbPro is extended from QueryRunner, by this
 * way DbContext have all JDBC methods of QueryRunner and DbPro. <br/>
 * 
 * As a ORM tool, DbContext focus on ORM methods like entity bean's CRUD methods
 * and EntityNet methods.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class DbContext extends DbPro {// NOSONAR

	protected static ShardingTool[] globalNextShardingTools = new ShardingTool[] { new ShardingModTool(),
			new ShardingRangeTool() };
	protected static SnowflakeCreator globalNextSnowflakeCreator = null;
	protected static Object[] globalNextSsModels = null;
	protected static boolean globalNextIgnoreNull = false;
	protected static boolean globalNextIgnoreEmpty = false;
	protected static Object globalNextAuditorGetter = null;

	public static final String NO_GLOBAL_SQLBOXCONTEXT_FOUND = "No default global DbContext found, need use method DbContext.setGlobalDbContext() to set a global default DbContext instance at the beginning of appication.";

	protected static DbContext globalDbContext = new DbContext(); // this is a empty ctx

	protected ShardingTool[] shardingTools = globalNextShardingTools;
	protected SnowflakeCreator snowflakeCreator = globalNextSnowflakeCreator;
	protected TableModel[] tailModels; // TableModels loaded from DB, only used for tail mode
	protected boolean ignoreNull = globalNextIgnoreNull;
	protected boolean ignoreEmpty = globalNextIgnoreEmpty;
	// auditorGetter should have a public Object getCurrentAuditor() method
	protected Object auditorGetter = globalNextAuditorGetter;

	public DbContext() {
		super();
	}

	public DbContext(DataSource ds) {
		super(ds);
	}

	public DbContext(DataSource ds, Dialect dialect) {
		super(ds, dialect);
	}
	
	public DbContext(DataSource ds, StatementConfiguration stmtConfig) {
		super(ds, stmtConfig);
	}

	public DbContext(DataSource ds, Dialect dialect, StatementConfiguration stmtConfig) {
		super(ds, dialect, stmtConfig);
	}

	// ==========================Global Transaction about================
	/** If current GlobalTxCM opened global Transaction */
	public boolean isGtxOpen() {
		return connectionManager instanceof GtxConnectionManager && getGtxManager().isInTransaction();
	}

	/** Get current GtxLockId, should be called inside of a global transaction */
	public GtxInfo getGtxInfo() {
		return (GtxInfo) getGtxManager().getThreadTxInfo();
	}

	/** Get current ConnectionManager and assume it's a GlobalTxCM */
	public GtxConnectionManager getGtxManager() {
		return (GtxConnectionManager) connectionManager;
	}

	// ==========================end=============

	protected void miscMethods______________________________() {// NOSONAR
	}

	/** Reset all global SqlBox variants to default values */
	public static void resetGlobalVariants() {
		setGlobalNextAllowShowSql(false);
		setGlobalNextMasterSlaveOption(SqlOption.USE_AUTO);
		setGlobalNextConnectionManager(TinyTxConnectionManager.instance());
		setGlobalNextSqlHandlers((SqlHandler[]) null);
		setGlobalNextBatchSize(300);
		setGlobalNextDialect(null);
		setGlobalNextShardingTools(new ShardingTool[] { new ShardingModTool(), new ShardingRangeTool() });
		setGlobalNextIgnoreNull(false);
		setGlobalNextIgnoreEmpty(false);
		setGlobalNextAuditorGetter(null);
		globalDbContext = new DbContext();
		Dialect.setGlobalJdbcTypeConverter(new BasicJavaConverter());
	}

	/** Shortcut method equal to getGlobalDbContext() */
	public static DbContext gctx() {
		return DbContext.globalDbContext;
	}

	/** Get the global static DbContext instance */
	public static DbContext getGlobalDbContext() {
		return DbContext.globalDbContext;
	}

	private Method methodOfGetCurrentAuditor = null;

	/** For &#064;CreatedBy and &#064;LastModifiedBy, get current auditor */
	public Object getCurrentAuditor() {
		DbException.assureNotNull(auditorGetter, "Can not call getCurrentAuditor() when auditorGetter is null.");
		Object result = null;
		if (methodOfGetCurrentAuditor == null) {
			methodOfGetCurrentAuditor = ClassUtils.getMethod(auditorGetter.getClass(), "getCurrentAuditor");
		}
		try {
			result = methodOfGetCurrentAuditor.invoke(auditorGetter);
		} catch (Exception e) {
			throw new DialectException("Fail to call auditorGetter's getCurrentAuditor method. ", e);
		}
		return result;
	}

	/**
	 * Override DbPro's dealOneSqlItem method to deal DbContext's SqlItem
	 */
	@Override
	protected boolean dealOneSqlItem(boolean iXxxStyle, PreparedSQL ps, Object item) {// NOSONAR
		if (super.dealOneSqlItem(iXxxStyle, ps, item)) {
			return true; // if super class DbPro can deal it, let it do
		}
		if (item instanceof SqlOption) {
			if (SqlOption.IGNORE_EMPTY.equals(item)) {
				ps.setIgnoreEmpty(true);
			} else if (SqlOption.IGNORE_NULL.equals(item)) {
				ps.setIgnoreNull(true);
			} else if (SqlOption.AUTO_SQL.equals(item)) {
				DbContextUtils.appendLeftJoinSQL(ps);
			} else {
				return false;
			}
		} else if (item instanceof TableModel) {
			ps.addModel(item);
			DbContextUtils.createLastAutoAliasName(ps);
		} else if (item instanceof Class) {
			ps.addModel(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			DbContextUtils.createLastAutoAliasName(ps);
			return true;
		} else if (item instanceof SqlItem) {
			SqlItem sqItem = (SqlItem) item;
			SqlOption sqlItemType = sqItem.getType();
			if (SqlOption.SHARD_TABLE.equals(sqlItemType)) {
				handleShardTable(ps, sqItem);
			} else if (SqlOption.SHARD_DATABASE.equals(sqlItemType)) {
				handleShardDatabase(ps, sqItem);
			} else if (SqlOption.GIVE.equals(sqlItemType)) {
				Object[] o = ((SqlItem) item).getParameters();
				String[] s = new String[o.length];
				for (int i = 0; i < o.length; i++) {
					s[i] = (String) o[i];
				}
				ps.addGives(s);
			} else if (SqlOption.GIVE_BOTH.equals(sqlItemType)) {
				Object[] a = ((SqlItem) item).getParameters();
				ps.addGives(new String[] { (String) a[0], (String) a[1] });
				ps.addGives(new String[] { (String) a[1], (String) a[0] });
			} else if (SqlOption.ALIAS.equals(sqlItemType)) {
				if (sqItem.getParameters().length == 0) {
					throw new DbException("alias method need parameter");
				}
				ps.setLastAliases((String[]) sqItem.getParameters());// NOSONAR
			} else {
				return SqlOption.TAIL.equals(sqlItemType);
			}
		} else if (item instanceof EntityNet) {
			ps.setEntityNet((EntityNet) item);
			ps.addHandler(new EntityNetHandler());
		} else {
			return false;
		}
		return true;
	}

	/** Get the sharded table name by given shard values */
	public String getShardedTB(Object entityOrClass, Object... shardvalues) {
		String table = DbContextUtils.getShardedTB(this, entityOrClass, shardvalues);
		if (table == null) {
			throw new DbException("No found ShardingTool can handle target '" + entityOrClass + "' ");
		}
		return table;
	}

	/** Get the sharded DB(=DbContext) instance by given shard values */
	public DbContext getShardedDB(Object entityOrClass, Object... shardvalues) {
		DbContext ctx = DbContextUtils.getShardedDB(this, entityOrClass, shardvalues);
		if (ctx == null) {
			throw new DbException("Not found ShardingTool can handle entity '" + entityOrClass + "' ");
		}
		return ctx;
	}

	protected String handleShardTable(PreparedSQL predSQL, SqlItem item) {
		Object[] params = item.getParameters();
		String table = null;
		if (predSQL.getModels() == null || predSQL.getModels().length == 0) {
			throw new DbException("ShardTable not found model setting");
		}
		TableModel model = (TableModel) predSQL.getModels()[0];
		if (params.length == 1) {
			table = DbContextUtils.getShardedTB(this, model, params[0]);
		} else if (params.length == 2) {
			table = DbContextUtils.getShardedTB(this, model, params[0], params[1]);
		} else {
			throw new DbException("ShardTable need 1 or 2 parameters");
		}
		if (table == null) {
			throw new DbException("No ShardTable Tool found.");
		} else {
			predSQL.addSql(table);
		}
		return table;
	}

	protected DbPro handleShardDatabase(PreparedSQL predSQL, SqlItem item) {
		Object[] params = item.getParameters();
		DbContext ctx = null;
		if (predSQL.getModels() == null || predSQL.getModels().length == 0) {
			return this;
		}
		TableModel model = (TableModel) predSQL.getModels()[0];
		if (params.length == 1) {
			ctx = DbContextUtils.getShardedDB(this, model, params[0]);
		} else if (params.length == 2) {
			ctx = DbContextUtils.getShardedDB(this, model, params[0], params[1]);
		} else {
			throw new DbException("ShardDatabase need 1 or 2 parameters");
		}
		if (ctx == null) {
			throw new DbException("No ShardDatabase Tool found.");
		} else {
			predSQL.setSwitchTo(ctx);
		}
		return ctx;
	}

	private static void checkOnlyOneRowAffected(int result, String curdType) {
		if (result <= 0) {
			throw new DbException("No record found in database when do '" + curdType + "' operation.");
		}
		if (result > 1) {
			throw new DbException("Affect more than 1 row record in database when do '" + curdType + "' operation.");
		}
	}

	/** Use i style to query for an entity list */
	public <T> List<T> qryEntityList(Object... optionItems) {
		return this.qry(new EntityListHandler(), optionItems);
	}

	/** Build a entityNet, only give both between start class and end classes */
	public EntityNet autoNet(Class<?>... entityClass) {
		return DbContextUtils.entityAutoNet(this, entityClass);
	}

	/** If dbModels not loaded, loaded from database */
	public void ensureTailModelLoaded() {
		if (tailModels != null) {
			return;
		}
		reloadTailModels();
	}

	/** Start a transaction on a given locker server */
	public void startTransOnLockDb(int lockDb) {
		this.getConnectionManager().startTransaction();
		GtxConnectionManager gcm = (GtxConnectionManager) this.getConnectionManager();
		GtxInfo gtxInfo = (GtxInfo) gcm.getThreadTxInfo();
		gtxInfo.setLockDb(lockDb);
	}

	/**
	 * Manually call this method to reload tail TableModels when database structure
	 * be changed by DDL command
	 */
	public synchronized void reloadTailModels() {
		DataSource ds = getDataSource();
		DbException.assureNotNull(ds, "Can not load tail TableModels when datasource is null");
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			tailModels = TableModelUtilsOfDb.db2Models(conn, dialect);
		} catch (SQLException e) {
			throw new DbException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					DbException.eatException(e);
				}
			}
		}
	}

	protected void beanCrudMethods______________________________() {// NOSONAR
	}

	/** Insert entity to database, if not 1 row updated, throw SqlBoxException */
	public <T> T entityInsert(T entity, Object... optionItems) {
		int result = DbContextUtils.entityInsertTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "insert");
		return entity;
	}

	/** Update entity in database, if not 1 row updated, throw SqlBoxException */
	public <T> T entityUpdate(Object entity, Object... optionItems) {
		int result = DbContextUtils.entityUpdateTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "update");
		return (T) entity;
	}

	/** Update entity in database, return how many rows affected */
	public int entityUpdateTry(Object entity, Object... optionItems) {
		return DbContextUtils.entityUpdateTry(this, entity, optionItems);
	}

	/** Delete entity in database, if not 1 row deleted, throw SqlBoxException */
	public void entityDelete(Object entity, Object... optionItems) {
		int result = DbContextUtils.entityDeleteTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "delete");
	}

	/** Delete entity in database, return how many rows affected */
	public int entityDeleteTry(Object entity, Object... optionItems) {
		return DbContextUtils.entityDeleteTry(this, entity, optionItems);
	}

	/** Delete entity by given id, if not 1 row deleted, throw SqlBoxException */
	public void entityDeleteById(Class<?> entityClass, Object id, Object... optionItems) {
		int result = DbContextUtils.entityDeleteByIdTry(this, entityClass, id, optionItems);
		checkOnlyOneRowAffected(result, "deleteById");
	}

	/** Delete entity by given id, return how many rows deleted */
	public int entityDeleteByIdTry(Class<?> entityClass, Object id, Object... optionItems) {
		return DbContextUtils.entityDeleteByIdTry(this, entityClass, id, optionItems);
	}

	/** Check if entity exist by its id, all fields should equal in GTX transaction */
	public boolean entityExistStrict(Object entity, Object... optionItems) {
		return DbContextUtils.entityExistStrict(this, entity, optionItems);
	}

	/** Check if entity exist by its id */
	public boolean entityExist(Object entity, Object... optionItems) {
		return DbContextUtils.entityExist(this, entity, optionItems);
	}

	/** Check if entity exist by given id */
	public boolean entityExistById(Class<?> entityClass, Object id, Object... optionItems) {
		return DbContextUtils.entityExistById(this, entityClass, id, optionItems);
	}

	/** Return how many records for current entity class */
	public int entityCount(Class<?> entityClass, Object... optionItems) {
		return DbContextUtils.entityCount(this, entityClass, optionItems);
	}

	/** Load entity according its id, if not 1 row round, throw SqlBoxException */
	public <T> T entityLoad(T entity, Object... optionItems) {
		int result = DbContextUtils.entityLoadTry(this, entity, optionItems);
		checkOnlyOneRowAffected(result, "load");
		return entity;
	}

	/** Load entity according its id, return how many rows found */
	public int entityLoadTry(Object entity, Object... optionItems) {
		return DbContextUtils.entityLoadTry(this, entity, optionItems);
	}

	/** Load entity by given id, if not 1 row found, throw SqlBoxException */
	public <T> T entityLoadById(Class<T> entityClass, Object entityId, Object... optionItems) {
		T entity = DbContextUtils.entityLoadByIdTry(this, entityClass, entityId, optionItems);
		if (entity == null) {
			throw new DbException("No record found in database when do 'LoadById' operation.");
		}
		return entity;
	}

	/**
	 * Load one entity according SQL, if not found or found more than 1, throw
	 * DbException
	 * 这个函数假设有且只能加载一个实体，否则应抛出异常，这样更利于发现编程错误。如果用户不确定数据库有没有，应使用eFindBySQL或eFindAll获取实体列表
	 */
	public <T> T entityLoadBySql(Object... optionItems) {
		List<T> entities = qryEntityList(optionItems);
		if (entities == null || entities.isEmpty())
			throw new DbException("No record found in database when try to load entity.");
		if (entities.size() > 1)
			throw new DbException("More than 1 record found when try to load 1 entity.");
		return entities.get(0);
	}

	/** Load entity by given id, if not found, return null */
	public <T> T entityLoadByIdTry(Class<T> entityClass, Object entityId, Object... optionItems) {
		return DbContextUtils.entityLoadByIdTry(this, entityClass, entityId, optionItems);
	}

	/**
	 * Find all entity of given entity class as List, if not found, return empty
	 * list
	 */
	public <T> List<T> entityFind(Class<T> entityClass, Object... optionItems) {
		return DbContextUtils.entityFind(this, entityClass, optionItems);
	}

	/**
	 * Find entity according SQL, entityClass usually is first param, if not found,
	 * return empty list
	 */
	public <T> List<T> entityFindBySql(Object... optionItems) {
		return qryEntityList(optionItems);
	}

	/** Find entity according SQL, if not found, return null */
	public <T> T entityFindOneBySQL(Object... optionItems) {
		List<T> objects = qryEntityList(optionItems);
		if (CollectionUtils.isEmpty(objects)) {
			return null;
		}
		return objects.get(0);
	}

	/**
	 * Find entity according a sample bean, ignore null fields, if not found, return
	 * empty list
	 */
	public <T> List<T> entityFindBySample(Object sampleBean, Object... optionItems) {
		return DbContextUtils.entityFindBySample(this, sampleBean, optionItems);
	}

	/** Find one related entity by given entity */
	public <E> E entityFindRelatedOne(Object entity, Object... sqlItems) {
		return DbContextUtils.entityFindRelatedOne(this, entity, sqlItems);
	}

	/** Find related entity list by given entity or Iterable */
	public <E> List<E> entityFindRelatedList(Object entityOrIterable, Object... sqlItems) {
		return DbContextUtils.entityFindRelatedList(this, entityOrIterable, sqlItems);
	}

	/** Find related entity set by given entity or Iterable */
	public <E> Set<E> entityFindRelatedSet(Object entity, Object... sqlItems) {
		return DbContextUtils.entityFindRelatedSet(this, entity, sqlItems);
	}

	/** Find related entity map(key is entityID) by given entity or Iterable */
	public <E> Map<Object, E> entityFindRelatedMap(Object entity, Object... sqlItems) {
		return DbContextUtils.entityFindRelatedMap(this, entity, sqlItems);
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

	/** Shortcut call to dialect.toCreateDDL method */
	public String[] toCreateGtxLogDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		TableModel[] mds = new TableModel[entityClasses.length];
		for (int i = 0; i < entityClasses.length; i++) {
			mds[i] = GtxUtils.entity2GtxLogModel(entityClasses[i]);
		}
		return dialect.toCreateDDL(mds);
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

	/** Execute DDL stored in a String array */
	public void executeDDL(String[] sqls) {
		for (String sql : sqls) {
			exe(sql);
		}
	}

	private void assertDialectNotNull() {
		if (dialect == null) {
			throw new DbProException("Try use a dialect method but dialect is null");
		}
	}

	// static global variants setting
	protected void staticGlobalSetMethods______________________() {// NOSONAR
	}

	public static void setGlobalNextAuditorGetter(Object globalNextAuditorGetter) {
		DbContext.globalNextAuditorGetter = globalNextAuditorGetter;
	}

	public static void setGlobalNextIgnoreNull(boolean globalNextIgnoreNull) {
		DbContext.globalNextIgnoreNull = globalNextIgnoreNull;
	}

	public static void setGlobalNextIgnoreEmpty(boolean globalNextIgnoreEmpty) {
		DbContext.globalNextIgnoreEmpty = globalNextIgnoreEmpty;
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

	// ========= Global variants setting=======

	public static void setGlobalDbContext(DbContext globalDbContext) {
		DbContext.globalDbContext = globalDbContext;
	}

	public static void setGlobalJdbcTypeConverter(JavaConverter jdbcTypeConverter) {
		Dialect.setGlobalJdbcTypeConverter(jdbcTypeConverter);
	}
	
	protected void getteSetters__________________________() {// NOSONAR
	}

	// =========getter & setter =======

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

	public TableModel[] getTailModels() {
		return tailModels;
	}

	public void setTailModels(TableModel[] tailModels) {
		this.tailModels = tailModels;
	}

	public boolean isIgnoreNull() {
		return ignoreNull;
	}

	public void setIgnoreNull(boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

	public boolean isIgnoreEmpty() {
		return ignoreEmpty;
	}

	public void setIgnoreEmpty(boolean ignoreEmpty) {
		this.ignoreEmpty = ignoreEmpty;
	}

	public Object getAuditorGetter() {
		return auditorGetter;
	}

	public void setAuditorGetter(Object auditorGetter) {
		this.auditorGetter = auditorGetter;
	}
}