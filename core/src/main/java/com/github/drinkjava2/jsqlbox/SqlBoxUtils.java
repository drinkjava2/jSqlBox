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
import java.util.WeakHashMap;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * SqlBoxUtils is utility class to bind SqlBox instance to a entity bean, there
 * are 2 different ways to bind:
 * 
 * <pre>
 * 1. For instance implemented ActiveRecordSupport (by developer), bind SqlBox buy call its bindBox() method
 * 
 * 2. For Entity without box field, is a POJO, will use a threadLocal WeakHashMap cache to store SqlBox instance, and make the key
 *    point to POJO, value point to SqlBox instance.
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class SqlBoxUtils {// NOSONAR

	/**
	 * Store boxes binded to entities in a threadLocal WeakHashMap, after entity no
	 * reference, SqlBox will be also be garage collected.
	 */
	public static final ThreadLocal<WeakHashMap<Object, SqlBox>> boxCache = new ThreadLocal<WeakHashMap<Object, SqlBox>>() {
		@Override
		protected WeakHashMap<Object, SqlBox> initialValue() {
			return new WeakHashMap<Object, SqlBox>();
		}
	};

	/** As method name said */
	public static void bindBoxToPOJO(Object pojo, SqlBox box) {
		boxCache.get().put(pojo, box);
	}

	/** As method name said */
	public static SqlBox findBoxOfPOJO(Object entity) {
		return boxCache.get().get(entity);
	}

	/** As method name said */
	public static void unbindBoxOfPOJO(Object entity) {
		SqlBox box = boxCache.get().get(entity);
		if (box != null)
			boxCache.get().remove(entity);
	}

	/**
	 * Get a SqlBox instance binded for a entity bean, if no, create a new one and
	 * bind to entity
	 */
	public static SqlBox getBindedBox(Object entity) {
		if (entity == null)
			throw new SqlBoxException("Can not find SqlBox for null entity");
		else {
			if (entity instanceof ActiveRecordSupport)
				return ((ActiveRecordSupport) entity).bindedBox();
			else
				return findBoxOfPOJO(entity);
		}
	}

	/**
	 * Find a binded SqlBox for a bean, if no binded SqlBox found, create a new one
	 * based on SqlBoxContext.defaultContext and bind it to bean
	 */
	public static SqlBox findBox(Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = getBindedBox(entity);
		if (box != null)
			return box;
		return SqlBoxUtils.findAndBindSqlBox(SqlBoxContext.getGlobalSqlBoxContext(), entity);
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
			unbindBoxOfPOJO(entity);
	}

	/**
	 * Bind a box to a bean, if box has no SqlBoxContext, use givenSqlBoxContext
	 */
	public static void bindBoxToBean(SqlBox box, Object entity) {
		if (entity == null)
			throw new SqlBoxException("Bind box error, entity can not be null");
		if (box == null)
			throw new SqlBoxException("Bind box error, box can not be null");
		else if (entity instanceof ActiveRecordSupport)
			((ActiveRecordSupport) entity).bindBox(box);
		else
			bindBoxToPOJO(entity, box);
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
		SqlBoxUtils.bindBoxToBean(box, entity);
		return box;
	}

	/**
	 * Create a SqlBox by given entity or entityClass
	 */
	public static SqlBox createSqlBox(SqlBoxContext ctx, Class<?> entityOrBoxClass) {// NOSONAR
		Class<?> boxClass = null;
		if (entityOrBoxClass == null)
			throw new SqlBoxException("Bean Or SqlBox class can not be null");
		if (SqlBox.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = ClassCacheUtils.checkClassExist(entityOrBoxClass.getName() + SqlBoxContext.SQLBOX_SUFFIX);
		if (boxClass == null)
			boxClass = ClassCacheUtils.checkClassExist(
					entityOrBoxClass.getName() + "$" + entityOrBoxClass.getSimpleName() + SqlBoxContext.SQLBOX_SUFFIX);
		if (boxClass != null && !SqlBox.class.isAssignableFrom((Class<?>) boxClass))
			boxClass = null;
		SqlBox box = null;

		if (boxClass == null) {
			box = new SqlBox();
			TableModel t=TableModelUtils.entity2Model(entityOrBoxClass);//is a class 
			box.setTableModel( t); 
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				TableModel model = box.getTableModel();
				if (model == null) {
					model = TableModelUtils.entity2Model(entityOrBoxClass); 
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
