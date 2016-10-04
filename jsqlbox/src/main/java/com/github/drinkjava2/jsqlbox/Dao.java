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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 * @update 2016-09-28
 */
@SuppressWarnings("unchecked")
public class Dao {
	private Class<?> beanClass;
	private Object bean;

	private String sql;
	private String tablename;
	private HashMap<Object, Column> fields = new HashMap<Object, Column>();
	private SQLBoxContext context = SQLBoxContext.defaultContext;
	public JdbcTemplate jdbc = context.getJdbc();

	// ====================== Utils methods begin======================
	public static Dao createDefaultDao(Object bean) {
		return SQLBoxUtils.findDao(bean.getClass(), SQLBoxContext.defaultContext).setBean(bean);
	}

	public Object findID(Object o, Class<?> poClass) {
		return null;
	}
	// ====================== Utils methods end======================

	// ====================== CRUD methods begin======================

	public <T> T create() {
		return (T) SQLBoxUtils.createProxyBean(beanClass, this);
	}

	public static <T> T get(Object id) {
		return null;
	}

	public void fillColumnValues(ArrayList<Column> columns) {
		for (Column column : columns) {
			//todo
		}
	}

	public ArrayList<Column> getBeanProperties() {
		ArrayList<Column> columns = new ArrayList<Column>();
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(beanClass);
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "Dao introspector error, beanClass=" + beanClass);
		}
		PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (!"class".equals(pd.getName())) {
				Method md = pd.getReadMethod();
				Column column = new Column();
				try {
					column.setValue(md.invoke(this, new Object[] {}));
				} catch (Exception e) {
					SQLBoxUtils.throwEX(e, "Dao introspector error, beanClass=" + beanClass + ", name=" + pd.getName());
				}
				column.setName(pd.getName());
				column.setColumnDefinition(pd.getName());// to be changed
				column.setPropertyType(pd.getPropertyType());
				columns.add(column);
			}
		}
		return columns;
	}

	public void save() {
		ArrayList<Column> columns = getBeanProperties();
		fillColumnValues(columns);
		System.out.println("saved");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("insert into user(username, address) values(?,?)",
						new String[] { "username" });
				ps.setString(1, "123");
				ps.setString(2, "456");
				return ps;
			}
		}, keyHolder);
		System.out.println(keyHolder.getKeyList());
	}

	public void load(Object... args) {
	};

	public void delete(Object... args) {
	};

	public void find(Object... args) {
	};

	// ====================== CRUD methods end======================

	// =============Garbage Getter & Setter code begin========

	public Object getBean() {
		return bean;
	}

	public Dao setBean(Object bean) {
		this.bean = bean;
		return this;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public Dao setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
		return this;
	}

	public String getSql() {
		return sql;
	}

	public Dao setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public String getTablename() {
		return tablename;
	}

	public Dao setTablename(String tablename) {
		this.tablename = tablename;
		return this;
	}

	public Dao setField(Object field, Column column) {
		System.out.println(field);
		fields.put(field, column);
		return this;
	}

	public SQLBoxContext getContext() {
		return context;
	}

	public Dao setContext(SQLBoxContext context) {
		this.context = context;
		jdbc = context.getJdbc();
		return this;
	}
	// =============Garbage Getter & Setter code end========
}
