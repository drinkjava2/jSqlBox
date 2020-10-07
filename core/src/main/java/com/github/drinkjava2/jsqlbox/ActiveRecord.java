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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.github.drinkjava2.jdialects.ArrayUtils;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;

/**
 * Entity class extended from ActiveRecord will have CRUD methods, see below
 * difference in jSqlBox to save ActiveRecord entity and normal entity(POJO)
 * into database:
 * 
 * <pre>
 * ActiveRecord style:   
 * 
 *    DbContext ctx=new DbContext(dataSource);
 *    DbContext.setDefaultContext(ctx);           
 *    entity.insert(); 
 * 
 *    or 
 *    
 *    DbContext ctx=new DbContext(dataSource);
 *    entity.useContext(ctx);
 *    entity.insert();
 *    
 *    or 
 *    DbContext ctx=new DbContext(dataSource);
 *    entity.insert(ctx);
 *    
 *    
 * Data Mapper style:  
 * 
 *    DbContext ctx=new DbContext(dataSource);
 *    ctx.eInsert(pojo);
 *    
 *    or
 *    
 *    DbContext ctx=new DbContext(dataSource);
 *    DbContext.setDefaultContext(ctx);           
 *    eInsert(pojo); //static import JSQLBOX.eInsert
 *    
 *    or 
 *    
 *    DbContext ctx0=new DbContext(dataSource);
 *    ctx1.insert(entity, ctx0);
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class ActiveRecord<T> implements TailType, EntityType {
	static final ThreadLocal<String[]> forFieldsOrTails = new ThreadLocal<String[]>();
	static final ThreadLocal<Boolean> isForfield = new ThreadLocal<Boolean>();
	private DbContext ctx;
	private Map<String, Object> tailsMap;

	protected void miscMethods__________________() {// NOSONAR
	}

	public DbContext ctx() {
		if (ctx != null)
			return ctx;
		DbException.assureNotNull(DbContext.globalDbContext, DbContext.NO_GLOBAL_SQLBOXCONTEXT_FOUND);
		return DbContext.globalDbContext;
	}

	public TableModel model() {
		return DbContextUtils.findEntityOrClassTableModel(this).newCopy();
	}

	public T useContext(DbContext ctx) {
		this.ctx = ctx;
		return (T) this;
	}

	public Map<String, Object> tails() {
		if (tailsMap == null)
			tailsMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		return tailsMap;
	}

	public <V> V getField(String fieldName) {
		return (V) ClassCacheUtils.readValueFromBeanField(this, fieldName);
	}

	public <V> V getTail(String columnName) {
		if (tailsMap == null)
			return null;
		return (V) tailsMap.get(columnName);
	}

	public T putTail(Object... columAndValues) {
		DbException.assureTrue(columAndValues.length % 2 == 0, "Column and values should be paired");
		for (int i = 0; i < columAndValues.length / 2; i++)
			tails().put((String) columAndValues[i * 2], columAndValues[i * 2 + 1]);
		return (T) this;
	}

	public T putField(Object... fieldAndValues) {
		DbException.assureTrue(fieldAndValues.length % 2 == 0, "Field and values should be paired");
		for (int i = 0; i < fieldAndValues.length / 2; i++)
			ClassCacheUtils.writeValueToBeanField(this, (String) fieldAndValues[i * 2], fieldAndValues[i * 2 + 1]);
		return (T) this;
	}

	public T forFields(String... fieldNames) {
		forFieldsOrTails.set(fieldNames);
		isForfield.set(true);
		return (T) this;
	}

	public T forTails(String... columnNames) {
		forFieldsOrTails.set(columnNames);
		isForfield.set(false);
		return (T) this;
	}

	public T putValues(Object... values) {
		String[] fields = forFieldsOrTails.get();
		if (values.length == 0 || fields == null || fields.length == 0)
			throw new DbException("putValues fields or values can not be empty");
		if (values.length != fields.length)
			throw new DbException("putValues quantity does not match forFields or forColumns");
		for (int i = 0; i < fields.length; i++)
			if (Boolean.TRUE.equals(isForfield.get()))
				ClassCacheUtils.writeValueToBeanField(this, fields[i], values[i]);
			else
				tails().put(fields[i], values[i]);
		return (T) this;
	}

	/** Return current table based on shard key value */
	public String shardTB(Object... items) {
		TableModel model = DbContextUtils.findTableModel(this.getClass(), items);
		ColumnModel col = model.getShardTableColumn();
		if (col == null || col.getShardTable() == null || col.getShardTable().length == 0)
			throw new DbException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this, false, false);
		return DbContextUtils.getShardedTB(ctx(), model.getEntityClass(), shardKey1);
	}

	/** Return current DbContext based on shard key value */
	public DbContext shardDB(Object... items) {
		TableModel model = DbContextUtils.findTableModel(this.getClass(), items);
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new DbException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this, false, false);

		return DbContextUtils.getShardedDB(ctx(), model.getEntityClass(), shardKey1);
	}

	/** Return current table and DbContext based on shard key value */
	public Object[] shard(Object... items) {
		return new Object[] { shardTB(items), shardDB(items) };
	}

	/**
	 * For ORM query, if self class is ignored, use this method to put entity's
	 * model class
	 */
	protected static Object[] insertThisClassIfNotHave(Object entity, Object... items) {
		Object[] newItems = items;
		TableModel[] models = DbContextUtils.findAllModels(items);
		if (models.length == 0)
			throw new DbException("No TableMode found for entity.");
		TableModel model = models[0];
		if (!entity.getClass().equals(model.getEntityClass())) {// NOSONAR
			model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
			newItems = ArrayUtils.insertArray(model, items);
		}
		return newItems;
	}

	protected void crudMethods__________________() {// NOSONAR
	}

	//@formatter:off
	public T insert(Object... items) {return (T) ctx().entityInsert(this, items);}
	public T update(Object... items) {return ctx().entityUpdate(this, items);}
	public int updateTry(Object... items) {return ctx().entityUpdateTry(this, items);}
	public void delete(Object... items) {ctx().entityDelete(this, items);}
	public int deleteTry(Object... items) {return ctx().entityDeleteTry(this, items);}
	public void deleteById(Object id, Object... items) {ctx().entityDeleteById(this.getClass(), id, items);}
	public int deleteByIdTry(Object id, Object... items) {return ctx().entityDeleteByIdTry(this.getClass(), id, items);}
	public boolean existStrict(Object... items) {return ctx().entityExistStrict(this, items);}
	public boolean existId(Object... items) {return ctx().entityExist(this, items);}
	public boolean existById(Object id, Object... items) {return ctx().entityExistById(this.getClass(), id, items);}
	public int countAll(Object... items) {return ctx().entityCount(this.getClass(), items);} 
	public T load(Object... items) {return (T) ctx().entityLoad(this, items);}
	public int loadTry(Object... items) {return ctx().entityLoadTry(this, items);}
	public T loadById(Object id, Object... items) {return (T) ctx().entityLoadById(this.getClass(), id, items);}
	public T loadByIdTry(Object id, Object... items) {return (T) ctx().entityLoadByIdTry(this.getClass(), id, items);}
	public T loadBySQL(Object... items) {return  ctx().entityLoadBySql(items);}
	public List<T> findAll(Object... items) {return (List<T>) ctx().entityFind(this.getClass(), items);}
	public List<T> findBySQL(Object... items) {return ctx().entityFindBySql(this.getClass(), items);}
	public List<T> findBySample(Object... items) {return ctx().entityFindBySample(this, items);} 
	public EntityNet autoNet(Class<?>... entityClass) {return  ctx().autoNet(entityClass);}
	public <E> E findRelatedOne(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().entityFindRelatedOne(this, newItems);}
	public <E> List<E> findRelatedList(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().entityFindRelatedList(this, newItems);}
	public <E> Set<E> findRelatedSet(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().entityFindRelatedSet(this, newItems);}
	public <E> Map<Object, E> findRelatedMap(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().entityFindRelatedMap(this, newItems);}
  
}