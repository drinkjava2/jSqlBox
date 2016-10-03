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
 * For developers: this project is set to each line 120 characters.  
 */

package com.github.drinkjava2.jsqlbox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
	private Context context = Context.defaultContext;
	public JdbcTemplate jdbc;

	public static ThreadLocal<HashMap<Object, Object>> poCache = new ThreadLocal<HashMap<Object, Object>>() {
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<Object, Object>();
		}
	};

	// ====================== Utils methods begin======================
	public Object findID(Object o, Class<?> poClass) {
		return null;
	}
	// ====================== Utils methods end======================

	// ====================== CRUD methods begin======================

	public static <T> T create(Class<?> clazz) {
		return Context.defaultContext.create(clazz);
	}

	public <T> T create() {
		return (T) SQLBoxUtils.createProxyBean(beanClass, this);
	}

	public static <T> T get(Object id) {
		return null;
	}

	public <T> T save() {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("insert into tb_test1(name,password) values(?,?)",
						new String[] { "id" });
				ps.setString(1, "");
				ps.setString(2, "");
				return ps;
			}
		}, keyHolder);

		return (T) keyHolder.getKeyList().get(0);
	}

	public <T> T update() {
		return null;
	}

	public <T> T delete() {
		return null;
	}

	// ====================== CRUD methods end======================

	// ====================== Getter & Setter methods begin======================

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

	public Context getContext() {
		return context;
	}

	public Dao setContext(Context context) {
		this.context = context;
		jdbc = context.getJdbc();
		return this;
	}
	// ====================== Getter & Setter methods end======================
}
