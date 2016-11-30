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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	private Class<?> beanClass;

	// Real Columns, use it after entity bean be created
	private Map<String, Column> realColumns = new HashMap<>();

	// Real Table Name, use it after entity bean be created
	private String realTable;

	// Configuration Columns, set it before entity bean be created
	private Map<String, String> configColumns = new HashMap<>();

	// Configuration Table Name, set it before entity bean be created
	private String configTable;

	// Prime key generate strategy value
	private GeneratedValue generatedValue;

	private SqlBoxContext context;

	public SqlBox() {
		// Default Constructor
	}

	public SqlBox(SqlBoxContext context) {
		this.context = context;
	}

	/**
	 * Initialize a SqlBox instance<br/>
	 * 1. Use bean field as column name, field userName map to DB userName
	 * column <br/>
	 * 2. If find configuration column name, use it, for example: user_name<br/>
	 * 3. Fit column name to real DB column name, automatic fit camel and
	 * underline format<br/>
	 */
	public void initialize() {
		buildDefaultConfig();// field userName map to column userName
		changeColumnNameAccordingConfig(); // map to column ConfigUserName
		automaticFitColumnName();// fit to ConfigUserName or config_user_name
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
		this.initialize();
		try {
			bean = this.getBeanClass().newInstance();
			this.beanInitialize(bean);
			Dao dao = new Dao(this);
			dao.setBean(bean);
			Method m = this.getBeanClass().getMethod("putDao", new Class[] { Dao.class });
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
			Method m = this.getBeanClass().getMethod("initialize", new Class[] { SqlBox.class });
			if (m != null)
				m.invoke(bean, new Object[] { this });
		} catch (Exception e) {
			SqlBoxException.eatException(e);
		}
	}

	/**
	 * Use default Bean configuration written in Bean to fill sqlBox<br/>
	 * Field name will be used as database table column name
	 */
	private void buildDefaultConfig() {
		if (this.getBeanClass() == null)
			SqlBoxException.throwEX(null, "SqlBox buildDefaultConfig error, BeanClass is null");
		HashMap<String, String> fieldIdNameMap = new HashMap<>();
		Field[] fields = this.getBeanClass().getFields();
		for (int i = fields.length - 1; i >= 0; i--) {
			Field field = fields[i];
			String fieldname = field.getName();
			if (SqlBoxUtils.isCapitalizedString(fieldname)) {
				String value = null;
				try {
					value = (String) field.get(null);
				} catch (Exception e) {
					SqlBoxException.throwEX(e,
							"SqlBox buildDefaultConfig error, cann't access field \"" + fieldname + "\"");
				}

				if ("Table".equals(fieldname))
					this.realTable = value;
				else
					fieldIdNameMap.put(SqlBoxUtils.toFirstLetterLowerCase(fieldname), value);
			}
		}
		fillProperties(fieldIdNameMap);
	}

	/**
	 * Use Introspector to fill column properties
	 */
	private void fillProperties(HashMap<String, String> fieldIdNameMap) {
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(this.getBeanClass());
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBox buildDefaultConfig error");
		}
		for (PropertyDescriptor pd : pds) {
			String fieldname = fieldIdNameMap.get(pd.getName());
			if (!SqlBoxUtils.isEmptyStr(fieldname)) {
				Column column = new Column();
				column.setName(pd.getName());
				if ("id".equals(pd.getName()))
					column.setPrimeKey(true);
				column.setColumnName(fieldname);
				column.setPropertyType(pd.getPropertyType());
				column.setReadMethodName(pd.getReadMethod().getName());
				column.setWriteMethodName(pd.getWriteMethod().getName());
				this.putRealColumn(pd.getName(), column);
			}
		}
	}

	/**
	 * Override configuration by given SqlBox configuration
	 */
	private void changeColumnNameAccordingConfig() {
		if (!SqlBoxUtils.isEmptyStr(configTable))
			this.realTable = configTable;
		for (Entry<String, String> f : configColumns.entrySet()) {
			Column col = realColumns.get(f.getKey());
			if (col == null) {
				Column newCol = new Column();
				newCol.setColumnName(f.getValue());
				realColumns.put(f.getKey(), newCol);
			} else {
				col.setColumnName(f.getValue());
				if (SqlBoxUtils.isEmptyStr(f.getValue()))
					realColumns.remove(f.getKey());
			}
		}
	}

	/**
	 * Correct column name, for "userName" field <br/>
	 * Find column ignore case like "userName","UserName","USERNAME","username",
	 * or "user_name"<br/>
	 * if not found or more than 1, throw SqlBoxException
	 */
	private void automaticFitColumnName() {// NOSONAR
		if (!context.existTable(realTable))
			realTable = context.cacheTableStructure(realTable);
		Map<String, Column> databaseColumns = context.getTableStructure(realTable);
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			String columnName = col.getColumnName();
			if (!SqlBoxUtils.isEmptyStr(columnName)) {

				String lowerCase = columnName.toLowerCase();
				String realColumnNameignoreCase = null;
				Column realColumn = databaseColumns.get(lowerCase);
				if (realColumn != null)
					realColumnNameignoreCase = realColumn.getColumnName();
				String underlineCase = SqlBoxUtils.camelToLowerCaseUnderline(columnName);
				String realColumnNameUnderline = null;
				realColumn = databaseColumns.get(underlineCase);
				if (realColumn != null)
					realColumnNameUnderline = realColumn.getColumnName();
				if (realColumnNameignoreCase == null && realColumnNameUnderline == null)
					SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \""
							+ columnName + "\" does not match any table column in table " + realTable);

				if (realColumnNameignoreCase != null && realColumnNameUnderline != null
						&& !realColumnNameignoreCase.equals(realColumnNameUnderline))
					SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \""
							+ columnName + "\" found mutiple columns in table " + realTable);

				if (realColumnNameignoreCase != null)
					col.setColumnName(realColumnNameignoreCase);
				else if (realColumnNameUnderline != null)
					col.setColumnName(realColumnNameUnderline);
			}
		}
	}

	/**
	 * SqlBoxRowMapper implements RowMapper interface
	 */
	public static class SqlBoxRowMapper implements RowMapper<Object> {
		SqlBox box;

		public SqlBoxRowMapper(SqlBox box) {
			this.box = box;
		}

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Object bean = null;
			try {
				bean = box.getBeanClass().newInstance();
			} catch (Exception e) {
				SqlBoxException.throwEX(e, "SqlBox getRowMapper error, beanClass=" + box.getBeanClass());
			}
			return bean;
		}
	}

	/**
	 * Get RowMapper
	 */
	protected RowMapper<Object> getRowMapper() {
		return new SqlBoxRowMapper(this);
	}

	// ========Config methods begin==============

	public void configTable(String tableName) {
		configTable = tableName;
		this.realTable = tableName;
	}

	public void configColumnName(String fieldID, String cfgColumnName) {
		configColumns.put(fieldID, cfgColumnName);
	}

	public void setGenerator(GenerationType type, String name) {
		this.setGeneratedValue(new GeneratedValue(type, name));
	}

	public String defineTableGenerator(String name, String table, String pkColumnName, String pkColumnValue,
			String valueColumnName, int initialValue, int allocationSize) {
		if (!this.getContext().existGeneratorInCache(name)) {
			TableGenerator generator = new TableGenerator(name, table, pkColumnName, pkColumnValue, valueColumnName,
					initialValue, allocationSize);
			this.getContext().putGeneratorToCache(name, generator);
		}
		return name;
	}

	// ========Config methods end==============

	// ========getter & setters below==============

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public String getRealTable() {
		return realTable;
	}

	public void setRealTable(String realTable) {
		this.realTable = realTable;
	}

	public Map<String, Column> getRealColumns() {
		return realColumns;
	}

	public void setRealColumns(Map<String, Column> realColumns) {
		this.realColumns = realColumns;
	}

	public void putRealColumn(String fieldID, Column column) {
		this.realColumns.put(fieldID, column);
	}

	public Column getRealColumn(String fieldID) {
		return realColumns.get(fieldID);
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

	public GeneratedValue getGeneratedValue() {
		return generatedValue;
	}

	public void setGeneratedValue(GeneratedValue generatedValue) {
		this.generatedValue = generatedValue;
	}

}
