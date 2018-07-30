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
import java.util.ArrayList;
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
 * <pre>
 * Entity Id Utils, an Entity ID can be:
 * 
 * 1. MAP (For compound id)
 * 2. Basic Java Objects (For single id)
 * 3. Instance of Entity Class, which implements ActiveRecordSupport interface or have @Entity annotation (for compound or single id)
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
	public static Object buildEntityIdFromOneRow(String[] titles, Object[] oneRow, TableModel model,
			String alias) {// NOSONAR
		int pkeyCount = model.getPKeyCount();
		if (pkeyCount == 0)
			throw new SqlBoxException(" No Pkey setting for '" + model.getTableName() + "'");
		ColumnModel firstPkeyCol = model.getFirstPKeyColumn();
		// DbUtils don't care UP/LOW case
		
		Object firstPKeyValue =null; 
	       String keColName=new StringBuilder(alias).append("_").append(firstPkeyCol.getColumnName()).toString();
	       for (int i = 0; i < titles.length; i++) {
			if(titles[i].equalsIgnoreCase(keColName))
				firstPKeyValue=oneRow[i];
		} 
		if (firstPKeyValue == null)
			return null;// 
		
		if (pkeyCount == 1)
			return firstPKeyValue;
		List<ColumnModel> l = model.getPKeyColsSortByColumnName();
		StringBuilder sb = new StringBuilder();
		for (ColumnModel col : l) {
			if (sb.length() > 0)
				sb.append(COMPOUND_ID_SEPARATOR);
			
			Object value =null; 
		         keColName=new StringBuilder(alias).append("_").append(col.getColumnName()).toString();
		       for (int i = 0; i < titles.length; i++) {
				if(titles[i].equalsIgnoreCase(keColName))
					value=oneRow[i];
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
				sb.append(COMPOUND_ID_SEPARATOR);
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
		if (entityId instanceof Map) {
			Map<String, Object> idMap = (Map<String, Object>) entityId;
			for (Entry<String, Object> item : idMap.entrySet())
				ClassCacheUtils.writeValueToBeanField(bean, item.getKey(), item.getValue());
		} else {
			if (TypeUtils.canMapToSqlType(entityId.getClass())) {
				if (model.getPKeyCount() == 0)
					throw new SqlBoxException("No PKey column found for '" + bean.getClass() + "'");
				if (model.getPKeyCount() != 1)
					throw new SqlBoxException("Not give enough PKey column value for '" + bean.getClass() + "'");
				ColumnModel col = model.getFirstPKeyColumn();
				ClassCacheUtils.writeValueToBeanField(bean, col.getEntityField(), entityId);
				return bean;
			}

			boolean isEntity = false;
			if (entityId instanceof ActiveRecordSupport)
				isEntity = true;
			else {
				Annotation[] anno = entityId.getClass().getAnnotations();
				for (Annotation annotation : anno)
					if (annotation.annotationType().getName().endsWith(".Entity")) {
						isEntity = true;
						break;
					}
			}
			if (!isEntity)
				throw new SqlBoxException(
						"Can not determine entityId type, if it's a entity, put @Entity annotation on it");
			List<ColumnModel> cols = model.getPKeyColsSortByColumnName();
			for (ColumnModel col : cols) {
				Object value = ClassCacheUtils.readValueFromBeanField(entityId, col.getEntityField());
				ClassCacheUtils.writeValueToBeanField(bean, col.getEntityField(), value);
			}
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
	public static Object readFeidlValueFromEntityId(Object entityId, TableModel model, String entityFieldName) {
		SqlBoxException.assureNotNull(entityId, "entityId can not be null.");
		if (entityId instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> idMap = (Map<String, Object>) entityId;
			return idMap.get(entityFieldName);
		} else {
			if (TypeUtils.canMapToSqlType(entityId.getClass()))
				return entityId;

			boolean isEntity = false;
			if (entityId instanceof ActiveRecordSupport)
				isEntity = true;
			else {
				Annotation[] anno = entityId.getClass().getAnnotations();
				for (Annotation annotation : anno)
					if (annotation.annotationType().getName().endsWith(".Entity")) {
						isEntity = true;
						break;
					}
			}
			if (!isEntity)
				throw new SqlBoxException(
						"Can not determine entityId type, if it's a entity, put @Entity annotation on it");
			return ClassCacheUtils.readValueFromBeanField(entityId, entityFieldName);
		}
	}

	/**
	 * Get the real id list only for one java field, because Iterable ids may be
	 * compound id
	 */
	public static List<Object> getOnlyOneFieldFromIds(Iterable<?> ids, TableModel model, String entityFieldName) {
		List<Object> result = new ArrayList<Object>();
		for (Object entityId : ids) {
			Object realEntityId = EntityIdUtils.readFeidlValueFromEntityId(entityId, model, entityFieldName);
			result.add(realEntityId);
		}
		return result;
	}

}
