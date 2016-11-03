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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 */
public class Dao {
	private SqlBox sqlBox;
	private JdbcTemplate jdbc;
	private Object bean; // PO Bean Instance
	public static final Dao dao = new Dao(SqlBox.DEFAULT_SQLBOX);

	public Dao(SqlBox sqlBox) {
		this.sqlBox = sqlBox;
		this.jdbc = new JdbcTemplate(sqlBox.getContext().getDataSource());
	}

	public static Dao defaultDao(Object bean) {
		SqlBox box = SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.findAndBuildSqlBox(bean.getClass());
		Dao doa = new Dao(box);
		doa.setBean(bean);
		return doa;
	}

	// ========JdbcTemplate wrap methods begin============

	/**
	 * Equal to jdbcTemplate.queryForObject(sql,Integer.class)
	 */
	public Integer queryForInteger(String sql) {
		return getJdbc().queryForObject(sql, Integer.class);
	}

	/**
	 * Equal to jdbcTemplate.queryForObject(sql,Long.class)
	 */
	public Long queryForLong(String sql) {
		return getJdbc().queryForObject(sql, Long.class);
	}

	public void cacheSQL(String... sql) {
		SqlHelper.cacheSQL(sql);
	}

	public void executeCachedSQLs() {
		try {
			List<List<SqlAndParameters>> subSPlist = SqlHelper.getSQLandParameterSubList();
			printCachedSQL(subSPlist);
			for (final List<SqlAndParameters> splist : subSPlist) {
				jdbc.batchUpdate(SqlHelper.getSqlForBatch().get(), new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						SqlAndParameters sp = splist.get(i);
						int index = 1;
						for (String parameter : sp.getParameters()) {
							ps.setString(index++, parameter);
						}
					}

					@Override
					public int getBatchSize() {
						return splist.size();
					}
				});
			}
		} finally {
			SqlHelper.clearBatchSQLs();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List query(RowMapper rowMapper, String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			printSQL(sp);
			return jdbc.query(sp.getSql(), sp.getParameters(), rowMapper);
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	public Integer execute(String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			printSQL(sp);
			return jdbc.update(sp.getSql(), (Object[]) sp.getParameters());
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Print SQL and parameters, usually used for debug
	 */
	private void printSQL(SqlAndParameters sp) {
		// check if allowed print SQL
		if (!this.getSqlBox().getContext().isShowSql())
			return;
		SqlBoxUtils.println(sp.getSql());
		String[] args = sp.getParameters();
		if (args.length > 0) {
			SqlBoxUtils.print("Parameters: ");
			for (int i = 0; i < args.length; i++) {
				SqlBoxUtils.print(args[i]);
				if (i != args.length - 1)
					SqlBoxUtils.print(",");
				else
					SqlBoxUtils.println();
			}
		}
		SqlBoxUtils.println();
	}

	/**
	 * Print Cached SQL and parameters, usually used for debug
	 */
	private void printCachedSQL(List<List<SqlAndParameters>> subSPlist) {
		if (this.getSqlBox().getContext().isShowSql()) {
			if (subSPlist != null) {
				List<SqlAndParameters> l = subSPlist.get(0);
				if (l != null) {
					SqlAndParameters sp = l.get(0);
					SqlBoxUtils.println("First Cached SQL:");
					printSQL(sp);
				}
			}
			if (subSPlist != null) {
				List<SqlAndParameters> l = subSPlist.get(subSPlist.size() - 1);
				if (l != null) {
					SqlAndParameters sp = l.get(l.size() - 1);
					SqlBoxUtils.println("Last Cached SQL:");
					printSQL(sp);
				}
			}
		}
	}

	// ========JdbcTemplate wrap methods end============

	// ===========utils methods begin==========

	// ===========utils methods end==========

	// =============== CRUD methods begin ===============
	/**
	 * Insert a Bean to Database
	 */
	public void save() {
		try {
			doSave();
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Insert a Bean to Database
	 */
	private void doSave() {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(sqlBox.getTableName()).append(" (");
		int howManyFields = 0;
		for (Column col : sqlBox.getColumns().values()) {
			if (!col.isPrimeKey() && !SqlBoxUtils.isEmptyStr(col.getColumnDefinition())) {

				Method m = col.getReadMethod();
				Object value = null;
				try {
					value = m.invoke(this.bean, new Object[] {});
				} catch (Exception e) {
					SqlBoxUtils.throwEX(e, "Dao save error, invoke method wrong.");
				}
				if (null != value) {
					howManyFields++;
					sb.append(col.getColumnDefinition()).append(",");
					SqlHelper.e(value);
				}
			}
		}
		sb.deleteCharAt(sb.length() - 1).append(") ");
		sb.append(SqlHelper.createValueString(howManyFields));
		this.execute(sb.toString());
	}

	// =============== CRUD methods end ===============

	// ================ Getters & Setters===============
	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	/**
	 * If possible, do not use JdbcTemplate directly, JDBCTemplate may be deleted in future version *
	 * 
	 * @return JdbcTemplate
	 */
	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public SqlBox getSqlBox() {
		return sqlBox;
	}

	public Dao setSqlBox(SqlBox sqlBox) {
		this.sqlBox = sqlBox;
		return this;
	}

}
