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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * jSqlBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 */
public class SqlBox {
	private Class<?> beanClass;
	private String tablename;
	private Map<String, Column> columns = new HashMap<>();
	private Map<String, String> configColumns = new HashMap<>();
	private String overrideTableName;

	private SqlBoxContext context = SqlBoxContext.DEFAULT_SQLBOX_CONTEXT;
	public static final SqlBox DEFAULT_SQLBOX = new SqlBox(SqlBoxContext.DEFAULT_SQLBOX_CONTEXT);

	public SqlBox() {
		// Default Constructor
	}

	public SqlBox(SqlBoxContext context) {
		this.context = context;
	}

	public static <T> T get(Class<?> beanOrBoxClass) {
		return SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.get(beanOrBoxClass);
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public SqlBox setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
		return this;
	}

	public String getTablename() {
		return tablename;
	}

	public void overrideTableName(String tableName) {
		overrideTableName = tableName;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public Map<String, Column> getColumns() {
		return columns;
	}

	public SqlBox setColumns(Map<String, Column> columns) {
		this.columns = columns;
		return null;
	}

	public SqlBox setColumnName(String fieldID, String columnDefinition) {
		configColumns.put(fieldID, columnDefinition);
		Column col=columns.get(fieldID);
		if (col != null)
			col.setColumnDefinition(columnDefinition);
		return this;
	}

	public Column getColumn(String fieldID) {
		return columns.get(fieldID);
	}

	public void putColumn(String fieldID, Column column) {
		this.columns.put(fieldID, column);
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public SqlBox setContext(SqlBoxContext context) {
		this.context = context;
		return this;
	}

	public void initialize() {
		buildDefaultConfig();
		changeColumnNameAccordingConfig();
		automaticFitColumnName();
	}

	/**
	 * Use default Bean configuration written in Bean to fill sqlBox<br/>
	 * Field name will be used as database table column name
	 */
	public void buildDefaultConfig() {
		if (this.getBeanClass() == null)
			SqlBoxUtils.throwEX(null, "SqlBox buildDefaultConfig error, BeanClass is null");
		HashMap<String, String> fieldMap = new HashMap<>();
		Field[] fields = this.getBeanClass().getFields();
		for (int i = fields.length - 1; i >= 0; i--) {
			Field field = fields[i];
			if (SqlBoxUtils.isCapitalizedString(field.getName())) {
				String value = null;
				try {
					value = (String) field.get(null);
				} catch (Exception e) {
					SqlBoxUtils.throwEX(e, "SqlBox buildDefaultConfig error, can not get field value");
				}
				fieldMap.put(SqlBoxUtils.toFirstLetterLowerCase(field.getName()), value);
				if ("Table".equals(field.getName()))
					this.setTablename(value);
			}
		}
		fillProperties(fieldMap);
	}

	/**
	 * Override configuration by given SqlBox configuration
	 */
	private void changeColumnNameAccordingConfig() {
		if (!SqlBoxUtils.isEmptyStr(overrideTableName))
			this.setTablename(overrideTableName);
		for (Entry<String, String> f : configColumns.entrySet()) {
			Column col = columns.get(f.getKey());
			col.setColumnDefinition(f.getValue());
			if (SqlBoxUtils.isEmptyStr(f.getValue()))
				columns.remove(f.getKey());
		}
	}

	/**
	 * Correct column name, for "userName" field <br/>
	 * 1. Check if exist column key "username" in cached table structure<br/>
	 * 2. If not found, check and correct to "user_name"<br/>
	 * 3. if not found or found both, throw SqlBoxException
	 */
	private void automaticFitColumnName() {
		if (!context.existTable(tablename))
			context.loadTableStructure(tablename);
		Map<String, Column> databaseColumns = context.getTableStructure(tablename);

		for (Entry<String, Column> entry : columns.entrySet()) {
			Column col = entry.getValue();
			String columnName = col.getColumnDefinition();
			if (!SqlBoxUtils.isEmptyStr(columnName)) {
				String lowerCase = columnName.toLowerCase();
				String lowerCaseUnderline = SqlBoxUtils.camelToLowerCaseUnderline(columnName);
				if (databaseColumns.get(lowerCase) == null && databaseColumns.get(lowerCaseUnderline) == null)
					SqlBoxUtils.throwEX(null, "SqlBox automaticFitColumnName error, in table " + tablename
							+ ", column defination " + columnName + " does match any table column");
				if (!lowerCase.equals(lowerCaseUnderline) && databaseColumns.get(lowerCase) == null
						&& databaseColumns.get(lowerCaseUnderline) != null)
					col.setColumnDefinition(lowerCaseUnderline);
			}
		}
	}

	/**
	 * Use Introspector to fill column properties
	 */
	private void fillProperties(HashMap<String, String> fieldMap) {
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(this.getBeanClass());
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			SqlBoxUtils.throwEX(e, "SqlBox buildDefaultConfig error");
		}
		for (PropertyDescriptor pd : pds) {
			String columnDef = fieldMap.get(pd.getName());
			if (!SqlBoxUtils.isEmptyStr(columnDef)) {
				Column column = new Column();
				column.setName(pd.getName());
				if ("id".equals(pd.getName()))
					column.setPrimeKey(true);
				column.setColumnDefinition(columnDef);
				column.setPropertyType(pd.getPropertyType());
				column.setReadMethod(pd.getReadMethod());
				column.setWriteMethod(pd.getWriteMethod());
				this.putColumn(pd.getName(), column);
			}
		}
	}

	public void debug() {
		SqlBoxUtils.debug("Table=" + this.getTablename());
		Set<String> columnkeys = columns.keySet();
		for (String fieldname : columnkeys) {
			Column col = columns.get(fieldname);
			SqlBoxUtils.debug("=============================");
			SqlBoxUtils.debug("fieldname=" + fieldname);
			SqlBoxUtils.debug("getColumnDefinition=" + col.getColumnDefinition());
			SqlBoxUtils.debug("getForeignKey=" + col.getForeignKey());
			SqlBoxUtils.debug("getName=" + col.getName());
			SqlBoxUtils.debug("getPropertyType=" + col.getPropertyType());
			SqlBoxUtils.debug("getReadMethod=" + col.getReadMethod());
			SqlBoxUtils.debug("getWriteMethod=" + col.getWriteMethod());
			SqlBoxUtils.debug("isPrimeKey=" + col.isPrimeKey());
		}
	}
}
