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
package com.github.drinkjava2.jsqlbox.entitynet;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TypeUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityType;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.DbException;

/**
 * <pre>
 * Entity Id Utils, an Entity ID can be:
 * 
 * 1. A POJO Entity
 * 2. 
 * 3. Basic Java Objects (String, Integer, Long, Date, Boolean...)
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class EntityIdUtils {// NOSONAR
	/** Used to combine compound key column values into a single String */
	public static final String COMPOUND_ID_SEPARATOR = "__";

	/**
	 * Build entityId from titles, oneRow, model, alias
	 */
	public static Object buildEntityIdFromOneRow(String[] titles, Object[] oneRow, TableModel model, String alias) {// NOSONAR
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new DbException(" No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case

		Object firstPKeyValue = null;
		String keColName = new StringBuilder(alias).append("_").append(firstPkeyCol.getClearQuoteColumnName()).toString();
		for (int i = 0; i < titles.length; i++) {
			if (titles[i].equalsIgnoreCase(keColName))
				firstPKeyValue = oneRow[i];
		}
		if (firstPKeyValue == null)
			return null;//

		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColumns();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(COMPOUND_ID_SEPARATOR);

			Object value = null;
			keColName = new StringBuilder(alias).append("_").append(col.getClearQuoteColumnName()).toString();
			for (int i = 0; i < titles.length; i++) {
				if (titles[i].equalsIgnoreCase(keColName))
					value = oneRow[i];
			}
			if (value == null)
				return null;//
			sb.append(value);
		}
		return sb.toString();
	}

	public static Object buildEntityIdFromEntity(Object entity, TableModel model) {
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new DbException("No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case
		Object firstPKeyValue = DbContextUtils.readValueFromBeanFieldOrTail(firstPkeyCol, entity, false, false);
		if (firstPKeyValue == null)
			return null;// Single or Compound Pkey not found in entity
		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColumns();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(COMPOUND_ID_SEPARATOR);
			Object value = DbContextUtils.readValueFromBeanFieldOrTail(col, entity, false, false);
			if (value == null)
				return null;
			sb.append(value);
		}
		return sb.toString();
	}

	public static Object buildEntityIdFromMap(Map<String, Object> map, TableModel model) {
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new DbException("No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case
		Object firstPKeyValue = map.get(firstPkeyCol.getEntityField());
		if (firstPKeyValue == null)
			return null;// Single or Compound Pkey not found in oneRow
		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColumns();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(COMPOUND_ID_SEPARATOR);
			Object value = map.get(col.getEntityField());
			if (value == null)
				return null;
			sb.append(value);
		}
		return sb.toString();
	}

	/** Build Node id from unknown entityIdOrIdMap */
	@SuppressWarnings("all")
	public static Object buildEntityIdFromUnknow(Object entityId, TableModel model) {
		DbException.assureNotNull(entityId, "entityId can not be null.");
		if (entityId instanceof Map) {// map
			Map<String, Object> mp = (Map<String, Object>) entityId;
			return buildEntityIdFromMap(mp, model);
		} else {
			if (TypeUtils.canMapToDialectType(entityId.getClass()))
				return entityId;

			if (entityId instanceof EntityType)// ActiveRecord or ActiveEntity
				return buildEntityIdFromEntity(entityId, model);

			Annotation[] anno = entityId.getClass().getAnnotations();
			for (Annotation annotation : anno)
				if (annotation.annotationType().getName().endsWith(".Entity"))
					return buildEntityIdFromEntity(entityId, model);
			throw new DbException("Can not determine entityId type, if it's a entity, put @Entity annotation on it");
		}
	}

	/**
	 * Put one id value into a entity bean, or put values according a
	 * map<String,Object>
	 */
	public static <T> T setEntityIdValues(T bean, Object entityId, Collection<ColumnModel> cols) {
		DbException.assureNotNull(entityId, "entityId can not be null.");
		if (entityId instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> idMap = (Map<String, Object>) entityId;
			for (Entry<String, Object> item : idMap.entrySet())
				ClassCacheUtils.writeValueToBeanField(bean, item.getKey(), item.getValue());
			return bean;
		}

		if (TypeUtils.canMapToDialectType(entityId.getClass())) {
			for (ColumnModel col : cols) {
				if (!col.getTransientable() && col.getPkey()) {
					DbContextUtils.writeValueToBeanFieldOrTail(col, bean, entityId);
					return bean;
				}
			}
			throw new DbException("No primary key configuration found");
		}
		// now entityId is a entity
		for (ColumnModel col : cols) {
			if (!col.getPkey())
				continue;
			Object value = DbContextUtils.readValueFromBeanFieldOrTail(col, entityId, false, false);
			DbContextUtils.writeValueToBeanFieldOrTail(col, bean, value);
		}
		return bean;
	}

	/**
	 * Read field value from entityId:
	 * 
	 * 1) if entityId is map, get the value by use fieldName as key <br/>
	 * 2) if entityId is basic java Object, direct return it 3) if is Entity? read
	 * field value from it.
	 * 
	 */
	public static Object readFeidlValueFromEntityId(Object entityId, ColumnModel col) {
		DbException.assureNotNull(entityId, "entityId can not be null.");
		if (entityId instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> idMap = (Map<String, Object>) entityId;
			if (idMap.containsKey(col.getEntityField()))
				return idMap.get(col.getEntityField());
			else
				return idMap.get(col.getColumnName());
		}

		if (TypeUtils.canMapToDialectType(entityId.getClass()))
			return entityId;
		else
			return DbContextUtils.readValueFromBeanFieldOrTail(col, entityId, false, false);
	}

}
