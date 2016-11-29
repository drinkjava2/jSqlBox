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

import com.github.drinkjava2.jsqlbox.id.GeneratedValue;
import com.github.drinkjava2.jsqlbox.id.GenerationType;
import com.github.drinkjava2.jsqlbox.jpa.Column;

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

	// RuntimeColumns, use it after entity bean be created
	private Map<String, Column> runtimeColumns = new HashMap<>();

	// RuntimeTableName, use it after entity bean be created
	private String runtimeTableName;

	// ConfigColumns, set it before entity bean be created
	private Map<String, String> configColumns = new HashMap<>();

	// ConfigTableName, set it before entity bean be created
	private String configTableName;

	// Prime key generate strategy value
	private GeneratedValue generatedValue;

	private GenerationType generationType;

	private SqlBoxContext context;

	public SqlBox() {
		// Default Constructor
	}

	public SqlBox(SqlBoxContext context) {
		this.context = context;
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
					this.runtimeTableName = value;
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
				column.setColumnDefinition(fieldname);
				column.setPropertyType(pd.getPropertyType());
				column.setReadMethodName(pd.getReadMethod().getName());
				column.setWriteMethodName(pd.getWriteMethod().getName());
				this.putRuntimeColumn(pd.getName(), column);
			}
		}
	}

	/**
	 * Override configuration by given SqlBox configuration
	 */
	private void changeColumnNameAccordingConfig() {
		if (!SqlBoxUtils.isEmptyStr(configTableName))
			this.runtimeTableName = configTableName;
		for (Entry<String, String> f : configColumns.entrySet()) {
			Column col = runtimeColumns.get(f.getKey());
			if (col == null) {
				Column newCol = new Column();
				newCol.setColumnDefinition(f.getValue());
				runtimeColumns.put(f.getKey(), newCol);
			} else {
				col.setColumnDefinition(f.getValue());
				if (SqlBoxUtils.isEmptyStr(f.getValue()))
					runtimeColumns.remove(f.getKey());
			}
		}
	}

	/**
	 * Correct column name, for "userName" field <br/>
	 * Find column ignore case like "userName","UserName","USERNAME","username", or "user_name"<br/>
	 * if not found or more than 1, throw SqlBoxException
	 */
	private void automaticFitColumnName() {// NOSONAR
		if (!context.existTable(runtimeTableName))
			runtimeTableName = context.cacheTableStructure(runtimeTableName);
		Map<String, Column> databaseColumns = context.getTableStructure(runtimeTableName);
		for (Entry<String, Column> entry : runtimeColumns.entrySet()) {
			Column col = entry.getValue();
			String columnName = col.getColumnDefinition();
			if (!SqlBoxUtils.isEmptyStr(columnName)) {

				String lowerCase = columnName.toLowerCase();
				String realColumnNameignoreCase = null;
				Column realColumn = databaseColumns.get(lowerCase);
				if (realColumn != null)
					realColumnNameignoreCase = realColumn.getColumnDefinition();
				String underlineCase = SqlBoxUtils.camelToLowerCaseUnderline(columnName);
				String realColumnNameUnderline = null;
				realColumn = databaseColumns.get(underlineCase);
				if (realColumn != null)
					realColumnNameUnderline = realColumn.getColumnDefinition();
				if (realColumnNameignoreCase == null && realColumnNameUnderline == null)
					SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \""
							+ columnName + "\" does not match any table column in table " + runtimeTableName);

				if (realColumnNameignoreCase != null && realColumnNameUnderline != null
						&& !realColumnNameignoreCase.equals(realColumnNameUnderline))
					SqlBoxException.throwEX(null, "SqlBox automaticFitColumnName error, column defination \""
							+ columnName + "\" found mutiple columns in table " + runtimeTableName);

				if (realColumnNameignoreCase != null)
					col.setColumnDefinition(realColumnNameignoreCase);
				else if (realColumnNameUnderline != null)
					col.setColumnDefinition(realColumnNameUnderline);
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

	public void configTableName(String tableName) {
		configTableName = tableName;
		this.runtimeTableName = tableName;
	}

	public void configColumnName(String fieldID, String columnDefinition) {
		configColumns.put(fieldID, columnDefinition);
		Column col = runtimeColumns.get(fieldID);
		if (col != null)
			col.setColumnDefinition(columnDefinition);
	}
	// ========Config methods end==============

	// ========getter & setters below==============

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public void setPKStrategy(GenerationType generationType, String... args) {
		this.generationType = generationType;
		if (generationType == GenerationType.AUTO) {
			// TODO
		} else if (generationType == GenerationType.TABLE) {
			// TODO
		}
	}

	public String getRuntimeTableName() {
		return runtimeTableName;
	}

	public void setRuntimeTableName(String runtimeTableName) {
		this.runtimeTableName = runtimeTableName;
	}

	public Map<String, Column> getRuntimeColumns() {
		return runtimeColumns;
	}

	public void setRuntimeColumns(Map<String, Column> realColumns) {
		this.runtimeColumns = realColumns;
	}

	public void putRuntimeColumn(String fieldID, Column column) {
		this.runtimeColumns.put(fieldID, column);
	}

	public Column getRuntimeColumn(String fieldID) {
		return runtimeColumns.get(fieldID);
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}
}
