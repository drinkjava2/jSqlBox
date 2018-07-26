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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ArrayUtils;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Entity class extended from ActiveRecord or implements ActiveRecordSupport
 * interface will get CRUD methods, see below difference in jSqlBox to save
 * ActiveRecord entity and normal entity(POJO) into database:
 * 
 * <pre>
 * ActiveRecord style:   
 * 
 *    SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *    SqlBoxContext.setDefaultContext(ctx);           
 *    entity.insert(); 
 * 
 *    or 
 *    
 *    SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *    entity.useContext(ctx);
 *    entity.insert();
 *    
 *    or 
 *    SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *    entity.insert(ctx);
 *    
 *    
 * Data Mapper style (for POJO entity):   
 *    SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *    ctx.insert(entity);
 *    
 *    or 
 *    
 *    SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *    someOtherCtx.insert(entity, ctx);
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class ActiveRecord<T> implements ActiveRecordSupport<T> {
	SqlBoxContext ctx;

	@Override
	public SqlBoxContext ctx(Object... optionItems) {
		for (Object item : optionItems)
			if (item != null && item instanceof SqlBoxContext)
				return (SqlBoxContext) item;
		if (ctx == null)
			ctx = SqlBoxContext.globalSqlBoxContext;
		if (ctx == null)
			throw new SqlBoxException(SqlBoxContext.NO_GLOBAL_SQLBOXCONTEXT_FOUND);
		return ctx;
	}

	public T useContext(SqlBoxContext ctx) {
		this.ctx = ctx;
		return (T) this;
	}

	protected void crudMethods__________________() {// NOSONAR
	}

	@Override
	public T insert(Object... optionItems) {
		return (T) ctx(optionItems).entityInsert(this, optionItems);
	}

	@Override
	public T update(Object... optionItems) {
		return ctx(optionItems).entityUpdate(this, optionItems);
	}

	@Override
	public int updateTry(Object... optionItems) {
		return ctx(optionItems).entityUpdateTry(this, optionItems);
	}

	@Override
	public void delete(Object... optionItems) {
		ctx(optionItems).entityDelete(this, optionItems);
	}

	@Override
	public int deleteTry(Object... optionItems) {
		return ctx(optionItems).entityDeleteTry(this, optionItems);
	}

	@Override
	public void deleteById(Object id, Object... optionItems) {
		ctx(optionItems).entityDeleteById(this.getClass(), id, optionItems);
	}

	@Override
	public int deleteByIdTry(Object id, Object... optionItems) {
		return ctx(optionItems).entityDeleteByIdTry(this.getClass(), id, optionItems);
	}

	@Override
	public boolean exist(Object... optionItems) {
		return ctx(optionItems).entityExist(this, optionItems);
	}

	@Override
	public boolean existById(Object id, Object... optionItems) {
		return ctx(optionItems).entityExistById(this.getClass(), id, optionItems);
	}

	@Override
	public T load(Object... optionItems) {
		return (T) ctx(optionItems).entityLoad(this, optionItems);
	}

	@Override
	public int loadTry(Object... optionItems) {
		return ctx(optionItems).entityLoadTry(this, optionItems);
	}

	@Override
	public T loadById(Object id, Object... optionItems) {
		return (T) ctx(optionItems).entityLoadById(this.getClass(), id, optionItems);
	}

	@Override
	public T loadByIdTry(Object id, Object... optionItems) {
		return (T) ctx(optionItems).entityLoadByIdTry(this.getClass(), id, optionItems);
	}

	@Override
	public List<T> findAll(Object... optionItems) {
		return (List<T>) ctx(optionItems).entityFindAll(this.getClass(), optionItems);
	}

	@Override
	public List<T> findByIds(Iterable<?> ids, Object... optionItems) {
		return (List<T>) ctx(optionItems).entityFindByIds(this.getClass(), ids, optionItems);
	}

	@Override
	public List<T> findBySQL(Object... optionItems) {
		return (List<T>) ctx(optionItems).iQueryForEntityList(this.getClass(),optionItems);
	}

	@Override
	public List<T> findBySample(Object sampleBean, Object... optionItems) {
		return ctx(optionItems).entityFindBySample(sampleBean, optionItems);
	}

	static Object[] insertThisClassIfNotHave(Object entity, Object... optionItems) {
		Object[] items = optionItems;
		TableModel[] models = SqlBoxContextUtils.findAllModels(optionItems);
		if (models.length == 0)
			throw new SqlBoxException("No TableMode found for entity.");
		TableModel model = models[0];
		if (!entity.getClass().equals(model.getEntityClass())) {// NOSONAR
			model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
			items = ArrayUtils.insertArray(model, items);
		}
		return items;
	}

	@Override
	public <E> E findOneRelated(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedOne(this, items);
	}

	@Override
	public <E> List<E> findRelatedList(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedList(this, items);
	}

	@Override
	public <E> Set<E> findRelatedSet(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedSet(this, items);
	}

	@Override
	public <E> Map<Object, E> findRelatedMap(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedMap(this, items);
	}

	@Override
	public int countAll(Object... optionItems) {
		return ctx(optionItems).entityCountAll(this.getClass(), optionItems);
	}

	protected void miscMethods__________________() {// NOSONAR
	}

	@Override
	public T put(Object... fieldAndValues) {
		for (int i = 0; i < fieldAndValues.length / 2; i++) {
			String field = (String) fieldAndValues[i * 2];
			Object value = fieldAndValues[i * 2 + 1];
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(this.getClass(), field);
			try {
				writeMethod.invoke(this, value);
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
		}
		return (T) this;
	}

	@Override
	public T putFields(String... fieldNames) {
		lastTimePutFieldsCache.set(fieldNames);
		return (T) this;
	}

	@Override
	public T putValues(Object... values) {
		String[] fields = lastTimePutFieldsCache.get();
		if (values.length == 0 || fields == null || fields.length == 0)
			throw new SqlBoxException("putValues fields or values can not be empty");
		if (values.length != fields.length)
			throw new SqlBoxException("putValues fields and values number not match");
		for (int i = 0; i < fields.length; i++) {
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(this.getClass(), fields[i]);
			if (writeMethod == null)
				throw new SqlBoxException(
						"Not found writeMethod for '" + this.getClass() + "' class's method '" + fields[i] + "'");
			try {
				writeMethod.invoke(this, values[i]);
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
		}
		return (T) this;
	}

	@Override
	public <U> U guess(Object... params) {// NOSONAR
		return ctx().getSqlMapperGuesser().guess(ctx(), this, params);
	}

	@Override
	public String guessSQL() {
		return ctx().getSqlMapperGuesser().guessSQL(ctx(), this);
	}

	@Override
	public PreparedSQL guessPreparedSQL(Object... params) {
		return ctx().getSqlMapperGuesser().doGuessPreparedSQL(ctx(), this, params);
	}

	@Override
	public SqlItem bind(Object... parameters) {
		return new SqlItem(SqlOption.BIND, parameters);
	}

	@Override
	public String shardTB(Object... optionItems) {
		TableModel model = SqlBoxContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardTableColumn();
		if (col == null || col.getShardTable() == null || col.getShardTable().length == 0)
			throw new SqlBoxException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = ClassCacheUtils.readValueFromBeanField(this, col.getColumnName());
		return SqlBoxContextUtils.getShardedTB(ctx(), model.getEntityClass(), shardKey1);
	}

	@Override
	public SqlBoxContext shardDB(Object... optionItems) {
		TableModel model = SqlBoxContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new SqlBoxException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = ClassCacheUtils.readValueFromBeanField(this, col.getColumnName());
		return SqlBoxContextUtils.getShardedDB(ctx(), model.getEntityClass(), shardKey1);
	}

}