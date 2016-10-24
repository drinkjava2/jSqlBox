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

/**
 * Dedicate this project to my wife Mei, thanks her work hard take care of our family
 */

package com.github.drinkjava2.jsqlbox;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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
	private Object bean;
	private String tablename;
	private Map<String, Column> columns = new HashMap<>();
	private SQLBoxContext context = SQLBoxContext.defaultContext;
	public static final SQLBox defaultSQLBox = new SQLBox(SQLBoxContext.defaultContext);

	public SQLBox() {
		// Default Constructor
	}

	public SQLBox(SQLBoxContext context) {
		this.context = context;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public SQLBox setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
		return this;
	}

	public Object getBean() {
		return bean;
	}

	public SQLBox setBean(Object bean) {
		this.bean = bean;
		return this;
	}

	public String getTablename() {
		return tablename;
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

	public void buildDefaultConfig() {
		if (this.getBeanClass() == null)
			SQLBoxUtils.throwEX(null, "SQLBox buildDefaultConfig error, BeanClass is null");
		if (this.getBean() == null)
			SQLBoxUtils.throwEX(null, "SQLBox buildDefaultConfig error, Bean is null");
		HashMap<String, String> fieldMap = new HashMap<>();
		Field[] fields = this.getBeanClass().getFields();
		for (Field field : fields) {
			if (SQLBoxUtils.isCapitalizedString(field.getName())) {
				String value = null;
				try {
					value = (String) field.get(null);
				} catch (Exception e) {
					SQLBoxUtils.logException(e);
				}
				fieldMap.put(SQLBoxUtils.toFirstLetterLowerCase(field.getName()), value);
				if ("Table".equals(field.getName()))
					this.setTablename(value);
			}
		}
		// fields declared by self
		fields = this.getBeanClass().getDeclaredFields();
		for (Field field : fields) {
			if (SQLBoxUtils.isCapitalizedString(field.getName())) {
				String value = null;
				try {
					value = (String) field.get(null);
				} catch (Exception e) {
					SQLBoxUtils.logException(e);
				}
				fieldMap.put(SQLBoxUtils.toFirstLetterLowerCase(field.getName()), value);
				if ("Table".equals(field.getName()))
					this.setTablename(value);
			}
		}

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
		System.out.println("Table=" + this.getTablename());
		Set<String> columnkeys = columns.keySet();
		for (String fieldname : columnkeys) {
			Column col = columns.get(fieldname);
			System.out.println("=============================");
			System.out.println("fieldname=" + fieldname);
			System.out.println("getColumnDefinition=" + col.getColumnDefinition());
			System.out.println("getForeignKey=" + col.getForeignKey());
			System.out.println("getName=" + col.getName());
			System.out.println("getPropertyType=" + col.getPropertyType());
			System.out.println("getReadMethod=" + col.getReadMethod());
			System.out.println("getWriteMethod=" + col.getWriteMethod());
			System.out.println("isPrimeKey=" + col.isPrimeKey());
		}
	}
}
