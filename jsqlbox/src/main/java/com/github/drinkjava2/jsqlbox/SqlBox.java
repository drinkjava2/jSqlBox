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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ReflectionUtils;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.id.AssignedGenerator;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;
import com.github.drinkjava2.jsqlbox.id.IdentityGenerator;

/**
 * jSQLBox is a macro scale persistence tool for Java 7 and above.
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 */

@SuppressWarnings({ "unchecked" })
public class SqlBox {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(SqlBox.class);
	// The entity bean class
	private Class<?> entityClass;

	// Entity Bean Instance
	private Object entityBean;

	// Configure Columns
	private Map<String, Column> configColumns = new HashMap<>();

	// Configure Table Name
	private String configTable;

	private List<Mapping> configMappings;

	// Configure Table Alias Name
	private String alias;

	private SqlBoxContext context;

	/**
	 * Store parent entity corresponded fieldID <br/>
	 * like: map1(customer1, set("id","customerName"))
	 */
	private Map<Entity, Set<String>> partents;

	/**
	 * Point to entityCache, which is built by queryForEntityList or
	 * queryForEntityMaps methods or transfer methods<br/>
	 * like: map1(Customer.class, Map("id1", customer1))
	 */
	private Map<Class<?>, Map<Object, Entity>> entityCache;

	/**
	 * store the SqlAndParameters cache, which is used by queryForEntityList or
	 * queryForEntityMaps or transfer methods
	 */
	private SqlAndParameters spCache;

