/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import static com.github.drinkjava2.jsqlbox.SqlBoxException.throwEX;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBoxContext {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(SqlBoxContext.class);
	private static SqlBoxContext defaultSqlBoxContext;

	// print SQL to console or log depends logging.properties
	private Boolean showSql = false;
	private Boolean formatSql = false;
	private Boolean showQueryResult = false;

	public static final String SQLBOX_IDENTITY = "BOX";

	private JdbcTemplate jdbc = new JdbcTemplate();
	private DataSource dataSource = null;

	private DBMetaData metaData;

	/**
	 * Store paging pageNumber in ThreadLocal
	 */
	protected static ThreadLocal<String> paginationEndCache = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return null;
		}
	};

	/**
	 * Store order by SQL piece, only needed for SQL Server 2005 and later
	 */
	protected static ThreadLocal<String> paginationOrderByCache = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return null;
		}
	};

	/**
	 * Store boxes binded on entities
	 */
	protected static ThreadLocal<Map<Object, SqlBox>> boxCache = new ThreadLocal<Map<Object, SqlBox>>() {
		@Override
		protected Map<Object, SqlBox> initialValue() {
			return new HashMap<>();
		}
	};

	public static final ThreadLocal<HashMap<Object, Object>> classExistCache = new ThreadLocal<HashMap<Object, Object>>() {
		@Override
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<>();
		}
	};

	protected static ThreadLocal<Integer> circleDependencyCache = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};

	public SqlBoxContext() {
		// Default constructor
	}

	/**
	 * Create a SqlBoxContext and register dataSoruce & DB class
	 */
	public SqlBoxContext(DataSource dataSource) {
		this.dataSource = dataSource;
		if (dataSource != null) {
			this.jdbc.setDataSource(dataSource);
			refreshMetaData();
		}
	}

	public static SqlBoxContext getDefaultSqlBoxContext() {
		if (defaultSqlBoxContext == null)
			defaultSqlBoxContext = new SqlBoxContext();
		return defaultSqlBoxContext;

	}

	// ================== getter & setters below============
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set DataSource for SqlBoxContext
	 */
	public SqlBoxContext setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbc.setDataSource(dataSource);
		refreshMetaData();
		return this;
	}

	public Boolean getShowSql() {
		return showSql;
	}

	public SqlBoxContext setShowSql(Boolean showSql) {
		this.showSql = showSql;
		return this;
	}

	public Boolean getFormatSql() {
		return formatSql;
	}

	public SqlBoxContext setFormatSql(Boolean formatSql) {
		this.formatSql = formatSql;
		return this;
	}

	public Boolean getShowQueryResult() {
		return showQueryResult;
	}

	public SqlBoxContext setShowQueryResult(Boolean showQueryResult) {
		this.showQueryResult = showQueryResult;
		return this;
	}

	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public SqlBoxContext setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		return this;
	}

	public DBMetaData getMetaData() {
		return metaData;
	}

	public SqlBoxContext setMetaData(DBMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	/**
	 * Put a box instance into thread local cache for a bean
	 */
	public void bind(Object bean, SqlBox box) {
		if (bean == null)
			throwEX("SqlBoxContext putBox error, entityBean can not be null");
		else {
			box.setEntityBean(bean);
			box.setEntityClass(bean.getClass());
			boxCache.get().put(bean, box);
		}
	}

	/**
	 * Get a box instance from thread local cache for a bean
	 */
	public static SqlBox getBindedBox(Object bean) {
		if (bean == null)
			return (SqlBox) throwEX("SqlBoxContext putBox error, entityBean can not be null");
		else
			return boxCache.get().get(bean);
	}

	/**
	 * Set default SqlBoxContext
	 */
	public static <T> void setDefaultSqlBoxContext(T sqlBoxContext) {
		defaultSqlBoxContext = (SqlBoxContext) sqlBoxContext;
	}

	/**
	 * Release resources (DataSource handle), usually no need call this method except use multiple SqlBoxContext
	 */
	public void close() {
		this.dataSource = null;
		this.metaData = null;
		this.showSql = false;
	}

	/**
	 * Get a box instance from thread local cache for a bean
	 */
	public SqlBox getBox(Object bean) {
		SqlBox box = getBindedBox(bean);
		if (box != null)
			return box;
		box = findAndBuildSqlBox(bean.getClass());
		SqlBoxContext.bindBoxToBean(bean, box);
		return box;
	}

	/**
	 * Get a box instance from thread local cache for a bean
	 */
	public static SqlBox getDefaultBox(Object bean) {
		SqlBox box = getBindedBox(bean);
		if (box != null)
			return box;
		box = defaultSqlBoxContext.findAndBuildSqlBox(bean.getClass());
		SqlBoxContext.bindBoxToBean(bean, box);
		return box;
	}

	/**
	 * Create an entity instance
	 */
	public <T> T createEntity(Class<?> entityOrBoxClass) {
		SqlBox box = findAndBuildSqlBox(entityOrBoxClass);
		Object bean = null;
		try {
			bean = box.getEntityClass().newInstance();
			// Trick here: if already used defaultBox (through its constructor or static block) then
			// change to use this context
			SqlBox box2 = getBindedBox(bean);
			if (box2 == null)
				bindBoxToBean(bean, box);
			else
				box2.setContext(this);
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext create error");
		}
		return (T) bean;

	}

	public static void bindBoxToBean(Object bean, SqlBox box) {
		box.setEntityBean(bean);
		box.getSqlBoxContext().bind(bean, box);
	}

	/**
	 * Find and create a SqlBox instance according bean class or SqlBox Class
	 */
	public SqlBox findAndBuildSqlBox(Class<?> entityOrBoxClass) {
		Class<?> boxClass = null;
		if (entityOrBoxClass == null) {
			SqlBoxException.throwEX("SqlBoxContext findAndBuildSqlBox error! Bean Or SqlBox Class not set");
			return null;
		}
		if (SqlBox.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(entityOrBoxClass.getName() + SQLBOX_IDENTITY);
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(
					entityOrBoxClass.getName() + "$" + entityOrBoxClass.getSimpleName() + SQLBOX_IDENTITY);
		SqlBox box = null;
		if (boxClass == null) {
			box = new SqlBox(this);
			box.setEntityClass(entityOrBoxClass);
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				if (box.getEntityClass() == null)
					box.setEntityClass(entityOrBoxClass);
				box.setContext(this);
			} catch (Exception e) {
				SqlBoxException.throwEX(e,
						"SqlBoxContext findAndBuildSqlBox error! Can not create SqlBox instance: " + entityOrBoxClass);
			}
		}
		return box;
	}

	/**
	 * Find real table name from database meta data
	 */
	protected String findRealTableName(String tableName) {
		String realTableName;
		DBMetaData meta = this.getMetaData();
		realTableName = meta.getTableNames().get(tableName.toLowerCase());
		if (!SqlBoxUtils.isEmptyStr(realTableName))
			return realTableName;
		realTableName = meta.getTableNames().get(tableName.toLowerCase() + 's');
		if (!SqlBoxUtils.isEmptyStr(realTableName))
			return realTableName;
		return null;
	}

	public DatabaseType getDatabaseType() {
		return this.getMetaData().getDatabaseType();
	}

	public void refreshMetaData() {
		this.metaData = DBMetaData.getMetaData(this);
	}

	/**
	 * Print SQL and parameters to console, usually used for debug <br/>
	 * Use context.setShowSql to control, Default showSql is "false"
	 */
	protected void logSql(SqlAndParameters sp) {
		// check if allowed print SQL
		if (!this.getShowSql())
			return;
		StringBuilder sb = new StringBuilder(sp.getSql());
		Object[] args = sp.getParameters();
		if (args.length > 0) {
			sb.append("\r\nParameters: ");
			for (int i = 0; i < args.length; i++) {
				sb.append("" + args[i]);
				if (i != args.length - 1)
					sb.append(",");
				else
					sb.append("\r\n");
			}
		}
		String sql = sb.toString();
		if (getFormatSql())
			sql = SqlBoxUtils.formatSQL(sql);
		log.info(sql);
	}

	private void logCachedSQL(List<List<SqlAndParameters>> subSPlist) {
		if (this.getShowSql()) {
			if (subSPlist != null) {
				List<SqlAndParameters> l = subSPlist.get(0);
				if (l != null) {
					SqlAndParameters sp = l.get(0);
					log.info("First Cached SQL:");
					logSql(sp);
				}
			}
			if (subSPlist != null) {
				List<SqlAndParameters> l = subSPlist.get(subSPlist.size() - 1);
				if (l != null) {
					SqlAndParameters sp = l.get(l.size() - 1);
					log.info("Last Cached SQL:");
					logSql(sp);
				}
			}
		}
	}

	// Only wrap some common used JdbcTemplate methods
	public Integer queryForInteger(String... sql) {
		return this.queryForObject(Integer.class, sql);
	}

	/**
	 * Return String type query result, sql be translated to prepared statement
	 */
	public String queryForString(String... sql) {
		return this.queryForObject(String.class, sql);
	}

	/**
	 * Return Object type query result, sql be translated to prepared statement
	 */
	public <T> T queryForObject(Class<?> clazz, String... sql) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(sql);
		logSql(sp);
		if (sp.getParameters().length != 0)
			return (T) getJdbc().queryForObject(sp.getSql(), sp.getParameters(), clazz);
		else {
			try {
				return (T) getJdbc().queryForObject(sp.getSql(), clazz);
			} catch (EmptyResultDataAccessException e) {
				SqlBoxException.eatException(e);
				return null;
			}
		}
	}

	/**
	 * Cache SQL in memory for executeCachedSQLs call, sql be translated to prepared statement
	 * 
	 * @param sql
	 */
	public void cacheSQL(String... sql) {
		SqlHelper.cacheSQL(sql);
	}

	/**
	 * Execute sql and return how many record be affected, sql be translated to prepared statement<br/>
	 * Return -1 if no parameters sql executed<br/>
	 * 
	 */
	public Integer execute(String... sql) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(sql);
		logSql(sp);
		if (sp.getParameters().length != 0)
			return getJdbc().update(sp.getSql(), sp.getParameters());
		else {
			getJdbc().execute(sp.getSql());
			return -1;
		}
	}

	/**
	 * Execute sql and return how many record be affected, sql be translated to prepared statement<br/>
	 * Return -1 if no parameters sql executed<br/>
	 * 
	 */
	public Integer executeInsert(String... sql) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(sql);
		logSql(sp);
		if (sp.getParameters().length != 0)
			return getJdbc().update(sp.getSql(), sp.getParameters());
		else {
			getJdbc().execute(sp.getSql());
			return -1;
		}
	}

	/**
	 * Load a entity by its entityID from database or L1 Cache
	 */
	public <T> T load(Class<?> entityOrBoxClass, Object entityID) {
		T bean = (T) createEntity(entityOrBoxClass);
		SqlBox box = SqlBoxContext.getBindedBox(bean);
		return box.load(entityID);
	}

	/**
	 * Execute sql without exception threw, return -1 if no parameters sql executed, return -2 if exception found
	 */
	public Integer executeQuiet(String... sql) {
		try {
			return execute(sql);
		} catch (Exception e) {
			SqlBoxException.eatException(e);
			return -2;
		}
	}

	/**
	 * Transfer cached SQLs to Prepared Statement and batch execute these SQLs
	 */
	public void executeCachedSQLs() {
		String sql = SqlHelper.getAndClearBatchSqlString();
		List<List<SqlAndParameters>> subSPlist = SqlHelper.getAndClearBatchSQLs();
		logCachedSQL(subSPlist);
		for (List<SqlAndParameters> splist : subSPlist) {
			getJdbc().batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					SqlAndParameters sp = splist.get(i);
					int index = 1;
					for (Object parameter : sp.getParameters()) {
						ps.setObject(index++, parameter);
					}
				}

				@Override
				public int getBatchSize() {
					return splist.size();
				}
			});
		}
	}

	/**
	 * Store "order by xxx desc" in ThreadLocal, return "", this is for MSSQL2005+ only <br/>
	 */
	public String orderBy(String... orderBy) {
		StringBuilder sb = new StringBuilder(" order by ");
		for (String str : orderBy)
			sb.append(str);
		if (this.getDatabaseType().isMsSQLSERVER()) {
			paginationOrderByCache.set(sb.toString());
			return " ";
		} else
			return sb.toString();
	}

	/**
	 * Return pagination SQL depends different database type <br/>
	 * PageNumber Start from 1
	 */
	public String pagination(int pageNumber, int pageSize) {
		String start;
		String end;
		if (this.getDatabaseType().isH2() || this.getDatabaseType().isMySql()) {
			start = " ";
			end = " limit " + (pageNumber - 1) * pageSize + ", " + pageSize + " ";
		} else if (this.getDatabaseType().isMsSQLSERVER()) {
			// For SQL Server 2005 and later
			start = " a_tb.* from (select row_number() over(__ORDERBY__) as rownum, ";
			end = ") as a_tb where rownum between " + ((pageNumber - 1) * pageSize + 1) + " and "
					+ pageNumber * pageSize + " ";
			/**
			 * For SqlServer 2012 and later can also use <br/>
			 * start = " "; <br/>
			 * end = " offset " + (pageNumber - 1) * pageSize + " rows fetch next " + pageSize + " rows only ";
			 */
		} else if (this.getDatabaseType().isOracle()) {
			start = " * FROM (SELECT a_tb.*, ROWNUM r_num FROM ( SELECT ";
			end = " ) a_tb WHERE ROWNUM <= " + pageNumber * pageSize + ") WHERE r_num > " + (pageNumber - 1) * pageSize
					+ " ";
		} else
			return (String) SqlBoxException.throwEX("pagination error: so far do not support this database.");
		paginationEndCache.set(end);
		return start;
	}

	/**
	 * Query for get a List<Map<String, Object>> List
	 */
	public List<Map<String, Object>> queryForList(String... sql) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(sql);
		logSql(sp);
		List<Map<String, Object>> list = getJdbc().queryForList(sp.getSql(), sp.getParameters());
		if (this.getShowQueryResult())
			for (Map<String, Object> map : list) {
				log.info(map.toString());
			}
		return list;
	}

	/**
	 * Query for get Entity List
	 */
	@SuppressWarnings("rawtypes")
	public <T> List<T> queryForEntityList(Class<?> clazz, String... sql) {
		return new ArrayList(queryForEntityMaps(sql).get(clazz).values());
	}

	/**
	 * Query for get Entity List Map, different entity list use different key (column name) to distinguish
	 */
	public Map<Class<?>, Map<Object, Entity>> queryForEntityMaps(String... sql) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(sql);
		logSql(sp);
		List<Map<String, Object>> list = getJdbc().queryForList(sp.getSql(), sp.getParameters());
		if (this.getShowQueryResult())
			for (Map<String, Object> map : list) {
				log.info(map.toString());
			}
		return transfer(list, sp);
	}

	/**
	 * Transfer resultList List<Map<String, Object>> to Map<Class<?>, List<Object>>,<br/>
	 * 
	 * <pre>
	 * sqlResult: 
	 * A1,B1
	 * A1,B1,C1 
	 * A2,B2,C2
	 * A2,B2,C3
	 * D1
	 * D2
	 * 
	 * return:
	 * A.class->[A1 (A1.b=B1 (B1.c=[C1]), A2 (A2.b=b2 (b2.c=[c2,c3]]]
	 * D.class->[D1, D2]
	 * B.class->...
	 * C.class->...
	 * </pre>
	 */
	public Map<Class<?>, Map<Object, Entity>> transfer(List<Map<String, Object>> sqlResultList,
			final SqlAndParameters sp) {
		Map<Class<?>, Map<Object, Entity>> entityCache = new HashMap<>();
		List<Entity> templates = sp.getEntityTemplates();
		for (Entity entity : templates) {
			Map<Object, Entity> list = new LinkedHashMap<>();
			entityCache.put(entity.box().getEntityClass(), list);
		}

		for (Map<String, Object> oneLine : sqlResultList)
			doOneLineTransfer(entityCache, oneLine, sp);

		boolean hasTreeMap = false;
		for (Mapping map : sp.getMappingList())
			if (map.getMappingType().isTree())
				hasTreeMap = true;
		if (hasTreeMap)
			for (Map<String, Object> oneLine : sqlResultList)
				doTransferTree(entityCache, oneLine, sp);
		return entityCache;
	}

	/**
	 * <pre>
	 * Do transfer for 1 line
	 * 1)Create bean instances for each entity classes, put bean in resultMap if not exist
	 * 2)If find property match column, set it to bean 
	 * 3)Assemble mapping relationship between these beans 
	 *    
	 * </
	 * <pre>
	 */
	private void doOneLineTransfer(Map<Class<?>, Map<Object, Entity>> entityCache, Map<String, Object> oneLine, // NOSONAR
			final SqlAndParameters sp) {
		List<Entity> classes = sp.getEntityTemplates();

		ArrayList<Entity> thisLineEntities = new ArrayList<>();

		// create entity, if not cached in entityCache, put it in
		for (Entity template : classes) {
			Entity entity = this.createEntity(template.box().getEntityClass());
			SqlBoxUtils.fetchValueFromList(template.box().getAlias(), oneLine, entity);
			Map<String, Object> id = entity.box().getEntityID();
			if (id != null && !id.isEmpty()) {
				Map<Object, Entity> entityMap = entityCache.get(template.box().getEntityClass());
				Entity cachedEntity = SqlBoxUtils.findEntityByID(id, entityMap);
				if (cachedEntity == null) {
					entity.box().setEntityCache(entityCache);
					entity.box().setSpCache(sp);
					SqlBoxUtils.cacheEntityToEntityMap(entity, entityMap);
					thisLineEntities.add(entity);
				} else
					thisLineEntities.add(cachedEntity);
			}
		}

		// now cached thisLineEntities in entityResult, start to assemble relationship
		for (Entity entity1 : thisLineEntities) {// entity1
			SqlBox box1 = entity1.box();
			Class<?> c1 = box1.getEntityClass();
			for (Entity entity2 : thisLineEntities) {// entity2
				SqlBox box2 = entity2.box();
				Class<?> c2 = box2.getEntityClass();

				Map<Entity, Set<String>> parent2 = box2.getPartents();

				for (Mapping map : sp.getMappingList()) {
					if (map.getMappingType().isTree())
						continue;
					Class<?> thisClass = map.getThisEntity().getClass();
					Class<?> otherClass = map.getOtherEntity().getClass();
					String thisField = map.getThisField();
					String otherField = map.getOtherfield();
					String thisProperty = map.getThisPropertyName();
					String otherProperty = map.getOtherPropertyName();

					if (c1.equals(thisClass) && c2.equals(otherClass)) {// 2 classes match mapping setting
						Object value1 = SqlBoxUtils.getFieldValueByFieldID(entity1, thisField);
						Object value2 = SqlBoxUtils.getFieldValueByFieldID(entity2, otherField);

						if (value1 != null && value1.equals(value2) && !"".equals(value1)) {// 2 values match
							if (parent2 == null) {
								// If parent is null, create a new parent map
								// Note: one box can have many parent entities, so, parent is a map
								// one child can have many parents, one parent can have many child
								// key is parent entity, value is set<parent fields>
								Map<Entity, Set<String>> parentMap2 = new HashMap<>();
								Set<String> fieldSet2 = new HashSet<>();
								fieldSet2.add(thisField);
								parentMap2.put(entity1, fieldSet2);
								box2.setPartents(parentMap2);

								// if parent entity1 need bind child entity2
								if (!SqlBoxUtils.isEmptyStr(thisProperty)) {
									// oneToOne
									if (map.getMappingType().isOneToOne()) {
										SqlBoxUtils.setFieldValueByFieldID(entity1, thisProperty, entity2);
									} else
									// oneToMany
									if (map.getMappingType().isOneToMany()) {
										SqlBoxUtils.addFieldValueByFieldID(entity1, thisProperty, entity2);
									}
								}

								// if child entity2 need bind parent entity1
								if (!SqlBoxUtils.isEmptyStr(otherProperty)
										&& (map.getMappingType().isOneToOne() || map.getMappingType().isOneToMany())) {
									SqlBoxUtils.setFieldValueByFieldID(entity2, otherProperty, entity1);
								}

							} else {
								// Already have parent map found, only need insert new founded parent into it
								Set<String> fieldSet2 = parent2.get(entity1);
								if (fieldSet2 == null) {
									fieldSet2 = new HashSet<>();
									parent2.put(entity1, fieldSet2);
								}
								fieldSet2.add(thisField);
							}
						}
					}
				}

			}
		}
	}

	private void doTransferTree(Map<Class<?>, Map<Object, Entity>> entityCache, Map<String, Object> oneLine, // NOSONAR
			final SqlAndParameters sp) {
		List<Entity> classes = sp.getEntityTemplates();

		ArrayList<Entity> thisLineEntities = new ArrayList<>();

		// create entity, if not cached in entityCache, put it in
		for (Entity template : classes) {
			Entity entity = this.createEntity(template.box().getEntityClass());
			SqlBoxUtils.fetchValueFromList(template.box().getAlias(), oneLine, entity);
			Map<String, Object> id = entity.box().getEntityID();
			if (id != null && !id.isEmpty()) {
				Map<Object, Entity> entityMap = entityCache.get(template.box().getEntityClass());
				Entity cachedEntity = SqlBoxUtils.findEntityByID(id, entityMap);
				SqlBoxException.assureNotNull(cachedEntity, "doTransferTree cachedEntity can not be null");
				thisLineEntities.add(cachedEntity);
			}
		}

		// now cached thisLineEntities in entityResult, start to assemble relationship
		for (Entity entity : thisLineEntities) {// entity1
			SqlBox box = entity.box();
			Class<?> clazz = box.getEntityClass();

			Map<Entity, Set<String>> boxParent = box.getPartents();

			for (Mapping map : sp.getMappingList()) {// NOSONAR
				if (!map.getMappingType().isTree())
					continue;
				Class<?> mappingClass = map.getThisEntity().getClass();
				if (!clazz.equals(mappingClass))
					continue;
				String pidField = map.getOtherfield();// pid
				String parentProperty = map.getOtherPropertyName();// parentNode
				String childProperty = map.getThisPropertyName(); // childs 

				Object pidValue = SqlBoxUtils.getFieldValueByFieldID(entity, pidField);
				if (pidValue == null || "".equals(pidValue))
					continue;

				// now find parentEntity from entityCache map
				Map<Object, Entity> entityMap = entityCache.get(mappingClass);
				Map<String, Object> pid = new HashMap<>();
				pid.put(pidField, pidValue);
				Entity parentEntity = SqlBoxUtils.findEntityByID(pid, entityMap);

				if (parentEntity != null) {// found parent entity
					if (boxParent == null) { // if boxParent is null, need create it
						Map<Entity, Set<String>> parentMap = new HashMap<>();
						// one entity may have many parents, but for tree only 1 parent
						Set<String> parentFields = new HashSet<>();
						parentFields.add(pidField);
						parentMap.put(parentEntity, parentFields);
						box.setPartents(parentMap);

						// if parent cachedEntity need bind child entity2
						if (!SqlBoxUtils.isEmptyStr(childProperty))
							SqlBoxUtils.addFieldValueByFieldID(parentEntity, childProperty, entity);

						// if child entity2 need bind parent cachedEntity
						if (!SqlBoxUtils.isEmptyStr(parentProperty))
							SqlBoxUtils.setFieldValueByFieldID(entity, parentProperty, parentEntity);

					} else {
						// Already have parent map found, only need insert new founded parent into it
						// But it's very rare a entity has many parents
						Set<String> parentFields = boxParent.get(parentEntity);
						if (parentFields == null) {
							parentFields = new HashSet<>();
							boxParent.put(parentEntity, parentFields);
						}
						parentFields.add(pidField);
					}
				}

			}

		}
	}

}