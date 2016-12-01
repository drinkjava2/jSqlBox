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
	private Class<?> beanClass;

	// Configuration Columns, set it before entity bean be created
	private Map<String, Column> configColumns = new HashMap<>();

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

	public static <T> T createBean(Class<?> beanOrSqlBoxClass) {
		SqlBoxContext ctx = SqlBoxContext.getDefaultSqlBoxContext();
		return ctx.createBean(beanOrSqlBoxClass);
	}

	/**
	 * Create entity bean
	 */
	public <T> T createBean() {
		Object bean = null;
		// this.initialize();
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

	public String getRealTable() {
		return configTable;
		// TODO add method body
	}

	public Map<String, Column> getRealColumns() {
		return null;
		// TODO add method body
	}

	public Column getRealColumn(String fieldID) {
		return null;
		// TODO add method body
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
		configColumns.get(fieldID).setColumnName(columnName);
	}

	public void configIdGenerator(GenerationType type, String name) {
		this.setGeneratedValue(new GeneratedValue(type, name));
	}

	public String configTableGenerator(String name, String table, String pkColumnName, String pkColumnValue,
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

	public GeneratedValue getGeneratedValue() {
		return generatedValue;
	}

	public void setGeneratedValue(GeneratedValue generatedValue) {
		this.generatedValue = generatedValue;
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

}
