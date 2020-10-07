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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		DbException.assureNotNull(DbContext.getGlobalDbContext(), DbContext.NO_GLOBAL_SQLBOXCONTEXT_FOUND);
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
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this, false, false);
		return DbContextUtils.getShardedTB(ctx(), model.getEntityClass(), shardKey1);
	}

	public default DbContext shardDB(Object... optionItems) {
		TableModel model = DbContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new DbException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this, false, false);
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
	public default T insert(Object... items) {return (T) ctx(items).entityInsert(this, items);}
	public default T update(Object... items) {return ctx(items).entityUpdate(this, items);}
	public default int updateTry(Object... items) {return ctx(items).entityUpdateTry(this, items);}
	public default void delete(Object... items) {ctx(items).entityDelete(this, items);}
	public default int deleteTry(Object... items) {return ctx(items).entityDeleteTry(this, items);}
	public default void deleteById(Object id, Object... items) {ctx(items).entityDeleteById(this.getClass(), id, items);}
	public default int deleteByIdTry(Object id, Object... items) {return ctx(items).entityDeleteByIdTry(this.getClass(), id, items);}
	public default boolean exist(Object... items) {return ctx(items).entityExist(this, items);}
	public default boolean existById(Object id, Object... items) {return ctx(items).entityExistById(this.getClass(), id, items);}
	public default int countAll(Object... items) {return ctx(items).entityCount(this.getClass(), items);} 
	public default T load(Object... items) {return (T) ctx(items).entityLoad(this, items);}
	public default int loadTry(Object... items) {return ctx(items).entityLoadTry(this, items);}
	public default T loadById(Object id, Object... items) {return (T) ctx(items).entityLoadById(this.getClass(), id, items);}
	public default T loadByIdTry(Object id, Object... items) {return (T) ctx(items).entityLoadByIdTry(this.getClass(), id, items);}
	public default T loadBySQL(Object... items) {return ctx(items).entityLoadBySql(items);}	
	public default List<T> findAll(Object... items) {return (List<T>) ctx(items).entityFind(this.getClass(), items);}
	public default List<T> findBySQL(Object... items) {return ctx(items).entityFindBySql(this.getClass(), items);}
	public default List<T> findBySample(Object sampleBean, Object... items) {return ctx(items).entityFindBySample(sampleBean, items);} 
	public default EntityNet autoNet(Class<?>... entityClass) {return  ctx().autoNet(entityClass);}
	public default <E> E findRelatedOne(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).entityFindRelatedOne(this, newItems);}
	public default <E> List<E> findRelatedList(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).entityFindRelatedList(this, newItems);}
	public default <E> Set<E> findRelatedSet(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).entityFindRelatedSet(this, newItems);}
	public default <E> Map<Object, E> findRelatedMap(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx(items).entityFindRelatedMap(this, newItems);}

}
/* JAVA8_END */