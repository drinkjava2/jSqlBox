/*
 * Copyright (C) 2016 Yong Zhu.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;

/**
 * SqlBoxUtils is utility class to bind SqlBox instance to a entity bean, there
 * are 4 different ways to bind:
 * 
 * <pre>
 * 1. For ActiveRecord child class, jSqlBox bind SqlBox by call its bindBox() method and 
 *    store SqlBox instance in its box field;
 * 
 * 2. For instance implemented ActiveRecordSupport, bind SqlBox buy call its bindBox() method
 * 
 * 3. For Entity with a "public SqlBox box;" field, jSqlBox will directly access this field to store SqlBox instance.
 * 
 * 4. For Entity without box field, will use a threadLocal Map cache to store SqlBox instance, and make the key
 *    point to entity, value point to SqlBox instance. 
 *    
 * Note: Method 4 is not recommended because if do batch insert like 100000 times may cause out of memory error, because 
 *       before LhreadLocal map be cleaned when thread close, memory will full, simliar like Hibernate's L1 cache full problem.
 * </pre>
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public abstract class SqlBoxUtils {
	private static boolean printWarningIfEntityIsNotActiveRecord = true;

	/**
	 * Store boxes binded to entities in a threadLocal Map
	 */
	private static ThreadLocal<Map<Object, SqlBox>> boxCache = new ThreadLocal<Map<Object, SqlBox>>() {
		@Override
		protected Map<Object, SqlBox> initialValue() {
			return new HashMap<Object, SqlBox>();
		}
	};

	private static void logoutEntityHaveNoBoxFieldWarning(Class<?> clazz) {
		printWarningIfEntityIsNotActiveRecord = false; // only print at first
														// time
		SqlBoxContext.log.warn("For entity class '" + clazz.getName()
				+ "', suggest extends from ActiveRecord or put a \"SqlBox box\" field to improve performacne.");
	}

	private static void bindNonActiveRecordBox(Object entity, SqlBox box) {
		Field boxField = ClassCacheUtils.getBoxField(entity.getClass());
		if (boxField != null)
			ReflectionUtils.setField(boxField, entity, box);
		else {
			if (printWarningIfEntityIsNotActiveRecord)
				logoutEntityHaveNoBoxFieldWarning(entity.getClass());
			boxCache.get().put(entity, box);
		}
	}

	private static SqlBox findNonActiveRecordBox(Object entity) {
		Field boxField = ClassCacheUtils.getBoxField(entity.getClass());
		if (boxField != null)
			return (SqlBox) ReflectionUtils.getField(boxField, entity);
		else {
			if (printWarningIfEntityIsNotActiveRecord)
				logoutEntityHaveNoBoxFieldWarning(entity.getClass());
			return boxCache.get().get(entity);
		}
	}

	private static void unbindNonActiveRecordBox(Object entity) {
		Field boxField = ClassCacheUtils.getBoxField(entity.getClass());
		if (boxField != null)
			ReflectionUtils.setField(boxField, entity, null);
		else {
			SqlBox box = boxCache.get().get(entity);
			if (box != null)
				boxCache.get().remove(entity);
		}
	}

	/**
	 * Get a SqlBox instance binded for a entity bean, if no, create a new one
	 * and bind to entity
	 */
	public static SqlBox getBindedBox(Object entity) {
		if (entity == null)
			throw new SqlBoxException("Can not find SqlBox for null entity");
		else {
			if (entity instanceof ActiveRecordSupport)
				return ((ActiveRecordSupport) entity).bindedBox();
			else
				return findNonActiveRecordBox(entity);
		}
	}

	/**
	 * Find a binded SqlBox for a bean, if no binded SqlBox found, create a new
	 * one based on SqlBoxContext.defaultContext and bind it to bean
	 */
	public static SqlBox findBox(Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = getBindedBox(entity);
		if (box != null)
			return box;
		return SqlBoxUtils.findAndBindSqlBox(SqlBoxContext.defaultContext, entity);
	}

	/**
	 * Unbind a bean's box
	 */
	public static void unbindBox(Object entity) {
		if (entity == null)
			throw new SqlBoxException("Unbind box error, entity can not be null");
		if (entity instanceof ActiveRecordSupport) {
			ActiveRecordSupport ac = (ActiveRecordSupport) entity;
			ac.unbindBox();
		} else
			unbindNonActiveRecordBox(entity);
	}

	/**
	 * Bind a box to a bean, if box has no SqlBoxContext, use givenSqlBoxContext
	 */
	public static void bindBoxToBean(SqlBox box, Object entity) {
		if (entity == null)
			throw new SqlBoxException("Bind box error, entity can not be null");
		if (box == null)
			throw new SqlBoxException("Bind box error, box can not be null"); 
		if (entity instanceof ActiveRecord)
			((ActiveRecord) entity).bindBox(box);
		else if (entity instanceof ActiveRecordSupport)
			((ActiveRecordSupport) entity).bindBox(box);
		else
			bindNonActiveRecordBox(entity, box);
	}

	/**
	 * Find SqlBox of entity, if not found, create a new one based on given
	 * SqlBoxContext and bind it to entity
	 */
	public static SqlBox findAndBindSqlBox(SqlBoxContext ctx, Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = SqlBoxUtils.getBindedBox(entity);
		if (box != null)
			return box;
		box = SqlBoxUtils.createSqlBox(ctx, entity.getClass());
		box.setContext(ctx);
		SqlBoxUtils.bindBoxToBean(box, entity);
		return box;
	}

	/**
	 * Create a SqlBox by given entity or entityClass
	 */
	public static SqlBox createSqlBox(SqlBoxContext ctx, Class<?> entityOrBoxClass) {
		Class<?> boxClass = null;
		if (entityOrBoxClass == null)
			throw new SqlBoxException("Bean Or SqlBox class can not be null");
		if (SqlBox.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = ClassCacheUtils.checkClassExist(entityOrBoxClass.getName() + SqlBoxContext.sqlBoxClassSuffix);
		if (boxClass == null)
			boxClass = ClassCacheUtils.checkClassExist(entityOrBoxClass.getName() + "$"
					+ entityOrBoxClass.getSimpleName() + SqlBoxContext.sqlBoxClassSuffix);
		if (boxClass != null && !SqlBox.class.isAssignableFrom((Class<?>) boxClass))
			boxClass = null;
		SqlBox box = null;

		if (boxClass == null) {
			box = new SqlBox();
			box.setTableModel(ModelUtils.oneEntity2Model(entityOrBoxClass));
			box.setEntityClass(entityOrBoxClass);
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				TableModel model = box.getTableModel();
				if (model == null) {
					model = ModelUtils.oneEntity2Model(entityOrBoxClass);
					box.setTableModel(model);
				}
				Method configMethod = null;
				try {// NOSONAR
					configMethod = boxClass.getMethod("config", TableModel.class);
				} catch (Exception e) {// NOSONAR
				}
				if (configMethod != null)
					configMethod.invoke(box, model);
			} catch (Exception e) {
				throw new SqlBoxException("Can not create SqlBox instance: " + entityOrBoxClass, e);
			}
		}
		if (box.getContext() == null)
			box.setContext(ctx);
		return box;
	}

}
