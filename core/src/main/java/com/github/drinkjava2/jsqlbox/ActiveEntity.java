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
/*- JAVA8_BEGIN */

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ArrayUtils;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;

 
@SuppressWarnings("all")
public interface ActiveEntity<T> extends EntityType {

	default void miscMethods__________________() {// NOSONAR
	}

	public default DbContext ctx(Object... optionItems) {
		for (Object item : optionItems)
			if (item != null && item instanceof DbContext)
				return (DbContext) item;
		DbException.assureNotNull(DbContext.getGlobalDbContext(),
				DbContext.NO_GLOBAL_SQLBOXCONTEXT_FOUND);
		return DbContext.getGlobalDbContext();
	}

	public default T putField(Object... fieldAndValues) {
		for (int i = 0; i < fieldAndValues.length / 2; i++) {
			String field = (String) fieldAndValues[i * 2];
			Object value = fieldAndValues[i * 2 + 1];
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(this.getClass(), field);
			try {
				writeMethod.invoke(this, value);
			} catch (Exception e) {
				throw new DbException(e);
			}
		}
		return (T) this;
	}

	public default T forFields(String... fieldNames) {
		ActiveRecord.forFieldsOrTails.set(fieldNames);
		return (T) this;
	}

	public default T putValues(Object... values) {
		String[] fields = ActiveRecord.forFieldsOrTails.get();
		if (values.length == 0 || fields == null || fields.length == 0)
			throw new DbException("putValues fields or values can not be empty");
		if (values.length != fields.length)
			throw new DbException("putValues fields and values number not match");
		for (int i = 0; i < fields.length; i++) {
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(this.getClass(), fields[i]);
			if (writeMethod == null)
				throw new DbException(
						"Not found writeMethod for '" + this.getClass() + "' class's method '" + fields[i] + "'");
			try {
				writeMethod.invoke(this, values[i]);
			} catch (Exception e) {
				throw new DbException(e);
			}
		}
		return (T) this;
	}

	public default SqlItem bind(Object... parameters) {
		return new SqlItem(SqlOption.BIND, parameters);
	}

