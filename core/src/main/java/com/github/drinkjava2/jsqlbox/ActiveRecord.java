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

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
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
public class ActiveRecord implements ActiveRecordSupport {
	SqlBoxContext ctx;

	@Override
	public SqlBoxContext ctx() {
		if (ctx == null)
			ctx = SqlBoxContext.globalSqlBoxContext;
		if (ctx == null)
			throw new SqlBoxException(SqlBoxContext.NO_GLOBAL_SQLBOXCONTEXT_FOUND);
		return ctx;
	}

	public ActiveRecordSupport useContext(SqlBoxContext ctx) {
		this.ctx = ctx;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T insert(Object... optionalSqlItems) {
		int affectedRow = ctx().insertEntity(this, optionalSqlItems);
		if (affectedRow != 1)
			throw new SqlBoxException("No record inserted into database.");
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T update(Object... optionalSqlItems) {
		ctx().updateEntity(this, optionalSqlItems);
		return (T) this;
	}

	@Override
	public void delete(Object... optionalSqlItems) {
		ctx().deleteEntity(this, optionalSqlItems);
	}

	@Override
	public <T> T load(Object... optionalSqlItems) {
		return ctx().loadEntity(this, optionalSqlItems);
	}

	@Override
	public <T> T loadById(Object idOrIdMap, Object... optionalSqlItems) {
		return ctx().loadById(this, idOrIdMap, optionalSqlItems);
	}
	

	@Override
	public int countAll(Object... optionalSqlItems) {
		return ctx().countAllEntity(this.getClass(), optionalSqlItems);
	}

	@Override
	public ActiveRecordSupport put(Object... fieldAndValues) {
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
		return this;
	}

	@Override
	public ActiveRecordSupport putFields(String... fieldNames) {
		lastTimePutFieldsCache.set(fieldNames);
		return this;
	}

	@Override
	public ActiveRecordSupport putValues(Object... values) {
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
		return this;
	}

	@Override
	public <T> T guess(Object... params) {// NOSONAR
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