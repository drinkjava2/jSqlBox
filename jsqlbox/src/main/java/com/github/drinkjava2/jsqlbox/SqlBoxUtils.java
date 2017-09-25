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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * SqlBoxUtils is help class to store some public methods concern to SqlBox
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public abstract class SqlBoxUtils {
	public static String SQLBOX_IDENTITY = "BX";

	// To check if a class exist, if exist, cache it to avoid check again
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<String, Integer>();

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

	/**
	 * Find a binded SqlBox for a bean, if no binded SqlBox found, create a new one
	 * and bind it to bean
	 */
	public static SqlBox findBox(Object entity) {
		SqlBox box = getBindedBox(entity);
		if (box != null)
			return box;
		box = findAndBuildSqlBox(entity.getClass());
		bindBoxToBean(entity, box, SqlBoxContext.defaultContext);
		return box;
	}

	/**
	 * Find and create a SqlBox instance according bean class or SqlBox Class
	 */
	public static SqlBox findAndBuildSqlBox(Class<?> entityOrBoxClass) {
		Class<?> boxClass = null;
		if (entityOrBoxClass == null)
			throw new SqlBoxException("Bean Or SqlBox class can not be null");
		if (SqlBox.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(entityOrBoxClass.getName() + SQLBOX_IDENTITY);
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(
					entityOrBoxClass.getName() + "$" + entityOrBoxClass.getSimpleName() + SQLBOX_IDENTITY);
		SqlBox box = null;
		if (boxClass == null) {
			box = new SqlBox();
			box.setTableModel(TableModel.fromPojo(entityOrBoxClass));
		} else {
			try {
				System.out.println("boxClass=" + boxClass);
				box = (SqlBox) boxClass.newInstance();
			} catch (Exception e) {
				throw new SqlBoxException("Can not create SqlBox instance: " + entityOrBoxClass, e);
			}
		}
		return box;
	}

	/** * Check class if exist */
	public static Class<?> checkSqlBoxClassExist(String className) {
		Integer i = classExistCache.get(className);
		if (i == null)
			try {
				Class<?> clazz = Class.forName(className);
				if (SqlBox.class.isAssignableFrom((Class<?>) clazz)) {
					classExistCache.put(className, 1);
					return clazz;
				}
				classExistCache.put(className, 0);
				return null;
			} catch (Exception e) {
				SqlBoxException.eatException(e);
				classExistCache.put(className, 0);
				return null;
			}
		if (1 == i) {
			try {
				return Class.forName(className);
			} catch (Exception e) {
				SqlBoxException.eatException(e);
			}
		}
		return null;
	}

	/** Bind a box to a bean, if box has no SqlBoxContext, use givenSqlBoxContext */
	public static void bindBoxToBean(Object entity, SqlBox box, SqlBoxContext givenSqlBoxContext) {
		if (entity == null)
			throw new SqlBoxException("Bind box error, entity can not be null");
		if (box == null)
			throw new SqlBoxException("Bind box error, box can not be null");
		if (box.getContext() == null)
			box.setContext(givenSqlBoxContext);
		box.setEntityBean(entity);
		boxCache.get().put(entity, box);
	}
}
