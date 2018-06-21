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

import static com.github.drinkjava2.jsqlbox.JSQLBOX.model;

import java.lang.reflect.Method;
import java.util.List;

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
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
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

	public SqlBoxContext() {
		super();
		this.dialect = SqlBoxContextConfig.globalNextDialect;
		copyConfigs(null);
		findAndExecuteInitializer();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		dialect = Dialect.guessDialect(ds);
		copyConfigs(null);
		findAndExecuteInitializer();
	}

	public SqlBoxContext(SqlBoxContextConfig config) {
		super(config);
		copyConfigs(config);
		findAndExecuteInitializer();
	}

	public SqlBoxContext(DataSource ds, SqlBoxContextConfig config) {
		super(ds, config);
		copyConfigs(config);
		if (dialect == null)
			dialect = Dialect.guessDialect(ds);
		findAndExecuteInitializer();
	}

	private void findAndExecuteInitializer() {
		Class<?> callerClass = ClassCacheUtils
				.checkClassExist("com.github.drinkjava2.jsqlbox.SqlBoxContextInitializer");
		if (callerClass == null)
			return;// not found
		try {
			Method initMethod = callerClass.getMethod("initialize", SqlBoxContext.class);
			initMethod.invoke(null, this);
		} catch (Exception e) {
			throw new SqlBoxException("SqlBoxContextInitializer found but failed call it's initialize method.");
		}
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
		SqlBoxContextConfig.setGlobalNextMasterSlaveOption(SqlOption.USE_AUTO);
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

	/**
	 * Override DbPro's dealItem to deal SqlBoxContext's SqlItem
	 */
	@Override
	protected boolean dealItem(boolean iXxxStyle, PreparedSQL ps, StringBuilder sql, Object item) {// NOSONAR
		if (super.dealItem(iXxxStyle, ps, sql, item))
			return true; // if super class DbPro can deal it, let it do
		else if (item instanceof TableModel)
			ps.addModel(item);
		else if (item instanceof ActiveRecordSupport)
			ps.addModel(((ActiveRecordSupport) item).tableModel());
		else if (item instanceof SqlBox)
			ps.addModel(((SqlBox) item).getTableModel());
		else if (item instanceof SqlItem) {
			SqlItem sqItem = (SqlItem) item;
			SqlOption sqlItemType = sqItem.getType();
			if (SqlOption.SHARD_TABLE.equals(sqlItemType))
				handleShardTable(sql, sqItem);
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
			} else if (SqlOption.MODEL.equals(sqlItemType) || SqlOption.MODEL_AUTO_ALIAS.equals(sqlItemType)) {
				Object[] args = sqItem.getParameters();
				if (args.length == 0)
					throw new SqlBoxException("Model item can not be empty");
				for (Object object : args) {
					TableModel t = SqlBoxContextUtils.configToModel(object);
					// if auto alias? for example: UserOrder.class -> UR
					if (SqlOption.MODEL_AUTO_ALIAS.equals(sqlItemType) && StrUtils.isEmpty(t.getAlias()))
						t.setAlias(SqlBoxContextUtils.createAutoAliasNameForEntityClass(t.getEntityClass()));
					ps.addModel(t);
				}
			} else if (SqlOption.MODEL_ALIAS.equals(sqlItemType)) {
				Object[] args = sqItem.getParameters();
				if (args.length < 2)
					throw new SqlBoxException(
							"MODEL_ALIAS item need model1, alias1, model2, alias2... format parameters");
				for (int i = 0; i < args.length / 2; i++) {
					TableModel t = SqlBoxContextUtils.configToModel(args[i * 2]);
					SqlBoxException.assureNotNull(t.getEntityClass(),
							"'entityClass' property not set for model " + t);
					SqlBoxException.assureNotEmpty((String) args[i * 2 + 1],
							"Alias can not be empty for class '" + t.getEntityClass() + "'");
					t.setAlias((String) args[i * 2 + 1]);
					ps.addModel(t);
				}
			} else
				return false;
		} else
			return false;
		return true;
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

	protected String handleShardTable(StringBuilder sql, SqlItem item) {
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

	protected DbPro handleShardDatabase(PreparedSQL predSQL, SqlItem item) {
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

	public <T> List<T> iQueryForEntityList(Object config, Object... optionItems) {
		return this.iQuery(new EntityListHandler(), model(config), optionItems);
	}

	public <T> List<T> pQueryForEntityList(Object config, Object... optionItems) {
		return this.pQuery(new EntityListHandler(), model(config), optionItems);
	}

	public <T> List<T> tQueryForEntityList(Object config, Object... optionItems) {
		return this.tQuery(new EntityListHandler(), model(config), optionItems);
	}

	protected void crudMethods______________________________() {// NOSONAR
	}

	/** Insert an entity to database */
	public void insert(Object entity, Object... optionItems) {
		SqlBoxContextUtils.insert(this, entity, optionItems);
	}

	/** Update an entity in database */
	public int update(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.update(this, entity, optionItems);
	}

	/** Delete an entity in database */
	public void delete(Object entity, Object... optionItems) {
		SqlBoxContextUtils.delete(this, entity, optionItems);
	}

	/** Load an entity from database */
	public <T> T load(Object entity, Object... optionItems) {
		return SqlBoxContextUtils.load(this, entity, optionItems);
	}

	/** Load an entity from database by entityId, entityId can be one object or a Map */
	public <T> T loadById(Object entity, Object entityId, Object... optionItems) {
		return SqlBoxContextUtils.loadById(this, entity, entityId, optionItems);
	}

	/** Load an entity from database by entityId, entityId can be one object or a Map */
	public <T> T loadById(Class<T> entityClass, Object entityId, Object... optionItems) {
		return SqlBoxContextUtils.loadById(this, entityClass, entityId, optionItems);
	}

	/** Load an entity from database by query */
	public <T> T loadByQuery(Class<T> config, Object... optionItems) {
		return SqlBoxContextUtils.loadByQuery(this, config, optionItems);
	}

	/** Load all entity from database */
	public <T> List<T> loadAll(Class<T> config, Object... optionItems) {
		return SqlBoxContextUtils.loadAll(this, config, optionItems);
	}

	// ========== Dialect shortcut methods ===============
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