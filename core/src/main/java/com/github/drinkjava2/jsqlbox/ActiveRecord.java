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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.inline.SqlAndParams;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/**
 * Entity class extended from ActiveRecord will get CRUD methods, see below
 * difference in jSqlBox to save ActiveRecord entity and normal entity(POJO)
 * into database:
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
 *    entity.box().setContext(ctx);
 *    entity.insert();
 *    
 *    
 * Data Mapper style (for POJO entity):   
 *    SqlBoxContext ctx=new SqlBoxContext(dataSource);
 *    ctx.insert(entity);
 * 
 * </pre>
 * 
 * 
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class ActiveRecord implements ActiveRecordSupport {
	private static ThreadLocal<String[]> lastTimePutFieldsCache = new ThreadLocal<String[]>();

	SqlBox box;

	@Override
	public SqlBox bindedBox() {
		return box;
	}

	@Override
	public void unbindBox() {
		box = null;
	}

	@Override
	public void bindBox(SqlBox box) {
		if (box == null)
			throw new SqlBoxException("Can not bind null SqlBox to entity");
		this.box = box;
	}

	@Override
	public SqlBox box() {
		if (box == null)
			this.bindBox(SqlBoxUtils.createSqlBox(Config.getGlobalSqlBoxContext(), this.getClass()));
		return box;
	}

	@Override
	public TableModel tableModel() {
		return box().getTableModel();
	}

	public ColumnModel columnModel(String columnName) {
		return box().getTableModel().getColumn(columnName);
	}

	@Override
	public String table() {
		return box().getTableModel().getTableName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T alias(String alias) {
		box().getTableModel().setAlias(alias);
		return (T) this;
	}

	@Override
	public SqlBoxContext ctx() {
		SqlBox theBox = box();
		if (theBox.getContext() == null)
			theBox.setContext(Config.getGlobalSqlBoxContext());
		return theBox.getContext();
	}

	@Override
	public void insert() {
		SqlBoxContext ctx = ctx();
		if (ctx == null)
			throw new SqlBoxException(
					"No default global SqlBoxContext be set,  please use method SqlBoxContext.setDefaultContext(new SqlBoxContext(dataSource)) to set a global default SqlBoxContext instance");
		ctx.insert(this);
	}

	@Override
	public int update() {
		SqlBoxContext ctx = ctx();
		if (ctx == null)
			throw new SqlBoxException("No default global SqlBoxContext be set.");
		return ctx.update(this);
	}

	@Override
	public void delete() {
		SqlBoxContext ctx = ctx();
		if (ctx == null)
			throw new SqlBoxException("No default global SqlBoxContext be set. ");
		ctx.delete(this);
	}

	@Override
	public <T> T load(Object pkey) {
		SqlBoxContext ctx = ctx();
		if (ctx == null)
			throw new SqlBoxException("No default global SqlBoxContext be set.  ");
		return ctx.load(this.getClass(), pkey);
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

	/**
	 * Run current SQL based on current method @Sql annotated String or Text String
	 * and parameters, return execute or query result
	 */
	@SuppressWarnings("unchecked")
	public <T> T guess(Object... params) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.ActiveRecord".equals(stack.getClassName())
					&& "guess".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos].getClassName();
		String callerMethodName = stacks[callerPos].getMethodName();
		Class<?> callerClass = null;

		try {
			callerClass = Class.forName(callerClassName);
		} catch (ClassNotFoundException e) {
			throw new SqlBoxException(e);
		}
		Method callerMethod = null;
		Method[] methods = callerClass.getMethods();
		for (Method method : methods)
			if (callerMethodName != null && callerMethodName.equals(method.getName())) {
				callerMethod = method;
				break;
			}
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		Annotation[] annos = callerMethod.getAnnotations();
		Sql sqlAnno = null;
		Class<?> handlerClass = null;
		for (Annotation anno : annos) {
			if (Sql.class.equals(anno.annotationType()))
				sqlAnno = (Sql) anno;
			if (Handler.class.equals(anno.annotationType())) {
				handlerClass = ((Handler) anno).value();
			}
		}
		String sql = null;
		if (sqlAnno != null)
			sql = sqlAnno.value()[0];
		else {
			String src;
			try {
				src = TextUtils.getJavaSourceCodeUTF8(callerClassName);
			} catch (Exception e) {
				throw new SqlBoxException("Method '" + callerMethodName + "' in '" + callerClassName
						+ "' have no Sql annotation or text.");
			}
			sql = StrUtils.substringAfter(src, callerMethodName + "(");
			sql = StrUtils.substringBetween(sql, "/*-", "*/");
		}
		if (sql != null)
			sql = sql.trim(); 
		char dotype;
		if (StrUtils.startsWithIgnoreCase(sql, "select"))
			dotype = 's';
		else if (StrUtils.startsWithIgnoreCase(sql, "delete"))
			dotype = 'u';
		else if (StrUtils.startsWithIgnoreCase(sql, "update"))
			dotype = 'u';
		else if (StrUtils.startsWithIgnoreCase(sql, "insert"))
			dotype = 'u';
		else
			dotype = 'e';
		switch (dotype) {
		case 's': {
			if (handlerClass == null)
				handlerClass = ScalarHandler.class;
			ResultSetHandler<T> resultSetHandler = null;
			try {
				resultSetHandler = (ResultSetHandler<T>) handlerClass.newInstance();
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
			return ctx().nQuery(resultSetHandler, sql, params);
		}
		case 'u': {
			Object o = ctx().nUpdate(sql, params);
			return (T) o;
		}
		default:
			if (handlerClass == null) {
				Object o = ctx().nExecute(sql, params);
				return (T) o;
			}
			ResultSetHandler<T> resultSetHandler = null;
			try {
				resultSetHandler = (ResultSetHandler<T>) handlerClass.newInstance();
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
			Object o = ctx().nExecute(resultSetHandler, sql, params);
			return (T) o;
		}
	}

	/** Return current method @Sql annotated String or Text String */
	public String sql() {
		return null;
	}

	/**
	 * Return SqlAndParams object created based on current method @Sql annotated
	 * String or Text String and parameters
	 */
	public SqlAndParams sqlAndParams(Object... params) {
		return null;
	}

}