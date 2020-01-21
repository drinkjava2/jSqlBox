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

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdialects.ArrayUtils;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;

/**
 * Entity class extended from ActiveRecord will have CRUD methods, see below
 * difference in DbUtil-Plus to save ActiveRecord entity and normal entity(POJO)
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
			if (isForfield.get())
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
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this);
		return DbContextUtils.getShardedTB(ctx(), model.getEntityClass(), shardKey1);
	}

	/** Return current DbContext based on shard key value */
	public DbContext shardDB(Object... items) {
		TableModel model = DbContextUtils.findTableModel(this.getClass(), items);
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new DbException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = DbContextUtils.readValueFromBeanFieldOrTail(col, this);
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
	public T insert(Object... items) {return (T) ctx().eInsert(this, items);}
	public T update(Object... items) {return ctx().eUpdate(this, items);}
	public int updateTry(Object... items) {return ctx().eUpdateTry(this, items);}
	public void delete(Object... items) {ctx().eDelete(this, items);}
	public int deleteTry(Object... items) {return ctx().eDeleteTry(this, items);}
	public void deleteById(Object id, Object... items) {ctx().eDeleteById(this.getClass(), id, items);}
	public int deleteByIdTry(Object id, Object... items) {return ctx().eDeleteByIdTry(this.getClass(), id, items);}
	public boolean existStrict(Object... items) {return ctx().eExistStrict(this, items);}
	public boolean existId(Object... items) {return ctx().eExist(this, items);}
	public boolean existById(Object id, Object... items) {return ctx().eExistById(this.getClass(), id, items);}
	public int countAll(Object... items) {return ctx().eCountAll(this.getClass(), items);} 
	public T load(Object... items) {return (T) ctx().eLoad(this, items);}
	public int loadTry(Object... items) {return ctx().eLoadTry(this, items);}
	public T loadById(Object id, Object... items) {return (T) ctx().eLoadById(this.getClass(), id, items);}
	public T loadByIdTry(Object id, Object... items) {return (T) ctx().eLoadByIdTry(this.getClass(), id, items);}
	public T loadBySQL(Object... items) {return  ctx().eLoadBySQL(items);}
	
	
	public List<T> findAll(Object... items) {return (List<T>) ctx().eFindAll(this.getClass(), items);}
	public List<T> findBySQL(Object... items) {return ctx().eFindBySQL(this.getClass(), items);}
	public List<T> findBySample(Object... items) {return ctx().eFindBySample(this, items);} 
	public EntityNet autoNet(Class<?>... entityClass) {return  ctx().autoNet(entityClass);}
	public <E> E findRelatedOne(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().eFindRelatedOne(this, newItems);}
	public <E> List<E> findRelatedList(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().eFindRelatedList(this, newItems);}
	public <E> Set<E> findRelatedSet(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().eFindRelatedSet(this, newItems);}
	public <E> Map<Object, E> findRelatedMap(Object... items) {Object[] newItems = insertThisClassIfNotHave(this, items);return ctx().eFindRelatedMap(this, newItems);}
 
	public <E> List<E> eFindAll(Class<E> entityClass, Object... items) {return ctx().eFindAll(entityClass, items);}
	public <E> List<E> eFindBySample(Object sampleBean, Object... items) {return ctx().eFindBySample(sampleBean, items);}
	public <E> List<E> eFindBySQL(Object... items) {return ctx().eFindBySQL(items);}   
	public <E> E eInsert(E entity, Object... items) {return ctx().eInsert(entity, items);} 
	public <E> E eLoad(E entity, Object... items) {return ctx().eLoad(entity, items);} 
	public <E> E eLoadById(Class<E> entityClass, Object entityId, Object... items) {return ctx().eLoadById(entityClass, entityId, items);}
    public <E> E eLoadByIdTry(Class<E> entityClass, Object entityId, Object... items) {return ctx().eLoadByIdTry(entityClass, entityId, items);}
	public <E> E eUpdate(Object entity, Object... items) {return ctx().eUpdate(entity, items);}
	public boolean eExist(Object entity, Object... items) {return ctx().eExist(entity, items);}
	public boolean eExistById(Class<?> entityClass, Object id, Object... items) {return ctx().eExistById(entityClass, id, items);}
	public int eCountAll(Class<?> entityClass, Object... items) {return ctx().eCountAll(entityClass, items);}
	public int eDeleteByIdTry(Class<?> entityClass, Object id, Object... items) {return ctx().eDeleteByIdTry(entityClass, id, items);}
	public int eDeleteTry(Object entity, Object... items) {return ctx().eDeleteTry(entity, items);}
	public int eLoadTry(Object entity, Object... items) {return ctx().eLoadTry(entity, items);}
	public int eUpdateTry(Object entity, Object... items) {return ctx().eUpdateTry(entity, items);}
	public void eDelete(Object entity, Object... items) { ctx().eDelete(entity, items);}
	public void eDeleteById(Class<?> entityClass, Object id, Object... items) {ctx().eDeleteById(entityClass, id, items);}
	public <E> E eFindRelatedOne(Object entity, Object... items) {return  ctx().eFindRelatedOne(entity, items);}
	public <E> List<E> eFindRelatedList(Object entityOrIterable, Object... items) {return  ctx().eFindRelatedList(entityOrIterable, items);}
	public <E> Set<E> eFindRelatedSet(Object entity, Object... items) {return  ctx().eFindRelatedSet(entity, items);}
	public <E> Map<Object, E> eFindRelatedMap(Object entity, Object... items) {return  ctx().eFindRelatedMap(entity, items);}
  
	// PINT series methods from jDbPro
	public <E> E pQuery(Object... items) {return ctx().pQuery(items);}
	public <E> E pQueryForObject(Object... items) {return ctx().pQueryForObject(items);}
	public long pQueryForLongValue(Object... items) {return ctx().pQueryForLongValue(items);}
	public int pQueryForIntValue(Object... items) {return ctx().pQueryForIntValue(items);}
	public String pQueryForString(Object... items) {return ctx().pQueryForString(items);}
	public List<Map<String, Object>> pQueryForMapList(Object... items) {return ctx().pQueryForMapList(items);}
	public int pUpdate(Object... items) {return ctx().pUpdate(items);}
	public <E> E pInsert(Object... items) {return ctx().pInsert(items);}
	public <E> E pExecute(Object... items) {return ctx().pExecute(items); }  
	public <E> List<E> pQueryForEntityList(Object... items) {return ctx().pQueryForEntityList(items);} 
	
	public <E> E iQuery(Object... items) {return  ctx().iQuery(items);}
	public <E> E iQueryForObject(Object... items) {return ctx().iQueryForObject(items);}
	public long iQueryForLongValue(Object... items) {return ctx().iQueryForLongValue(items);}
	public int iQueryForIntgValue(Object... items) {return ctx().iQueryForIntValue(items);}
	public String iQueryForString(Object... items) {return ctx().iQueryForString(items);}
	public List<Map<String, Object>> iQueryForMapList(Object... items) {return ctx().iQueryForMapList(items);}
	public int iUpdate(Object... items) {return ctx().iUpdate(items);}
	public <E> E iInsert(Object... items) {return ctx().iInsert(items);}
	public <E> E iExecute(Object... items) {return ctx().iExecute(items); }
	public <E> List<E> iQueryForEntityList(Object... items) {return ctx().iQueryForEntityList(items);}
	   
	public <E> E nQuery(Connection conn, ResultSetHandler<E> rsh, String sql, Object... items) {return ctx().nQuery(conn, rsh, sql, items);}
	public <E> E nQueryForObject(Connection conn, String sql, Object... items) {return ctx().nQueryForObject(conn, sql, items);}
	public String nQueryForString(Connection conn, String sql, Object... items) {return ctx().nQueryForString(conn, sql, items);}
	public long nQueryForLongValue(Connection conn, String sql, Object... items) {return ctx().nQueryForLongValue(conn, sql, items);}
	public int nQueryForIntValue(Connection conn, String sql, Object... items) {return ctx().nQueryForIntValue(conn, sql, items);}
	public List<Map<String, Object>> nQueryForMapList(Connection conn, String sql, Object... items) {return ctx().nQueryForMapList(conn, sql, items);}
	public int nUpdate(Connection conn, String sql, Object... items) {return ctx().nUpdate(conn, sql, items);}
	public <E> E nInsert(Connection conn, ResultSetHandler<E> rsh, String sql, Object... items) {return ctx().nInsert(conn, rsh, sql, items);}
	public int nExecute(Connection conn, String sql, Object... items) {return ctx().nExecute(conn, sql, items);}
	public <E> List<E> nExecute(Connection conn, ResultSetHandler<E> rsh, String sql, Object... items) {return ctx().nExecute(conn, rsh, sql, items);}
	public <E> E nQuery(ResultSetHandler<E> rsh, String sql, Object... items) {return ctx().nQuery(rsh, sql, items);}
	public <E> E nQueryForObject(String sql, Object... items) {return ctx().nQueryForObject(sql, items);}
	public String nQueryForString(String sql, Object... items) {return ctx().nQueryForString(sql, items);}
	public long nQueryForLongValue(String sql, Object... items) {return ctx().nQueryForLongValue(sql, items);}
	public int nQueryForIntValue(String sql, Object... items) {return ctx().nQueryForIntValue(sql, items);}
	public List<Map<String, Object>> nQueryForMapList(String sql, Object... items) {return ctx().nQueryForMapList(sql, items);}
	public int nUpdate(String sql, Object... items) {return ctx().nUpdate(sql, items);}
	public <E> E nInsert(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... items) {return ctx().nInsert(rsh, sql, items);}
	public int nExecute(String sql, Object... items) {return ctx().nExecute(sql, items);}
	public <E> List<E> nExecute(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... items) {return ctx().nExecute(rsh, sql, items);}
			
	public <E> E tQuery(Object... items) {return ctx().tQuery(items);}
	public <E> E tQueryForObject(Object... items) {return ctx().tQueryForObject(items);}
	public long tQueryForLongValue(Object... items) {return ctx().tQueryForLongValue(items);}
	public int tQueryForIntValue(Object... items) {return ctx().tQueryForIntValue(items);}
	public String tQueryForString(Object... items) {return ctx().tQueryForString(items);}
	public List<Map<String, Object>> tQueryForMapList(Object... items) {return ctx().tQueryForMapList(items);}
	public int tUpdate(Object... items) {return ctx().tUpdate(items);}
	public <E> E tInsert(Object... items) {return ctx().tInsert(items);}
	public <E> E tExecute(Object... items) {return ctx().tExecute(items);}
	public <E> List<E> tQueryForEntityList(Class<E> entityClass, Object... items) {return ctx().tQueryForEntityList(entityClass, items); }
  
}