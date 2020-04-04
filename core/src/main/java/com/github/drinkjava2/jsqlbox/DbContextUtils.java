/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jdbpro.JDBPRO.valuesQuestions;
import static com.github.drinkjava2.jsqlbox.DB.AUTO_SQL;
import static com.github.drinkjava2.jsqlbox.DB.shardDB;
import static com.github.drinkjava2.jsqlbox.DB.shardTB;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdbpro.LinkArrayList;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.TypeUtils;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.IdentityIdGenerator;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.converter.FieldConverter;
import com.github.drinkjava2.jsqlbox.converter.FieldConverterUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityIdUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.gtx.GtxTag;
import com.github.drinkjava2.jsqlbox.gtx.GtxUtils;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;
import com.github.drinkjava2.jsqlbox.sqlitem.EntityKeyItem;
import com.github.drinkjava2.jsqlbox.sqlitem.SampleItem;

/**
 * DbContextUtils is utility class store static methods about DbContext
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
/**
 * @author Yong Zhu
 * @since 1.7.0
 */
public abstract class DbContextUtils {// NOSONAR
	/**
	 * Read database Meta info into SqlBox[]
	 */
	public static TableModel[] loadMetaTableModels(DbContext ctx, Dialect dialect) {
		Connection con = null;
		SQLException sqlException = null;
		try {
			con = ctx.prepareConnection();
			return TableModelUtils.db2Models(con, dialect);
		} catch (SQLException e) {
			sqlException = e;
		} finally {
			try {
				ctx.close(con);
			} catch (SQLException e) {
				if (sqlException != null)
					sqlException.setNextException(e);
				else
					sqlException = e;
			}
		}
		throw new DbException(sqlException);
	}

	/**
	 * Use current DbContext's shardingTools to calculate the master DbContext
	 */
	public static DbContext getShardedDB(DbContext currentCtx, Object entityOrClass, Object... shardKey) {
		Integer shardDBCode = getShardedDBCode(currentCtx, entityOrClass, shardKey);
		if (shardDBCode == null)
			return currentCtx;
		else
			return (DbContext) currentCtx.getMasters()[shardDBCode];
	}

	/**
	 * Calculate a entityBean's sharded dBCode, if no, return given ctx's dbCode
	 */
	public static Integer getShardedDbCodeByBean(DbContext ctx, Object entity) {
		ColumnModel col = TableModelUtils.entity2ReadOnlyModel(entity.getClass()).getShardDatabaseColumn();
		if (col == null)
			return ctx.getDbCode();
		Object value = readValueFromBeanFieldOrTail(col, entity, false, false);
		DbException.assureNotNull(value, "Entity bean's shardDatabase field value can not be null.");
		Integer dbCode = getShardedDBCode(ctx, entity.getClass(), value);
		DbException.assureNotNull(dbCode, "Entity bean's shardDatabase value can not map to a dbCode.");
		return dbCode;
	}

	/**
	 * Calculate a entityBean's sharded tableName, format tablename_code, if no,
	 * return default tableName
	 */
	public static String getShardedTbByBean(DbContext ctx, Object entity) {
		TableModel model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
		ColumnModel col = model.getShardTableColumn();
		if (col == null)
			return model.getTableName();
		Object value = readValueFromBeanFieldOrTail(col, entity, false, false);
		DbException.assureNotNull(value, "Entity bean's shardTable field value can not be null.");
		Integer tbCode = getShardedTBCode(ctx, entity.getClass(), value);
		DbException.assureNotNull(tbCode, "Entity bean's shardTable value can not map to a table code");
		return new StringBuilder(model.getTableName()).append("_").append(tbCode).toString();
	}

