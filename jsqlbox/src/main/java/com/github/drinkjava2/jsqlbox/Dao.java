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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.github.drinkjava2.jsqlbox.SQLHelper.SqlAndParameters;
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
	private String tablename;
	private String id;

	private HashMap<String, Column> columns = new HashMap<String, Column>();
	private SQLBoxContext context = SQLBoxContext.defaultContext;
	public static Dao dao = new Dao().setContext(SQLBoxContext.defaultContext);
	public JdbcTemplate jdbc = context.getJdbc();

	// ====================== Utils methods begin======================
	public static Dao createDefaultDao(Object bean) {
		return SQLBoxUtils.findDao(bean.getClass(), SQLBoxContext.defaultContext).setBean(bean);
	}

	public Object findID(Object o, Class<?> poClass) {
		return null;
	}

	public <T> T create() {
		return (T) SQLBoxUtils.createProxyBean(beanClass, this);
	}

	public static void initialize() {
		SQLHelper.clearSQLCache();
	}
	// ====================== Utils methods end======================

	// ========JdbcTemplate wrap methods begin============
	private static ThreadLocal<ArrayList<SqlAndParameters>> sqlBatchCache = new ThreadLocal<ArrayList<SqlAndParameters>>() {
		protected ArrayList<SqlAndParameters> initialValue() {
			return new ArrayList<SqlAndParameters>();
		}
	};
	private static ThreadLocal<String> sqlForBatch = new ThreadLocal<String>() {
		protected String initialValue() {
			return "";
		}
	};

	public void cleanCachedSql() {
		SQLHelper.clearSQLCache();
		sqlBatchCache.get().clear();
	}

	public void cacheSQL(String sql) {
		SqlAndParameters sp = SQLHelper.splitSQLandParameters(sql);
		sqlBatchCache.get().add(sp);
		sqlForBatch.set(sql);
	}

	public void executeCatchedSQLs() {
		try {
			int batchSize = 500;
			List<List<SqlAndParameters>> subSPlist = SQLBoxUtils.subList(sqlBatchCache.get(), batchSize);
			for (final List<SqlAndParameters> splist : subSPlist) {
				jdbc.batchUpdate(sqlForBatch.get(), new BatchPreparedStatementSetter() {
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						SqlAndParameters sp = splist.get(i);
						int index = 1;
						for (String parameter : sp.parameters) {
							ps.setString(index++, parameter);
						}
					}

					public int getBatchSize() {
						return splist.size();
					}
				});
			}
		} finally {
			this.cleanCachedSql();
		}
	}

	public Boolean execute(String sql) {
		return (Boolean) doRealExecute(sql, 0);
	}

	public ResultSet executeQuery(String sql) {
		return (ResultSet) doRealExecute(sql, 1);
	}

	public Integer executeUpdate(String sql) {
		return (Integer) doRealExecute(sql, 2);
	}

	private Object doRealExecute(String sql, int doWhat) {
		SqlAndParameters sp = SQLHelper.splitSQLandParameters(sql);
		DataSource ds = context.getDataSource();
		Connection con = DataSourceUtils.getConnection(ds);
		try {
			PreparedStatement stat = con.prepareStatement(sql);
			int index = 1;
			for (String parameter : sp.parameters) {
				stat.setString(index++, parameter);
			}
			if (doWhat == 0)
				return stat.execute();
			if (doWhat == 1)
				return stat.executeQuery();
			if (doWhat == 2)
				return stat.executeUpdate();
			return null;
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "SQLHelper exec error, sql=" + sql);
		} finally {
			this.cleanCachedSql();
			DataSourceUtils.releaseConnection(con, ds);
		}
		return false;
	}

	// ========JdbcTemplate wrap methods end============

	// ====================== CRUD methods begin======================

	public static <T> T get(Object id) {
		return null;
	}

	public void fillColumnValues(ArrayList<Column> columns) {

	}

	public void fillColumns() {
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(beanClass);
		} catch (Exception e) {
			SQLBoxUtils.throwEX(e, "Dao introspector error, beanClass=" + beanClass);
		}
		PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			// System.out.println("pd.getName()=" + pd.getName());
			if (!"class".equals(pd.getName())) {
				Method md = pd.getReadMethod();
				Column column = new Column();
				try {
					column.setValue(md.invoke(bean, new Object[] {}));
					// System.out.println("value=" + column.getValue());
				} catch (Exception e) {
					SQLBoxUtils.throwEX(e, "Dao introspector error, beanClass=" + beanClass + ", name=" + pd.getName());
				}
				column.setPropertyType(pd.getPropertyType());
				columns.put(pd.getName(), column);
			}
		}
	}

	public void save() {
		System.out.println("saved");// TODO not finished
		fillColumns();

		// KeyHolder keyHolder = new GeneratedKeyHolder();
		// jdbc.update(new PreparedStatementCreator() {
		// @Override
		// public PreparedStatement createPreparedStatement(Connection
		// connection) throws SQLException {
		// PreparedStatement ps = connection.prepareStatement(
		// "insert into user(username, address, age) values(?,?,?)", new
		// String[] { "id" });
		// ps.setString(1, "123");
		// ps.setString(2, "456");
		// ps.setString(3, "50");
		// return ps;
		// }
		// }, keyHolder);
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTablename() {
		return tablename;
	}

	public Dao setTablename(String tablename) {
		this.tablename = tablename;
		return this;
	}

	public Column getColumn(String fieldID) {
		return columns.get(fieldID);
	};

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
