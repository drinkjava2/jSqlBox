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

import java.lang.reflect.Field;
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

import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;
import com.github.drinkjava2.springsrc.ReflectionUtils;

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
	private boolean showSql = false;

	public static final String SQLBOX_IDENTITY = "BX";

	private JdbcTemplate jdbc = new JdbcTemplate();
	private DataSource dataSource = null;

	private TinyDbMetaData metaData;

	private Class<?> dbClass;

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
	public SqlBoxContext(DataSource dataSource, Class<?> dbClass) {
		this.dataSource = dataSource;
		this.dbClass = dbClass;
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

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public TinyDbMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(TinyDbMetaData metaData) {
		this.metaData = metaData;
	}

	public Class<?> getDbClass() {
		return dbClass;
	}

	public void setDbClass(Class<?> dbClass) {
		this.dbClass = dbClass;
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
		this.dbClass = null;
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
		SqlBox.initializeBox(bean, box);
		return box;
	}

	/**
	 * Create an entity instance
	 */
	public <T> T createEntity(Class<?> entityOrBoxClass) {
		SqlBox box = findAndBuildSqlBox(entityOrBoxClass);
		return (T) box.createEntity();
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
		TinyDbMetaData meta = this.getMetaData();
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
		this.metaData = TinyJdbc.getMetaData(dataSource);
	}

	// ========JdbcTemplate wrap methods begin============
	/**
	 * Print SQL and parameters to console, usually used for debug <br/>
	 * Use context.setShowSql to control, Default showSql is "false"
	 */
	protected void logSql(SqlAndParameters sp) {
		// check if allowed print SQL
		if (!this.isShowSql())
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
		log.info(sb.toString());
	}

	private void logCachedSQL(List<List<SqlAndParameters>> subSPlist) {
		if (this.isShowSql()) {
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
			SqlHelper.clearLastSQL();
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
				return getJdbc().update(sp.getSql(), (Object[]) sp.getParameters());
			else {
				getJdbc().execute(sp.getSql());
				return -1;
			}
		} finally {
			SqlHelper.clearLastSQL();
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
				return getJdbc().update(sp.getSql(), (Object[]) sp.getParameters());
			else {
				getJdbc().execute(sp.getSql());
				return -1;
			}
		} finally {
			SqlHelper.clearLastSQL();
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
	 * Query for a DB type List
	 */
	public <T> List<T> queryForList(Class<?> dbClass, String... sql) {
		List<T> result = new ArrayList<>();
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			List<Map<String, Object>> lst;
			if (sp.getParameters().length == 0)
				lst = getJdbc().queryForList(sp.getSql());
			else
				lst = getJdbc().queryForList(sp.getSql(), sp.getParameters());
			Field field = ReflectionUtils.findField(dbClass, "map");
			for (Map<String, Object> map : lst) {
				Object db = dbClass.newInstance();
				field.set(db, map);
				result.add((T) db);
			}
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext queryForList, sql=" + sql);
		} finally {
			SqlHelper.clearLastSQL();
		}
		return result;
	}
	// ========JdbcTemplate wrap methods End============

}