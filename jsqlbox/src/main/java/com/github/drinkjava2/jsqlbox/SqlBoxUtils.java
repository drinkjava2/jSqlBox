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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.DialectUtils;

/**
 * SqlBoxUtils is utility class store public static methods of SqlBox
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public abstract class SqlBoxUtils {
	/**
	 * Store boxes binded to entities in a threadLocal Map
	 */
	private static ThreadLocal<Map<Object, SqlBox>> boxCache = new ThreadLocal<Map<Object, SqlBox>>() {
		@Override
		protected Map<Object, SqlBox> initialValue() {
			return new HashMap<Object, SqlBox>();
		}
	};

	/**
	 * Get a box instance from thread local cache for a bean
	 */
	public static SqlBox getBindedBox(Object bean) {
		if (bean == null)
			throw new SqlBoxException("bean can not be null");
		else
			return boxCache.get().get(bean);
	}

	public static void clearSqlBoxCache() {
		boxCache.get().clear();
	}

	/**
	 * Find a binded SqlBox for a bean, if no binded SqlBox found, create a new
	 * one and bind it to bean
	 */
	public static SqlBox findBox(Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = getBindedBox(entity);
		if (box != null)
			return box;
		// if (SqlBoxContext.defaultContext==null)
		return SqlBoxUtils.findAndBindSqlBox(SqlBoxContext.defaultContext, entity);
	}

	/**
	 * Bind a box to a bean, if box has no SqlBoxContext, use givenSqlBoxContext
	 */
	public static void bindBoxToBean(SqlBox box, Object entity, SqlBoxContext givenSqlBoxContext) {
		if (entity == null)
			throw new SqlBoxException("Bind box error, entity can not be null");
		if (box == null)
			throw new SqlBoxException("Bind box error, box can not be null");
		if (box.getContext() == null)
			box.setContext(givenSqlBoxContext);
		box.setEntityBean(entity);
		boxCache.get().put(entity, box);
	}

	/**
	 * Unbind a bean's box
	 */
	public static void unbind(Object entity) {
		if (entity == null)
			throw new SqlBoxException("Unbind box error, entity can not be null");
		boxCache.get().remove(entity);
	}

	public static SqlBox findAndBindSqlBox(SqlBoxContext ctx, Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = SqlBoxUtils.getBindedBox(entity);
		if (box != null)
			return box;
		box = SqlBoxUtils.createSqlBox(ctx, entity.getClass());
		SqlBoxUtils.bindBoxToBean(box, entity, ctx);
		return box;
	}

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
			box.setTableModel(DialectUtils.pojo2Model(entityOrBoxClass));
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				TableModel model = box.getTableModel();
				if (model == null) {
					model = DialectUtils.pojo2Model(entityOrBoxClass);
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
