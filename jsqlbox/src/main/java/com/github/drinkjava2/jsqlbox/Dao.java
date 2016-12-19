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

import static com.github.drinkjava2.jsqlbox.SqlBoxException.assureNotNull;
import static com.github.drinkjava2.jsqlbox.SqlBoxException.throwEX;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

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

	// In future version may delete JDBCTemplate and only use pure JDBC

	private Object entityBean; // Entity Bean Instance

	public Dao(SqlBoxContext ctx) {
		if (ctx == null)
			throwEX("Dao create error, SqlBoxContext  can not be null");
		else if (ctx.getDataSource() == null)
			throwEX("Dao create error,  dataSource can not be null");
		SqlBox sb = new SqlBox(ctx);
		this.sqlBox = sb;
	}

	public Dao(SqlBox sqlBox) {
		if (sqlBox == null)
			throwEX("Dao create error, sqlBox can not be null");
		else if (sqlBox.getContext() == null)
			throwEX("Dao create error, sqlBoxContext can not be null");
		else if (sqlBox.getContext().getDataSource() == null)
			throwEX("Dao create error, dataSource can not be null");
		this.sqlBox = sqlBox;
	}

	/**
	 * Get default Dao
	 */
	public static Dao getDao(Object bean, Dao dao) {
		if (dao != null)
			return dao;
		SqlBoxContext ctx = SqlBoxContext.getDefaultSqlBoxContext();
		SqlBox box = ctx.findAndBuildSqlBox(bean.getClass());
		box.beanInitialize(bean);
		Dao d = new Dao(box);
		d.setEntityBean(bean);
		try {
			Method m = ReflectionUtils.getDeclaredMethod(bean, "putDao", new Class[] { Dao.class });
			m.invoke(bean, new Object[] { d });
		} catch (Exception e) {
			throwEX(e, "Dao getDao error for bean \"" + bean + "\", no putDao method found");
		}
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
			if (sp.getParameters().length != 0)
				return (T) getJdbc().queryForObject(sp.getSql(), sp.getParameters(), clazz);
			else
				return (T) getJdbc().queryForObject(sp.getSql(), clazz);
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
	 * Execute sql and return how many record be affected, sql be translated to prepared statement<br/>
	 * Return -1 if no parameters sql executed<br/>
	 * 
	 */
	public Integer execute(String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			if (sp.getParameters().length != 0)
				return getJdbc().update(sp.getSql(), (Object[]) sp.getParameters());
			else {
				getJdbc().execute(sp.getSql());
				return -1;
			}
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Execute sql and return how many record be affected, sql be translated to prepared statement<br/>
	 * Return -1 if no parameters sql executed<br/>
	 * 
	 */
	public Integer executeInsert(String... sql) {
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			if (sp.getParameters().length != 0)
				return getJdbc().update(sp.getSql(), (Object[]) sp.getParameters());
			else {
				getJdbc().execute(sp.getSql());
				return -1;
			}
		} finally {
			SqlHelper.clearLastSQL();
		}
	}

	/**
	 * Execute sql without exception threw, return -1 if no parameters sql executed, return -2 if exception found
	 */
	public Integer executeQuiet(String... sql) {
		try {
			return execute(sql);
		} catch (Exception e) {
			SqlBoxException.eatException(e);
			return -2;
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
				getJdbc().batchUpdate(SqlHelper.getSqlForBatch().get(), new BatchPreparedStatementSetter() {
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
		return this.queryEntity(this.getBox(), sql);
	}

	/**
	 * Query and return entity list by sql
	 */
	public <T> List<T> queryEntity(Class<?> beanOrSqlBoxClass, String... sql) {
		SqlBox box = this.getBox().getContext().findAndBuildSqlBox(beanOrSqlBoxClass);
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
		if (!this.getBox().getContext().isShowSql())
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
		if (this.getBox().getContext().isShowSql()) {
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
	 * Get last auto increase id, supported by MySQL, SQL Server, DB2, Derby, Sybase, PostgreSQL
	 */
	private Object getLastAutoIncreaseIdentity(Column col) {
		String sql = "SELECT MAX(" + col.getColumnName() + ") from " + sqlBox.getRealTable();
		return this.getJdbc().queryForObject(sql, col.getPropertyType());
	}

	/**
	 * Insert a Bean to Database
	 */
	public void insert() {// NOSONAR
		if (entityBean == null)
			throwEX("Dao doSave error, bean is null");
		// generatedValues to record all generated values like UUID, sequence
		Map<Column, Object> generatedValues = new HashMap<>();

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		int count = 0;
		sb.append("insert into ").append(sqlBox.getRealTable()).append(" ( ");
		for (Column col : sqlBox.buildRealColumns().values()) {
			IdGenerator idGen = col.getIdGenerator();
			if (idGen != null) {
				Object idValue = idGen.getNextID(this.getBox().getContext());
				assureNotNull(idValue, "Dao insert error, ID can not be null, column=" + col.getColumnName());
				// if is an Identity type, no need insert this field in sql
				if (!IdentityGenerator.IDENTITY_TYPE.equals(idValue)) {
					sb.append(col.getColumnName()).append(",");
					setFieldRealValue(col, idValue);
					parameters.add(idValue);
					count++;
				}
				generatedValues.put(col, idValue);
			} else if (!col.isObjectID() && !SqlBoxUtils.isEmptyStr(col.getColumnName())) {// normal fields
				Object value = getFieldRealValue(col);
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
		// delete the last ","
		sb.deleteCharAt(sb.length() - 1).append(") ");
		sb.append(SqlHelper.createValueString(count));
		if (this.getBox().getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		// here you go
		int result = getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Dao insert error, no record be inserted, sql=" + sb.toString());

		// if success, set id values to bean
		for (Entry<Column, Object> entry : generatedValues.entrySet()) {
			Column col = entry.getKey();
			Object idValue = entry.getValue();
			// if is Identity type, need read auto generated max id from database
			if (IdentityGenerator.IDENTITY_TYPE.equals(idValue))
				idValue = getLastAutoIncreaseIdentity(col);
			setFieldRealValue(col, idValue);
		}
	}

	/**
	 * Update a Bean in Database
	 */
	public void update() {// NOSONAR
		if (entityBean == null)
			throwEX("Dao update error, bean is null");

		List<Column> idColumns = new ArrayList<>();
		// cache id columns
		Map<String, Column> realColumns = sqlBox.buildRealColumns();

		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (col.isObjectID()) {
				Object idValue = getFieldRealValue(col);
				assureNotNull(idValue, "Dao update error, ID can not be null, column=" + col.getColumnName());
				col.setPropertyValue(idValue);
				idColumns.add(col);
			}
		}
		if (idColumns.isEmpty())
			throwEX("Dao update error, no prime key set for class " + this.sqlBox.getEntityClass());

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		sb.append("update ").append(sqlBox.getRealTable()).append(" set ");

		// set values
		for (Column col : realColumns.values()) {
			if (col.getIdGenerator() == null) {
				Object value = getFieldRealValue(col);
				sb.append(col.getColumnName()).append("=?, ");
				if (Boolean.class.isInstance(value)) {// NOSONAR
					if (((Boolean) value).equals(true))// NOSONAR
						value = 1;
					else
						value = 0;
				}
				parameters.add(value);
			}
		}

		// delete the last ","
		sb.setLength(sb.length() - 2);
		sb.append(" where ");
		for (Column col : idColumns) {
			sb.append(col.getColumnName()).append("=").append(col.getPropertyValue()).append(" and ");
		}
		sb.setLength(sb.length() - 4);

		if (this.getBox().getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		// here you go
		int result = getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Dao insert error, no record be updated, sql=" + sb.toString());
	}
	
	/**
	 * @param objectID
	 * @return
	 */
	public void delete( ) {
		 
	}

	

	/**
	 * Load a entity from Database by its ID
	 */
	public <T> T load(Object objectID) {
		return null;
	}

	/**
	 * Get Field value by it's column definition
	 */
	private Object getFieldRealValue(Column col) {
		try {
			Method m = ReflectionUtils.getDeclaredMethod(this.entityBean, col.getReadMethodName(), new Class[] {});
			return m.invoke(this.entityBean, new Object[] {});
		} catch (Exception e) {
			return throwEX(e, "Dao getFieldRealValue error, method " + col.getReadMethodName()
					+ " invoke error in class " + entityBean);
		}
	}

	/**
	 * Set Field value by it's column defination
	 */
	private void setFieldRealValue(Column col, Object value) {
		try {
			Method m = ReflectionUtils.getDeclaredMethod(this.entityBean, col.getWriteMethodName(),
					new Class[] { col.getPropertyType() });
			m.invoke(this.entityBean, new Object[] { value });
		} catch (Exception e) {
			throwEX(e, "Dao setFieldRealValue error, method " + col.getWriteMethodName() + " invoke error in class "
					+ entityBean);
		}
	}

	/**
	 * get field's database real column name
	 */
	public String getColumnName() {
		String method1 = Thread.currentThread().getStackTrace()[1].getMethodName();
		String realMethodName = "getColumnName".equals(method1)
				? Thread.currentThread().getStackTrace()[2].getMethodName() : method1;
		return this.getBox().getRealColumnName(realMethodName);
	}
	// ========Dao query/crud methods end=======

	// =============identical methods copied from SqlBox or SqlBoxContext==========
	public void refreshMetaData() {
		this.getContext().refreshMetaData();
	}

	// =============Misc methods end==========

	/**
	 * Load a entity from Database by its ID, use default global context
	 */
	public static <T> T load(Class entityOrBoxClass, Object objectID) {
		return SqlBoxContext.getDefaultSqlBoxContext().load(entityOrBoxClass, objectID);
	}

	// ================ Getters & Setters===============
	/**
	 * Return Bean instance which related to this dao
	 */
	public Object getEntityBean() {
		return entityBean;
	}

	/**
	 * Set a Bean instance related to this dao
	 */
	public void setEntityBean(Object bean) {
		this.entityBean = bean;
	}

	/**
	 * Return a JdbcTemplate instance<br/>
	 * It's not recommended to use JdbcTemplate directly unless very necessary, JdbcTemplate may be deprecated or
	 * replaced by pure JDBC in future version
	 * 
	 * @return JdbcTemplate
	 */
	public JdbcTemplate getJdbc() {
		return this.getBox().getContext().getJdbc();
	}

	public SqlBox getBox() {
		return sqlBox;
	}

	public SqlBoxContext getContext() {
		return sqlBox.getContext();
	}

	public DatabaseType getDatabaseType() {
		return sqlBox.getContext().getDatabaseType();
	}

	public Dao setSqlBox(SqlBox sqlBox) {
		this.sqlBox = sqlBox;
		return this;
	}

}
