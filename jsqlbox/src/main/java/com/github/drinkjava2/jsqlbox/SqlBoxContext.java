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

import java.lang.reflect.Method;
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
	// print SQL to console or log depends logging.properties
	private boolean showSql = false;

	private static final SqlBoxLogger log = SqlBoxLogger.getLog(SqlBoxContext.class);

	private static String sqlBoxConfigClass = "SqlBoxConfig";
	private static String getSqlBoxContextMethod = "getSqlBoxContext";

	public static final String SQLBOX_IDENTITY = "BOX";

	private JdbcTemplate jdbc = new JdbcTemplate();
	private DataSource dataSource = null;

	private TinyDbMetaData metaData;

	public static final ThreadLocal<HashMap<Object, Object>> classExistCache = new ThreadLocal<HashMap<Object, Object>>() {
		@Override
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<>();
		}
	};

	public SqlBoxContext(DataSource dataSource) {
		this.dataSource = dataSource;
		if (dataSource != null) {
			this.jdbc.setDataSource(dataSource);
			refreshMetaData();
		}

	}

	/**
	 * Config a global invoke method, used to get a default SqlBoxContext for global use<br/>
	 * The default method is: public static SqlBoxContext getSqlBoxContext() in SqlBoxConfig class
	 */
	public static void configDefaultContext(String configClassName, String invokeMethodName) {
		sqlBoxConfigClass = configClassName;
		getSqlBoxContextMethod = invokeMethodName;
	}

	public static void resetDefaultContext() {
		sqlBoxConfigClass = "SqlBoxConfig";
		getSqlBoxContextMethod = "getSqlBoxContext";
	}

	/**
	 * Return a default global SqlBoxContext <br/>
	 * Note: a config class SqlBoxConfig.java is needed in class root folder
	 */
	public static SqlBoxContext getDefaultSqlBoxContext() {
		final String errorinfo = "SqlBoxContext getDefaultSqlBoxContext error: ";
		SqlBoxContext ctx = null;
		try {
			Class<?> configClass = Class.forName(sqlBoxConfigClass);
			Method method = configClass.getMethod(getSqlBoxContextMethod, new Class[] {});
			ctx = (SqlBoxContext) method.invoke(configClass, new Object[] {});
			if (ctx == null)
				SqlBoxException.throwEX(errorinfo + sqlBoxConfigClass + "." + getSqlBoxContextMethod + "()");
		} catch (Exception e1) {
			SqlBoxException.throwEX(e1, errorinfo + sqlBoxConfigClass + "." + getSqlBoxContextMethod + "()");
		} catch (Error error) {// NOSONAR
			log.error(errorinfo + sqlBoxConfigClass + "." + getSqlBoxContextMethod + "()");
			throw error;
		}
		return ctx;
	}

	/**
	 * Create an entity instance
	 */
	public <T> T createBean(Class<?> beanOrSqlBoxClass) {
		SqlBox box = findAndBuildSqlBox(beanOrSqlBoxClass);
		return (T) box.createBean();
	}

	/**
	 * Find and create a SqlBox instance according bean class or SqlBox Class
	 */
	public SqlBox findAndBuildSqlBox(Class<?> beanOrSqlBoxClass) {
		Class<?> boxClass = null;
		if (beanOrSqlBoxClass == null) {
			SqlBoxException.throwEX("SqlBoxContext findAndBuildSqlBox error! Bean Or SqlBox Class not set");
			return null;
		}
		if (SqlBox.class.isAssignableFrom(beanOrSqlBoxClass))
			boxClass = beanOrSqlBoxClass;
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(beanOrSqlBoxClass.getName() + SQLBOX_IDENTITY);
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(
					beanOrSqlBoxClass.getName() + "$" + beanOrSqlBoxClass.getSimpleName() + SQLBOX_IDENTITY);
		SqlBox box = null;
		if (boxClass == null) {
			box = new SqlBox(this);
			box.setEntityClass(beanOrSqlBoxClass);
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				if (box.getEntityClass() == null)
					box.setEntityClass(beanOrSqlBoxClass);
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

}