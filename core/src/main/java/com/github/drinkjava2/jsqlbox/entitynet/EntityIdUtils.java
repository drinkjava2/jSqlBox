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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TypeUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecordSupport;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * EntityNet Utils
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class EntityIdUtils {// NOSONAR

	public static Object buildEntityIdFromOneRow(Map<String, Object> oneRow, TableModel model) {
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new SqlBoxException("No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case
		Object firstPKeyValue = oneRow
				.get(new StringBuilder(model.getAlias()).append("_").append(firstPkeyCol.getColumnName()).toString());
		if (firstPKeyValue == null)
			return null;// Single or Compound Pkey not found in oneRow
		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColsSortByColumnName();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(EntityNet.COMPOUND_VALUE_SEPARATOR);
			Object value = oneRow
					.get(new StringBuilder(model.getAlias()).append("_").append(col.getColumnName()).toString());
			if (value == null)
				return null;
			sb.append(value);
		}
		return sb.toString();
	}

	public static Object buildEntityIdFromEntity(Object entity, TableModel model) {
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new SqlBoxException("No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case
		Object firstPKeyValue = ClassCacheUtils.readValueFromBeanField(entity, firstPkeyCol.getEntityField());
		if (firstPKeyValue == null)
			return null;// Single or Compound Pkey not found in entity
		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColsSortByColumnName();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(EntityNet.COMPOUND_VALUE_SEPARATOR);
			Object value = ClassCacheUtils.readValueFromBeanField(entity, col.getEntityField());
			if (value == null)
				return null;
			sb.append(value);
		}
		return sb.toString();
	}

	public static Object buildEntityIdFromMap(Map<String, Object> map, TableModel model) {
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new SqlBoxException("No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case
		Object firstPKeyValue = map.get(firstPkeyCol.getEntityField());
		if (firstPKeyValue == null)
			return null;// Single or Compound Pkey not found in oneRow
		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColsSortByColumnName();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(EntityNet.COMPOUND_VALUE_SEPARATOR);
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
		SqlBoxException.assureNotNull(entityId, "entityId can not be null.");
		if (entityId instanceof Map) {// map
			Map<String, Object> mp = (Map<String, Object>) entityId;
			return buildEntityIdFromMap(mp, model);
		} else {
			if (TypeUtils.canMapToSqlType(entityId.getClass()))
				return entityId;

			if (entityId instanceof ActiveRecordSupport)// ActiveRecord or ActiveEntity
				return buildEntityIdFromEntity(entityId, model);

			Annotation[] anno = entityId.getClass().getAnnotations();
			for (Annotation annotation : anno)
				if (annotation.annotationType().getName().endsWith(".Entity"))// TODO:unit test
					return buildEntityIdFromEntity(entityId, model);
			throw new SqlBoxException(
					"Can not determine entityId type, if it's a entity, put @Entity annotation on it");
		}
	}

	/**
	 * Put one id value into a entity bean, or put values according a
	 * map<String,Object>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T setEntityIdValues(T bean, Object entityId, TableModel model) {
		SqlBoxException.assureNotNull(entityId, "entityId can not be null.");
		if (entityId instanceof Map) { // MAP for compound key
			Map<String, Object> idMap = (Map<String, Object>) entityId;
			for (Entry<String, Object> item : idMap.entrySet())
				ClassCacheUtils.writeValueToBeanField(bean, item.getKey(), item.getValue());
		} else {
			if (TypeUtils.canMapToSqlType(entityId.getClass())) {
				if (model.getPKeyCount() != 1)
					throw new SqlBoxException("More than 1 PKey column but only give 1 primary type parameter");
				ColumnModel col = model.getFirstPKeyColumn();
				ClassCacheUtils.writeValueToBeanField(bean, col.getEntityField(), entityId);
				return bean;
			}

			boolean isEntity = false;
			if (entityId instanceof ActiveRecordSupport)
				isEntity = true;
			Annotation[] anno = entityId.getClass().getAnnotations();
			for (Annotation annotation : anno)
				if (annotation.annotationType().getName().endsWith(".Entity")) {
					isEntity = true;
					break;
				}
			if (!isEntity)
				throw new SqlBoxException(
						"Can not determine entityId type, if it's a entity, put @Entity annotation on it");
			List<ColumnModel> Cols = model.getPKeyColsSortByColumnName();
			for (ColumnModel col : Cols) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityId, col.getEntityField());
				ClassCacheUtils.writeValueToBeanField(bean, col.getEntityField(), value);
			}
		}
		return bean;
	}

}
