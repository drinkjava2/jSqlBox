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

import java.util.HashMap;
import java.util.Map;

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

	public SQLBoxContext getContext() {
		return context;
	}

	public SQLBox setContext(SQLBoxContext context) {
		this.context = context;
		return this;
	}

	// ==========static methods=========
}
