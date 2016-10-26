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
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 */
public class SQLBox {
	private Class<?> beanClass;
	private String tablename;
	private Map<String, Column> columns = new HashMap<>();
	private Map<String, String> configColumns = new HashMap<>();
	private String overrideTableName;

	private SQLBoxContext context = SQLBoxContext.DEFAULT_SQLBOX_CONTEXT;
	public static final SQLBox DEFAULT_SQLBOX = new SQLBox(SQLBoxContext.DEFAULT_SQLBOX_CONTEXT);

	public SQLBox() {
		// Default Constructor
	}

	public SQLBox(SQLBoxContext context) {
		this.context = context;
	}

	public static <T> T get(Class<?> beanOrBoxClass) {
		return SQLBoxContext.DEFAULT_SQLBOX_CONTEXT.get(beanOrBoxClass);
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public SQLBox setBeanClass(Class<?> beanClass) {
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

	public SQLBox setColumns(Map<String, Column> columns) {
		this.columns = columns;
		return null;
	}

	public SQLBox overrideColumnDefinition(String fieldID, String columnDefinition) {
		configColumns.put(fieldID, columnDefinition);
		return this;
	}

	public Column getColumn(String fieldID) {
		return columns.get(fieldID);
	}

	public void putColumn(String fieldID, Column column) {
		this.columns.put(fieldID, column);
	}

	public SQLBoxContext getContext() {
		return context;
	}

	public SQLBox setContext(SQLBoxContext context) {
		this.context = context;
		return this;
	}

	public void initialize() {
		buildDefaultConfig();
		automaticCorrectColumnName();
		changeColumnNameAccordingConfig();
	}

	/**
	 * Use default Bean configuration written in Bean to fill sqlBox
	 */
	public void buildDefaultConfig() {
		if (this.getBeanClass() == null)
			SQLBoxUtils.throwEX(null, "SQLBox buildDefaultConfig error, BeanClass is null");
		HashMap<String, String> fieldMap = new HashMap<>();
		Field[] fields = this.getBeanClass().getFields();
		for (int i = fields.length - 1; i >= 0; i--) {
			Field field = fields[i];
			if (SQLBoxUtils.isCapitalizedString(field.getName())) {
				String value = null;
				try {
					value = (String) field.get(null);
				} catch (Exception e) {
					SQLBoxUtils.throwEX(e, "SQLBox buildDefaultConfig error, can not get field value");
				}
				fieldMap.put(SQLBoxUtils.toFirstLetterLowerCase(field.getName()), value);
				if ("Table".equals(field.getName()))
					this.setTablename(value);
			}
		}
		fillProperties(fieldMap);
	}

	/**
	 * Override configuration by given SQLBox configuration
	 */
	private void changeColumnNameAccordingConfig() {
		if (!SQLBoxUtils.isEmptyStr(overrideTableName))
			this.setTablename(overrideTableName);
		for (Entry<String, String> f : configColumns.entrySet()) {
			Column col = columns.get(f.getKey());
			col.setColumnDefinition(f.getValue());
			if (SQLBoxUtils.isEmptyStr(f.getValue()))
				columns.remove(f.getKey());
		}
	}

	/**
	 * Correct column name, <br/>
	 * "userName" field auto correct to "UserName" or "user_name"
	 */
	private void automaticCorrectColumnName() {
		if (!context.existTable(tablename))
			context.loadTableStructure(tablename);
		Map<String, Column> _columns = context.getTableStructure(tablename);

		for (Entry<String, Column> entry : columns.entrySet()) {
			String fieldname = entry.getKey();
			Column col = entry.getValue();
			col.getColumnDefinition();
			//TODO

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
			SQLBoxUtils.throwEX(e, "SQLBox buildDefaultConfig error");
		}
		for (PropertyDescriptor pd : pds) {
			String columnDef = fieldMap.get(pd.getName());
			if (!SQLBoxUtils.isEmptyStr(columnDef)) {
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
		SQLBoxUtils.debug("Table=" + this.getTablename());
		Set<String> columnkeys = columns.keySet();
		for (String fieldname : columnkeys) {
			Column col = columns.get(fieldname);
			SQLBoxUtils.debug("=============================");
			SQLBoxUtils.debug("fieldname=" + fieldname);
			SQLBoxUtils.debug("getColumnDefinition=" + col.getColumnDefinition());
			SQLBoxUtils.debug("getForeignKey=" + col.getForeignKey());
			SQLBoxUtils.debug("getName=" + col.getName());
			SQLBoxUtils.debug("getPropertyType=" + col.getPropertyType());
			SQLBoxUtils.debug("getReadMethod=" + col.getReadMethod());
			SQLBoxUtils.debug("getWriteMethod=" + col.getWriteMethod());
			SQLBoxUtils.debug("isPrimeKey=" + col.isPrimeKey());
		}
	}
}
