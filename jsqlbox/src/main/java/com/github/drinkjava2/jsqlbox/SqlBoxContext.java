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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBoxContext {
	public static final SqlBoxContext defaultSqlBoxContext = new SqlBoxContext();

	// print SQL to console or log depends logging.properties
	private boolean showSql = false;

	private static final SqlBoxLogger log = SqlBoxLogger.getLog(SqlBoxContext.class);

	public static final String SQLBOX_IDENTITY = "BOX";

	private JdbcTemplate jdbc = new JdbcTemplate();
	private DataSource dataSource = null;

	private TinyDbMetaData metaData;

	private Class<?> dbClass;

	public static final ThreadLocal<HashMap<Object, Object>> classExistCache = new ThreadLocal<HashMap<Object, Object>>() {
		@Override
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<>();
		}
	};

	public SqlBoxContext() {
		// Default constructor
	}

	public SqlBoxContext(DataSource dataSource, Class<?> dbClass) {
		this.dataSource = dataSource;
		this.dbClass = dbClass;
		if (dataSource != null) {
			this.jdbc.setDataSource(dataSource);
			refreshMetaData();
		}
	}

	/**
	 * Load entity from database
	 */
	public <T> T load(Class<?> entityOrBoxClass, Object entityID) {
		T bean = (T) createEntity(entityOrBoxClass);
		Dao dao = SqlBoxUtils.getDao(bean);
		return dao.load(entityID);
	}

	/**
	 * Query for a DB type List
	 */
	public <T> List<T> queryForList(String... sql) {
		List<T> result = new ArrayList<>();
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			List<Map<String, Object>> lst;
			if (sp.getParameters().length == 0)
				lst = getJdbc().queryForList(sp.getSql());
			else
				lst = getJdbc().queryForList(sp.getSql(), sp.getParameters());
			Field field = ReflectionUtils.getDeclaredField(dbClass, "map");
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
				SqlBoxException.throwEX(e, "SqlBoxContext findAndBuildSqlBox error! Can not create SqlBox instance");
			}
		}
		return box;
	}

	/**
	 * Find real table name from database meta data
	 */
	public String findRealTableName(String tableName) {
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

	/**
	 * Print SQL and parameters to console, usually used for debug <br/>
	 * Use context.setShowSql to control, Default showSql is "false"
	 */
	private void logSql(SqlAndParameters sp) {
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

	// ================== getter & setters below============
	public DataSource getDataSource() {
		return dataSource;
	}

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

}