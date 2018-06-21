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
import com.github.drinkjava2.jsqlbox.entitynet.EntityIdUtils;
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

	private static ColumnModel findMatchColumnForJavaField(String entityField, TableModel tableModel) {
		SqlBoxException.assureNotNull(tableModel, "Can not find column for '" + entityField + "' in null table ");
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
		if (result == null)
			throw new SqlBoxException(
					"Can not find column for '" + entityField + "' in table '" + tableModel.getTableName() + "'");
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
	public static int insert(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel model = SqlBoxContextUtils.configToModel(entityBean, optionItems);
		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();

		String identityFieldName = null;
		Type identityType = null;
		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());

		jSQL.append(" (");
		boolean foundColumnToInsert = false;
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (!col.getTransientable() && col.getInsertable()) {
				if (col.getIdGenerationType() != null || !StrUtils.isEmpty(col.getIdGeneratorName())) {
					if (col.getIdGenerator() == null)
						throw new SqlBoxException("No IdGenerator found for column '" + col.getColumnName() + "'");
					IdGenerator idGen = col.getIdGenerator();
					if (GenerationType.IDENTITY.equals(idGen.getGenerationType())) {// Identity
						if (identityFieldName != null)
							throw new SqlBoxException(
									"More than 1 identity field found for table '" + model.getTableName() + "'");
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
					shardTableItem = shardTB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
		}
		if (foundColumnToInsert)
			jSQL.remove(jSQL.size() - 1);// delete the last ", "

		if (shardTableItem != null)
			jSQL.frontAdd(shardTableItem);
		else
			jSQL.frontAdd(model.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);

		jSQL.frontAdd("insert into ");// insert into xxx (
		jSQL.append(") "); // insert into xxx ()
		jSQL.append(valuesQuestions()); // insert into xxx () values(?,?)

		if (optionItems != null) // optional SqlItems put at end
			for (Object item : optionItems)
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
	public static int update(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel model = SqlBoxContextUtils.configToModel(entityBean, optionItems);

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
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
					shardTableItem = shardTB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
		}

		jSQL.frontAdd(" set ");
		if (shardTableItem != null)
			jSQL.frontAdd(shardTableItem);
		else
			jSQL.frontAdd(model.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.frontAdd("update ");
		jSQL.append(" where ");// NOSONAR
		jSQL.addAll(where);

		if (optionItems != null) // optional SqlItems put at end
			for (Object item : optionItems)
				jSQL.append(item);
		return ctx.iUpdate(jSQL.toObjectArray());
	}

	/**
	 * Delete entityBean in database according primary key value
	 */
	public static void delete(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel model = SqlBoxContextUtils.configToModel(entityBean, optionItems);

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (!col.getTransientable() && col.getPkey()) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
			}

			if (col.getShardTable() != null) // Sharding Table?
				shardTableItem = shardTB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

			if (col.getShardDatabase() != null) // Sharding DB?
				shardDbItem = shardDB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
		}
		if (where.isEmpty())
			throw new SqlBoxException("No primary key found for entityBean");

		jSQL.append("delete from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(model.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.append(" where ").addAll(where);

		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);

		jSQL.append(SingleTonHandlers.arrayHandler);
		int rowAffected = ctx.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return;
		if (rowAffected <= 0)
			throw new SqlBoxException("No row be deleted for entityBean");
		if (rowAffected > 1)
			throw new SqlBoxException("Multiple rows affected when delete entityBean");
	}

	public static <T> T loadByQuery(SqlBoxContext ctx, Object config, Object... sqlItems) {
		List<Map<String, Object>> rows = ctx.iQueryForMapList(sqlItems);
		if (rows == null || rows.isEmpty())
			throw new SqlBoxException("No no record found in database.");
		if (rows.size() > 1)
			throw new SqlBoxException("More than 1 record found in database.");
		Map<String, Object> oneRow = rows.get(0);
		return mapToEntityBean(ctx, config, oneRow);
	}

	/** Convert one row data into EntityBean */
	public static <T> T mapToEntityBean(SqlBoxContext ctx, Object config, Map<String, Object> oneRow) {
		if (oneRow == null || oneRow.isEmpty())
			throw new SqlBoxException("Can not use null or empty row to convert to EntityBean");
		TableModel model = SqlBoxContextUtils.configToModel(config);
		SqlBoxException.assureNotNull(model.getEntityClass(), "Can not find entityClass setting in model.");
		@SuppressWarnings("unchecked")
		T bean = (T) ClassCacheUtils.createNewEntity(model.getEntityClass());
		ctx.getSqlBox(bean).setTableModel(model.newCopy());
		for (ColumnModel col : model.getColumns()) {
			boolean foundValue = false;
			for (Entry<String, Object> row : oneRow.entrySet()) {
				if (row.getKey().equalsIgnoreCase(col.getColumnName())) {
					foundValue = true;
					SqlBoxException.assureNotEmpty(col.getEntityField(),
							"EntityField not found for column '" + col.getColumnName() + "'");
					ClassCacheUtils.writeValueToBeanField(bean, col.getEntityField(), row.getValue());
				}
			}
			if (col.getPkey() && !foundValue)
				throw new SqlBoxException("One prime-key not set value: '" + col.getColumnName() + "'");
		}
		return bean;
	}

	public static <T> List<T> loadAll(SqlBoxContext ctx, Object config, Object... optionItems) {
		TableModel t = configToModel(config, optionItems);
		Object[] items = new Object[optionItems.length + 1];
		items[0] = "select * from " + t.getTableName();
		System.arraycopy(optionItems, 0, items, 1, optionItems.length);
		return ctx.iQueryForEntityList(config, items);
	}

	public static <T> T loadById(SqlBoxContext ctx, Object entityOrClass, Object idOrIdMap, Object... optionItems) {// NOSONAR
		TableModel model = SqlBoxContextUtils.configToModel(entityOrClass, optionItems);
		SqlBoxException.assureNotNull(model.getEntityClass(),
				"Can not find TableModel setting in '" + entityOrClass + "'");
		T bean = SqlBoxContextUtils.configToBean(entityOrClass);
		bean =  EntityIdUtils.setEntityIdValues(bean, idOrIdMap, model);
		return load(ctx, bean, optionItems);
	}

	@SuppressWarnings("unchecked")
	public static <T> T load(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		SqlBoxException.assureNotNull(entityBean, "entityBean can not be null");

		TableModel model = SqlBoxContextUtils.configToModel(entityBean, optionItems);
		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		List<String> allFieldNames = new ArrayList<String>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityBean.getClass());
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (!col.getTransientable()) {
				if (col.getPkey())
					where.append(col.getColumnName()).append("=?")
							.append(param(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName)))
							.append(" and ");
				jSQL.append(col.getColumnName()).append(", ");
				allFieldNames.add(col.getEntityField());

				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(model, ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
		}
		jSQL.remove(jSQL.size() - 1);// delete the last ", "
		if (where.isEmpty())
			throw new SqlBoxException("No PKey column found from tableModel '" + model.getTableName() + "'");
		where.remove(where.size() - 1);// delete the last " and"

		jSQL.frontAdd("select ").append(" from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(model.getTableName());
		if (shardDbItem != null)
			jSQL.append(shardDbItem);

		jSQL.append(" where ").addAll(where);

		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);

		jSQL.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(jSQL.toObjectArray());
		if (valuesList.isEmpty())
			throw new SqlBoxException("Try to load entity but no record found in database");
		if (valuesList.size() > 1)
			throw new SqlBoxException("Try to load entity but more than 1 record found in database");
		Object[] values = valuesList.get(0);
		try {
			for (int i = 0; i < values.length; i++) {
				Method writeMethod = writeMethods.get(allFieldNames.get(i));
				SqlBoxException.assureNotNull(writeMethod,
						"Not found write method of field '" + allFieldNames.get(i) + "' in " + entityBean.getClass());
				writeMethod.invoke(entityBean, values[i]);
			}
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
		return (T) entityBean;
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
	public static <T> T configToBean(Object entityOrClass) {
		if (entityOrClass == null)
			throw new SqlBoxException("Can build Bean for null entityOrClass");
		Class<?> t = null;
		if (entityOrClass instanceof TableModel)
			t = ((TableModel) entityOrClass).getEntityClass();
		else if (entityOrClass instanceof ActiveRecordSupport)
			return (T) entityOrClass;
		else if (entityOrClass instanceof SqlBox)
			t = ((SqlBox) entityOrClass).getTableModel().getEntityClass();
		else if (entityOrClass instanceof Class)
			t = (Class<?>) entityOrClass;
		else // it's a entity bean
			return (T) entityOrClass;
		try {
			return (T) t.newInstance();
		} catch (Exception e) {
			throw new SqlBoxException("Can not create new instance for '" + t + "'");
		}
	}

	/**
	 * Transfer Object to TableModel, object can be SqlBox instance, entityClass or
	 * entity Bean
	 * 
	 * <pre>
	 * 0. If TableModel in option items, use it first
	 * 1. if entityOrClass is TableModel instance, will use it
	 * 2. if entityOrClass is SqlBox instance, will use its tableModel
	 * 3. if entityOrClass is Class, will call TableModelUtils.entity2Model to create tableModel
	 * 4. if entityOrClass is Object, will call TableModelUtils.entity2Model(entityOrClass.getClass()) to create a SqlBox instance
	 * </pre>
	 */
	public static TableModel configToModel(Object entityOrClass, Object... optionItems) {
		for (Object item : optionItems) { // If Model in option items, use it first
			if (item instanceof TableModel)
				return (TableModel) item;
			if (item instanceof ActiveRecordSupport)
				return ((ActiveRecordSupport) item).tableModel();
			if (item instanceof SqlBox)
				return ((SqlBox) item).getTableModel();
			if (item instanceof SqlItem) {
				SqlItem sqlItem = (SqlItem) item;
				SqlOption sqlItemType = sqlItem.getType();
				if (SqlOption.MODEL.equals(sqlItemType) || SqlOption.MODEL_AUTO_ALIAS.equals(sqlItemType)) {
					Object[] args = sqlItem.getParameters();
					if (args.length != 1)
						throw new SqlBoxException("Model item need one parameter here.");
					TableModel t = SqlBoxContextUtils.configToModel(args[0]);// deal first
					// if auto alias? for example: UserOrder.class -> UR
					if (SqlOption.MODEL_AUTO_ALIAS.equals(sqlItemType) && StrUtils.isEmpty(t.getAlias()))
						t.setAlias(createAutoAliasNameForEntityClass(t.getEntityClass()));
					return t;// return first model
				} else if (SqlOption.MODEL_ALIAS.equals(sqlItemType)) {
					Object[] args = sqlItem.getParameters();
					if (args.length != 2)
						throw new SqlBoxException("MODEL_ALIAS item need 'model, alias' format 2 parameters");
					TableModel t = SqlBoxContextUtils.configToModel(0);
					SqlBoxException.assureNotNull(t.getEntityClass(), "'entityClass' property not set for model " + t);
					SqlBoxException.assureNotEmpty((String) args[1],
							"Alias can not be empty for class '" + t.getEntityClass() + "'");
					t.setAlias((String) args[1]);
					return t;
				}
			}
		}

		if (entityOrClass == null)
			throw new SqlBoxException("Can build TableModel configuration for null entityOrClass");
		if (entityOrClass instanceof TableModel)
			return (TableModel) entityOrClass;
		else if (entityOrClass instanceof ActiveRecordSupport)
			return ((ActiveRecordSupport) entityOrClass).tableModel();
		else if (entityOrClass instanceof SqlBox)
			return ((SqlBox) entityOrClass).getTableModel();
		else if (entityOrClass instanceof Class)
			return TableModelUtils.entity2Model((Class<?>) entityOrClass);
		else // it's a entity bean
			return SqlBoxUtils.findAndBindSqlBox(null, entityOrClass).getTableModel();
	}

	/** Create a Auto Alias name for a Entity Class */
	public static String createAutoAliasNameForEntityClass(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		char[] chars = clazz.getSimpleName().toCharArray();
		for (char c : chars)
			if (c >= 'A' && c <= 'Z')
				sb.append(c);
		return sb.toString().toLowerCase(); 
	} 
 
}