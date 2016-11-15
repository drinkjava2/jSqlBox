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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBox {
	private Class<?> beanClass;
	private String tableName;
	private Map<String, Column> columns = new HashMap<>();
	private Map<String, String> configColumns = new HashMap<>();
	private String configTableName;

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

	/**
	 * Initialize a SqlBox instance<br/>
	 * 1. Use bean field as column name, field userName map to DB userName column <br/>
	 * 2. If find configuration column name, use it, for example: user_name<br/>
	 * 3. Fit column name to real DB column name, automatic fit camel and underline format<br/>
	 */
	public void initialize() {
		buildDefaultConfig();// field userName map to column userName
		changeColumnNameAccordingConfig(); // map to column ConfigUserName
		automaticFitColumnName();// fit to ConfigUserName or config_user_name
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
			if (SqlBoxUtils.isCapitalizedString(field.getName())) {
				String value = null;
				try {
					value = (String) field.get(null);
				} catch (Exception e) {
					SqlBoxException.throwEX(e,
							"SqlBox buildDefaultConfig error, cann't access field \"" + field.getName() + "\"");
				}

				if ("Table".equals(field.getName()))
					this.tableName = value;
				else
					fieldIdNameMap.put(SqlBoxUtils.toFirstLetterLowerCase(field.getName()), value);
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
				column.setColumnDefinition(fieldname);
				column.setPropertyType(pd.getPropertyType());
				column.setReadMethod(pd.getReadMethod());
				column.setWriteMethod(pd.getWriteMethod());
				this.putColumn(pd.getName(), column);
			}
		}
		for (Entry<String, String> keyAndName : fieldIdNameMap.entrySet())
			if (columns.get(keyAndName.getKey()) == null)
				SqlBoxException.throwEX(null, "SqlBox fillProperties error, no getter/setter method found for field \""
						+ keyAndName.getKey() + "\"");
	}

	/**
	 * Override configuration by given SqlBox configuration
	 */
	private void changeColumnNameAccordingConfig() {
		if (!SqlBoxUtils.isEmptyStr(configTableName))
			this.tableName = configTableName;
		for (Entry<String, String> f : configColumns.entrySet()) {
			Column col = columns.get(f.getKey());
			if (col == null) {
				Column newCol = new Column();
				newCol.setColumnDefinition(f.getValue());
				columns.put(f.getKey(), newCol);
			} else {
				col.setColumnDefinition(f.getValue());
				if (SqlBoxUtils.isEmptyStr(f.getValue()))
					columns.remove(f.getKey());
			}
		}
	}

	/**
	 * Correct column name, for "userName" field <br/>
	 * 1. Check if exist column key "username" in cached table structure<br/>
	 * 2. If not found, check and correct to "user_name"<br/>
	 * 3. if not found or found both, throw SqlBoxException
	 */
	private void automaticFitColumnName() {
		if (!context.existTable(tableName))
			context.cacheTableStructure(tableName);
		Map<String, Column> databaseColumns = context.getTableStructure(tableName);
		for (Entry<String, Column> entry : columns.entrySet()) {
			Column col = entry.getValue();
			String columnName = col.getColumnDefinition();
			if (!SqlBoxUtils.isEmptyStr(columnName)) {
				String lowerCase = columnName.toLowerCase();
				String lowerCaseUnderline = SqlBoxUtils.camelToLowerCaseUnderline(columnName);
				if (databaseColumns.get(lowerCase) == null && databaseColumns.get(lowerCaseUnderline) == null)
					SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \""
							+ columnName + "\" does match any table column in table " + tableName);
				if (!lowerCase.equals(lowerCaseUnderline) && databaseColumns.get(lowerCase) == null
						&& databaseColumns.get(lowerCaseUnderline) != null)
					col.setColumnDefinition(lowerCaseUnderline);
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
				ResultSetMetaData metaData = rs.getMetaData();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					Debugger.print(metaData.getTableName(i) + ".");
					Debugger.print(metaData.getColumnName(i) + "\t|\t");
					Debugger.print(metaData.getColumnLabel(i) + "\t|\t");
					Debugger.println(metaData.getColumnTypeName(i));
				}
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

	/**
	 * Print Debug info, for debug use only
	 */
	public void debug() {
		Debugger.println("Table=" + tableName);
		Set<String> columnkeys = columns.keySet();
		Debugger.println("==============Column values===============");
		for (String fieldname : columnkeys) {
			Column col = columns.get(fieldname);
			Debugger.println("fieldname=" + fieldname);
			Debugger.println("getColumnDefinition=" + col.getColumnDefinition());
			Debugger.println("getForeignKey=" + col.getForeignKey());
			Debugger.println("getName=" + col.getName());
			Debugger.println("getPropertyType=" + col.getPropertyType());
			Debugger.println("PropertyTypeName=" + col.getPropertyTypeName());
			Debugger.println("getReadMethod=" + col.getReadMethod());
			Debugger.println("getWriteMethod=" + col.getWriteMethod());
			Debugger.println("isPrimeKey=" + col.isPrimeKey());
			Debugger.println();
		}
		Map<String, Column> tableStructure = this.getContext().getTableStructure(tableName);
		columnkeys = tableStructure.keySet();
		Debugger.println("==============Table structure values===============");
		for (String fieldname : columnkeys) {
			Column col = tableStructure.get(fieldname);
			Debugger.println("fieldname=" + fieldname);
			Debugger.println("getColumnDefinition=" + col.getColumnDefinition());
			Debugger.println("getForeignKey=" + col.getForeignKey());
			Debugger.println("getName=" + col.getName());
			Debugger.println("getPropertyType=" + col.getPropertyType());
			Debugger.println("PropertyTypeName=" + col.getPropertyTypeName());
			Debugger.println("getReadMethod=" + col.getReadMethod());
			Debugger.println("getWriteMethod=" + col.getWriteMethod());
			Debugger.println("isPrimeKey=" + col.isPrimeKey());
			Debugger.println();
		}

	}

	// ========getter & setters below==============

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		configTableName = tableName;
		this.tableName = tableName;
	}

	public Map<String, Column> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, Column> columns) {
		this.columns = columns;
	}

	public void setColumnName(String fieldID, String columnDefinition) {
		configColumns.put(fieldID, columnDefinition);
		Column col = columns.get(fieldID);
		if (col != null)
			col.setColumnDefinition(columnDefinition);
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

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}
}
