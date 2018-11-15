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
package com.github.drinkjava2.jdialects;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;

/**
 * The tool to convert entity classes to TableModels
 * 
 * @author Yong Zhu
 * @since 1.0.6
 */
@SuppressWarnings("all")
public abstract class TableModelUtilsOfEntity {// NOSONAR

	public static Map<Class<?>, TableModel> globalTableModelCache = new ConcurrentHashMap<Class<?>, TableModel>();

	private static boolean matchNameCheck(String annotationName, String cName) {
		if (("javax.persistence." + annotationName).equals(cName))
			return true;
		if (("com.github.drinkjava2.jdialects.annotation.jpa." + annotationName).equals(cName))
			return true;
		if (("com.github.drinkjava2.jdialects.annotation.jdia." + annotationName).equals(cName))
			return true;
		for (int i = 0; i <= 9; i++) {
			if (("com.github.drinkjava2.jdialects.annotation.jdia." + annotationName + i).equals(cName))
				return true;
		}
		return false;
	}

	/** Get all entity annotations started with annotationName */
	private static List<Map<String, Object>> getEntityAnnos(Object targetClass, String annotationName) {
		Annotation[] anno = null;
		if (targetClass instanceof Field)
			anno = ((Field) targetClass).getAnnotations();
		else
			anno = ((Class<?>) targetClass).getAnnotations();
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		for (Annotation annotation : anno) {
			Class<? extends Annotation> type = annotation.annotationType();
			String cName = type.getName();
			if (matchNameCheck(annotationName, cName)) {
				l.add(changeAnnotationValuesToMap(annotation, type));
			}
		}
		return l;
	}

	private static Map<String, Object> getFirstEntityAnno(Object targetClass, String annotationName) {
		Annotation[] anno = null;
		if (targetClass instanceof Field)
			anno = ((Field) targetClass).getAnnotations();
		else
			anno = ((Class<?>) targetClass).getAnnotations();
		for (Annotation annotation : anno) {
			Class<? extends Annotation> type = annotation.annotationType();
			String cName = type.getName();
			if (matchNameCheck(annotationName, cName))
				return changeAnnotationValuesToMap(annotation, type);
		}
		return new HashMap<String, Object>();
	}

	private static boolean existEntityAnno(Object targetClass, String annotationName) {
		Map<String, Object> annotion = getFirstEntityAnno(targetClass, annotationName);
		return annotion.size() == 1;
	}