	/**
	 * Use current DbContext's shardingTools to calculate the shardedDBCode
	 */
	public static Integer getShardedDBCode(DbContext ctx, Object entityOrClass, Object... shardKey) {
		if (ctx.getMasters() == null || ctx.getMasters().length == 0)
			return null;
		if (ctx.getShardingTools() == null || ctx.getShardingTools().length == 0)
			return null;
		TableModel model = DbContextUtils.findEntityOrClassTableModel(entityOrClass);
		for (ShardingTool sh : ctx.getShardingTools()) {
			Integer[] result = sh.handleShardDatabase(model, shardKey);
			if (result != null) {
				if (result.length > 1)
					throw new DbException("Found more than 1 DbContext tables for target '" + model.getEntityClass()
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardDatabase search condition.");
				return result[0];
			}
		}
		return null;
	}

	/**
	 * Use current DbContext's shardingTools to calculate the real shardTable name
	 */
	public static String getShardedTB(DbContext ctx, Object entityOrClass, Object... shardKey) {
		Integer shardedTBCode = getShardedTBCode(ctx, entityOrClass, shardKey);
		TableModel model = DbContextUtils.findEntityOrClassTableModel(entityOrClass);
		if (shardedTBCode == null)
			return model.getTableName();
		else
			return model.getTableName() + "_" + shardedTBCode;
	}

	/**
	 * Use current DbContext's shardingTools to calculate the shardedTBCode
	 */
	public static Integer getShardedTBCode(DbContext ctx, Object entityOrClass, Object... shardKey) {
		if (ctx.getShardingTools() == null || ctx.getShardingTools().length == 0)
			return null;
		TableModel model = DbContextUtils.findEntityOrClassTableModel(entityOrClass);
		for (ShardingTool sh : ctx.getShardingTools()) {
			Integer[] result = sh.handleShardTable(model, shardKey);
			if (result != null) {
				if (result.length > 1)
					throw new DbException("Found more than 1 sharding tables for target '" + model.getEntityClass()
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardTable search condition");
				return result[0];
			}
		}
		return null;
	}

	/**
	 * if optionItems has a TabelModel, use it, otherwise find tablemodel from
	 * entityOrClass
	 */
	public static TableModel findTableModel(Object entityOrClass, Object... optionItems) {
		TableModel model = findFirstModel(optionItems);
		if (model != null)
			return model;
		return findEntityOrClassTableModel(entityOrClass);
	}

	public static TableModel findEntityOrClassTableModel(Object entityOrClassOrModel) {
		if (entityOrClassOrModel == null)
			throw new DbException("Can not build TableModel from null entityOrClass");
		else if (entityOrClassOrModel instanceof TableModel)
			return (TableModel) entityOrClassOrModel;
		else if (entityOrClassOrModel instanceof Class)
			return TableModelUtils.entity2ReadOnlyModel((Class<?>) entityOrClassOrModel);
		else // it's a entity bean
			return TableModelUtils.entity2ReadOnlyModel(entityOrClassOrModel.getClass());
	}

	public static TableModel findFirstModel(Object... optionItems) {// NOSONAR
		for (Object item : optionItems) { // If Model in option items, use it first
			if (item instanceof TableModel)
				return (TableModel) item;
			else if (item instanceof Class)
				return TableModelUtils.entity2ReadOnlyModel((Class<?>) item);
		}
		return null;
	}

	/**
	 * Return first tail TableModel, if not found return null
	 */
	public static TableModel findTailModel(DbContext ctx, TableModel entityModel, Object... optionItems) {// NOSONAR
		String tailTable = null;
		for (Object item : optionItems)
			if (item instanceof SqlItem && (SqlOption.TAIL.equals(((SqlItem) item).getType()))) {
				if (((SqlItem) item).getParameters().length == 0)
					tailTable = "";
				else
					tailTable = (String) ((SqlItem) item).getParameters()[0];
				break;
			}
		if (tailTable == null)
			return null;
		if ("".equals(tailTable))//
			tailTable = entityModel.getTableName();
		ctx.ensureTailModelLoaded();
		for (TableModel model : ctx.getTailModels()) {
			if (tailTable.equalsIgnoreCase(model.getTableName()))
				return model;
		}
		throw new DbException("Not found table '" + tailTable + "' in database");
	}

	/**
	 * Extract models from sqlItems
	 */
	public static TableModel[] findAllModels(Object... sqlItems) {// NOSONAR
		List<TableModel> result = new ArrayList<TableModel>();
		doFindAllModels(result, sqlItems);
		return result.toArray(new TableModel[result.size()]);
	}

	private static void doFindAllModels(List<TableModel> result, Object... sqlItems) {
		for (Object item : sqlItems) { // If Model in option items, use it first
			if (item instanceof TableModel)
				result.add((TableModel) item);
			else if (item instanceof Class)
				result.add(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			else if (item.getClass().isArray())
				doFindAllModels(result, (Object[]) item);
		}
	}

	/**
	 * Find model and alias items from sqlItems
	 */
	public static Object[] findModelAlias(Object... sqlItems) {// NOSONAR
		List<Object> result = new ArrayList<Object>();
		dofindModelAlias(result, sqlItems);
		return result.toArray(new Object[result.size()]);
	}

	private static void dofindModelAlias(List<Object> result, Object... sqlItems) {
		for (Object item : sqlItems) { // If Model in option items, use it first
			if (item instanceof TableModel) {
				result.add((TableModel) item);
			} else if (item instanceof Class) {
				result.add(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			} else if (item.getClass().isArray()) {
				dofindModelAlias(result, (Object[]) item);
			} else if (item instanceof SqlItem) {
				SqlItem sqItem = (SqlItem) item;
				SqlOption sqlItemType = sqItem.getType();
				if (SqlOption.ALIAS.equals(sqlItemType)) {
					result.add(item);
				}
			}
		}
	}

	/**
	 * Find not model/alias items from sqlItems
	 */
	public static Object[] findNotModelAlias(Object... sqlItems) {// NOSONAR
		List<Object> result = new ArrayList<Object>();
		dofindNotModelAlias(result, sqlItems);
		return result.toArray(new Object[result.size()]);
	}

	private static void dofindNotModelAlias(List<Object> result, Object... sqlItems) {
		for (Object item : sqlItems) { // If Model in option items, use it first
			if (item instanceof TableModel || item instanceof Class) {// NOSONAR
			} else if (item.getClass().isArray()) {
				dofindNotModelAlias(result, (Object[]) item);
			} else if (item instanceof SqlItem) {
				if (!SqlOption.ALIAS.equals(((SqlItem) item).getType()))
					result.add(item);
			} else
				result.add(item);
		}
	}

	/**
	 * Extract DbContext param from sqlitems
	 */
	private static DbContext extractCtx(Object... sqlItems) {
		for (Object item : sqlItems) {
			if (item instanceof DbContext) {// NOSONAR
				return (DbContext) item;
			} else if (item != null && item.getClass().isArray()) {
				DbContext ctx = extractCtx((Object[]) item);
				if (ctx != null)
					return ctx;
			}
		}
		return null;
	}

	/** Remove first found DbContext From sqlItems */
	private static void removeDbContextFromParam(List<Object> resultList, boolean founded, Object... sqlItems) {
		boolean found = founded;
		for (Object item : sqlItems) {
			if (item instanceof DbContext) {// NOSONAR
				if (founded)
					resultList.add(item);
				found = true;
			} else if (item != null && item.getClass().isArray()) {
				removeDbContextFromParam(resultList, found, (Object[]) item);
			} else
				resultList.add(item);
		}
	}

	/** Remove first found DbContext From sqlItems */
	private static Object[] cleanUpParam(Object... sqlItems) {
		List<Object> resultList = new ArrayList<Object>();
		removeDbContextFromParam(resultList, false, sqlItems);
		return resultList.toArray(new Object[resultList.size()]);
	}

	/**
	 * Create auto Alias name based on capital letters of class name in models of
	 * PreparedSQL, if alias already exists, put a number at end, for example: <br/>
	 * User ->u <br/>
	 * User, UserRole, UserOther, Order, Order_a -> u, u1, u2, o, o1
	 * 
	 * if no EntityClass, alais will be created by table name, for example: <br/>
	 * user ->u <br/>
	 * user_role -> UR
	 */
	public static void createLastAutoAliasName(PreparedSQL ps) {// NOSONAR
		if (ps.getModels() == null || ps.getModels().length == 0)
			throw new DbException("No tableModel found");
		TableModel model = (TableModel) ps.getModels()[ps.getModels().length - 1];
		String alias;
		StringBuilder sb = new StringBuilder();
		char[] chars;
		if (model.getEntityClass() != null)
			chars = model.getEntityClass().getSimpleName().toCharArray();
		else
			chars = model.getTableName().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i == 0 //
					|| (c >= 'A' && c <= 'Z') //
					|| ((i > 0) && (chars[i - 1]) == '_') //
			)
				sb.append(c);
		}
		alias = sb.toString().toLowerCase();
		String[] aliases = ps.getAliases();
		int count = 1;
		String newAlias = alias;
		boolean found = false;
		do {
			for (int i = 0; i < aliases.length - 2; i++) {
				if (newAlias.equals(aliases[i])) {
					newAlias = alias + count++;
					found = true;
					break;
				}
			}
		} while (found);
		ps.setLastAliases(newAlias);
	}

	/** Convert one row data into EntityBean */
	public static <T> T mapToEntityBean(TableModel model, Map<String, Object> oneRow) {// NOSONAR
		if (oneRow == null || oneRow.isEmpty())
			throw new DbException("Can not use null or empty row to convert to EntityBean");
		DbException.assureNotNull(model.getEntityClass(), "Can not find entityClass setting in model.");
		@SuppressWarnings("unchecked")
		T bean = (T) ClassCacheUtils.createNewEntity(model.getEntityClass());
		for (Entry<String, Object> entry : oneRow.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			ColumnModel col = model.getColumnByColName(key);
			if (col == null) {
				if (bean instanceof TailType)
					((TailType) bean).tails().put(key, value);
			} else
				writeValueToBeanFieldOrTail(col, bean, value);
		}
		return bean;
	}

	/**
	 * Transfer Object to TableModel, object can be SqlBox instance, entityClass or
	 * entity Bean
	 * 
	 * <pre>
	 * 1. TableModel instance, will use it
	 * 2. SqlBox instance, will use its tableModel
	 * 3. Class, will call TableModelUtils.entity2Model to create tableModel
	 * 4. Object, will call TableModelUtils.entity2Model(entityOrClass.getClass()) to create a SqlBox instance
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T entityOrClassToBean(Object entityOrClass) {
		if (entityOrClass == null)
			throw new DbException("Can build Bean for null entityOrClass");
		if (entityOrClass instanceof Class)
			try {
				return ((Class<T>) entityOrClass).newInstance();
			} catch (Exception e) {
				throw new DbException("Can not create new instance for '" + entityOrClass + "'");
			}
		else // it's a bean
			return (T) entityOrClass;
	}

	/**
	 * Based on PreparedSQL's models and alias, automatically build and append a SQL
	 * like below:
	 * 
	 * <pre>
	 * select a.**, b.**, c.**... from xxx a  
	 * left join xxx b on a.bid=b.id  
	 * left join xxx c on b.cid=c.id ...
	 * </pre>
	 */
	@SuppressWarnings("all")
	protected static void appendLeftJoinSQL(PreparedSQL ps) {
		Object[] m = ps.getModels();
		String[] a = ps.getAliases();
		DbException.assureTrue(m != null && a != null && m.length == a.length);

		StringBuilder sb = new StringBuilder(" select ");
		boolean ifFirst = true;
		for (int i = 0; i < m.length; i++) {// NOSONAR

			TableModel md = (TableModel) m[i];
			for (ColumnModel col : md.getColumns()) {
				if (col.getTransientable())
					continue;
				if (ifFirst)
					ifFirst = false;
				else
					sb.append(", ");
				sb.append(a[i]).append(".").append(col.getColumnName()).append(" as ").append(a[i]).append("_")
						.append(col.getColumnName());
			}
		}
		sb.append(" from ");
		sb.append(((TableModel) m[0]).getTableName()).append(" ").append(a[0]).append(" ");
		for (int i = 1; i < m.length; i++) {
			sb.append(" left join ");
			sb.append(((TableModel) m[i]).getTableName()).append(" ").append(a[i]);
			sb.append(" on ");
			appendKeyEquelsSqlPiece(sb, a[i - 1], ((TableModel) m[i - 1]), a[i], ((TableModel) m[i]));
		}
		ps.addSql(sb.toString());
	}

	/**
	 * Find relationship of 2 classes, build "a.bid1=b.id1 and a.bid2=b.id2..." SQL
	 * piece
	 */
	private static void appendKeyEquelsSqlPiece(StringBuilder sb, String a1, TableModel m1, String a2, TableModel m2) {
		List<FKeyModel> fkeys = m1.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m2.getTableName())) {// m2 is parent
				realDoAppendKeyEquelsSqlPiece(sb, a1, a2, fkey);
				return;
			}
		}
		fkeys = m2.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m1.getTableName())) {// m1 is parent
				realDoAppendKeyEquelsSqlPiece(sb, a2, a1, fkey);
				return;
			}
		}
		throw new DbException("Not found relationship(foreign key) setting between '" + m1.getEntityClass() + "' and '"
				+ m2.getEntityClass() + "'");
	}

	/** Build a.bid1=b.id1 and a.bid2=b.id2 SQL piece */
	private static void realDoAppendKeyEquelsSqlPiece(StringBuilder sb, String a, String b, FKeyModel fkey) {
		int i = 0;
		for (String col : fkey.getColumnNames()) {
			if (i > 0)
				sb.append("and ");
			sb.append(a).append(".").append(col).append("=").append(b).append(".")
					.append(fkey.getRefTableAndColumns()[i + 1]).append(" ");
			i++;
		}
	}

	/** Read value from entityBean field or tail */
	public static Object readValueFromBeanFieldOrTail(ColumnModel col, Object entityBean, boolean isInsert,
			boolean isUpdate) {
		DbException.assureNotNull(col, "columnModel can not be null.");
		if (col.getValueExist()) // NOSONAR value is stored in model
			return col.getValue();
		if (col.getConverterClassOrName() != null) {
			FieldConverter cust = FieldConverterUtils.getFieldConverter(col.getConverterClassOrName());
			return cust.entityFieldToDbValue(col, entityBean);
		}
		Object result = doReadFromFieldOrTail(col, entityBean);
		if (result != null && Date.class.isAssignableFrom(result.getClass())) {// convert date to jdbc
			Date d = (Date) result;
			if (col.getColumnType() == Type.DATE)
				result = new java.sql.Date(d.getTime());
			else if (col.getColumnType() == Type.TIMESTAMP)
				result = new java.sql.Timestamp(d.getTime());
			else if (col.getColumnType() == Type.TIME)
				result = new java.sql.Time(d.getTime());
		}
		if ((isInsert && result == null && col.isCreateTimestamp())
				|| ((isUpdate || isInsert) && col.isUpdateTimestamp())) {
			result = new Timestamp(new Date().getTime());
			writeValueToBeanFieldOrTail(col, entityBean, result);
		}
		return result;
	}

	/** Read value from entityBean field or tail */
	public static Object doReadFromFieldOrTail(ColumnModel columnModel, Object entityBean) {
		if (columnModel.getTransientable())
			return null;
		String fieldName = columnModel.getEntityField();
		if (fieldName == null) {
			if (entityBean instanceof TailType) {
				return ((TailType) entityBean).tails().get(columnModel.getColumnName());
			} else
				throw new DbException("Can not read tail value from instance which is not TailSupport");
		} else {
			Method readMethod = ClassCacheUtils.getClassFieldReadMethod(entityBean.getClass(), fieldName);
			if (readMethod != null)
				try {
					return readMethod.invoke(entityBean);
				} catch (Exception e) {
					throw new DbException(e);
				}
			else if (entityBean instanceof TailType) {
				return ((TailType) entityBean).tails().get(fieldName);
			} else
				throw new DbException("No read method for '" + fieldName + "'");
		}
	}

	/** write value to entityBean field or tail */
	public static void writeValueToBeanFieldOrTail(ColumnModel columnModel, Object entityBean, Object value) {
		DbException.assureNotNull(columnModel, "columnModel can not be null");
		if (columnModel.getConverterClassOrName() != null) {
			FieldConverter converter = FieldConverterUtils.getFieldConverter(columnModel.getConverterClassOrName());
			converter.writeDbValueToEntityField(entityBean, columnModel, value);
		} else
			doWriteToFieldOrTail(columnModel, entityBean, value);
	}

	/** write value to entityBean field or tail */
	public static void doWriteToFieldOrTail(ColumnModel columnModel, Object entityBean, Object value) {
		DbException.assureNotNull(columnModel, "columnModel can not be null");
		if (columnModel.getTransientable())
			return;
		String fieldName = columnModel.getEntityField();
		if (fieldName == null) {
			if (entityBean instanceof TailType) {
				((TailType) entityBean).tails().put(columnModel.getColumnName(), value);
			} else
				throw new DbException("Can not write tail value for entity which is not TailSupport");
		} else
			try {
				Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(entityBean.getClass(), fieldName);
				if (value != null && value.getClass() != writeMethod.getParameterTypes()[0])
					value = TypeUtils.jdbcValue2JavaValue(value, writeMethod.getParameterTypes()[0]);
				writeMethod.invoke(entityBean, value);
			} catch (Exception e) {
				throw new DbException("Field '" + fieldName + "' can not write with "
						+ (value == null ? "" : " type '" + value.getClass() + "' value '") + value + "'\n"
						+ e.getMessage(), e);
			}
	}

	/** Return "" if 2 bean's all fields equal, otherwise return field name */
	public static String entityCompare(Object entityBean1, Object entityBean2, TableModel... optionModel) {
		TableModel model;
		if (optionModel.length > 0)
			model = optionModel[0];
		else
			model = TableModelUtils.entity2Model(entityBean1.getClass());
		for (ColumnModel col : model.getColumns()) {
			if (col == null || col.getTransientable())
				continue;
			Object value1 = readValueFromBeanFieldOrTail(col, entityBean1, false, false);
			Object value2 = readValueFromBeanFieldOrTail(col, entityBean2, false, false);
			if (value1 != null && !value1.equals(value2))
				return col.getEntityField();
			if (value2 != null && !value2.equals(value1))
				return col.getEntityField();
		}
		return "";
	}

	@SuppressWarnings("unused")
	private static void coreMethods___________________________________() {// NOSONAR
	}

	public static int entityInsertTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityInsertTry(paramCtx, entityBean, newParams);
		}

		int result = doEntityInsertTry(ctx, entityBean, optionItems);
		if (result == 1 && ctx.isGtxOpen() && !(entityBean instanceof GtxTag)) // if in GTX transaction?
			GtxUtils.reg(ctx, entityBean, GtxUtils.INSERT);
		return result;
	}

	/** Check if has a IGNORE_NULL sqlItem */
	private static boolean hasIgnoreNullItem(Object... optionItems) {
		for (Object itemObject : optionItems)
			if (SqlOption.IGNORE_NULL.equals(itemObject))
				return true;
		return false;
	}

	/** Check if has a IGNORE_EMPTY sqlItem */
	private static boolean hasIgnoreEmptyItem(Object... optionItems) {
		for (Object itemObject : optionItems)
			if (SqlOption.IGNORE_EMPTY.equals(itemObject))
				return true;
		return false;
	}

	/**
	 * Insert entityBean into database, and change ID fields to values generated by
	 * IdGenerator (identity or sequence or UUID...), return row affected
	 */
	private static int doEntityInsertTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityBean);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (entityBean instanceof TailType) {
			tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) entityBean).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		ColumnModel identityCol = null;
		Type identityType = null;
		boolean ignoreNull = ctx.isIgnoreNull() || hasIgnoreNullItem(optionItems); // if have ignoreNull?
		boolean ignoreEmpty = ctx.isIgnoreEmpty() || hasIgnoreEmptyItem(optionItems); // if have ignoreEmpty?
		sqlBody.append(" (");
		boolean foundColumnToInsert = false;
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (ColumnModel col : cols.values()) {// NOSONAR
			if (col == null || col.getTransientable() || !col.getInsertable())
				continue;
			if (col.getConverterClassOrName() != null) { // converter
				FieldConverter converter = FieldConverterUtils.getFieldConverter(col.getConverterClassOrName());
				converter.handleSQL(SqlOption.INSERT, ctx, col, entityBean, sqlBody, null);
				continue;
			}
			Object value = readValueFromBeanFieldOrTail(col, entityBean, true, false);
			if (col.isCreatedBy() || col.isLastModifiedBy()) { // deal CreatedBy annotation
				value = ctx.getCurrentAuditor();
				writeValueToBeanFieldOrTail(col, entityBean, value);
			}
			if (value == null && col.getIdGenerationType() != null || !StrUtils.isEmpty(col.getIdGeneratorName())) {
				if (col.getIdGenerator() == null)
					throw new DbException("No IdGenerator found for column '" + col.getColumnName() + "'");
				IdGenerator idGen = col.getIdGenerator();
				if (GenerationType.IDENTITY.equals(idGen.getGenerationType())) {// Identity
					if (identityCol != null)
						throw new DbException(
								"More than 1 identity field found for table '" + model.getTableName() + "'");
					identityType = col.getColumnType();
					identityCol = col;
				} else if (GenerationType.SNOWFLAKE.equals(idGen.getGenerationType())) {// Snow
					sqlBody.append(col.getColumnName());
					SnowflakeCreator snow = ctx.getSnowflakeCreator();
					if (snow == null)
						throw new DbException(
								"Current DbContext no SnowflakeCreator found when try to create a Snowflake value");
					Object id = snow.nextId();
					sqlBody.append(param(id));
					sqlBody.append(", ");
					foundColumnToInsert = true;
					writeValueToBeanFieldOrTail(col, entityBean, id);
				} else {// Normal Id Generator
					sqlBody.append(col.getColumnName());
					Object id = idGen.getNextID(ctx, ctx.getDialect(), col.getColumnType());
					sqlBody.append(param(id));
					sqlBody.append(", ");
					foundColumnToInsert = true;
					writeValueToBeanFieldOrTail(col, entityBean, id);
				}
			} else {
				if (!(((ignoreNull || ignoreEmpty) && value == null) || (ignoreEmpty && StrUtils.isEmpty(value)))) {
					sqlBody.append(col.getColumnName());
					sqlBody.append(new SqlItem(SqlOption.PARAM, value));
					sqlBody.append(", ");
					foundColumnToInsert = true;
				}
			}

			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(readValueFromBeanFieldOrTail(col, entityBean, true, false));

			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(readValueFromBeanFieldOrTail(col, entityBean, true, false));
		}

		if (foundColumnToInsert)
			sqlBody.remove(sqlBody.size() - 1);// delete the last ", "

		if (shardTableItem != null)
			sqlBody.frontAdd(shardTableItem);
		else
			sqlBody.frontAdd(tableName);
		if (shardDbItem != null)
			sqlBody.append(shardDbItem);

		sqlBody.frontAdd("insert into ");// insert into xxx (
		sqlBody.append(") "); // insert into xxx ()
		sqlBody.append(valuesQuestions()); // insert into xxx () values(?,?)

		if (optionItems != null) // optional SqlItems put at end
			for (Object item : optionItems)
				sqlBody.append(item);

		if (optionModel == null)// No optional model, force use entity's
			sqlBody.frontAdd(model);

		int result = ctx.iUpdate(sqlBody.toArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		if (identityCol != null) {// write identity id to Bean field
			Object identityId = IdentityIdGenerator.INSTANCE.getNextID(ctx, ctx.getDialect(), identityType);
			writeValueToBeanFieldOrTail(identityCol, entityBean, identityId);
		}
		return result;
	}

	/** Update entityBean according primary key, return row affected */
	public static int entityUpdateTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityUpdateTry(paramCtx, entityBean, newParams);
		}
		Object oldEntity = null;
		if (ctx.isGtxOpen() && !(entityBean instanceof GtxTag))// if in GTX transaction?
			oldEntity = doEntityLoadByIdTry(ctx, entityBean.getClass(), entityBean, optionItems);
		int result = doEntityUpdateTry(ctx, entityBean, optionItems);
		if (result == 1 && ctx.isGtxOpen() && !(entityBean instanceof GtxTag)) {
			GtxUtils.reg(ctx, oldEntity, GtxUtils.BEFORE);
			GtxUtils.reg(ctx, entityBean, GtxUtils.AFTER);
		}
		return result;
	}

	/** Update entityBean according primary key, return row affected */
	private static int doEntityUpdateTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityBean);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (entityBean instanceof TailType) {
			tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) entityBean).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		LinkArrayList<Object> sqlWhere = new LinkArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		boolean ignoreNull = hasIgnoreNullItem(optionItems); // if have ignoreNull?
		boolean ignoreEmpty = hasIgnoreEmptyItem(optionItems); // if have ignoreEmpty?

		for (ColumnModel col : cols.values()) {// NOSONAR
			if (col.getTransientable() || !col.getUpdatable())
				continue;
			String fieldName = col.getEntityField();
			if (StrUtils.isEmpty(fieldName))
				fieldName = col.getColumnName();
			DbException.assureNotEmpty(fieldName,
					"Found a column not mapped to entity field or DB column in model '" + model + "'");

			if (col.getConverterClassOrName() != null) { // converter
				FieldConverter converter = FieldConverterUtils.getFieldConverter(col.getConverterClassOrName());
				converter.handleSQL(SqlOption.UPDATE, ctx, col, entityBean, sqlBody, sqlWhere);
				continue;
			}
			Object value = readValueFromBeanFieldOrTail(col, entityBean, false, true);
			if (col.isLastModifiedBy()) { // deal LastModified annotation
				value = ctx.getCurrentAuditor();
				writeValueToBeanFieldOrTail(col, entityBean, value);
			}
			if (col.getPkey()) {
				if (!sqlWhere.isEmpty())
					sqlWhere.append(" and ");// NOSONAR
				sqlWhere.append(col.getColumnName()).append("=?");
				sqlWhere.append(param(value));
			} else {
				if (!(((ignoreNull || ignoreEmpty) && value == null) || (ignoreEmpty && StrUtils.isEmpty(value)))) {
					if (!sqlBody.isEmpty())
						sqlBody.append(", ");
					sqlBody.append(col.getColumnName()).append("=? ");
					sqlBody.append(param(value));
				}
			}
			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(value);
			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(value);
		}

		sqlBody.frontAdd(" set ");
		if (shardTableItem != null)
			sqlBody.frontAdd(shardTableItem);
		else
			sqlBody.frontAdd(tableName);
		if (shardDbItem != null)
			sqlBody.append(shardDbItem);
		sqlBody.frontAdd("update ");
		sqlBody.append(" where ");// NOSONAR
		sqlBody.addAll(sqlWhere);

		if (optionItems != null) // optional SqlItems put at end
			for (Object item : optionItems)
				sqlBody.append(item);

		if (optionModel == null)
			sqlBody.frontAdd(model);
		int rowAffected = ctx.iUpdate(sqlBody.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	/**
	 * Delete entityBean in database according primary key value, return row
	 * affected
	 */
	public static int entityDeleteTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		return entityDeleteByIdTry(ctx, entityBean.getClass(), entityBean, optionItems);
	}

	/**
	 * Try delete entity by Id, return row affected, return row affected
	 */
	public static int entityDeleteByIdTry(DbContext ctx, Class<?> entityClass, Object id, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityDeleteByIdTry(paramCtx, entityClass, id, newParams);
		}
		Object oldEntity = null;
		if (ctx.isGtxOpen() && !(id instanceof GtxTag))// if in GTX transaction?
			oldEntity = doEntityLoadByIdTry(ctx, entityClass, id, optionItems);
		int result = doEntityDeleteByIdTry(ctx, entityClass, id, optionItems);
		if (result == 1 && ctx.isGtxOpen() && !(id instanceof GtxTag))
			GtxUtils.reg(ctx, oldEntity, GtxUtils.DELETE);
		return result;
	}

	protected static int doEntityDeleteByIdTry(DbContext ctx, Class<?> entityClass, Object id, // NOSONAR
			Object... optionItems) {
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityDeleteByIdTry(paramCtx, entityClass, id, newParams);
		}
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (id instanceof TailType) {
			tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) id).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		LinkArrayList<Object> sqlWhere = new LinkArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		for (ColumnModel col : cols.values()) {// NOSONAR
			if (col == null || col.getTransientable())
				continue;
			if (col.getConverterClassOrName() != null) { // converter
				FieldConverter converter = FieldConverterUtils.getFieldConverter(col.getConverterClassOrName());
				converter.handleSQL(SqlOption.DELETE, ctx, col, id, sqlBody, sqlWhere);
				continue;
			}
			if (col.getPkey()) {
				Object value = EntityIdUtils.readFeidlValueFromEntityId(id, col);
				if (!sqlWhere.isEmpty())
					sqlWhere.append(" and ");
				sqlWhere.append(param(value));
				sqlWhere.append(col.getColumnName()).append("=? ");
			}
			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(EntityIdUtils.readFeidlValueFromEntityId(id, col));
			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(EntityIdUtils.readFeidlValueFromEntityId(id, col));
		}
		if (sqlWhere.isEmpty())
			throw new DbException("No primary key found for entityBean");

		sqlBody.append("delete from ");
		if (shardTableItem != null)
			sqlBody.append(shardTableItem);
		else
			sqlBody.append(tableName);
		if (shardDbItem != null)
			sqlBody.append(shardDbItem);
		sqlBody.append(" where ").addAll(sqlWhere);

		if (optionItems != null)
			for (Object item : optionItems)
				sqlBody.append(item);

		if (optionModel == null)
			sqlBody.frontAdd(model);

		sqlBody.append(SingleTonHandlers.arrayHandler);
		int rowAffected = ctx.iUpdate(sqlBody.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	/** Load entity according entity's id fields, return row affected */
	public static int entityLoadTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityLoadTry(paramCtx, entityBean, newParams);
		}
		int result = doEntityLoadTry(ctx, entityBean, optionItems);
		if (result == 1 && ctx.isGtxOpen() && !(entityBean instanceof GtxTag))
			GtxUtils.reg(ctx, entityBean, GtxUtils.BEFORE);
		return result;
	}

	/** Load entity according entity's id fields, return row affected */
	private static int doEntityLoadTry(DbContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityBean);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (entityBean instanceof TailType) {
			tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) entityBean).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		LinkArrayList<Object> sqlWhere = new LinkArrayList<Object>();
		List<ColumnModel> effectColumns = new ArrayList<ColumnModel>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				sqlWhere.append(col.getColumnName()).append("=?")
						.append(param(readValueFromBeanFieldOrTail(col, entityBean, false, false))).append(" and ");
			}
			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(readValueFromBeanFieldOrTail(col, entityBean, false, false));
			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(readValueFromBeanFieldOrTail(col, entityBean, false, false));
			sqlBody.append(col.getColumnName()).append(", ");
			effectColumns.add(col);
		}
		sqlBody.remove(sqlBody.size() - 1);// delete the last ", "
		if (sqlWhere.isEmpty())
			throw new DbException("No PKey column found from tableModel '" + model.getTableName() + "'");
		sqlWhere.remove(sqlWhere.size() - 1);// delete the last " and"

		sqlBody.frontAdd("select ").append(" from ");
		if (shardTableItem != null)
			sqlBody.append(shardTableItem);
		else
			sqlBody.append(tableName);
		if (shardDbItem != null)
			sqlBody.append(shardDbItem);

		sqlBody.append(" where ").addAll(sqlWhere);

		if (optionItems != null)
			for (Object item : optionItems)
				sqlBody.append(item);
		if (optionModel == null)
			sqlBody.frontAdd(model);

		sqlBody.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(sqlBody.toObjectArray());

		if (valuesList == null || valuesList.isEmpty())
			return 0;
		Object[] values = valuesList.get(0);
		for (int i = 0; i < values.length; i++)
			DbContextUtils.writeValueToBeanFieldOrTail(effectColumns.get(i), entityBean, values[i]);
		return valuesList.size();
	}

	public static <T> T entityLoadByIdTry(DbContext ctx, Class<T> entityClass, Object id, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityLoadByIdTry(paramCtx, entityClass, id, newParams);
		}
		T result = doEntityLoadByIdTry(ctx, entityClass, id, optionItems);
		if (result != null && ctx.isGtxOpen() && !(id instanceof GtxTag))
			GtxUtils.reg(ctx, result, GtxUtils.EXISTSTRICT);
		return result;
	}

	/**
	 * Create a new Entity, load from DB according given ID, return null if entity
	 * does not exist in DB
	 */
	private static <T> T doEntityLoadByIdTry(DbContext ctx, Class<T> entityClass, Object id, Object... optionItems) {// NOSONAR
		T bean = buildBeanById(ctx, entityClass, id, optionItems);
		int result = doEntityLoadTry(ctx, bean, optionItems);
		if (result != 1)
			return null;
		else
			return bean;
	}

	private static <T> T buildBeanById(DbContext ctx, Class<T> entityClass, Object id, Object... optionItems) {
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
		if (tailModel != null)
			for (ColumnModel col : tailModel.getColumns())
				if (col != null)
					cols.put(col.getColumnName().toLowerCase(), col);
		T bean = DbContextUtils.entityOrClassToBean(entityClass);
		bean = EntityIdUtils.setEntityIdValues(bean, id, cols.values());
		return bean;
	}

	/**
	 * Check if entityBean exist in database by its id and all other effect fields
	 */
	public static boolean entityExistStrict(DbContext ctx, Object entityBean, Object... optionItems) {
		Object entityBean2 = entityLoadByIdTry(ctx, entityBean.getClass(), entityBean, optionItems);
		if (entityBean2 == null)
			return false;
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityBean.getClass());
		return StrUtils.isEmpty(entityCompare(entityBean, entityBean2, model));
	}

	/**
	 * Equal to entityExistById method, Check if entityBean exist in database by its
	 * id
	 */
	public static boolean entityExist(DbContext ctx, Object entityBean, Object... optionItems) {
		return entityExistById(ctx, entityBean.getClass(), entityBean, optionItems);
	}

	/**
	 * Check if entityBean exist in database by its id
	 */
	public static boolean entityExistById(DbContext ctx, Class<?> entityClass, Object id, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityExistById(paramCtx, entityClass, id, newParams);
		}
		Object bean = buildBeanById(ctx, entityClass, id, optionItems);
		boolean result = doEntityExistById(ctx, entityClass, bean, optionItems);
		if (result && ctx.isGtxOpen() && !(id instanceof GtxTag))
			GtxUtils.reg(ctx, bean, GtxUtils.EXISTID);
		return result;
	}

	private static boolean doEntityExistById(DbContext ctx, Class<?> entityClass, Object id, // NOSONAR
			Object... optionItems) {
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityExistById(paramCtx, entityClass, id, newParams);
		}
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (id instanceof TailType) {
			tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) id).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		LinkArrayList<Object> sqlWhere = new LinkArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = EntityIdUtils.readFeidlValueFromEntityId(id, col);
				if (!sqlWhere.isEmpty())
					sqlWhere.append(" and ");
				sqlWhere.append(param(value));
				sqlWhere.append(col.getColumnName()).append("=? ");
			}
			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(EntityIdUtils.readFeidlValueFromEntityId(id, col));
			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(EntityIdUtils.readFeidlValueFromEntityId(id, col));
		}
		if (sqlWhere.isEmpty())
			throw new DbException("No primary key found for entityBean");

		sqlBody.append("select count(1) from ");
		if (shardTableItem != null)
			sqlBody.append(shardTableItem);
		else
			sqlBody.append(tableName);
		if (shardDbItem != null)
			sqlBody.append(shardDbItem);
		sqlBody.append(" where ").addAll(sqlWhere);

		if (optionItems != null)
			for (Object item : optionItems)
				sqlBody.append(item);

		if (optionModel == null)
			sqlBody.frontAdd(model);

		long result = ctx.iQueryForLongValue(sqlBody.toObjectArray());
		if (result == 1)
			return true;
		else if (result == 0)
			return false;
		else
			throw new DbException("Fail to check entity exist because found " + result + " rows record in database");
	}

	/** Count quantity of all entity, this method does not support sharding */
	public static int entityCountAll(DbContext ctx, Class<?> entityClass, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityCountAll(paramCtx, entityClass, newParams);
		}
		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
		if (tailModel != null)
			for (ColumnModel col : tailModel.getColumns())
				cols.put(col.getColumnName().toLowerCase(), col);
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if (col.getShardTable() != null)
				throw new DbException(
						"Fail to count all entity because shardTable column " + col.getColumnName() + " exist.");
		}

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		sqlBody.append("select count(1) from ").append(tableName);
		if (optionItems != null)
			for (Object item : optionItems)
				sqlBody.append(item);
		if (optionModel == null)
			sqlBody.frontAdd(model);
		return ctx.iQueryForIntValue(sqlBody.toObjectArray());// NOSONAR
	}

	public static <T> List<T> entityFindAll(DbContext ctx, Class<T> entityClass, Object... optionItems) {// NOSONAR
		DbContext paramCtx = extractCtx(optionItems);
		if (paramCtx != null) {
			Object[] newParams = cleanUpParam(optionItems);
			return entityFindAll(paramCtx, entityClass, newParams);
		}

		TableModel optionModel = DbContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		tailModel = DbContextUtils.findTailModel(ctx, model, optionItems);
		if (tailModel != null)
			for (ColumnModel col : tailModel.getColumns())
				cols.put(col.getColumnName().toLowerCase(), col);
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkArrayList<Object> sqlBody = new LinkArrayList<Object>();
		List<ColumnModel> effectColumns = new ArrayList<ColumnModel>();

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if (col.getShardTable() != null)
				throw new DbException("Fail to load all entity because ShardTable columns exist.");
			sqlBody.append(col.getColumnName()).append(", ");
			effectColumns.add(col);
		}
		sqlBody.remove(sqlBody.size() - 1);// delete the last ", "
		sqlBody.frontAdd("select ").append(" from ");
		sqlBody.append(tableName);
		if (optionItems != null)
			for (Object item : optionItems)
				sqlBody.append(item);
		if (optionModel == null)
			sqlBody.frontAdd(model);

		sqlBody.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(sqlBody.toObjectArray());

		List<T> result = new ArrayList<T>();
		if (valuesList == null || valuesList.isEmpty())
			return result;
		for (Object[] values : valuesList) {
			T bean = DbContextUtils.entityOrClassToBean(entityClass);
			for (int i = 0; i < effectColumns.size(); i++)
				DbContextUtils.writeValueToBeanFieldOrTail(effectColumns.get(i), bean, values[i]);
			result.add(bean);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> entityFindBySample(DbContext ctx, Object sampleBean, Object... sqlItems) {
		return (List<T>) entityFindAll(ctx, sampleBean.getClass(),
				new SampleItem(sampleBean).sql(" where ").notNullFields(), sqlItems);
	}

	@SuppressWarnings("unused")
	private static void ormQueryMethods___________________________________() {// NOSONAR
	}

	public static EntityNet entityAutoNet(DbContext ctx, Class<?>... entityClasses) {
		TableModel[] models = findAllModels((Object[]) entityClasses);
		PreparedSQL ps = ctx.iPrepare(SqlOption.QUERY, new EntityNetHandler(), models, AUTO_SQL);
		DbException.assureTrue(ps.getAliases() != null && ps.getAliases().length > 1);
		String firstAlias = ps.getAliases()[0];
		for (int i = 1; i < entityClasses.length; i++)
			ps.giveBoth(firstAlias, ps.getAliases()[i]);
		return (EntityNet) ctx.runPreparedSQL(ps);
	}

	public static <E> E entityFindRelatedOne(DbContext ctx, Object entity, Object... sqlItems) {
		List<E> list = entityFindRelatedList(ctx, entity, sqlItems);
		if (list.size() != 1)
			throw new DbException("Expect 1 entity but found " + list.size() + " records");
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> entityFindRelatedList(DbContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new DbException("Target entity class is required.");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedList(ctx, entity, sqlItems);

		DbException.assureNotNull(entity);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (List<E>) net.pickEntityList(models[models.length - 1].getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public static <E> Set<E> entityFindRelatedSet(DbContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new DbException("Target entity class is required");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedSet(ctx, entity, sqlItems);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (Set<E>) net.pickEntitySet(models[models.length - 1].getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public static <E> Map<Object, E> entityFindRelatedMap(DbContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new DbException("Target entity class is required");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedMap(ctx, entity, sqlItems);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (Map<Object, E>) net.pickEntityMap(models[models.length - 1].getEntityClass());
	}

}