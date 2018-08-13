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
import static com.github.drinkjava2.jsqlbox.JSQLBOX.AUTO_SQL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardDB;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardTB;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdbpro.LinkStyleArrayList;
import com.github.drinkjava2.jdbpro.PreparedSQL;
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
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityIdUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;
import com.github.drinkjava2.jsqlbox.sqlitem.EntityKeyItem;
import com.github.drinkjava2.jsqlbox.sqlitem.SampleItem;

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

	public static ColumnModel findMatchColumnForJavaField(String entityField, TableModel tableModel) {
		SqlBoxException.assureNotNull(tableModel, "Can not find column for '" + entityField + "' in null table ");
		ColumnModel col = tableModel.getColumnByFieldName(entityField);
		SqlBoxException.assureNotNull(col,
				"Can not find column for '" + entityField + "' in table '" + tableModel.getTableName() + "'");
		return col;
	}

	/**
	 * Use current SqlBoxContext's shardingTools to calculate the real shardTable
	 * name
	 */
	public static String getShardedTB(SqlBoxContext ctx, Object entityOrClass, Object... shardKey) {
		if (ctx.getShardingTools() == null || ctx.getShardingTools().length == 0)
			throw new SqlBoxException("No shardingTools be set.");
		TableModel model = SqlBoxContextUtils.findEntityOrClassTableModel(entityOrClass);
		String table = null;
		for (ShardingTool sh : ctx.getShardingTools()) {
			String[] result = sh.handleShardTable(ctx, model, shardKey);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException(
							"Can not find sharding table for target '" + model.getEntityClass() + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 sharding tables for target '" + model.getEntityClass()
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
		TableModel model = SqlBoxContextUtils.findEntityOrClassTableModel(entityOrClass);
		SqlBoxContext masterCtx = null;
		for (ShardingTool sh : currentCtx.getShardingTools()) {
			SqlBoxContext[] result = sh.handleShardDatabase(currentCtx, model, shardKey);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException("Can not find master SqlBoxContext for '" + model.getEntityClass() + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 SqlBoxContext tables for target '"
							+ model.getEntityClass()
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardDatabase search condition.");
				masterCtx = result[0];
				break;
			}
		}
		return masterCtx;
	}

	public static TableModel findTableModel(Object entityOrClass, Object... optionItems) {
		TableModel model = findFirstModel(optionItems);
		if (model != null)
			return model;
		return findEntityOrClassTableModel(entityOrClass);
	}

	public static TableModel findEntityOrClassTableModel(Object entityOrClass) {
		if (entityOrClass == null)
			throw new SqlBoxException("Can not build TableModel from null entityOrClass");
		else if (entityOrClass instanceof TableModel)
			return (TableModel) entityOrClass;
		else if (entityOrClass instanceof Class)
			return TableModelUtils.entity2ReadOnlyModel((Class<?>) entityOrClass);
		else // it's a entity bean
			return TableModelUtils.entity2ReadOnlyModel(entityOrClass.getClass());
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
	 * Create auto Alias name based on capital letters of class name in models of
	 * PreparedSQL, if alias already exists, put a number at end, for example: <br/>
	 * User ->u <br/>
	 * User, UserRole, UserOther, Order, Order_a -> u, u1, u2, o, o1
	 */
	public static void createLastAutoAliasName(PreparedSQL ps) {
		if (ps.getModels() == null || ps.getModels().length == 0)
			throw new SqlBoxException("No tableModel found");
		TableModel model = (TableModel) ps.getModels()[ps.getModels().length - 1];
		String[] aliases = ps.getAliases();
		StringBuilder sb = new StringBuilder();
		char[] chars = model.getEntityClass().getSimpleName().toCharArray();
		for (char c : chars)
			if (c >= 'A' && c <= 'Z')
				sb.append(c);
		String alias = sb.toString().toLowerCase();
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
	public static <T> T mapToEntityBean(TableModel model, Map<String, Object> oneRow) {
		if (oneRow == null || oneRow.isEmpty())
			throw new SqlBoxException("Can not use null or empty row to convert to EntityBean");
		SqlBoxException.assureNotNull(model.getEntityClass(), "Can not find entityClass setting in model.");
		@SuppressWarnings("unchecked")
		T bean = (T) ClassCacheUtils.createNewEntity(model.getEntityClass());
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
			throw new SqlBoxException("Can build Bean for null entityOrClass");
		if (entityOrClass instanceof Class)
			try {
				return ((Class<T>) entityOrClass).newInstance();
			} catch (Exception e) {
				throw new SqlBoxException("Can not create new instance for '" + entityOrClass + "'");
			}
		else // it's a bean
			return (T) entityOrClass;
	}

	public static void notAllowSharding(ColumnModel col) {
		if (col.getShardTable() != null || col.getShardDatabase() != null)
			throw new SqlBoxException(
					"Fail to execute entity CRUD operation because found sharding column is not included in prime Key columns");
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
	public static void appendLeftJoinSQL(PreparedSQL ps) {
		Object[] m = ps.getModels();
		String[] a = ps.getAliases();
		SqlBoxException.assureTrue(m != null && m != null & m.length == a.length);

		StringBuilder sb = new StringBuilder(" select ");
		boolean ifFirst = true;
		for (int i = 0; i < m.length; i++) {

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
				realDoAppendKeyEquelsSqlPiece(sb, a1, m1, a2, m2, fkey);
				return;
			}
		}
		fkeys = m2.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m1.getTableName())) {// m1 is parent
				realDoAppendKeyEquelsSqlPiece(sb, a2, m2, a1, m1, fkey);
				return;
			}
		}
		throw new SqlBoxException("Not found relationship(foreign key) setting between '" + m1.getEntityClass()
				+ "' and '" + m2.getEntityClass() + "'");
	}

	/** Build a.bid1=b.id1 and a.bid2=b.id2 SQL piece */
	private static void realDoAppendKeyEquelsSqlPiece(StringBuilder sb, String a, TableModel ma, String b,
			TableModel mb, FKeyModel fkey) {
		int i = 0;
		for (String col : fkey.getColumnNames()) {
			if (i > 0)
				sb.append("and ");
			sb.append(a).append(".").append(col).append("=").append(b).append(".")
					.append(fkey.getRefTableAndColumns()[i + 1]).append(" ");
			i++;
		}
	}

	@SuppressWarnings("unused")
	private static void crudMethods___________________________________() {
	}

	/**
	 * Insert entityBean into database, and change ID fields to values generated by
	 * IdGenerator (identity or sequence or UUID...)
	 */
	public static int entityInsertTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();

		String identityFieldName = null;
		Type identityType = null;
		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		Boolean ignoreNull = null;

		jSQL.append(" (");
		boolean foundColumnToInsert = false;
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable() || !col.getInsertable())
				continue;
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
				if (value == null && ignoreNull == null) {
					for (Object itemObject : optionItems)
						if (SqlOption.IGNORE_NULL.equals(itemObject)) {
							ignoreNull = true;
							break;
						}
					if (ignoreNull == null)
						ignoreNull = false;
				}
				if (ignoreNull == null || !ignoreNull || value != null) {
					jSQL.append(col.getColumnName());
					jSQL.append(new SqlItem(SqlOption.PARAM, value));
					jSQL.append(", ");
					foundColumnToInsert = true;
				}
			}

			if (col.getPkey()) {
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			} else
				notAllowSharding(col);
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

		if (optionModel == null)
			jSQL.frontAdd(model);

		int result = ctx.iUpdate(jSQL.toArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		if (identityFieldName != null) {// write identity id to Bean field
			Object identityId = IdentityIdGenerator.INSTANCE.getNextID(ctx, ctx.getDialect(), identityType);
			ClassCacheUtils.writeValueToBeanField(entityBean, identityFieldName, identityId);
		}
		return result;
	}

	/** Update entityBean according primary key */
	public static int entityUpdateTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		Boolean ignoreNull = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable() || !col.getUpdatable())
				continue;
			Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
			if (col.getPkey()) {
				if (!where.isEmpty())
					where.append(" and ");// NOSONAR
				where.append(col.getColumnName()).append("=?");
				where.append(param(value));
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			} else {
				notAllowSharding(col);
				if (value == null && ignoreNull == null) {
					for (Object itemObject : optionItems)
						if (SqlOption.IGNORE_NULL.equals(itemObject)) {
							ignoreNull = true;
							break;
						}
					if (ignoreNull == null)
						ignoreNull = false;
				}
				if (ignoreNull == null || !ignoreNull || value != null) {
					if (!jSQL.isEmpty())
						jSQL.append(", ");
					jSQL.append(col.getColumnName()).append("=? ");
					jSQL.append(param(value));
				}
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

		if (optionModel == null)
			jSQL.frontAdd(model);
		int rowAffected = ctx.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	/**
	 * Delete entityBean in database according primary key value
	 */
	public static int entityDeleteTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);

			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");

				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			}
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

		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayHandler);
		int rowAffected = ctx.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	/**
	 * Try delete entity by Id, return row affected
	 */
	public static int entityDeleteByIdTry(SqlBoxContext ctx, Class<?> entityClass, Object id, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityClass);
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = EntityIdUtils.readFeidlValueFromEntityId(id, model, fieldName);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(EntityIdUtils.readFeidlValueFromEntityId(id, model, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(EntityIdUtils.readFeidlValueFromEntityId(id, model, fieldName));
			} else
				notAllowSharding(col);
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

		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayHandler);
		int rowAffected = ctx.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	public static int entityLoadTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		List<String> allFieldNames = new ArrayList<String>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityBean.getClass());
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				where.append(col.getColumnName()).append("=?")
						.append(param(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName))).append(" and ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			} else
				notAllowSharding(col);
			jSQL.append(col.getColumnName()).append(", ");
			allFieldNames.add(col.getEntityField());
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
		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(jSQL.toObjectArray());

		if (valuesList == null || valuesList.isEmpty())
			return 0;
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
		return valuesList.size();
	}

	public static <T> T entityLoadByIdTry(SqlBoxContext ctx, Class<T> entityClass, Object idOrIdMap,
			Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		SqlBoxException.assureNotNull(model.getEntityClass());

		T bean = SqlBoxContextUtils.entityOrClassToBean(entityClass);
		bean = EntityIdUtils.setEntityIdValues(bean, idOrIdMap, model);
		int result = entityLoadTry(ctx, bean, optionItems);
		if (result != 1)
			return null;
		else
			return bean;
	}

	/**
	 * Check if entityBean exist in database by its id
	 */
	public static boolean entityExist(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
			} else
				notAllowSharding(col);
		}
		if (where.isEmpty())
			throw new SqlBoxException("No primary key found for entityBean");

		jSQL.append("select count(1) from ");
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

		if (optionModel == null)
			jSQL.frontAdd(model);

		long result = ctx.iQueryForLongValue(jSQL.toObjectArray());
		if (result == 1)
			return true;
		else if (result == 0)
			return false;
		else
			throw new SqlBoxException("Fail to check entity exist because found " + result + " records exist");
	}

	/**
	 * Try delete entity by Id, return row affected
	 */
	public static boolean entityExistById(SqlBoxContext ctx, Class<?> entityClass, Object id, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityClass);
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = EntityIdUtils.readFeidlValueFromEntityId(id, model, fieldName);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(EntityIdUtils.readFeidlValueFromEntityId(id, model, fieldName));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(EntityIdUtils.readFeidlValueFromEntityId(id, model, fieldName));
			} else
				notAllowSharding(col);
		}
		if (where.isEmpty())
			throw new SqlBoxException("No primary key found for entityBean");

		jSQL.append("select count(1) from ");
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

		if (optionModel == null)
			jSQL.frontAdd(model);

		long result = ctx.iQueryForLongValue(jSQL.toObjectArray());
		if (result == 1)
			return true;
		else if (result == 0)
			return false;
		else
			throw new SqlBoxException(
					"Fail to check entity exist because found " + result + " rows record in database");
	}

	/** Count quantity of all entity, this method does not support sharding */
	public static int entityCountAll(SqlBoxContext ctx, Class<?> entityClass, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		SqlBoxException.assureNotNull(model.getEntityClass());

		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityClass);
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (!col.getTransientable() && (col.getShardTable() != null || col.getShardDatabase() != null))
				throw new SqlBoxException("Fail to count entity quantity because sharding columns exist.");
		}

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		jSQL.append("select count(1) from ").append(model.getTableName());
		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);
		if (optionModel == null)
			jSQL.frontAdd(model);
		return ((Number) ctx.iQueryForObject(jSQL.toObjectArray())).intValue();// NOSONAR
	}

	public static <T> List<T> entityFindAll(SqlBoxContext ctx, Class<T> entityClass, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		List<String> allFieldNames = new ArrayList<String>();

		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityClass);
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable())
				continue;
			if ((col.getShardTable() != null || col.getShardDatabase() != null))
				throw new SqlBoxException("Fail to load all entity because sharding columns exist.");
			jSQL.append(col.getColumnName()).append(", ");
			allFieldNames.add(col.getEntityField());
		}
		jSQL.remove(jSQL.size() - 1);// delete the last ", "
		jSQL.frontAdd("select ").append(" from ");
		jSQL.append(model.getTableName());
		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);
		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(jSQL.toObjectArray());

		List<T> result = new ArrayList<T>();
		if (valuesList == null || valuesList.isEmpty())
			return result;
		for (Object[] values : valuesList) {
			T bean = SqlBoxContextUtils.entityOrClassToBean(entityClass);
			try {
				for (int i = 0; i < values.length; i++) {
					Method writeMethod = writeMethods.get(allFieldNames.get(i));
					SqlBoxException.assureNotNull(writeMethod,
							"Not found write method of field '" + allFieldNames.get(i) + "' in " + bean.getClass());
					writeMethod.invoke(bean, values[i]);
				}
			} catch (Exception e) {
				throw new SqlBoxException(
						"Write bean error, it may be caused by bean field type different with Db column type.", e);
			}
			result.add(bean);
		}
		return result;
	}

	public static <T> List<T> entityFindByIds(SqlBoxContext ctx, Class<T> entityClass, Iterable<?> ids, // NOSONAR
			Object... optionItems) {
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		SqlBoxException.assureNotNull(model.getEntityClass());

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		List<String> allFieldNames = new ArrayList<String>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityClass);
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, model);
			if (!col.getTransientable()) {
				if (col.getPkey()) {
					List<Object> oneFieldValues = EntityIdUtils.getOnlyOneFieldFromIds(ids, model, fieldName);
					where.append(col.getColumnName()).append(" in (");
					for (int i = 0; i < oneFieldValues.size(); i++) {
						if (i != 0)
							where.append(",");
						where.append("?");
						where.append(param(oneFieldValues.get(i)));
					}
					where.append(")");
					if (col.getShardTable() != null) // Sharding Table?
						shardTableItem = shardTB(oneFieldValues);
					if (col.getShardDatabase() != null) // Sharding DB?
						shardDbItem = shardDB(oneFieldValues);
					where.append(" and ");
				} else
					notAllowSharding(col);
				jSQL.append(col.getColumnName()).append(", ");
				allFieldNames.add(col.getEntityField());
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
		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(jSQL.toObjectArray());

		List<T> result = new ArrayList<T>();
		if (valuesList == null || valuesList.isEmpty())
			return result;
		for (Object[] values : valuesList) {
			T bean = SqlBoxContextUtils.entityOrClassToBean(entityClass);
			try {
				for (int i = 0; i < values.length; i++) {
					Method writeMethod = writeMethods.get(allFieldNames.get(i));
					SqlBoxException.assureNotNull(writeMethod,
							"Not found write method of field '" + allFieldNames.get(i) + "' in " + bean.getClass());
					writeMethod.invoke(bean, values[i]);
				}
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
			result.add(bean);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> entityFindBySample(SqlBoxContext ctx, Object sampleBean, Object... sqlItems) {
		return (List<T>) entityFindAll(ctx, sampleBean.getClass(),
				new SampleItem(sampleBean).sql(" where ").notNullFields(), sqlItems);
	}

	@SuppressWarnings("unused")
	private static void ormQueryMethods___________________________________() {
	}

	public static EntityNet entityAutoNet(SqlBoxContext ctx, Class<?>... entityClasses) {
		TableModel[] models = findAllModels((Object[]) entityClasses);
		PreparedSQL ps = ctx.iPrepare(SqlOption.QUERY, new EntityNetHandler(), models, AUTO_SQL);
		SqlBoxException.assureTrue(ps.getAliases() != null && ps.getAliases().length > 1);
		String firstAlias = ps.getAliases()[0];
		for (int i = 1; i < entityClasses.length; i++)
			ps.giveBoth(firstAlias, ps.getAliases()[i]);
		return (EntityNet) ctx.runPreparedSQL(ps);
	}

	public static <E> E entityFindRelatedOne(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		List<E> list = entityFindRelatedList(ctx, entity, sqlItems);
		if (list.size() != 1)
			throw new SqlBoxException("Expect 1 entity but found " + list.size() + " records");
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> entityFindRelatedList(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new SqlBoxException("Target entity class is required");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedList(ctx, entity, sqlItems);

		SqlBoxException.assureNotNull(entity);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (List<E>) net.pickEntityList(models[models.length - 1].getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public static <E> Set<E> entityFindRelatedSet(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new SqlBoxException("Target entity class is required");
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
	public static <E> Map<Object, E> entityFindRelatedMap(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new SqlBoxException("Target entity class is required");
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