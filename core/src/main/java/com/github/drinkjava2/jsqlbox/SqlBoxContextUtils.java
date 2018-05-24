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
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardDB;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardTB;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.jdbpro.LinkStyleArrayList;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.IdentityIdGenerator;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;

/**
 * SqlBoxContextUtils is utility class store static methods about SqlBoxContext
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class SqlBoxContextUtils {// NOSONAR
	/**
	 * Read database Meta info into SqlBox[]
	 */
	public static TableModel[] loadMetaTableModels(SqlBoxContext ctx, Dialect dialect) {
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
		throw new SqlBoxException(sqlException);
	}

	private static ColumnModel findMatchColumnForJavaField(String entityField, SqlBox box) {
		ColumnModel col = findMatchColumnForJavaField(entityField, box.getTableModel());
		if (col == null)
			throw new SqlBoxException("Can not find database column match entity field '" + entityField + "'");
		return col;
	}

	private static ColumnModel findMatchColumnForJavaField(String entityField, TableModel tableModel) {
		if (tableModel == null)
			return null;
		List<ColumnModel> columns = tableModel.getColumns();
		ColumnModel result = null;
		String underLineFieldName = SqlBoxStrUtils.camelToLowerCaseUnderline(entityField);
		for (ColumnModel col : columns) {
			if (entityField.equalsIgnoreCase(col.getEntityField())
					|| underLineFieldName.equalsIgnoreCase(col.getColumnName())) {
				if (result != null)
					throw new SqlBoxException("Field '" + entityField + "' found duplicated columns definition");
				result = col;
			}
		}
		return result;
	}

	/**
	 * Use current SqlBoxContext's shardingTools to calculate the real shardTable
	 * name
	 */
	public static String getShardedTB(SqlBoxContext ctx, Object entityOrClass, Object... shardKey) {
		if (ctx.getShardingTools() == null || ctx.getShardingTools().length == 0)
			throw new SqlBoxException("No shardingTools be set.");
		String table = null;
		for (ShardingTool sh : ctx.getShardingTools()) {
			String[] result = sh.handleShardTable(ctx, entityOrClass, shardKey);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException("Can not find sharding table for target '" + entityOrClass + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 sharding tables for target '" + entityOrClass
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardTable search condition");
				table = result[0];
				break;
			}
		}
		return table;
	}

	/**
	 * Use current SqlBoxContext's shardingTools to calculate the master
	 * SqlBoxContext
	 */
	public static SqlBoxContext getShardedDB(SqlBoxContext currentCtx, Object entityOrClass, Object... shardKey) {
		if (currentCtx.getMasters() == null || currentCtx.getMasters().length == 0)
			throw new SqlBoxException(
					"Current SqlBoxContext did not set masters property but try do shardDatabase opertation.");
		if (currentCtx.getShardingTools() == null || currentCtx.getShardingTools().length == 0)
			throw new SqlBoxException("No shardingTools be set.");
		SqlBoxContext masterCtx = null;
		for (ShardingTool sh : currentCtx.getShardingTools()) {
			SqlBoxContext[] result = sh.handleShardDatabase(currentCtx, entityOrClass, shardKey);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException("Can not find master SqlBoxContext for '" + entityOrClass + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 SqlBoxContext tables for target '" + entityOrClass
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardDatabase search condition.");
				masterCtx = result[0];
				break;
			}
		}
		return masterCtx;
	}

	/**
	 * Insert entityBean into database, and change ID fields to values generated by
	 * IdGenerator (identity or sequence or UUID...)
	 */
	public static int insert(SqlBoxContext ctx, Object entityBean, Object... optionalSqlItems) {// NOSONAR
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entityBean);
		checkBeanAndBoxExist(entityBean, box);
		TableModel tableModel = box.getTableModel();
		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();

		String identityFieldName = null;
		Type identityType = null;
		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());

		jSQL.append(" (");
		boolean foundColumnToInsert = false;
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, box);
			if (!col.getTransientable() && col.getInsertable()) {
				if (col.getIdGenerationType() != null || !StrUtils.isEmpty(col.getIdGeneratorName())) {
					if (col.getIdGenerator() == null)
						throw new SqlBoxException("No IdGenerator found for column '" + col.getColumnName() + "'");
					IdGenerator idGen = col.getIdGenerator();
					if (GenerationType.IDENTITY.equals(idGen.getGenerationType())) {// Identity
						if (identityFieldName != null)
							throw new SqlBoxException(
									"More than 1 identity field found for table '" + tableModel.getTableName() + "'");
						identityType = col.getColumnType();
						identityFieldName = fieldName;
					} else if (GenerationType.SNOWFLAKE.equals(idGen.getGenerationType())) {// Snow
						jSQL.append(col.getColumnName());
						SnowflakeCreator snow = ctx.getSnowflakeCreator();
						if (snow == null)
							throw new SqlBoxException(
									"Current SqlBoxContext no SnowflakeCreator found when try to create a Snowflake value");
						Object id = snow.nextId();
						jSQL.append(param(id));
						jSQL.append(", ");
						foundColumnToInsert = true;
						ClassCacheUtils.writeValueToBeanField(entityBean, fieldName, id);
					} else {// Normal Id Generator
						jSQL.append(col.getColumnName());
						Object id = idGen.getNextID(ctx, ctx.getDialect(), col.getColumnType());
						jSQL.append(param(id));
						jSQL.append(", ");
						foundColumnToInsert = true;
						ClassCacheUtils.writeValueToBeanField(entityBean, fieldName, id);
					}
				} else {
					Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
					jSQL.append(col.getColumnName());
					jSQL.append(new SqlItem(SqlOption.PARAM, value));
					jSQL.append(", ");
					foundColumnToInsert = true;
				}

				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
		}
		if (foundColumnToInsert)
			jSQL.remove(jSQL.size() - 1);// delete the last ", "

		if (shardTableItem != null)
			jSQL.frontAdd(shardTableItem);
		else
			jSQL.frontAdd(tableModel.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);

		jSQL.frontAdd("insert into ");// insert into xxx (
		jSQL.append(") "); // insert into xxx ()
		jSQL.append(valuesQuestions()); // insert into xxx () values(?,?)

		if (optionalSqlItems != null) // optional SqlItems put at end
			for (Object item : optionalSqlItems)
				jSQL.append(item);

		int result = ctx.iUpdate(jSQL.toArray());
		if (ctx.isBatchEnabled())
			return result; // in batch mode, no need fetch Identity value
		if (identityFieldName != null) {// write identity id to Bean field
			Object identityId = IdentityIdGenerator.INSTANCE.getNextID(ctx, ctx.getDialect(), identityType);
			ClassCacheUtils.writeValueToBeanField(entityBean, identityFieldName, identityId);
		}
		return result;
	}

	/** Update entityBean according primary key */
	public static int update(SqlBoxContext ctx, Object entityBean, Object... optionalSqlItems) {// NOSONAR
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entityBean);
		checkBeanAndBoxExist(entityBean, box);
		TableModel tableModel = box.getTableModel();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, box);
			if (!col.getTransientable() && col.getUpdatable()) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
				if (!col.getPkey()) {
					if (!jSQL.isEmpty())
						jSQL.append(", ");
					jSQL.append(col.getColumnName()).append("=? ");
					jSQL.append(param(value));
				} else {
					if (!where.isEmpty())
						where.append(" and ");// NOSONAR
					where.append(col.getColumnName()).append("=?");
					where.append(param(value));
				}
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
		}

		jSQL.frontAdd(" set ");
		if (shardTableItem != null)
			jSQL.frontAdd(shardTableItem);
		else
			jSQL.frontAdd(tableModel.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.frontAdd("update ");
		jSQL.append(" where ");// NOSONAR
		jSQL.addAll(where);

		if (optionalSqlItems != null) // optional SqlItems put at end
			for (Object item : optionalSqlItems)
				jSQL.append(item);
		return box.context.iUpdate(jSQL.toObjectArray());
	}

	/**
	 * Delete entityBean in database according primary key value
	 */
	public static void delete(SqlBoxContext ctx, Object entityBean, Object... optionalSqlItems) {// NOSONAR
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entityBean);
		checkBeanAndBoxExist(entityBean, box);
		TableModel tableModel = box.getTableModel();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, box);
			if (!col.getTransientable() && col.getPkey()) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
			}

			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
		}
		if (where.isEmpty())
			throw new SqlBoxException("No primary key found for entityBean");

		jSQL.append("delete from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(tableModel.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.append(" where ").addAll(where);

		if (optionalSqlItems != null)
			for (Object item : optionalSqlItems)
				jSQL.append(item);

		jSQL.append(SingleTonHandlers.arrayHandler);
		int rowAffected = box.context.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return;
		if (rowAffected <= 0)
			throw new SqlBoxException("No row be deleted for entityBean");
		if (rowAffected > 1)
			throw new SqlBoxException("Multiple rows affected when delete entityBean");
	}

	/**
	 * Put one id value into a entity bean, or put values according a
	 * map<String,Object>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T putIdValue(SqlBoxContext ctx, T bean, Object idOrIdMap) {
		if (idOrIdMap instanceof Map<?, ?>) {
			Map<String, Object> idMap = (Map<String, Object>) idOrIdMap;
			for (Entry<String, Object> item : idMap.entrySet())
				ClassCacheUtils.writeValueToBeanField(bean, item.getKey(), item.getValue());
		} else {
			SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, bean);
			TableModel tableModel = box.getTableModel();
			for (ColumnModel col : tableModel.getColumns())
				if (col.getPkey()) {
					ClassCacheUtils.writeValueToBeanField(bean, col.getEntityField(), idOrIdMap);
					break;
				}
		}
		return bean;
	}

	public static <T> T loadById(SqlBoxContext ctx, Class<T> entityClass, Object idOrIdMap,
			Object... optionalSqlItems) {// NOSONAR
		try {
			T bean = entityClass.newInstance();
			putIdValue(ctx, bean, idOrIdMap);
			return load(ctx, bean, optionalSqlItems);
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T load(SqlBoxContext ctx, Object entityBean, Object... optionalSqlItems) {// NOSONAR
		SqlBoxException.assureNotNull(entityBean, "entityClass can not be null");

		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entityBean);
		TableModel tableModel = box.getTableModel();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		List<String> allFieldNames = new ArrayList<String>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityBean.getClass());
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, box);
			if (!col.getTransientable()) {
				if (col.getPkey())
					where.append(col.getColumnName()).append("=?")
							.append(param(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName)))
							.append(" and ");
				jSQL.append(col.getColumnName()).append(", ");
				allFieldNames.add(col.getColumnName());

				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(tableModel, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
		}
		jSQL.remove(jSQL.size() - 1);// delete the last ", "
		where.remove(where.size() - 1);// delete the last " and"

		jSQL.frontAdd("select ").append(" from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(tableModel.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);

		jSQL.append(" where ").addAll(where);

		if (optionalSqlItems != null)
			for (Object item : optionalSqlItems)
				jSQL.append(item);

		jSQL.append(SingleTonHandlers.arrayHandler);
		Object[] values = ctx.iQuery(jSQL.toObjectArray());
		if (values == null || values.length == 0)
			throw new SqlBoxException("Try to load entity but no record found in database");
		try {
			for (int i = 0; i < values.length; i++) {
				Method writeMethod = writeMethods.get(allFieldNames.get(i));
				writeMethod.invoke(entityBean, values[i]);
			}
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
		return (T) entityBean;
	}

	private static void checkBeanAndBoxExist(Object entityBean, SqlBox box) {
		SqlBoxException.assureNotNull(entityBean, "Assert error, entityBean can not be null");
		SqlBoxException.assureNotNull(box, "Assert error, box of entityBean can not be null");
		SqlBoxException.assureNotNull(box.getContext(), "Assert error, box's SqlBoxContext can not be null");
		SqlBoxException.assureNotNull(box.getTableModel(), "Assert error, box's TableModel can not be null");
		SqlBoxException.assureNotEmpty(box.getTableModel().getTableName(),
				"Assert error, box's tableName can not be null");
	}

	/**
	 * @param ctx
	 * @param obj
	 * @return A TableModel instance related this object
	 */
	public static TableModel getTableModelFromEntityOrClass(SqlBoxContext ctx, Object entityOrClass) {
		if (entityOrClass == null)
			throw new SqlBoxException("Can build TableModel configuration for null netConfig");
		if (entityOrClass instanceof TableModel)
			return (TableModel) entityOrClass;
		else if (entityOrClass instanceof SqlBox)
			return ((SqlBox) entityOrClass).getTableModel();
		else if (entityOrClass instanceof Class)
			return SqlBoxUtils.createSqlBox(ctx, (Class<?>) entityOrClass).getTableModel();
		else
			return SqlBoxUtils.findAndBindSqlBox(ctx, entityOrClass).getTableModel();
	}
	

	/**
	 * Transfer Object[] to TableModel[], object can be SqlBox instance, entityClass
	 * or entity Bean
	 * 
	 * <pre>
	 * 1. TableModel instance, will use it
	 * 2. SqlBox instance, will use its tableModel
	 * 3. Class, will call ctx.createSqlBox() to create a SqlBox instance and use its tableModel
	 * 4. Object, will call SqlBoxUtils.findAndBindSqlBox() to create a SqlBox instance
	 * </pre>
	 */
	public static TableModel[] objectConfigsToModels(SqlBoxContext ctx, Object[] netConfigs) {
		if (netConfigs == null || netConfigs.length == 0)
			return new TableModel[0];
		TableModel[] result = new TableModel[netConfigs.length];
		for (int i = 0; i < netConfigs.length; i++)
			result[i] = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, netConfigs[i]);
		return result;
	}
	
	

}