	/** Change Annotation fields values into a Map */
	private static Map<String, Object> changeAnnotationValuesToMap(Annotation annotation,
			Class<? extends Annotation> type) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("AnnotationExist", true);
		for (Method method : type.getDeclaredMethods())
			try {
				result.put(method.getName(), method.invoke(annotation, (Object[]) null));
			} catch (Exception e) {// NOSONAR
			}
		return result;
	}

	/**
	 * Convert entity class to a read-only TableModel instance
	 */
	public static TableModel entity2ReadOnlyModel(Class<?> entityClass) {
		DialectException.assureNotNull(entityClass, "Entity class can not be null");
		TableModel model = globalTableModelCache.get(entityClass);
		if (model != null)
			return model;
		model = entity2ModelWithConfig(entityClass);
		model.setReadOnly(true);
		globalTableModelCache.put(entityClass, model);
		return model;
	}

	/** Convert entity classes to a read-only TableModel instances */
	public static TableModel[] entity2ReadOnlyModel(Class<?>... entityClasses) {
		List<TableModel> l = new ArrayList<TableModel>();
		for (Class<?> clazz : entityClasses) {
			l.add(entity2ReadOnlyModel(clazz));
		}
		return l.toArray(new TableModel[l.size()]);
	}

	/**
	 * Convert entity class to a Editable TableModel instance
	 */
	public static TableModel entity2EditableModel(Class<?> entityClass) {
		DialectException.assureNotNull(entityClass, "Entity class can not be null");
		TableModel model = globalTableModelCache.get(entityClass);
		if (model != null)
			return model.newCopy();
		model = entity2ModelWithConfig(entityClass);
		model.setReadOnly(true);
		globalTableModelCache.put(entityClass, model);
		return model.newCopy();
	}

	/** Convert entity classes to a editable TableModel instances */
	public static TableModel[] entity2EditableModels(Class<?>... entityClasses) {
		List<TableModel> l = new ArrayList<TableModel>();
		for (Class<?> clazz : entityClasses) {
			l.add(entity2EditableModel(clazz));
		}
		return l.toArray(new TableModel[l.size()]);
	}

	/**
	 * Convert a Java entity class or JPA annotated entity classes to "TableModel"
	 * Object, ignore config method
	 */
	private static TableModel entity2ModelIgnoreConfigMethod(Class<?> entityClass) {
		DialectException.assureNotNull(entityClass, "entity2Model method does not accept a null class");

		// Entity
		String tableName = null;
		Map<String, Object> entityMap = getFirstEntityAnno(entityClass, "Entity");
		tableName = (String) entityMap.get("name");

		// Table
		Map<String, Object> tableMap = getFirstEntityAnno(entityClass, "Table");
		if (!StrUtils.isEmpty(tableMap.get("name")))
			tableName = (String) tableMap.get("name");
		if (StrUtils.isEmpty(tableName))
			tableName = entityClass.getSimpleName();
		TableModel model = new TableModel(tableName); // Build the tableModel
		model.setEntityClass(entityClass);
		if (!tableMap.isEmpty()) {
			// Index
			Annotation[] indexes = (Annotation[]) tableMap.get("indexes");
			if (indexes != null && indexes.length > 0)
				for (Annotation anno : indexes) {
					Map<String, Object> mp = changeAnnotationValuesToMap(anno, anno.annotationType());
					String columnListString = (String) mp.get("columnList");
					String[] columns;
					if (columnListString.indexOf(',') >= 0)
						columns = columnListString.split(",");
					else
						columns = new String[] { columnListString };
					if (columns.length > 0)
						model.index((String) mp.get("name")).columns(columns).setUnique((Boolean) mp.get("unique"));
				}

			// Unique
			Annotation[] uniques = (Annotation[]) tableMap.get("uniqueConstraints");
			if (uniques != null && uniques.length > 0)
				for (Annotation anno : uniques) {
					Map<String, Object> mp = changeAnnotationValuesToMap(anno, anno.annotationType());
					String[] columnNames = (String[]) mp.get("columnNames");
					if (columnNames != null && columnNames.length > 0)
						model.unique((String) mp.get("name")).columns(columnNames);
				}
		}

		// SequenceGenerator
		Map<String, Object> seqMap = getFirstEntityAnno(entityClass, "SequenceGenerator");
		if (!seqMap.isEmpty()) {
			model.sequenceGenerator((String) seqMap.get("name"), (String) seqMap.get("sequenceName"),
					(Integer) seqMap.get("initialValue"), (Integer) seqMap.get("allocationSize"));
		}

		// TableGenerator
		Map<String, Object> tableGenMap = getFirstEntityAnno(entityClass, "TableGenerator");
		if (!tableGenMap.isEmpty()) {
			model.tableGenerator((String) tableGenMap.get("name"), (String) tableGenMap.get("table"),
					(String) tableGenMap.get("pkColumnName"), (String) tableGenMap.get("valueColumnName"),
					(String) tableGenMap.get("pkColumnValue"), (Integer) tableGenMap.get("initialValue"),
					(Integer) tableGenMap.get("allocationSize"));
		}

		// UUIDAny
		Map<String, Object> uuidAnyMp = getFirstEntityAnno(entityClass, "UUIDAny");
		if (!uuidAnyMp.isEmpty()) {
			model.uuidAny((String) uuidAnyMp.get("name"), (Integer) uuidAnyMp.get("length"));
		}

		// FKey
		List<Map<String, Object>> fkeys = getEntityAnnos(entityClass, "FKey");
		for (Map<String, Object> map : fkeys) {
			Boolean ddl = (Boolean) map.get("ddl");
			if (ddl == null)
				ddl = true;
			model.fkey((String) map.get("name")).columns((String[]) map.get("columns")).refs((String[]) map.get("refs"))
					.ddl(ddl);
		}

		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(entityClass);
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			DialectException.throwEX("entity2Model can not get bean info", e);
		}

		for (PropertyDescriptor pd : pds) {
			String entityfieldName = pd.getName();
			if ("class".equals(entityfieldName) || "simpleName".equals(entityfieldName)
					|| "canonicalName".equals(entityfieldName))
				continue;
			Class<?> propertyClass = pd.getPropertyType();

			Field field = ReflectionUtils.findField(entityClass, entityfieldName);
			if (field == null)
				continue;

			if (!getFirstEntityAnno(field, "Transient").isEmpty() || !TypeUtils.canMapToSqlType(propertyClass)) {
				ColumnModel col = new ColumnModel(entityfieldName);
				col.setColumnType(TypeUtils.toType(propertyClass));
				col.setLengths(new Integer[] { 255, 0, 0 });
				col.setTransientable(true);
				col.setEntityField(entityfieldName);
				col.setTableModel(model);
				model.addColumn(col);
			} else {

				// SequenceGenerator
				Map<String, Object> map = getFirstEntityAnno(field, "SequenceGenerator");
				if (!map.isEmpty()) {
					model.sequenceGenerator((String) map.get("name"), (String) map.get("sequenceName"),
							(Integer) map.get("initialValue"), (Integer) map.get("allocationSize"));
				}

				// TableGenerator
				map = getFirstEntityAnno(field, "TableGenerator");
				if (!map.isEmpty()) {
					model.tableGenerator((String) map.get("name"), (String) map.get("table"),
							(String) map.get("pkColumnName"), (String) map.get("valueColumnName"),
							(String) map.get("pkColumnValue"), (Integer) map.get("initialValue"),
							(Integer) map.get("allocationSize"));
				}

				// UUIDAny
				map = getFirstEntityAnno(field, "UUIDAny");
				if (!map.isEmpty()) {
					model.uuidAny((String) map.get("name"), (Integer) map.get("length"));
				}

				ColumnModel col = new ColumnModel(entityfieldName);
				col.entityField(entityfieldName);
				// Column
				Map<String, Object> colMap = getFirstEntityAnno(field, "Column");
				if (!colMap.isEmpty()) {
					if (!(Boolean) colMap.get("nullable"))
						col.setNullable(false);
					if (!StrUtils.isEmpty(colMap.get("name")))
						col.setColumnName((String) colMap.get("name"));
					col.setLength((Integer) colMap.get("length"));
					col.setPrecision((Integer) colMap.get("precision"));
					col.setScale((Integer) colMap.get("scale"));
					col.setLengths(new Integer[] { col.getLength(), col.getPrecision(), col.getScale() });
					if (!StrUtils.isEmpty(colMap.get("columnDefinition")))
						col.setColumnType(TypeUtils.toType((String) colMap.get("columnDefinition")));
					else
						col.setColumnType(TypeUtils.toType(propertyClass));
					col.setInsertable((Boolean) colMap.get("insertable"));
					col.setUpdatable((Boolean) colMap.get("updatable"));
				} else {
					col.setColumnType(TypeUtils.toType(propertyClass));
					col.setLengths(new Integer[] { 255, 0, 0 });
				}

				// Id
				if (!getFirstEntityAnno(field, "Id").isEmpty() || !getFirstEntityAnno(field, "PKey").isEmpty())
					col.pkey();

				// Is a ShardTable column?
				Map<String, Object> shardTableMap = getFirstEntityAnno(field, "ShardTable");
				if (!shardTableMap.isEmpty())
					col.shardTable((String[]) shardTableMap.get("value"));

				// Is ShardDatabase?
				Map<String, Object> shardDatabaseMap = getFirstEntityAnno(field, "ShardDatabase");
				if (!shardDatabaseMap.isEmpty())
					col.shardDatabase((String[]) shardDatabaseMap.get("value"));

				col.setEntityField(entityfieldName);
				col.setTableModel(model);
				// col will also set TableModel field point to its owner
				model.addColumn(col);

				// shortcut Id generator annotations
				if (existEntityAnno(field, "AutoId"))
					col.autoId();
				if (existEntityAnno(field, "IdentityId"))
					col.identityId();
				if (existEntityAnno(field, "TimeStampId"))
					col.timeStampId();
				if (existEntityAnno(field, "UUID25"))
					col.uuid25();
				if (existEntityAnno(field, "UUID32"))
					col.uuid32();
				if (existEntityAnno(field, "UUID36"))
					col.uuid36();
				if (existEntityAnno(field, "Snowflake"))
					col.snowflake();

				// GeneratedValue
				Map<String, Object> gvMap = getFirstEntityAnno(field, "GeneratedValue");
				if (!gvMap.isEmpty()) {
					Object strategy = gvMap.get("strategy");
					if (strategy != null) {
						String strategyStr = strategy.toString();

						if ("AUTO".equals(strategyStr))
							col.autoId();
						else if ("IDENTITY".equals(strategyStr))
							col.identityId();
						else if ("UUID25".equals(strategyStr))
							col.uuid25();
						else if ("UUID32".equals(strategyStr))
							col.uuid32();
						else if ("UUID36".equals(strategyStr))
							col.uuid36();
						else if ("TIMESTAMP".equals(strategyStr))
							col.timeStampId();
						else {
							String generator = (String) gvMap.get("generator");
							if (StrUtils.isEmpty(generator))
								throw new DialectException(
										"GeneratedValue strategy '" + strategyStr + "' can not find a generator");
							col.idGenerator(generator);
						}
					}
				}

				// SingleFKey is a shortcut format of FKey, only for 1
				// column
				Map<String, Object> refMap = getFirstEntityAnno(field, "SingleFKey");
				if (!refMap.isEmpty()) {
					Boolean ddl = (Boolean) refMap.get("ddl");
					if (ddl == null)
						ddl = true;
					model.fkey((String) refMap.get("name")).columns(col.getColumnName())
							.refs((String[]) refMap.get("refs")).ddl(ddl);
				}

				// SingleIndex is a ShortCut format of Index, only for 1
				// column
				Map<String, Object> idxMap = getFirstEntityAnno(field, "SingleIndex");
				if (!idxMap.isEmpty())
					model.index((String) idxMap.get("name")).columns(col.getColumnName());

				// SingleUnique is a ShortCut format of Unique, only for 1
				// column
				Map<String, Object> uniMap = getFirstEntityAnno(field, "SingleUnique");
				if (!uniMap.isEmpty())
					model.unique((String) uniMap.get("name")).columns(col.getColumnName());
			}

		} // End of columns loop
		return model;
	}

	/**
	 * Convert entity class to a Editable TableModel instance , if this class has a
	 * "config(TableModel tableModel)" method, will also call it
	 */
	private static TableModel entity2ModelWithConfig(Class<?> entityClass) {
		TableModel model;
		model = entity2ModelIgnoreConfigMethod(entityClass);
		Method method = null;
		try {
			method = entityClass.getMethod("config", TableModel.class);
		} catch (Exception e) {// NOSONAR
		}
		if (method != null)
			try {
				method.invoke(null, model);
			} catch (Exception e) {
				throw new DialectException(e);
			}
		if (model == null)
			throw new DialectException("Can not create TableModel for entityClass " + entityClass);
		return model;
	}

}
