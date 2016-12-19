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
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.drinkjava2.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

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
		SqlBoxContext ctx = SqlBoxContext.getDefaultSqlBoxContext();
		return ctx.createBean(beanOrSqlBoxClass);
	}

	/**
	 * Create entity bean
	 */
	public <T> T createBean() {
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
	 * In entity class, a legal fieldID like userName must have a same name no parameter method like userName()
	 */
	private boolean isLegalFieldID(String fieldID) {
		if (SqlBoxUtils.isEmptyStr(fieldID))
			return false;
		try {
			if (SqlBoxUtils.isCapitalizedString(fieldID))
				return false;
			Method method = ReflectionUtils.getDeclaredMethod(entityClass, fieldID, new Class[] {});
			if (method == null)
				return false;
		} catch (Exception e) { // NOSONAR
			return false;
		}
		return true;
	}

	/**
	 * Return real Columns match to table meta data
	 */
	public Map<String, Column> buildRealColumns() {
		if (this.entityClass == null)
			SqlBoxException.throwEX("SqlBox getRealColumns error, beanClass can not be null");
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
			}
		}
		findAndSetPrimeKeys(this.getEntityClass(), realColumns);
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
			column.setPrimeKey(configColumn.isObjectID());
			column.setIdGenerator(configColumn.getIdGenerator());
			column.setPrimeKey(configColumn.isObjectID());
		}
	}

	/**
	 * Manually set Prime Keys
	 */
	public void setPrimeKeys(String... fieldIDs) {
		for (String fieldID : fieldIDs) {
			Column col = this.getOrBuildConfigColumn(fieldID);
			col.setPrimeKey(true);
		}
	}

	/**
	 * Find and set Prime Keys automatically, rule:<br/>
	 * 1) check if already has PKey set by setPrimeKeys(someid1,someid2...), if found, exit <br/>
	 * 2) find field named "id", put it to primeKeys <br/>
	 * 3) if 1 and 2 not found, find a field which set IdGenertorValue, if found more than 2 throw an exception <br/>
	 */
	private void findAndSetPrimeKeys(Class<?> entityClass, Map<String, Column> realColumns) {// NOSONAR
		Column idCol = null;
		boolean foundPrimeKey = false;
		for (Entry<String, Column> cols : realColumns.entrySet()) {
			if ("id".equals(cols.getKey()))
				idCol = cols.getValue();
			if (cols.getValue().isObjectID()) {
				foundPrimeKey = true;
				break;
			}
		}
		if (!foundPrimeKey)
			if (idCol != null)
				idCol.setPrimeKey(true);
			else
				throwEX("SqlBox findAndSetPrimeKeys error, no prime key set for entity class " + entityClass);
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

	/**
	 * Get RowMapper
	 */
	protected RowMapper<Object> getRowMapper() {
		return new SqlBoxRowMapper(this);
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
	 * Config prime keys
	 */
	public void configPrimeKeys(String... fieldIDs) {
		for (Entry<String, Column> entry : getConfigColumns().entrySet())
			entry.getValue().setPrimeKey(false);
		for (String fieldID : fieldIDs)
			getOrBuildConfigColumn(fieldID).setPrimeKey(true);
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
	public void configIdGenerator(String fieldID, IdGenerator idGenerator) {
		getOrBuildConfigColumn(fieldID).setIdGenerator(idGenerator);
	}

	// ========Config methods end==============

	// == shortcut methods, just put some common used public static method here======

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
