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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

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

	public static final String SQLBOX_IDENTITY = "BX";

	private JdbcTemplate jdbc = new JdbcTemplate();
	private DataSource dataSource = null;

	private DBMetaData metaData;

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
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbc.setDataSource(dataSource);
		refreshMetaData();
	}

	public Boolean getShowSql() {
		return showSql;
	}

	public void setShowSql(Boolean showSql) {
		this.showSql = showSql;
	}

	public Boolean getFormatSql() {
		return formatSql;
	}

	public void setFormatSql(Boolean formatSql) {
		this.formatSql = formatSql;
	}

	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public DBMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(DBMetaData metaData) {
		this.metaData = metaData;
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
		return defaultSqlBoxContext.getBox(bean);
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

	// ========JdbcTemplate wrap methods begin============
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
		if (getFormatSql()) {
			sql = " " + sql;
			sql = StringUtils.replace(sql, ",", ",\r\n\t");
			sql = StringUtils.replace(sql, " select ", "\r\nselect \r\n\t");
			sql = StringUtils.replace(sql, " from ", "\r\nfrom \r\n\t");
			sql = StringUtils.replace(sql, " where ", "\r\nwhere \r\n\t");
			sql = StringUtils.replace(sql, " delete ", "\r\ndelete \r\n\t");
			sql = StringUtils.replace(sql, " update ", "\r\nupdate \r\n\t");
			sql = StringUtils.replace(sql, " left ", "\r\nleft ");
			sql = StringUtils.replace(sql, " right ", "\r\nright ");
			sql = StringUtils.replace(sql, " inner ", "\r\ninner ");
			sql = StringUtils.replace(sql, " join ", " join \r\n\t");
			sql = StringUtils.replace(sql, " on ", "\r\n   on   ");
			sql = StringUtils.replace(sql, " group ", "\r\ngroup \r\n\t");
			sql = StringUtils.replace(sql, " order ", "\r\norder \r\n\t");
		}
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
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
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
		} finally {
			SqlHelper.clear();
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
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			if (sp.getParameters().length != 0)
				return getJdbc().update(sp.getSql(), sp.getParameters());
			else {
				getJdbc().execute(sp.getSql());
				return -1;
			}
		} finally {
			SqlHelper.clear();
		}
	}

	/**
	 * Execute sql and return how many record be affected, sql be translated to prepared statement<br/>
	 * Return -1 if no parameters sql executed<br/>
	 * 
	 */
	public Integer executeInsert(String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			if (sp.getParameters().length != 0)
				return getJdbc().update(sp.getSql(), sp.getParameters());
			else {
				getJdbc().execute(sp.getSql());
				return -1;
			}
		} finally {
			SqlHelper.clear();
		}
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
		try {
			List<List<SqlAndParameters>> subSPlist = SqlHelper.getSQLandParameterSubList();
			logCachedSQL(subSPlist);
			for (final List<SqlAndParameters> splist : subSPlist) {
				getJdbc().batchUpdate(SqlHelper.getSqlForBatch().get(), new BatchPreparedStatementSetter() {
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
		} finally {
			SqlHelper.clearBatchSQLs();
		}
	}

	/**
	 * Query for get a DB or DB child type list
	 */
	public List<Entity> queryForEntityTree(String... sql) {
		List<Entity> result = new ArrayList<>();
		List<Map<String, Object>> mapList = null;
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			mapList = getJdbc().queryForList(sp.getSql(), sp.getParameters());
			for (Map<String, Object> map : mapList) {
				if (map == null) {
				}
				// TODO work on it
			}
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext queryForDbList error, sql=" + sql);
		} finally {
			SqlHelper.clear();
		}
		return result;
	}

	/**
	 * Query for get a List<Map<String, Object>> List
	 */
	public List<Map<String, Object>> queryForList(String... sql) {
		List<Map<String, Object>> result = null;
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			result = getJdbc().queryForList(sp.getSql(), sp.getParameters());
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext queryForList error, sql=" + sql);
		} finally {
			SqlHelper.clear();
		}
		return result;
	}

	public <T> T load(Class<?> entityOrBoxClass, Object entityID) {
		T bean = (T) createEntity(entityOrBoxClass);
		SqlBox box = SqlBoxContext.getBindedBox(bean);
		return box.load(entityID);
	}
	// ========JdbcTemplate wrap methods End============

}