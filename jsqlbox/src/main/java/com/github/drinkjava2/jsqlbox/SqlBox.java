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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBox {

	// The entity bean class
	private Class<?> entityClass;

	// Configuration Columns, set it before entity bean be created
	private Map<String, Column> configColumns = new HashMap<>();

	// Configuration Table Name, set it before entity bean be created
	private String configTable;

	private SqlBoxContext context;

	public SqlBox() {
		// Default Constructor
	}

	public SqlBox(SqlBoxContext context) {
		this.context = context;
	}

	public static <T> T createBean(Class<?> beanOrSqlBoxClass) {
		return SqlBoxContext.defaultSqlBoxContext.createEntity(beanOrSqlBoxClass);
	}

	/**
	 * Create entity instance
	 */
	public <T> T createEntity() {
		Object bean = null;
		try {
			bean = this.getEntityClass().newInstance();
			this.beanInitialize(bean);
			Dao dao = new Dao(this);
			dao.setEntityBean(bean);
			Method m = ReflectionUtils.getDeclaredMethod(this.getEntityClass(), "putDao", new Class[] { Dao.class });
			m.invoke(bean, new Object[] { dao });
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext create error");
		}
		return (T) bean;
	}

	/**
	 * @param instance
	 */
	public void beanInitialize(Object bean) {
		try {
			Method m = ReflectionUtils.getDeclaredMethod(getEntityClass(), "initialize", new Class[] { null });
			if (m != null)
				m.invoke(bean, new Object[] {});
		} catch (Exception e) {
			SqlBoxException.eatException(e);
		}
	}

	/**
	 * Get real database table name
	 */
	public String getRealTable() {
		String realTable = configTable;
		if (SqlBoxUtils.isEmptyStr(realTable))
			realTable = this.getEntityClass().getSimpleName();
		String resultTable = context.findRealTableName(realTable);
		if (SqlBoxUtils.isEmptyStr(resultTable))
			SqlBoxException
					.throwEX("SqlBox getRealTable error: " + this.getEntityClass() + ", table name:" + realTable);
		return resultTable;
	}

	/**
	 * Return a * for sql
	 */
	public String getStar() {
		return "*";// NOSONAR
	}

	/**
	 * In entity class, a legal fieldID like userName must have a same name no parameter method like userName()
	 */
	private boolean isLegalFieldID(String fieldID) {
		if ("class".equals(fieldID))
			return false;
		if (SqlBoxUtils.isEmptyStr(fieldID))
			return false;
		if (SqlBoxUtils.isCapitalizedString(fieldID))
			return false;
		/**
		 * try { Method method = ReflectionUtils.getDeclaredMethod(entityClass, fieldID, new Class[] {}); if (method ==
		 * null) return false; } catch (Exception e) { return false; }
		 */
		return true;
	}

	/**
	 * Return real Columns match to table meta data
	 */
	public Map<String, Column> buildRealColumns() {
		if (this.entityClass == null)
			SqlBoxException.throwEX("SqlBox getRealColumns error, beanClass can not be null");
		String realTableName = this.getRealTable();
		TinyDbMetaData meta = this.getContext().getMetaData();
		Map<String, Column> oneTable = meta.getOneTable(realTableName.toLowerCase());
		Map<String, Column> realColumns = new HashMap<>();
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(this.getEntityClass());
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBox buildDefaultConfig error");
		}
		for (PropertyDescriptor pd : pds) {
			String fieldID = pd.getName();
			if (isLegalFieldID(fieldID)) {
				Column realCol = new Column();
				realCol.setFieldID(fieldID);
				realCol.setColumnName(this.getRealColumnName(fieldID));
				realCol.setPropertyType(pd.getPropertyType());
				realCol.setReadMethodName(pd.getReadMethod().getName());
				realCol.setWriteMethodName(pd.getWriteMethod().getName());
				useConfigOverrideDefault(fieldID, realCol);
				realColumns.put(fieldID, realCol);
				Column metaDataCol = oneTable.get(fieldID.toLowerCase());
				if (metaDataCol != null)
					realCol.setAutoIncreament(metaDataCol.getAutoIncreament());
			}
		}
		findAndSetEntityID(this.getEntityClass(), realColumns);
		return realColumns;
	}

	/**
	 * Use config values to override default runtime values
	 */
	private void useConfigOverrideDefault(String fieldID, Column column) {
		Column configColumn = configColumns.get(fieldID);
		if (configColumn != null) {
			if (!SqlBoxUtils.isEmptyStr(configColumn.getColumnName()))
				column.setColumnName(configColumn.getColumnName());
			column.setEntityID(configColumn.getEntityID());
			column.setIdGenerator(configColumn.getIdGenerator());
			column.setEntityID(configColumn.getEntityID());
		}
	}

	/**
	 * Find and set Object IDs automatically, rule:<br/>
	 * 
	 * Find how many entityID <br/>
	 * Found lots? return <br/>
	 * only found 1? if no generator, set to auto type <br/>
	 * Not found? look for id field found? set as EntityID if no generator, set to auto type <br/>
	 * No found throw ex <br/>
	 */
	private void findAndSetEntityID(Class<?> entityClass, Map<String, Column> realColumns) {// NOSONAR
		Column idColumn = null;
		Column entityColumn = null;
		for (Entry<String, Column> cols : realColumns.entrySet()) {
			if (cols.getValue().getEntityID())
				if (entityColumn != null)
					return;
				else
					entityColumn = cols.getValue();
			if ("id".equals(cols.getValue().getFieldID()))
				idColumn = cols.getValue();
		}

		if (idColumn == null) {
			if (entityColumn == null)
				throwEX("SqlBox findAndSetEntityID error, no entityID set for entity class " + entityClass);
			else
				return;
		} else {
			if (entityColumn != null && !entityColumn.getFieldID().equals(idColumn.getFieldID()))
				return;
			else {
				idColumn.setEntityID(true);
				if (idColumn.getIdGenerator() == null)// entityColumn=null or entityColumn=idColumn
					idColumn.setIdGenerator(AutoGenerator.INSTANCE);
			}
		}
		return;
	}

	/**
	 * Get real column name by fieldID <br/>
	 * userName field will find userName or username or USERNAME or USER_NAME, but only allowed 1
	 */
	public String getRealColumnName(String fieldID) {// NOSONAR
		Column col = getOrBuildConfigColumn(fieldID);
		String columnName = col.getColumnName();
		if (columnName == null || columnName.length() == 0)
			columnName = fieldID;
		String realTable = getRealTable();
		Map<String, Column> oneTableMap = context.getMetaData().getOneTable(realTable.toLowerCase());

		String realColumnNameignoreCase = null;
		Column realColumn = oneTableMap.get(columnName.toLowerCase());
		if (realColumn != null)
			realColumnNameignoreCase = realColumn.getColumnName();

		String realColumnNameUnderline = null;
		realColumn = oneTableMap.get(SqlBoxUtils.camelToLowerCaseUnderline(columnName));
		if (realColumn != null)
			realColumnNameUnderline = realColumn.getColumnName();

		if (realColumnNameignoreCase == null && realColumnNameUnderline == null)
			SqlBoxException.throwEX("SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" does not match any table column in table " + realTable);

		if (realColumnNameignoreCase != null && realColumnNameUnderline != null
				&& !realColumnNameignoreCase.equals(realColumnNameUnderline))
			SqlBoxException.throwEX("SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" found mutiple columns in table " + realTable);
		return realColumnNameignoreCase != null ? realColumnNameignoreCase : realColumnNameUnderline;
	}

	public Column getOrBuildConfigColumn(String fieldID) {
		Column col = this.getConfigColumns().get(fieldID);
		if (col == null) {
			col = new Column();
			this.getConfigColumns().put(fieldID, col);
		}
		return col;
	}

	// ========Config methods begin==============
	/**
	 * Config table name
	 */
	public void configTable(String table) {
		configTable = table;
	}

	/**
	 * Clean old entityID setting, set with given entityIDs
	 */
	public void configEntityIDs(String... entityIDs) {
		for (Entry<String, Column> entry : getConfigColumns().entrySet())
			entry.getValue().setEntityID(false);
		for (String fieldID : entityIDs)
			getOrBuildConfigColumn(fieldID).setEntityID(true);
	}

	/**
	 * Config column name
	 */
	public void configColumnName(String fieldID, String columnName) {
		getOrBuildConfigColumn(fieldID).setColumnName(columnName);
	}

	/**
	 * Config column name
	 */
	public <T> void configIdGenerator(String fieldID, T idGenerator) {
		getOrBuildConfigColumn(fieldID).setIdGenerator((IdGenerator) idGenerator);
	}

	// ========Config methods end==============

	// == shortcut methods, just copy some common used Dao public static method here======

	public static Integer queryForInteger(String... sql) {
		return Dao.dao().queryForInteger(sql);
	}

	public static String queryForString(String... sql) {
		return Dao.dao().queryForString(sql);
	}

	public static <T> T queryForObject(Class<?> clazz, String... sql) {
		return Dao.dao().queryForObject(clazz, sql);
	}

	public static void cacheSQL(String... sql) {
		Dao.dao().cacheSQL(sql);
	}

	public static Integer execute(String... sql) {
		return Dao.dao().execute(sql);
	}

	public static Integer executeInsert(String... sql) {
		return Dao.dao().executeInsert(sql);
	}

	public static Integer executeQuiet(String... sql) {
		return Dao.dao().executeQuiet(sql);
	}

	public static void executeCachedSQLs() {
		Dao.dao().executeCachedSQLs();
	}

	public static JdbcTemplate getDefaultJdbc() {
		return Dao.dao().getJdbc();
	}

	public static SqlBoxContext getDefaultContext() {
		return Dao.dao().getContext();
	}

	public static DatabaseType getDefaultDatabaseType() {
		return Dao.dao().getDatabaseType();
	}

	public static void refreshMetaData() {
		Dao.dao().refreshMetaData();
	}

	public static <T> T load(Class<?> entityOrBoxClass, Object entityID) {
		return SqlBoxContext.defaultSqlBoxContext.load(entityOrBoxClass, entityID);
	}

	public static <T> List<T> queryForList(String... sql) {
		return SqlBoxContext.defaultSqlBoxContext.queryForList(sql);
	}
	// == shortcut methods end=======================================================

	// ========getter & setters below==============
	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Map<String, Column> getConfigColumns() {
		return configColumns;
	}

	public void setConfigColumns(Map<String, Column> columns) {
		this.configColumns = columns;
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

}
