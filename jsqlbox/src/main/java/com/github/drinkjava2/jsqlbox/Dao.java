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
import static com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType.MS_SQLSERVER;
import static com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType.MYSQL;
import static com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType.ORACLE;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.id.AssignedGenerator;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;
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

@SuppressWarnings({ "unchecked" })
public class Dao {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(Dao.class);
	private SqlBox box;

	// In future version may delete JDBCTemplate and only use pure JDBC

	private Object entityBean; // Entity Bean Instance

	public Dao(SqlBoxContext ctx) {
		if (ctx == null)
			throwEX("Dao create error, SqlBoxContext  can not be null");
		else if (ctx.getDataSource() == null)
			throwEX("Dao create error,  dataSource can not be null");
		SqlBox sb = new SqlBox(ctx);
		this.box = sb;
	}

	public Dao(SqlBox sqlBox) {
		if (sqlBox == null)
			throwEX("Dao create error, sqlBox can not be null");
		else if (sqlBox.getContext() == null)
			throwEX("Dao create error, sqlBoxContext can not be null");
		else if (sqlBox.getContext().getDataSource() == null)
			throwEX("Dao create error, dataSource can not be null");
		this.box = sqlBox;
	}

	/**
	 * Get default Dao
	 */
	public static Dao getDao(Object bean, Dao dao) {
		if (dao != null)
			return dao;
		SqlBox box = SqlBoxContext.defaultSqlBoxContext.findAndBuildSqlBox(bean.getClass());
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
		SqlBox box = new SqlBox(SqlBoxContext.defaultSqlBoxContext);
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
			else {
				try {
					return (T) getJdbc().queryForObject(sp.getSql(), clazz);
				} catch (EmptyResultDataAccessException e) {
					SqlBoxException.eatException(e);
					return null;
				}
			}
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
		String sql = "SELECT MAX(" + col.getColumnName() + ") from " + box.getRealTable();
		return this.getJdbc().queryForObject(sql, col.getPropertyType());
	}