	/**
	 * Store fieldID for SQL Helper or other place use
	 */
	private static ThreadLocal<String> fieldIDCache = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";
		}
	};

	public SqlBox() {
		// Default Constructor
	}

	public SqlBox(SqlBoxContext ctx) {
		if (ctx == null)
			throwEX("Box create error, SqlBoxContext  can not be null");
		else if (ctx.getDataSource() == null)
			throwEX("Box create error,  dataSource can not be null");
		this.context = ctx;
	}

	/**
	 * Get fieldIDCache from ThreadLocal
	 */
	public static ThreadLocal<String> getFieldIDCache() {
		return fieldIDCache;
	}

	/**
	 * Get default Box
	 */
	public static SqlBox defaultBox() {
		return new SqlBox(SqlBoxContext.getDefaultSqlBoxContext());
	}

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

	public SqlBoxContext getSqlBoxContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

	public Dialect getDialect() {
		return context.getDialect();
	}

	public String getAlias() {
		return alias;
	}

	public Map<Entity, Set<String>> getPartents() {
		return partents;
	}

	public void setPartents(Map<Entity, Set<String>> partents) {
		this.partents = partents;
	}

	public Map<Class<?>, Map<Object, Entity>> getEntityCache() {
		return entityCache;
	}

	public void setEntityCache(Map<Class<?>, Map<Object, Entity>> entityCache) {
		this.entityCache = entityCache;
	}

	public SqlAndParameters getSpCache() {
		return spCache;
	}

	public void setSpCache(SqlAndParameters spCache) {
		this.spCache = spCache;
	}

	public List<Mapping> getConfigMappings() {
		return configMappings;
	}

	public void setConfigMappings(List<Mapping> configMappings) {
		this.configMappings = configMappings;
	}

	/**
	 * Automatically search and return the targetClass entity node list, no need
	 * give the path, but targetClass and path should be unique in who object
	 * graph
	 */
	public <T> Set<T> getUniqueNodeSet(Class<?> targetClass) {
		Set<Class<?>> path = searchNodePath(this.getSpCache().getMappingList(), this.getEntityClass(), targetClass);
		return getNodeList(path.toArray(new Class[0]));
	}

	protected Set<Class<?>> searchNodePath(List<Mapping> mapping, Class<?> from, Class<?> to) {// NOSONAR
		Set<Class<?>> checked = new HashSet<>();
		checked.add(from);
		Set<Class<?>> path = new LinkedHashSet<>();
		path.add(from);
		List<Set<Class<?>>> paths = new ArrayList<>();
		paths.add(path);
		int i = 0;
		Set<Class<?>> newPath = null;
		do {
			i++;
			Class<?> foundClass;
			do {
				foundClass = null;
				for (Set<Class<?>> set : paths) {
					if (set.size() == i) {// NOSONAR
						Class<?> last = SqlBoxUtils.getLastElement(set);
						for (Mapping mp : mapping) {// NOSONAR
							Class<?> p = mp.getThisEntity().getClass();
							Class<?> c = mp.getOtherEntity().getClass();
							if (!checked.contains(c) && p.equals(last)) {
								foundClass = c;
								break;
							} else if (!checked.contains(p) && c.equals(last)) {
								foundClass = p;
								break;
							}
						}
					}
					if (foundClass != null) {// NOSONAR
						newPath = new LinkedHashSet<>(set);
						newPath.add(foundClass);
						if (foundClass.equals(to))
							return newPath;
						checked.add(foundClass);
						break;
					}
				}
				if (newPath != null)
					paths.add(newPath);
			} while (foundClass != null);
		} while (i < 100);
		return path;// this
	}

	/**
	 * Get the NodeList by given path c1,c2,...cn, cn is targetClass
	 */
	public <T> Set<T> getNodeList(Class<?>... classes) {
		Class<?> lastClass = this.getEntityClass();

		Set<Object> lastResult = new LinkedHashSet<>();
		lastResult.add(this.getEntityBean());

		if (entityCache == null || entityCache.isEmpty())
			return (Set<T>) lastResult;

		for (Class<?> thisClass : classes) {
			if (!thisClass.equals(this.getEntityClass())) {// skip self
				Mapping mapping = findTheMapping(lastClass, thisClass);
				Set<Object> newResult = new LinkedHashSet<>();
				if (mapping.getThisEntity().getClass().equals(lastClass)) {// find
																			// child
					for (Object lastEntity : lastResult) {// NOSONAR
						Set<Object> oneList = ((Entity) lastEntity).getChildNodeSet(mapping.getOtherEntity().getClass(),
								null);
						for (Object obj : oneList) {
							newResult.add(obj);
						}
					}
				} else {// find parent
					for (Object lastEntity : lastResult) {// NOSONAR
						Object p = ((Entity) lastEntity).box().getParentNode(mapping.getThisEntity().getClass());
						newResult.add(p);
					}
				}
				lastClass = thisClass;
				lastResult = newResult;
			}
		}
		return (Set<T>) lastResult;
	}

	private Mapping findTheMapping(Class<?> lastClass, Class<?> thisClass) {
		List<Mapping> mappings = this.getSpCache().getMappingList();
		for (Mapping mapping : mappings) {
			Class<?> c1 = mapping.getThisEntity().getClass();
			Class<?> c2 = mapping.getOtherEntity().getClass();
			if (lastClass.equals(c1) && thisClass.equals(c2))
				return mapping;
			if (lastClass.equals(c2) && thisClass.equals(c1))
				return mapping;
		}
		return (Mapping) SqlBoxException.throwEX("SqlBox findTheMapping error: can not find classes " + lastClass + ","
				+ thisClass + " in mapping cache.");
	}

	/**
	 * Return the child node list for a Entity binded with this box
	 */
	public <T> Set<T> getChildNodeSet(Class<?> entityClass, String fieldID) {
		Set<T> result = new LinkedHashSet<>();
		if (entityCache == null || entityCache.isEmpty())
			return result;
		Map<Object, Entity> entities = entityCache.get(entityClass);
		for (Entity entity : entities.values()) {
			Map<Entity, Set<String>> p = entity.box().getPartents();
			if (p != null && p.containsKey(this.getEntityBean())
					&& (SqlBoxUtils.isEmptyStr(fieldID) || p.get(this.getEntityBean()).contains(fieldID)))
				result.add((T) entity);
		}
		return result;
	}

	public <T> T getParentNode(Class<?> entityClass) {
		Set<Entity> keys = this.getPartents().keySet();
		for (Entity entity : keys) {
			if (entity.getClass().equals(entityClass))
				return (T) entity;
		}
		return null;
	}

	/**
	 * Return the first element in node list
	 */
	public <T> T getChildNode(Class<?> entityClass, String fieldID) {
		Set<Object> result = getChildNodeSet(entityClass, fieldID);
		if (result.isEmpty())
			return null;
		return (T) result.iterator().next();
	}

	/**
	 * Get last auto increase id, supported by MySQL, SQL Server, DB2, Derby,
	 * Sybase, PostgreSQL
	 */
	private Object getLastAutoIncreaseIdentity(Column col) {
		String sql = "SELECT MAX(" + col.getColumnName() + ") from " + realTable();
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
		Dialect dialect = this.getSqlBoxContext().getDialect();

		// start to spell sql
		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		int count = 0;
		sb.append("insert into ").append(realTable()).append(" ( ");

		Map<String, Column> realColumns = buildRealColumns();
		for (Column col : realColumns.values()) {
			IdGenerator idGen = col.getIdGenerator();
			if (idGen != null && !(idGen instanceof AssignedGenerator)) {
				Object idValue = idGen.getNextID(this.getSqlBoxContext());
				if (idGen instanceof IdentityGenerator) {
					if (dialect.isOracleFamily())// NOSONAR
						throwEX("Box insert error, IdentityGenerator type should not set to ORACLE");
				} else if (idGen instanceof AutoGenerator) {
					if (dialect.isMySqlFamily() || dialect.isSQLServerFamily()) {// NOSONAR
						if (!col.getAutoIncreament())
							throwEX("Box insert error, AutoGenerator type should set on indentity type field for table \""
									+ this.realTable() + "\"");
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
			} else if (!SqlBoxUtils.isEmptyStr(col.getColumnName())) {// normal
																		// fields
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
		sb.append("values").append(SqlHelper.buildQuestions(count));
		if (getSqlBoxContext().getShowSql())
			getSqlBoxContext()
					.logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

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
		sb.append("update ").append(realTable()).append(" set ");

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
			sb.append(col.getColumnName()).append("=?").append(" and ");
			parameters.add(col.getPropertyValue());
		}
		sb.setLength(sb.length() - 4);

		if (this.getSqlBoxContext().getShowSql())
			getSqlBoxContext()
					.logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

		// here you go
		int result = getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Box insert error, no record be updated, sql=" + sb.toString());
	}

	/**
	 * Load a entity from database by entityID
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
		sb.append(" from ").append(this.realTable()).append(" where ");

		for (Entry<String, Object> entry : idvalues.entrySet()) {
			sb.append(entry.getKey()).append("=").append("? and ");
			parameters.add(entry.getValue());
		}
		sb.setLength(sb.length() - 5);// delete the last " and "

		if (this.getSqlBoxContext().getShowSql())
			getSqlBoxContext()
					.logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));

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
		sb.append(" from ").append(this.realTable()).append(" where ");

		for (Entry<String, Object> entry : idvalues.entrySet()) {
			sb.append(entry.getKey()).append("=").append("? and ");
			parameters.add(entry.getValue());
		}
		sb.setLength(sb.length() - 5);// delete the last " and "

		if (this.getSqlBoxContext().getShowSql())
			getSqlBoxContext()
					.logSql(new SqlAndParameters(sb.toString(), parameters.toArray(new Object[parameters.size()])));
		int result = this.getJdbc().update(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (result != 1)
			throwEX("Box delete error, no record delete for entityID:" + entityID);
		deleteEntityID(realColumns, idvalues);
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

	public Map<String, Object> getEntityID() {
		Map<String, Column> realColumns = buildRealColumns();
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (col.getEntityID()) {
				Object idValue = SqlBoxUtils.getFieldRealValue(this.entityBean, col);
				if (idValue == null)
					return null;
				col.setPropertyValue(idValue);
				result.put(col.getColumnName(), idValue);
			}
		}
		if (result.isEmpty())
			throwEX("Box update error, no entityID set for class " + this.getEntityClass());
		return result;
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
	protected void setFieldRealValue(Column col, Object value) {
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
	 * get field's fieldID
	 */
	public String fieldID(String realColumnName) {
		try {
			String fieldID = getFieldIDCache().get();
			String toCompare;
			if (SqlHelper.getInAliasTag() && !SqlBoxUtils.isEmptyStr(this.getAlias()))
				toCompare = this.getAlias() + "_" + this.getRealColumnName(null, fieldID);
			else
				toCompare = this.getRealColumnName(null, fieldID);
			if (SqlBoxUtils.isEmptyStr(fieldID) || SqlBoxUtils.isEmptyStr(realColumnName)
					|| !realColumnName.equals(toCompare))
				throwEX("Box fieldID error, can only be called according fieldID(xx.SOMECOLUMNNAME()) format");

			return fieldID;
		} finally {
			getFieldIDCache().set(null);
		}
	}

	/**
	 * Get alias Column Name
	 */
	public String aliasByRealColumnName(String realColumnName) {
		if (SqlBoxUtils.isEmptyStr(this.getAlias()))
			return realColumnName;
		return this.getAlias() + "_" + realColumnName;
	}

	/**
	 * Get alias Column Name
	 */
	public String aliasByFieldID(String fieldID) {
		if (SqlBoxUtils.isEmptyStr(this.getAlias()))
			return this.getColumnName(fieldID);
		return this.getAlias() + "_" + this.getColumnName(fieldID);
	}

	/**
	 * Return a JdbcTemplate instance related to current context<br/>
	 */
	public JdbcTemplate getJdbc() {
		return this.getSqlBoxContext().getJdbc();
	}

	/**
	 * Get real database table name
	 */
	public String realTable() {
		String realTable = configTable;
		if (SqlBoxUtils.isEmptyStr(realTable)) {
			realTable = this.getEntityClass().getSimpleName();
			int locate = realTable.indexOf('$');// for inner class, get the real
												// class name
			if (locate > 0)
				realTable = realTable.substring(locate, realTable.length());
		}
		String resultTable = context.findRealTableName(realTable);
		if (SqlBoxUtils.isEmptyStr(resultTable))
			SqlBoxException.throwEX("Can not find database table \"" + realTable + "\" for class \""
					+ this.getEntityClass() + "\", sometimes may caused by forgot call refreshMetaData.");
		return resultTable;
	}

	/**
	 * Get real database table name
	 */
	public String table() {
		if (SqlBoxUtils.isEmptyStr(this.getAlias()) || !SqlHelper.getInSqlTag())
			return realTable();
		else
			return realTable() + " " + this.getAlias();
	}

	/**
	 * get field's database column name <br/>
	 * 1)No alias, return "columnName" <br/>
	 * 2)Not in sql, return "alias_columnName" <br/>
	 * 3)In Sql, in alias tag, return "alias.columnName as alias_columnName"
	 * <br/>
	 * 4)In Sql, not in alias tag, return "alias.columnName" <br/>
	 * 
	 */
	public String getColumnName(String fieldID, Object... args) {// NOSONAR
		// "insert into user(",u.ID(123),",", u.NAME("sam"),") values(?,?)"
		// "update user set "+u.NAME("John")+"=? where "+u.ID(123)+"=?"
		for (Object arg : args)
			SqlHelper.empty(arg);

		getFieldIDCache().set(fieldID);
		if (MappingHelper.isInMapping()) {
			// IdPair is to tell mapping which fieldIDs be linked
			// tPropertyPair is to tell mapping which property field be binded
			if (MappingHelper.getIdPairCache().size() < 2) {
				MappingHelper.getIdPairCache().add(fieldID);
				MappingHelper.getEntityPairCache().add(this.getEntityBean());
			} else
				MappingHelper.getPropertyPairCache().add(fieldID);
		}

		String colname = getRealColumnName(null, fieldID);
		String als = this.getAlias();
		boolean inSQL = SqlHelper.getInSqlTag();
		boolean inSelectTag = SqlHelper.getInSelectTag();
		if (inSQL) {
			if (!SqlBoxUtils.isEmptyStr(als)) {
				if (inSelectTag)
					return als + "." + colname + " as " + als + "_" + colname;
				else
					return als + "." + colname;

			} else
				return realTable() + "." + colname;
		} else {
			if (SqlHelper.getInAliasTag() && !SqlBoxUtils.isEmptyStr(als))
				return als + "_" + colname;
			else if (SqlBoxUtils.isEmptyStr(colname))
				return "COL_NOT_FOUND";// Need set a value to tell bind() method
										// in MappingHelper.java
			else
				return colname;
		}
	}

	/**
	 * Return all active fields for sql
	 */
	public String all() {
		String als = this.getAlias();
		boolean inSQL = SqlHelper.getInSqlTag();
		boolean inSelectTag = SqlHelper.getInSelectTag();
		if (inSelectTag)
			MappingHelper.getEntityTemplates().add((Entity) this.getEntityBean());
		StringBuilder sb = new StringBuilder();
		Map<String, Column> realColumns = buildRealColumns();
		for (Column column : realColumns.values()) {
			if (column.isEnable()) {
				String colname = column.getColumnName();

				if (inSQL) {
					if (!SqlBoxUtils.isEmptyStr(als)) {// NOSONAR
						if (inSelectTag)
							sb.append(als + "." + colname + " as " + als + "_" + colname).append(",");
						else
							sb.append(als + "." + colname).append(",");
					} else
						sb.append(realTable() + "." + colname).append(",");
				} else {
					if (SqlHelper.getInAliasTag() && !SqlBoxUtils.isEmptyStr(als))// NOSONAR
						sb.append(als + "_" + colname).append(",");
					else
						sb.append(colname).append(",");
				}
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Return real Columns match to table meta data
	 */
	public Map<String, Column> buildRealColumns() {
		if (this.entityClass == null)
			SqlBoxException.throwEX("SqlBox getRealColumns error, beanClass can not be null");
		String realTableName = this.realTable();
		DBMetaData meta = this.getSqlBoxContext().getMetaData();
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
			if (SqlBoxUtils.isLegalFieldID(fieldID, pd.getPropertyType())) {
				Column realCol = new Column();
				realCol.setFieldID(fieldID);
				String realColumnMatchName = this.getRealColumnName(realTableName, fieldID);
				if (SqlBoxUtils.isEmptyStr(realColumnMatchName)) {
					Column configColumn = configColumns.get(fieldID);
					// if is a disabled field, do not print warning
					if (configColumn != null && !configColumn.isEnable())
						continue;
					Field field = ReflectionUtils.findField(this.getEntityClass(), fieldID);
					if (this.getEntityClass().getDeclaredAnnotation(IgnoreField.class) == null// NOSONAR
							&& field.getAnnotation(IgnoreField.class) == null)
						log.warn("Field \"" + fieldID + "\" does not match any column in database table \""
								+ realTableName + "\", to disable this warning message, put an @"
								+ IgnoreField.class.getSimpleName() + " annotation on it. "
								+ (fieldID.contains("ID") ? " Try change xxxID to xxxxId" : ""));
					continue;
				}
				realCol.setColumnName(realColumnMatchName);// 3080ms cost for
															// speed test
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
	 * <pre>
	 * Find how many entityID?
	 * Found lots? return them
	 * only found 1? if no generator, set to UUID if is String, set to auto type if not String
	 * Not found? look for id field, found? set to UUID if is String, set to auto type if not String
	 * No found? throw ex
	 * </pre>
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
				throwEX("SqlBox findAndSetEntityID error, no entityID set for entity " + entityClass);
			else
				return;
		} else {
			if (entityColumn != null && !entityColumn.getFieldID().equals(idColumn.getFieldID()))
				return;
			else {
				idColumn.setEntityID(true);
				if (idColumn.getIdGenerator() == null) {
					if (idColumn.getPropertyType().equals(String.class))// NOSONAR
						idColumn.setIdGenerator(this.getSqlBoxContext().getDefaultIDGenerator());
					else
						idColumn.setIdGenerator(AutoGenerator.INSTANCE);
				}
			}
		}
		return;
	}

	/**
	 * Get real column name by fieldID , realTableName can leave null if don't
	 * know <br/>
	 * userName field will find userName or username or USERNAME or USER_NAME,
	 * but only allowed 1
	 */
	private String getRealColumnName(String realTableName, String fieldID) {// NOSONAR
		Column col = getOrBuildConfigColumn(fieldID);
		String columnName = col.getColumnName();
		if (columnName == null || columnName.length() == 0)
			columnName = fieldID;

		String realTable = realTableName;
		if (SqlBoxUtils.isEmptyStr(realTable))
			realTable = this.realTable();
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
			return null;

		if (realColumnNameignoreCase != null && realColumnNameUnderline != null
				&& !realColumnNameignoreCase.equals(realColumnNameUnderline))
			SqlBoxException.throwEX("SqlBox automaticFitColumnName error, column defination \"" + columnName
					+ "\" found mutiple columns in table " + realTable);
		return realColumnNameignoreCase != null ? realColumnNameignoreCase : realColumnNameUnderline;
	}

	/**
	 * Get or Build the config column
	 */
	protected Column getOrBuildConfigColumn(String fieldID) {
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
	 * Config table name
	 */
	public Object configAlias(String tableAlias) {
		alias = tableAlias;
		return this.getEntityBean();
	}

	/**
	 * Config mappings in box, The mappingList stored in box will join to
	 * mappingList created in SQL, Detail see
	 * SqlHelper.prepareSQLandParameters() method
	 * 
	 * @return The String result if not empty, can re-used in SQL
	 */
	public String configMapping(String... mappingSqls) {
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(mappingSqls);
		this.setConfigMappings(sp.getMappingList());
		return sp.getSql();
	}

	/**
	 * Config table name
	 */
	public String automaticQuerySQL() {
		return SqlHelper.select() + this.all() + SqlHelper.from() + this.table();
	}

	/**
	 * Clean old entityID setting, set with given entityIDs
	 */
	public Object configEntityIDs(String... entityIDs) {
		for (Entry<String, Column> entry : getConfigColumns().entrySet())
			entry.getValue().setEntityID(false);
		for (String fieldID : entityIDs)
			getOrBuildConfigColumn(fieldID).setEntityID(true);
		return this.getEntityBean();
	}

	/**
	 * Config column name, if has cached field, use it, otherwise use
	 */
	public void configColumnName(String fieldID, String columnName) {
		getOrBuildConfigColumn(fieldID).setColumnName(columnName);
	}

	/**
	 * Config column if allow insert
	 */
	public void configInsertable(String fieldID, Boolean insertable) {
		getOrBuildConfigColumn(fieldID).setInsertable(insertable);
	}

	/**
	 * Config column if allow update
	 */
	public void configUpdatable(String fieldID, Boolean updatable) {
		getOrBuildConfigColumn(fieldID).setUpdatable(updatable);
	}

	/**
	 * Config column enable
	 */
	public void configEnable(String fieldID, Boolean enable) {
		getOrBuildConfigColumn(fieldID).setEnable(enable);
	}

	/**
	 * Config column name
	 */
	public <T> void configIdGenerator(String fieldID, T idGenerator) {
		getOrBuildConfigColumn(fieldID).setIdGenerator((IdGenerator) idGenerator);
	}

	// ========Config methods end==============

	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("table=" + this.configTable).append("\r\n");
		sb.append("getRealTable=" + this.realTable()).append("\r\n");
		sb.append("entityBean=" + this.entityBean).append("\r\n");
		sb.append("entityClass=" + this.entityClass).append("\r\n");

		sb.append("=====getConfigColumns======").append("\r\n");
		Map<String, Column> cfgs = this.getConfigColumns();
		for (Entry<String, Column> entry : cfgs.entrySet()) {
			sb.append("key=" + entry.getKey()).append("\r\n");
			sb.append(entry.getValue().getDebugInfo());
		}

		sb.append("=====buildRealColumns======").append("\r\n");
		cfgs = this.buildRealColumns();
		for (Entry<String, Column> entry : cfgs.entrySet()) {
			sb.append("key=" + entry.getKey()).append("\r\n");
			sb.append(entry.getValue().getDebugInfo());
		}
		return sb.toString();
	}

}
