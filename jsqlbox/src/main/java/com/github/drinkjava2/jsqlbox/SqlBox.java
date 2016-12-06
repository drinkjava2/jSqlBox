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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.github.drinkjava2.jsqlbox.jpa.Column;
import com.github.drinkjava2.jsqlbox.jpa.GeneratedValue;
import com.github.drinkjava2.jsqlbox.jpa.GenerationType;
import com.github.drinkjava2.jsqlbox.jpa.TableGenerator;

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
			dao.setBean(bean);
			Method m = this.getEntityClass().getMethod("putDao", new Class[] { Dao.class });
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
			Method m = this.getEntityClass().getMethod("initialize", new Class[] { null });
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
		String table = null;
		if (!SqlBoxUtils.isEmptyStr(configTable))
			table = configTable;
		if (SqlBoxUtils.isEmptyStr(table))
			table = SqlBoxUtils.getStaticStringField(this.entityClass, "Table");
		if (!context.existTable(table))
			table = context.cacheTableStructure(table);
		return table;
	}

	/**
	 * In entity class, a legal fieldID must be "userName" format, and has a public UserName() method
	 */
	private boolean isLegalFieldID(String fieldID) {
		try {
			if (SqlBoxUtils.isCapitalizedString(fieldID))
				return false;
			String capitalCase = SqlBoxUtils.toFirstLetterUpperCase(fieldID);
			Method method = this.entityClass.getMethod(capitalCase, new Class[] {});
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
			SqlBoxException.throwEX(null, "SqlBox getRealColumns error, beanClass can not be null");
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
				if (!SqlBoxUtils.isEmptyStr(fieldID)) {// NOSONAR
					Column column = new Column();
					column.setFieldID(fieldID);
					column.setColumnName(this.getRealColumnName(fieldID));
					column.setPropertyType(pd.getPropertyType());
					column.setReadMethodName(pd.getReadMethod().getName());
					column.setWriteMethodName(pd.getWriteMethod().getName());
					useConfigOverrideDefault(fieldID, column);
					realColumns.put(fieldID, column);
				}
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
			column.setGeneratedValue(configColumn.getGeneratedValue());
			column.setPrimeKey(configColumn.isPrimeKey());
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
	private static void findAndSetPrimeKeys(Class<?> entityClass, Map<String, Column> realColumns) {// NOSONAR
		Column idCol = null;
		Column generatorCol = null;
		int generatorCount = 0;
		for (String fieldID : realColumns.keySet()) {// NOSONAR
			Column col = realColumns.get(fieldID);
			if (col.isPrimeKey())
				return;
			if ("id".equals(fieldID))
				idCol = col;
			if (col.getGeneratedValue() != null) {
				generatorCount++;
				generatorCol = col;
			}
		}
		if (idCol != null) {
			idCol.setPrimeKey(true);
			return;
		}
		if (generatorCount >= 2)
			SqlBoxException.throwEX(null,
					"SqlBox findAndSetPrimeKeys error: can not set prime key for entity which has many ID generators: "
							+ entityClass + ", please set PrimeKey by setPimeKeys() method");
		if (generatorCount == 0)
			SqlBoxException.throwEX(null,
					"SqlBox findAndSetPrimeKeys error: can not find prime key for entity class: " + entityClass);
		if (generatorCol != null)
			generatorCol.setPrimeKey(true);
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

		Map<String, Column> dbMetaData = context.getTableMetaData(realTable);

		String realColumnNameignoreCase = null;
		Column realColumn = dbMetaData.get(columnName.toLowerCase());
		if (realColumn != null)
			realColumnNameignoreCase = realColumn.getColumnName();

		String realColumnNameUnderline = null;
		realColumn = dbMetaData.get(SqlBoxUtils.camelToLowerCaseUnderline(columnName));
		if (realColumn != null)
			realColumnNameUnderline = realColumn.getColumnName();

		if (realColumnNameignoreCase == null && realColumnNameUnderline == null)
			SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" does not match any table column in table " + realTable);

		if (realColumnNameignoreCase != null && realColumnNameUnderline != null
				&& !realColumnNameignoreCase.equals(realColumnNameUnderline))
			SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" found mutiple columns in table " + realTable);
		return realColumnNameignoreCase != null ? realColumnNameignoreCase : realColumnNameUnderline;
	}

	/**
	 * Get RowMapper
	 */
	protected RowMapper<Object> getRowMapper() {
		return new SqlBoxRowMapper(this);
	}

	// ========Config methods begin==============
	public void configTable(String table) {
		configTable = table;
	}

	public void configColumnName(String fieldID, String columnName) {
		getOrBuildConfigColumn(fieldID).setColumnName(columnName);
	}

	public Column getOrBuildConfigColumn(String fieldID) {
		Column col = this.getConfigColumns().get(fieldID);
		if (col == null) {
			col = new Column();
			this.getConfigColumns().put(fieldID, col);
		}
		return col;
	}

	public void configIdGenerator(String fieldID, GenerationType type) {
		Column config = getOrBuildConfigColumn(fieldID);
		config.setGeneratedValue(new GeneratedValue(type));
	}

	public void configGeneratedValue(String fieldID, GenerationType type, String name) {
		Column config = getOrBuildConfigColumn(fieldID);
		config.setGeneratedValue(new GeneratedValue(type, name));
	}

	public String configTableGenerator(String name, String table, String pkColumnName, String pkColumnValue,
			String valueColumnName, int initialValue, int allocationSize) {
		TableGenerator generator = (TableGenerator) this.getContext().getGeneratorFromCache(name);
		if (generator == null) {
			generator = new TableGenerator(name, table, pkColumnName, pkColumnValue, valueColumnName, initialValue,
					allocationSize);
			this.getContext().putGeneratorToCache(name, generator);
		} else {
			if (!generator.ifEqual(name, table, pkColumnName, pkColumnValue, valueColumnName, initialValue,
					allocationSize))
				SqlBoxException.throwEX(null,
						"SqlBox configTableGenerator error: duplicated TableGenerator name but different defines, name: "
								+ name);
		}
		return name;
	}

	// ========Config methods end==============

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

	public String getConfigTable() {
		return configTable;
	}

	public void setConfigTable(String configTable) {
		this.configTable = configTable;
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

}
