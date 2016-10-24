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
	private SQLBox sqlBox;
	private JdbcTemplate jdbc;
	public static final Dao dao = new Dao(SQLBox.defaultSQLBox);

	public Dao(SQLBox sqlBox) {
		this.sqlBox = sqlBox;
		this.jdbc = new JdbcTemplate(sqlBox.getContext().getDataSource());
	}

	public static Dao defaultDao(Object bean) {
		SQLBox box = new SQLBox(SQLBoxContext.defaultContext);
		box.setBeanClass(bean.getClass());
		box.setBean(bean);
		return new Dao(box);
	}

	// ========JdbcTemplate wrap methods begin============

	public void cacheSQL(String... sql) {
		SQLHelper.cacheSQL(sql);
	}

	public void executeCachedSQLs() {
		try {
			List<List<SqlAndParameters>> subSPlist = SQLHelper.getSQLandParameterSubList();
			for (final List<SqlAndParameters> splist : subSPlist) {
				jdbc.batchUpdate(SQLHelper.getSqlForBatch().get(), new BatchPreparedStatementSetter() {
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
			SQLHelper.clearBatchSQLs();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List query(RowMapper rowMapper, String... sql) {
		try {
			SqlAndParameters sp = SQLHelper.splitSQLandParameters(sql);
			return jdbc.query(sp.getSql(), sp.getParameters(), rowMapper);
		} finally {
			SQLHelper.clearLastSQL();
		}
	}

	public Integer execute(String... sql) {
		try {
			SqlAndParameters sp = SQLHelper.splitSQLandParameters(sql);
			return jdbc.update(sp.getSql(), (Object[]) sp.getParameters());
		} finally {
			SQLHelper.clearLastSQL();
		}
	}

	// ========JdbcTemplate wrap methods end============

	// ===========utils methods begin==========

	// ===========utils methods end==========

	// =============== CRUD methods begin ===============
	/**
	 * 1. find User.class 2. use default bean property to fill column 3. if find config, use config value to override
	 * column
	 */
	public void save() {
		sqlBox.buildDefaultConfig();
		//sqlBox.findConfiguration();
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(sqlBox.getTablename()).append(" (");
		int howManyFields = 0;
		for (Column col : sqlBox.getColumns().values()) {
			if (!col.isPrimeKey()) {
				howManyFields++;
				sb.append(col.getColumnDefinition()).append(",");
				Method m = col.getReadMethod();
				Object value = null;
				try {
					value = m.invoke(sqlBox.getBean(), new Object[] {});
				} catch (Exception e) {
					SQLBoxUtils.throwEX(e, "Dao save error, invoke method wrong.");
				}
				SQLHelper.e(value);
			}
		}
		sb.deleteCharAt(sb.length() - 1).append(") ");
		sb.append(SQLHelper.createValueString(howManyFields));
		this.execute(sb.toString());
	}

	// =============== CRUD methods end ===============

	// ================ Getters & Setters===============
	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public SQLBox getSqlBox() {
		return sqlBox;
	}

	public Dao setSqlBox(SQLBox sqlBox) {
		this.sqlBox = sqlBox;
		return this;
	}

}
