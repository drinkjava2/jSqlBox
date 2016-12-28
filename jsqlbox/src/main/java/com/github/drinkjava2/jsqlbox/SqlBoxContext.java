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

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

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
	private static SqlBoxContext defaultSqlBoxContext;

	// print SQL to console or log depends logging.properties
	private boolean showSql = false;

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

	public static SqlBoxContext defaultSqlBoxContext() {
		if (defaultSqlBoxContext == null)
			defaultSqlBoxContext = new SqlBoxContext();
		return defaultSqlBoxContext;

	}

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
	 * Create an entity instance
	 */
	public <T> T createEntity(Class<?> entityOrBoxClass) {
		Box box = findAndBuildSqlBox(entityOrBoxClass);
		return (T) box.createEntity();
	}

	/**
	 * Find and create a SqlBox instance according bean class or SqlBox Class
	 */
	public Box findAndBuildSqlBox(Class<?> entityOrBoxClass) {
		Class<?> boxClass = null;
		if (entityOrBoxClass == null) {
			SqlBoxException.throwEX("SqlBoxContext findAndBuildSqlBox error! Bean Or SqlBox Class not set");
			return null;
		}
		if (Box.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(entityOrBoxClass.getName() + SQLBOX_IDENTITY);
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(
					entityOrBoxClass.getName() + "$" + entityOrBoxClass.getSimpleName() + SQLBOX_IDENTITY);
		Box box = null;
		if (boxClass == null) {
			box = new Box(this);
			box.setEntityClass(entityOrBoxClass);
		} else {
			try {
				box = (Box) boxClass.newInstance();
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

}