	/**
	 * Insert a entity bean to Database
	 */
	public void insert() {// NOSONAR
		if (entityBean == null)
			throwEX("Dao doSave error, bean is null");

		// generatedValues to record all generated values like UUID, sequence
		Map<Column, Object> idGeneratorCache = new HashMap<>();
		DatabaseType dbType = this.getDatabaseType();

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		int count = 0;
		sb.append("insert into ").append(box.getRealTable()).append(" ( ");

		Map<String, Column> realColumns = box.buildRealColumns();
		for (Column col : realColumns.values()) {
			IdGenerator idGen = col.getIdGenerator();
			if (idGen != null && !(idGen instanceof AssignedGenerator)) {
				Object idValue = idGen.getNextID(this.getBox().getContext());
				if (idGen instanceof IdentityGenerator) {
					if (dbType == ORACLE)// NOSONAR
						throwEX("Dao insert error, IdentityGenerator type should not set to ORACLE");
				} else if (idGen instanceof AutoGenerator) {

					if (dbType == MYSQL || dbType == MS_SQLSERVER) {// NOSONAR
						if (!col.getAutoIncreament())
							throwEX("Dao insert error, AutoGenerator type should set on indentity type field");
					} else {// ORACLE

					}
				} else {// for other IDgenerator like sequence, table, UUID...
					assureNotNull(idValue, "Dao insert error, ID can not be null, column=" + col.getColumnName());
					sb.append(col.getColumnName()).append(",");
					setFieldRealValue(col, idValue);
					parameters.add(idValue);
					count++;
				}
				idGeneratorCache.put(col, idValue);
			} else if (!SqlBoxUtils.isEmptyStr(col.getColumnName())) {// normal fields
				Object value = SqlBoxUtils.getFieldRealValue(this.entityBean, col);
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
		for (Entry<Column, Object> entry : idGeneratorCache.entrySet()) {
			Column col = entry.getKey();
			Object idValue = entry.getValue();
			// Identity need read max id from database
			if (col.getIdGenerator() instanceof IdentityGenerator || col.getIdGenerator() instanceof AutoGenerator)
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

		Map<String, Column> realColumns = box.buildRealColumns();

		List<Column> idColumns = extractIdColumnsWithValue(realColumns);

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		sb.append("update ").append(box.getRealTable()).append(" set ");

		// set values
		for (Column col : realColumns.values()) {
			if (!col.getEntityID() && col.getIdGenerator() == null) {
				Object value = SqlBoxUtils.getFieldRealValue(this.entityBean, col);
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

		sb.append("  where ");
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
	 * Load a entity from Database by its ID
	 */
	protected <T> T load(Object entityID) {// NOSONAR
		Map<String, Column> realColumns = box.buildRealColumns();
		Map<String, Object> idvalues = SqlBoxUtils.extractEntityIDValues(entityID, realColumns);

		// start to spell sql
		StringBuilder sb = new StringBuilder("select ");
		List<Object> parameters = new ArrayList<>();

		// set values
		for (Column col : realColumns.values()) {
			sb.append(col.getColumnName()).append(", ");
		}

		sb.setLength(sb.length() - 2);// delete the last ","
		sb.append(" from ").append(this.box.getRealTable()).append(" where ");

		for (Entry<String, Object> entry : idvalues.entrySet()) {
			sb.append(entry.getKey()).append("=").append("? and ");
			parameters.add(entry.getValue());
		}
		sb.setLength(sb.length() - 5);// delete the last " and "

		if (this.getBox().getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		List<Map<String, Object>> rows = this.getJdbc().queryForList(sb.toString(),
				parameters.toArray(new Object[parameters.size()]));
		if (rows == null || rows.isEmpty())
			throwEX("Dao load error, no record found for entityID:" + entityID);
		else if (rows.size() != 1)
			throwEX("Dao load error, multiple record found for entityID:" + entityID);
		else
			writeValuesToEntity(realColumns, rows.get(0));
		return (T) this.getEntityBean();
	}

	/**
	 * @param entityID
	 * @return
	 */
	public void delete() {
		Map<String, Column> realColumns = box.buildRealColumns();
		List<Column> entityID = extractIdColumnsWithValue(realColumns);
		Map<String, Object> idvalues = SqlBoxUtils.extractEntityIDValues(entityID, realColumns);

		// start to spell sql
		StringBuilder sb = new StringBuilder("delete ");
		List<Object> parameters = new ArrayList<>();
		sb.append(" from ").append(this.box.getRealTable()).append(" where ");

		for (Entry<String, Object> entry : idvalues.entrySet()) {
			sb.append(entry.getKey()).append("=").append("? and ");
			parameters.add(entry.getValue());
		}
		sb.setLength(sb.length() - 5);// delete the last " and "

		if (this.getBox().getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));
		int result = this.getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Dao delete error, no record delete for entityID:" + entityID);
		deleteEntityID(realColumns, idvalues);
	}

	private List<Column> extractIdColumnsWithValue(Map<String, Column> realColumns) {
		// cache id columns
		List<Column> idColumns = new ArrayList<>();
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (col.getEntityID()) {
				Object idValue = SqlBoxUtils.getFieldRealValue(this.entityBean, col);
				assureNotNull(idValue, "Dao update error, ID can not be null, column=" + col.getColumnName());
				col.setPropertyValue(idValue);
				idColumns.add(col);
			}
		}
		if (idColumns.isEmpty())
			throwEX("Dao update error, no entityID set for class " + this.box.getEntityClass());
		return idColumns;
	}

	public List<Column> getEntityID() {
		Map<String, Column> realColumns = box.buildRealColumns();
		return extractIdColumnsWithValue(realColumns);
	}

	/**
	 * Write one row value to entity
	 */
	private void writeValuesToEntity(Map<String, Column> realColumns, Map<String, Object> row) {
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (row.containsKey(col.getFieldID()))
				setFieldRealValue(col, row.get(col.getFieldID()));
		}
	}

	/**
	 * Delete entityID from entity
	 */
	private void deleteEntityID(Map<String, Column> realColumns, Map<String, Object> idvalues) {
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (idvalues.containsKey(col.getFieldID()))
				setFieldRealValue(col, null);
		}
	}

	/**
	 * Set Field value by it's column defination
	 */
	private void setFieldRealValue(Column col, Object value) {
		try {
			Method m = ReflectionUtils.getDeclaredMethod(this.entityBean, col.getWriteMethodName(),
					new Class[] { col.getPropertyType() });

			if (value != null && value instanceof BigDecimal && col.getPropertyType().isAssignableFrom(Integer.class)) {
				Integer valueForInvoke = Integer.parseInt("" + value);
				m.invoke(this.entityBean, new Object[] { valueForInvoke });
			} else if (value != null && col.getPropertyType().isAssignableFrom(Boolean.class)) {
				Boolean valueForInvoke = true;
				if ("0".equals(value) || "".equals(value))
					valueForInvoke = false;
				m.invoke(this.entityBean, new Object[] { valueForInvoke });
			} else
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
		return box;
	}

	public SqlBoxContext getContext() {
		return box.getContext();
	}

	public DatabaseType getDatabaseType() {
		return box.getContext().getDatabaseType();
	}

	public Dao setSqlBox(SqlBox sqlBox) {
		this.box = sqlBox;
		return this;
	}

}
