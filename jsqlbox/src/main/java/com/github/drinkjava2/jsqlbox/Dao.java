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
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Dao {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(Dao.class);
	private SqlBox sqlBox;
	private JdbcTemplate jdbc; // Spring JDBCTemplate
	// In future version may delete JDBCTemplate and only use pure JDBC

	private Object bean; // Entity Bean Instance

	public Dao(SqlBoxContext ctx) {
		if (ctx == null)
			SqlBoxException.throwEX(null, "Dao create error, SqlBoxContext  can not be null");
		else if (ctx.getDataSource() == null)
			SqlBoxException.throwEX(null, "Dao create error,  dataSource can not be null");
		else
			this.jdbc = new JdbcTemplate(ctx.getDataSource());
		SqlBox sb = new SqlBox(ctx);
		this.sqlBox = sb;
	}

	public Dao(SqlBox sqlBox) {
		if (sqlBox == null)
			SqlBoxException.throwEX(null, "Dao create error, sqlBox can not be null");
		else if (sqlBox.getContext() == null)
			SqlBoxException.throwEX(null, "Dao create error, sqlBoxContext can not be null");
		else if (sqlBox.getContext().getDataSource() == null)
			SqlBoxException.throwEX(null, "Dao create error, dataSource can not be null");
		else
			this.jdbc = new JdbcTemplate(sqlBox.getContext().getDataSource());
		this.sqlBox = sqlBox;
	}

	/**
	 * Get default Dao
	 */
	public static Dao defaultDao(Object bean) {
		SqlBoxContext ctx = SqlBoxContext.getDefaultSqlBoxContext();
		SqlBox box = ctx.findAndBuildSqlBox(bean.getClass());
		box.initialize();
		box.beanInitialize(bean);
		Dao d = new Dao(box);
		d.setBean(bean);
		return d;
	}

	/**
	 * Get default Dao
	 */
	public static Dao dao() {
		SqlBoxContext ctx = SqlBoxContext.getDefaultSqlBoxContext();
		SqlBox box = new SqlBox(ctx);
		return new Dao(box);
	}

	// ========JdbcTemplate wrap methods begin============
	// Only wrap some common used JdbcTemplate methods
	public Integer queryForInteger(String... sql) {
		return this.queryForObject(Integer.class, sql);
	}

	/**
	 * Return String type query result, sql be translated to prepared statement
	 */
	public String queryForString(String... sql) {
		return this.queryForObject(String.class, sql);
	}

	/**
	 * Return Object type query result, sql be translated to prepared statement
	 */
	public <T> T queryForObject(Class<?> clazz, String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			return (T) getJdbc().queryForObject(sp.getSql(), sp.getParameters(), clazz);
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Cache SQL in memory for executeCachedSQLs call, sql be translated to prepared statement
	 * 
	 * @param sql
	 */
	public void cacheSQL(String... sql) {
		SqlHelper.cacheSQL(sql);
	}

	// ========JdbcTemplate wrap methods End============

	/**
	 * Execute a sql and return how many record be affected, sql be translated to prepared statement
	 * 
	 */
	public Integer execute(String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			return jdbc.update(sp.getSql(), (Object[]) sp.getParameters());
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Transfer cached SQLs to Prepared Statement and batch execute these SQLs
	 */
	public void executeCachedSQLs() {
		try {
			List<List<SqlAndParameters>> subSPlist = SqlHelper.getSQLandParameterSubList();
			logCachedSQL(subSPlist);
			for (final List<SqlAndParameters> splist : subSPlist) {
				jdbc.batchUpdate(SqlHelper.getSqlForBatch().get(), new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						SqlAndParameters sp = splist.get(i);
						int index = 1;
						for (Object parameter : sp.getParameters()) {
							ps.setObject(index++, parameter);
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

	// ========JdbcTemplate wrap methods End============

	// ========Dao query/crud methods begin=======
	/**
	 * Query and return entity list by sql
	 */
	public List queryEntity(String... sql) {
		return this.queryEntity(this.getSqlBox(), sql);
	}

	/**
	 * Query and return entity list by sql
	 */
	public <T> List<T> queryEntity(Class<?> beanOrSqlBoxClass, String... sql) {
		SqlBox box = this.getSqlBox().getContext().findAndBuildSqlBox(beanOrSqlBoxClass);
		return this.queryEntity(box, sql);
	}

	/**
	 * Query and return entity list by SqlBox and sql
	 */
	private List queryEntity(SqlBox sqlBox, String... sql) {
		if (sqlBox == null)
			throw new SqlBoxException("Dao queryEntity error: sqlBox is null");
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			return getJdbc().query(sp.getSql(), sqlBox.getRowMapper(), sp.getParameters());
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Print SQL and parameters to console, usually used for debug <br/>
	 * Use context.setShowSql to control, Default showSql is "false"
	 */
	private void logSql(SqlAndParameters sp) {
		// check if allowed print SQL
		if (!this.getSqlBox().getContext().isShowSql())
			return;
		StringBuilder sb = new StringBuilder(sp.getSql());
		Object[] args = sp.getParameters();
		if (args.length > 0) {
			sb.append("\r\nParameters: ");
			for (int i = 0; i < args.length; i++) {
				sb.append("" + args[i]);
				if (i != args.length - 1)
					sb.append(",");
				else
					sb.append("\r\n");
			}
		}
		log.info(sb.toString());
	}

	/**
	 * Print Cached SQL and parameters, usually used for debug <br/>
	 * Use context.setShowSql to control, Default showSql is "false"
	 */
	private void logCachedSQL(List<List<SqlAndParameters>> subSPlist) {
		if (this.getSqlBox().getContext().isShowSql()) {
			if (subSPlist != null) {
				List<SqlAndParameters> l = subSPlist.get(0);
				if (l != null) {
					SqlAndParameters sp = l.get(0);
					log.info("First Cached SQL:");
					logSql(sp);
				}
			}
			if (subSPlist != null) {
				List<SqlAndParameters> l = subSPlist.get(subSPlist.size() - 1);
				if (l != null) {
					SqlAndParameters sp = l.get(l.size() - 1);
					log.info("Last Cached SQL:");
					logSql(sp);
				}
			}
		}
	}

	/**
	 * Insert or Update a Bean to Database
	 */
	public Integer save() {
		if (bean == null)
			SqlBoxException.throwEX(null, "Dao doSave error, bean is null");
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		int count = 0;
		sb.append("insert into ").append(sqlBox.getRealTable()).append(" ( ");
		for (Column col : sqlBox.getRealColumns().values()) {
			if (!col.isPrimeKey() && !SqlBoxUtils.isEmptyStr(col.getColumnName())) {
				String methodName = col.getReadMethodName();
				Object value = null;
				Method m;
				try {
					m = bean.getClass().getDeclaredMethod(methodName, new Class[] {});// NOSONAR
					value = m.invoke(this.bean, new Object[] {});
				} catch (Exception e1) {
					SqlBoxException.throwEX(e1,
							"Dao doSave error, method " + methodName + " not found in class " + bean);
				}
				if (value != null) {
					sb.append(col.getColumnName()).append(",");
					if (Boolean.class.isInstance(value)) {// NOSONAR
						if (((Boolean) value).equals(true))
							value = 1;
						else
							value = 0;
					}
					parameters.add(value);
					count++;
				}
			}
		}
		sb.deleteCharAt(sb.length() - 1).append(") ");
		sb.append(SqlHelper.createValueString(count));
		if (this.getSqlBox().getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));
		return jdbc.update(sb.toString(), parameters.toArray(new Object[parameters.size()]));

	}

	// ========Dao query/crud methods end=======

	// =============identical methods copied from SqlBox==========
	public String getRealColumnName(String fieldID) {
		return this.getSqlBox().getRealColumn(fieldID).getColumnName();
	}

	public String getRealTable() {
		return this.getSqlBox().getRealTable();
	}

	public void setRealTable(String realTable) {
		this.getSqlBox().setRealTable(realTable);
	}

	public void setRealColumnName(String fieldID, String realColumnName) {
		this.getSqlBox().getRealColumn(fieldID).setColumnName(realColumnName);
	}

	public <T> T createBean() {
		return this.getSqlBox().createBean();
	}

	// =============Misc methods end==========

	// ================ Getters & Setters===============
	/**
	 * Return Bean instance which related to this dao
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * Set a Bean instance related to this dao
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}

	/**
	 * Return a JdbcTemplate instance<br/>
	 * It's not recommended to use JdbcTemplate directly unless very necessary, JdbcTemplate may be deprecated or
	 * replaced by pure JDBC in future version
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
