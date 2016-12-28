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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
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

import com.github.drinkjava2.jsqlbox.id.AssignedGenerator;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;
import com.github.drinkjava2.springsrc.ReflectionUtils;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 */

@SuppressWarnings({ "unchecked" })
public class Box {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(Box.class);

	// The entity bean class
	private Class<?> entityClass;

	// Configuration Columns, set it before entity bean be created
	private Map<String, Column> configColumns = new HashMap<>();

	// Configuration Table Name, set it before entity bean be created
	private String configTable;

	private SqlBoxContext context;

	private static ThreadLocal<String> fieldIDCache = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";
		}
	};

	private Object entityBean; // Entity Bean Instance

	public Box() {
		// Default Constructor
	}

	public Box(SqlBoxContext ctx) {
		if (ctx == null)
			throwEX("Box create error, SqlBoxContext  can not be null");
		else if (ctx.getDataSource() == null)
			throwEX("Box create error,  dataSource can not be null");
		this.context = ctx;
	}

	/**
	 * Get fieldIDCache
	 */
	public static ThreadLocal<String> getFieldIDCache() {
		return fieldIDCache;
	}

	/**
	 * Get default Box
	 */
	public static Box getBox(Object bean, Box box) {
		if (box != null)
			return box;
		Box d = SqlBoxContext.defaultSqlBoxContext().findAndBuildSqlBox(bean.getClass());
		d.beanInitialize(bean);
		d.setEntityBean(bean);
		try {
			Method m = ReflectionUtils.findMethod(bean.getClass(), "putBox", new Class[] { Box.class });
			m.invoke(bean, new Object[] { d });
		} catch (Exception e) {
			throwEX(e, "Box getBox error for bean \"" + bean + "\", no putBox method found");
		}
		return d;
	}

	/**
	 * Get default Box
	 */
	public static Box box() {
		return new Box(SqlBoxContext.defaultSqlBoxContext());
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

	// ========Box query/crud methods begin=======

	/**
	 * Print SQL and parameters to console, usually used for debug <br/>
	 * Use context.setShowSql to control, Default showSql is "false"
	 */
	private void logSql(SqlAndParameters sp) {
		// check if allowed print SQL
		if (!this.getContext().isShowSql())
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
		if (this.getContext().isShowSql()) {
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
		String sql = "SELECT MAX(" + col.getColumnName() + ") from " + getRealTable();
		return this.getJdbc().queryForObject(sql, col.getPropertyType());
	}

	/**
	 * Insert a entity bean to Database
	 */
	public void insert() {// NOSONAR
		if (entityBean == null)
			throwEX("Box doSave error, bean is null");

		// generatedValues to record all generated values like UUID, sequence
		Map<Column, Object> idGeneratorCache = new HashMap<>();
		DatabaseType dbType = this.getDatabaseType();

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		int count = 0;
		sb.append("insert into ").append(getRealTable()).append(" ( ");

		Map<String, Column> realColumns = buildRealColumns();
		for (Column col : realColumns.values()) {
			IdGenerator idGen = col.getIdGenerator();
			if (idGen != null && !(idGen instanceof AssignedGenerator)) {
				Object idValue = idGen.getNextID(this.getContext());
				if (idGen instanceof IdentityGenerator) {
					if (dbType == ORACLE)// NOSONAR
						throwEX("Box insert error, IdentityGenerator type should not set to ORACLE");
				} else if (idGen instanceof AutoGenerator) {

					if (dbType == MYSQL || dbType == MS_SQLSERVER) {// NOSONAR
						if (!col.getAutoIncreament())
							throwEX("Box insert error, AutoGenerator type should set on indentity type field");
					} else {// ORACLE

					}
				} else {// for other IDgenerator like sequence, table, UUID...
					assureNotNull(idValue, "Box insert error, ID can not be null, column=" + col.getColumnName());
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
		if (this.getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		// here you go
		int result = getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Box insert error, no record be inserted, sql=" + sb.toString());

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
			throwEX("Box update error, bean is null");

		Map<String, Column> realColumns = buildRealColumns();

		List<Column> idColumns = extractIdColumnsWithValue(realColumns);

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		sb.append("update ").append(getRealTable()).append(" set ");

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

		if (this.getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		// here you go
		int result = getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Box insert error, no record be updated, sql=" + sb.toString());
	}

	/**
	 * Load a entity from Database by its ID
	 */
	protected <T> T load(Object entityID) {// NOSONAR
		Map<String, Column> realColumns = buildRealColumns();
		Map<String, Object> idvalues = SqlBoxUtils.extractEntityIDValues(entityID, realColumns);

		// start to spell sql
		StringBuilder sb = new StringBuilder("select ");
		List<Object> parameters = new ArrayList<>();

		// set values
		for (Column col : realColumns.values()) {
			sb.append(col.getColumnName()).append(", ");
		}

		sb.setLength(sb.length() - 2);// delete the last ","
		sb.append(" from ").append(this.getRealTable()).append(" where ");

		for (Entry<String, Object> entry : idvalues.entrySet()) {
			sb.append(entry.getKey()).append("=").append("? and ");
			parameters.add(entry.getValue());
		}
		sb.setLength(sb.length() - 5);// delete the last " and "

		if (this.getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		List<Map<String, Object>> rows = this.getJdbc().queryForList(sb.toString(),
				parameters.toArray(new Object[parameters.size()]));
		if (rows == null || rows.isEmpty())
			throwEX("Box load error, no record found for entityID:" + entityID);
		else if (rows.size() != 1)
			throwEX("Box load error, multiple record found for entityID:" + entityID);
		else
			writeValuesToEntity(realColumns, rows.get(0));
		return (T) this.getEntityBean();
	}

	/**
	 * @param entityID
	 * @return
	 */
	public void delete() {
		Map<String, Column> realColumns = buildRealColumns();
		List<Column> entityID = extractIdColumnsWithValue(realColumns);
		Map<String, Object> idvalues = SqlBoxUtils.extractEntityIDValues(entityID, realColumns);

		// start to spell sql
		StringBuilder sb = new StringBuilder("delete ");
		List<Object> parameters = new ArrayList<>();
		sb.append(" from ").append(this.getRealTable()).append(" where ");

		for (Entry<String, Object> entry : idvalues.entrySet()) {
			sb.append(entry.getKey()).append("=").append("? and ");
			parameters.add(entry.getValue());
		}
		sb.setLength(sb.length() - 5);// delete the last " and "

		if (this.getContext().isShowSql())
			logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));
		int result = this.getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Box delete error, no record delete for entityID:" + entityID);
		deleteEntityID(realColumns, idvalues);
	}

	/**
	 * Query for a DB type List
	 */
	public <T> List<T> queryForList(Class<?> dbClass, String... sql) {
		List<T> result = new ArrayList<>();
		try {
			SqlAndParameters sp = SqlHelper.splitSQLandParameters(sql);
			logSql(sp);
			List<Map<String, Object>> lst;
			if (sp.getParameters().length == 0)
				lst = getJdbc().queryForList(sp.getSql());
			else
				lst = getJdbc().queryForList(sp.getSql(), sp.getParameters());
			Field field = ReflectionUtils.findField(dbClass, "map");
			for (Map<String, Object> map : lst) {
				Object db = dbClass.newInstance();
				field.set(db, map);
				result.add((T) db);
			}
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext queryForList, sql=" + sql);
		} finally {
			SqlHelper.clearLastSQL();
		}
		return result;
	}

	private List<Column> extractIdColumnsWithValue(Map<String, Column> realColumns) {
		// cache id columns
		List<Column> idColumns = new ArrayList<>();
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (col.getEntityID()) {
				Object idValue = SqlBoxUtils.getFieldRealValue(this.entityBean, col);
				assureNotNull(idValue, "Box update error, ID can not be null, column=" + col.getColumnName());
				col.setPropertyValue(idValue);
				idColumns.add(col);
			}
		}
		if (idColumns.isEmpty())
			throwEX("Box update error, no entityID set for class " + this.getEntityClass());
		return idColumns;
	}

	public List<Column> getEntityID() {
		Map<String, Column> realColumns = buildRealColumns();
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
			Method m = ReflectionUtils.findMethod(this.entityBean.getClass(), col.getWriteMethodName(),
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
			throwEX(e, "Box setFieldRealValue error, method " + col.getWriteMethodName() + " invoke error in class "
					+ entityBean);
		}
	}

	/**
	 * get field's database real column name
	 */
	public String getColumnName() {
		String method1 = Thread.currentThread().getStackTrace()[1].getMethodName();
		String fieldID = "getColumnName".equals(method1) ? Thread.currentThread().getStackTrace()[2].getMethodName()
				: method1;
		getFieldIDCache().set(fieldID);
		return this.getRealColumnName(null, fieldID);
	}

	/**
	 * get field's fieldID
	 */
	public String fieldID(String realColumnName) {
		String fieldID = getFieldIDCache().get();
		if (SqlBoxUtils.isEmptyStr(fieldID) || SqlBoxUtils.isEmptyStr(realColumnName)
				|| !realColumnName.equals(this.getRealColumnName(null, fieldID)))
			throwEX("Box getFieldID error, can only be called with getFieldID(xx.xxFieldID()) format");
		return fieldID;
	}

	// ========Box query/crud methods end=======

	// =============identical methods copied from SqlBox or SqlBoxContext==========
	public void refreshMetaData() {
		this.getContext().refreshMetaData();
	}

	// =============Misc methods end==========

	// ================ Getters & Setters===============
	/**
	 * Return Bean instance which related to this Box
	 */
	public Object getEntityBean() {
		return entityBean;
	}

	/**
	 * Set a Bean instance related to this Box
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
		return this.getContext().getJdbc();
	}

	public DatabaseType getDatabaseType() {
		return getContext().getDatabaseType();
	}

	// ==============================================================================================
	// ==============================================================================================
	// ==============================================================================================
	// ==============================================================================================
	// ==============================================================================================

	/**
	 * Create entity instance
	 */
	public <T> T createEntity() {
		Object bean = null;
		try {
			bean = this.getEntityClass().newInstance();
			this.beanInitialize(bean);
			setEntityBean(bean);
			Method m = SqlBoxUtils.getDeclaredMethodQuickly(this.getEntityClass(), "putBox", Box.class);
			if (m != null)
				m.invoke(bean, new Object[] { this });// 5ms
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBoxContext create error");
		}
		return (T) bean;
	}

	/**
	 * @param instance
	 */
	public void beanInitialize(Object bean) {
		try {
			Method m = SqlBoxUtils.getDeclaredMethodQuickly(this.getEntityClass(), "initialize", null);
			if (m != null)
				m.invoke(bean, new Object[] {});
		} catch (Exception e) {
			SqlBoxException.eatException(e);
		}
	}

	/**
	 * Get real database table name
	 */
	public String getRealTable() {
		String realTable = configTable;
		if (SqlBoxUtils.isEmptyStr(realTable)) {
			realTable = this.getEntityClass().getSimpleName();
			int locate = realTable.indexOf('$');// for inner class, get the real class name
			if (locate > 0)
				realTable = realTable.substring(locate, realTable.length());
		}
		String resultTable = context.findRealTableName(realTable);
		if (SqlBoxUtils.isEmptyStr(resultTable))
			SqlBoxException
					.throwEX("SqlBox getRealTable error: " + this.getEntityClass() + ", table name:" + realTable);
		return resultTable;
	}

	/**
	 * Return a * for sql
	 */
	public String getStar() {
		return "*";// NOSONAR
	}

	/**
	 * In entity class, a legal fieldID like userName must have a same name no parameter method like userName()
	 */
	private boolean isLegalFieldID(String fieldID) {
		if ("class".equals(fieldID))
			return false;
		if (SqlBoxUtils.isEmptyStr(fieldID))
			return false;
		if (SqlBoxUtils.isCapitalizedString(fieldID))
			return false;
		/**
		 * try { Method method = ReflectionUtils.getDeclaredMethod(entityClass, fieldID, new Class[] {}); if (method ==
		 * null) return false; } catch (Exception e) { return false; }
		 */
		return true;
	}

	/**
	 * Return real Columns match to table meta data
	 */
	public Map<String, Column> buildRealColumns() {
		if (this.entityClass == null)
			SqlBoxException.throwEX("SqlBox getRealColumns error, beanClass can not be null");
		String realTableName = this.getRealTable();
		TinyDbMetaData meta = this.getContext().getMetaData();
		Map<String, Column> oneTable = meta.getOneTable(realTableName.toLowerCase());
		Map<String, Column> realColumns = new HashMap<>();
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(this.getEntityClass());
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SqlBox buildDefaultConfig error");
		}

		for (PropertyDescriptor pd : pds) {
			String fieldID = pd.getName();
			if (isLegalFieldID(fieldID)) {
				Column realCol = new Column();
				realCol.setFieldID(fieldID);
				realCol.setColumnName(this.getRealColumnName(realTableName, fieldID));// 3080ms cost for speed test
				realCol.setPropertyType(pd.getPropertyType());
				realCol.setReadMethodName(pd.getReadMethod().getName());
				realCol.setWriteMethodName(pd.getWriteMethod().getName());
				useConfigOverrideDefault(fieldID, realCol);
				realColumns.put(fieldID, realCol);
				Column metaDataCol = oneTable.get(fieldID.toLowerCase());
				if (metaDataCol != null)
					realCol.setAutoIncreament(metaDataCol.getAutoIncreament());
			}
		}
		findAndSetEntityID(this.getEntityClass(), realColumns);
		return realColumns;
	}

	/**
	 * Use config values to override default runtime values
	 */
	private void useConfigOverrideDefault(String fieldID, Column column) {
		Column configColumn = configColumns.get(fieldID);
		if (configColumn != null) {
			if (!SqlBoxUtils.isEmptyStr(configColumn.getColumnName()))
				column.setColumnName(configColumn.getColumnName());
			column.setEntityID(configColumn.getEntityID());
			column.setIdGenerator(configColumn.getIdGenerator());
			column.setEntityID(configColumn.getEntityID());
		}
	}

	/**
	 * Find and set Object IDs automatically, rule:<br/>
	 * 
	 * Find how many entityID <br/>
	 * Found lots? return <br/>
	 * only found 1? if no generator, set to auto type <br/>
	 * Not found? look for id field found? set as EntityID if no generator, set to auto type <br/>
	 * No found throw ex <br/>
	 */
	private void findAndSetEntityID(Class<?> entityClass, Map<String, Column> realColumns) {// NOSONAR
		Column idColumn = null;
		Column entityColumn = null;
		for (Entry<String, Column> cols : realColumns.entrySet()) {
			if (cols.getValue().getEntityID())
				if (entityColumn != null)
					return;
				else
					entityColumn = cols.getValue();
			if ("id".equals(cols.getValue().getFieldID()))
				idColumn = cols.getValue();
		}

		if (idColumn == null) {
			if (entityColumn == null)
				throwEX("SqlBox findAndSetEntityID error, no entityID set for entity class " + entityClass);
			else
				return;
		} else {
			if (entityColumn != null && !entityColumn.getFieldID().equals(idColumn.getFieldID()))
				return;
			else {
				idColumn.setEntityID(true);
				if (idColumn.getIdGenerator() == null)// entityColumn=null or entityColumn=idColumn
					idColumn.setIdGenerator(AutoGenerator.INSTANCE);
			}
		}
		return;
	}

	/**
	 * Get real column name by fieldID <br/>
	 * userName field will find userName or username or USERNAME or USER_NAME, but only allowed 1
	 */
	public String getRealColumnName(String realTableName, String fieldID) {// NOSONAR
		Column col = getOrBuildConfigColumn(fieldID);
		String columnName = col.getColumnName();
		if (columnName == null || columnName.length() == 0)
			columnName = fieldID;

		String realTable = realTableName;
		if (SqlBoxUtils.isEmptyStr(realTable))
			realTable = this.getRealTable();
		Map<String, Column> oneTableMap = context.getMetaData().getOneTable(realTable.toLowerCase());
		String realColumnNameignoreCase = null;
		Column realColumn = oneTableMap.get(columnName.toLowerCase());
		if (realColumn != null)
			realColumnNameignoreCase = realColumn.getColumnName();

		String realColumnNameUnderline = null;
		realColumn = oneTableMap.get(SqlBoxUtils.camelToLowerCaseUnderline(columnName));
		if (realColumn != null)
			realColumnNameUnderline = realColumn.getColumnName();

		if (realColumnNameignoreCase == null && realColumnNameUnderline == null)
			SqlBoxException.throwEX("SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" does not match any table column in table " + realTable);

		if (realColumnNameignoreCase != null && realColumnNameUnderline != null
				&& !realColumnNameignoreCase.equals(realColumnNameUnderline))
			SqlBoxException.throwEX("SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" found mutiple columns in table " + realTable);
		return realColumnNameignoreCase != null ? realColumnNameignoreCase : realColumnNameUnderline;
	}

	public Column getOrBuildConfigColumn(String fieldID) {
		Column col = this.getConfigColumns().get(fieldID);
		if (col == null) {
			col = new Column();
			this.getConfigColumns().put(fieldID, col);
		}
		return col;
	}

	// ========Config methods begin==============
	/**
	 * Config table name
	 */
	public void configTable(String table) {
		configTable = table;
	}

	/**
	 * Clean old entityID setting, set with given entityIDs
	 */
	public void configEntityIDs(String... entityIDs) {
		for (Entry<String, Column> entry : getConfigColumns().entrySet())
			entry.getValue().setEntityID(false);
		for (String fieldID : entityIDs)
			getOrBuildConfigColumn(fieldID).setEntityID(true);
	}

	/**
	 * Config column name, if has cached field, use it, otherwise use
	 */
	public void configColumnName(String fieldID, String columnName) {
		getOrBuildConfigColumn(fieldID).setColumnName(columnName);
	}

	/**
	 * Config column name
	 */
	public <T> void configIdGenerator(String fieldID, T idGenerator) {
		getOrBuildConfigColumn(fieldID).setIdGenerator((IdGenerator) idGenerator);
	}

	// ========Config methods end==============

	// ========getter & setters below==============
	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Map<String, Column> getConfigColumns() {
		return configColumns;
	}

	public void setConfigColumns(Map<String, Column> columns) {
		this.configColumns = columns;
	}

	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

}