	public default String shardTB(Object... optionItems) {
		TableModel model = DbContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardTableColumn();
		if (col == null || col.getShardTable() == null || col.getShardTable().length == 0)
			throw new DbException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this);
		return DbContextUtils.getShardedTB(ctx(), model.getEntityClass(), shardKey1);
	}

	public default DbContext shardDB(Object... optionItems) {
		TableModel model = DbContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new DbException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this);
		return DbContextUtils.getShardedDB(ctx(), model.getEntityClass(), shardKey1);
	}

	public default Object[] shard(Object... optionItems) {
		return new Object[] { shardTB(optionItems), shardDB(optionItems) };
	}

	static Object[] insertThisClassIfNotHave(Object entity, Object... optionItems) {
		Object[] items = optionItems;
		TableModel[] models = DbContextUtils.findAllModels(optionItems);
		if (models.length == 0)
			throw new DbException("No TableMode found for entity.");
		TableModel model = models[0];
		if (!entity.getClass().equals(model.getEntityClass())) {// NOSONAR
			model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
			items = ArrayUtils.insertArray(model, items);
		}
		return items;
	}

	default void crudMethods__________________() {// NOSONAR
	}

	//@formatter:off
	public default T insert(Object... items) {return (T) ctx(items).eInsert(this, items);}
	public default T update(Object... items) {return ctx(items).eUpdate(this, items);}
	public default int updateTry(Object... items) {return ctx(items).eUpdateTry(this, items);}
	public default void delete(Object... items) {ctx(items).eDelete(this, items);}
	public default int deleteTry(Object... items) {return ctx(items).eDeleteTry(this, items);}
	public default void deleteById(Object id, Object... items) {ctx(items).eDeleteById(this.getClass(), id, items);}
	public default int deleteByIdTry(Object id, Object... items) {return ctx(items).eDeleteByIdTry(this.getClass(), id, items);}
	public default boolean exist(Object... items) {return ctx(items).eExist(this, items);}
	public default boolean existById(Object id, Object... items) {return ctx(items).eExistById(this.getClass(), id, items);}
	public default int countAll(Object... items) {return ctx(items).eCountAll(this.getClass(), items);} 
	public default T load(Object... items) {return (T) ctx(items).eLoad(this, items);}
	public default int loadTry(Object... items) {return ctx(items).eLoadTry(this, items);}
	public default T loadById(Object id, Object... items) {return (T) ctx(items).eLoadById(this.getClass(), id, items);}
	public default T loadByIdTry(Object id, Object... items) {return (T) ctx(items).eLoadByIdTry(this.getClass(), id, items);}
	public default T loadBySQL(Object... items) {return ctx(items).eLoadBySQL(items);}
	
	
	public default List<T> findAll(Object... items) {return (List<T>) ctx(items).eFindAll(this.getClass(), items);}
	public default List<T> findBySQL(Object... items) {return ctx(items).eFindBySQL(this.getClass(), items);}
	public default List<T> findBySample(Object sampleBean, Object... items) {return ctx(items).eFindBySample(sampleBean, items);} 
	public default EntityNet autoNet(Class<?>... entityClass) {return  ctx().autoNet(entityClass);}
	public default <E> E findRelatedOne(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).eFindRelatedOne(this, newItems);}
	public default <E> List<E> findRelatedList(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).eFindRelatedList(this, newItems);}
	public default <E> Set<E> findRelatedSet(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).eFindRelatedSet(this, newItems);}
	public default <E> Map<Object, E> findRelatedMap(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).eFindRelatedMap(this, newItems);}
 
	public default <E> List<E> eFindAll(Class<E> entityClass, Object... items) {return ctx(items).eFindAll(entityClass, items);}
	public default <E> List<E> eFindBySample(Object sampleBean, Object... items) {return ctx(items).eFindBySample(sampleBean, items);}
	public default <E> List<E> eFindBySQL(Object... items) {return ctx(items).eFindBySQL(items);}   
	public default <E> E eInsert(E entity, Object... items) {return ctx(items).eInsert(entity, items);} 
	public default <E> E eLoad(E entity, Object... items) {return ctx(items).eLoad(entity, items);} 
	public default <E> E eLoadById(Class<E> entityClass, Object entityId, Object... items) {return ctx(items).eLoadById(entityClass, entityId, items);}
    public default <E> E eLoadByIdTry(Class<E> entityClass, Object entityId, Object... items) {return ctx(items).eLoadByIdTry(entityClass, entityId, items);}
	public default <E> E eUpdate(Object entity, Object... items) {return ctx(items).eUpdate(entity, items);}
	public default boolean eExist(Object entity, Object... items) {return ctx(items).eExist(entity, items);}
	public default boolean eExistById(Class<?> entityClass, Object id, Object... items) {return ctx(items).eExistById(entityClass, id, items);}
	public default int eCountAll(Class<?> entityClass, Object... items) {return ctx(items).eCountAll(entityClass, items);}
	public default int eDeleteByIdTry(Class<?> entityClass, Object id, Object... items) {return ctx(items).eDeleteByIdTry(entityClass, id, items);}
	public default int eDeleteTry(Object entity, Object... items) {return ctx(items).eDeleteTry(entity, items);}
	public default int eLoadTry(Object entity, Object... items) {return ctx(items).eLoadTry(entity, items);}
	public default int eUpdateTry(Object entity, Object... items) {return ctx(items).eUpdateTry(entity, items);}
	public default void eDelete(Object entity, Object... items) { ctx(items).eDelete(entity, items);}
	public default void eDeleteById(Class<?> entityClass, Object id, Object... items) {ctx(items).eDeleteById(entityClass, id, items);}
	public default <E> E eFindRelatedOne(Object entity, Object... items) {return  ctx(items).eFindRelatedOne(entity, items);}
	public default <E> List<E> eFindRelatedList(Object entityOrIterable, Object... items) {return  ctx(items).eFindRelatedList(entityOrIterable, items);}
	public default <E> Set<E> eFindRelatedSet(Object entity, Object... items) {return  ctx(items).eFindRelatedSet(entity, items);}
	public default <E> Map<Object, E> eFindRelatedMap(Object entity, Object... items) {return  ctx(items).eFindRelatedMap(entity, items);}
  
	// PINT series methods from jDbPro
	public default <E> E pQuery(Object... items) {return ctx(items).pQuery(items);}
	public default <E> E pQueryForObject(Object... items) {return ctx(items).pQueryForObject(items);}
	public default long pQueryForLongValue(Object... items) {return ctx(items).pQueryForLongValue(items);}
	public default String pQueryForString(Object... items) {return ctx(items).pQueryForString(items);}
	public default List<Map<String, Object>> pQueryForMapList(Object... items) {return ctx(items).pQueryForMapList(items);}
	public default int pUpdate(Object... items) {return ctx(items).pUpdate(items);}
	public default <E> E pInsert(Object... items) {return ctx(items).pInsert(items);}
	public default <E> E pExecute(Object... items) {return ctx(items).pExecute(items); }  
	public default <E> List<E> pQueryForEntityList(Object... items) {return ctx(items).pQueryForEntityList(items);} 
	
	public default <E> E iQuery(Object... items) {return  ctx(items).iQuery(items);}
	public default <E> E iQueryForObject(Object... items) {return ctx(items).iQueryForObject(items);}
	public default long iQueryForLongValue(Object... items) {return ctx(items).iQueryForLongValue(items);}
	public default String iQueryForString(Object... items) {return ctx(items).iQueryForString(items);}
	public default List<Map<String, Object>> iQueryForMapList(Object... items) {return ctx(items).iQueryForMapList(items);}
	public default int iUpdate(Object... items) {return ctx(items).iUpdate(items);}
	public default <E> E iInsert(Object... items) {return ctx(items).iInsert(items);}
	public default <E> E iExecute(Object... items) {return ctx(items).iExecute(items); }
	public default <E> List<E> iQueryForEntityList(Object... items) {return ctx(items).iQueryForEntityList(items);}
	   
	public default <E> E nQuery(Connection conn, ResultSetHandler<E> rsh, String sql, Object... items) {return ctx(items).nQuery(conn, rsh, sql, items);}
	public default <E> E nQueryForObject(Connection conn, String sql, Object... items) {return ctx(items).nQueryForObject(conn, sql, items);}
	public default String nQueryForString(Connection conn, String sql, Object... items) {return ctx(items).nQueryForString(conn, sql, items);}
	public default long nQueryForLongValue(Connection conn, String sql, Object... items) {return ctx(items).nQueryForLongValue(conn, sql, items);}
	public default List<Map<String, Object>> nQueryForMapList(Connection conn, String sql, Object... items) {return ctx(items).nQueryForMapList(conn, sql, items);}
	public default int nUpdate(Connection conn, String sql, Object... items) {return ctx(items).nUpdate(conn, sql, items);}
	public default <E> E nInsert(Connection conn, ResultSetHandler<E> rsh, String sql, Object... items) {return ctx(items).nInsert(conn, rsh, sql, items);}
	public default int nExecute(Connection conn, String sql, Object... items) {return ctx(items).nExecute(conn, sql, items);}
	public default <E> List<E> nExecute(Connection conn, ResultSetHandler<E> rsh, String sql, Object... items) {return ctx(items).nExecute(conn, rsh, sql, items);}
	public default <E> E nQuery(ResultSetHandler<E> rsh, String sql, Object... items) {return ctx(items).nQuery(rsh, sql, items);}
	public default <E> E nQueryForObject(String sql, Object... items) {return ctx(items).nQueryForObject(sql, items);}
	public default String nQueryForString(String sql, Object... items) {return ctx(items).nQueryForString(sql, items);}
	public default long nQueryForLongValue(String sql, Object... items) {return ctx(items).nQueryForLongValue(sql, items);}
	public default List<Map<String, Object>> nQueryForMapList(String sql, Object... items) {return ctx(items).nQueryForMapList(sql, items);}
	public default int nUpdate(String sql, Object... items) {return ctx(items).nUpdate(sql, items);}
	public default <E> E nInsert(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... items) {return ctx(items).nInsert(rsh, sql, items);}
	public default int nExecute(String sql, Object... items) {return ctx(items).nExecute(sql, items);}
	public default <E> List<E> nExecute(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... items) {return ctx(items).nExecute(rsh, sql, items);}
			
	public default <E> E tQuery(Object... items) {return ctx(items).tQuery(items);}
	public default <E> E tQueryForObject(Object... items) {return ctx(items).tQueryForObject(items);}
	public default long tQueryForLongValue(Object... items) {return ctx(items).tQueryForLongValue(items);}
	public default String tQueryForString(Object... items) {return ctx(items).tQueryForString(items);}
	public default List<Map<String, Object>> tQueryForMapList(Object... items) {return ctx(items).tQueryForMapList(items);}
	public default int tUpdate(Object... items) {return ctx(items).tUpdate(items);}
	public default <E> E tInsert(Object... items) {return ctx(items).tInsert(items);}
	public default <E> E tExecute(Object... items) {return ctx(items).tExecute(items);}
	public default <E> List<E> tQueryForEntityList(Class<E> entityClass, Object... items) {return ctx(items).tQueryForEntityList(entityClass, items); }
   
}
/*- JAVA8_END */