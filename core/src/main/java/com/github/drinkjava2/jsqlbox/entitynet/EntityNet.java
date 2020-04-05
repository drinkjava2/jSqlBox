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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.DbException;
import com.github.drinkjava2.jsqlbox.TailType;

/**
 * EntityNet is Entity net, after created by using EntityNetHandler, can use
 * pickXxxx methods to pick entity list/set/map from it, and also can use
 * findRelatedXxx methods to search items inside of it, no need send SQL to DB
 * again
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class EntityNet {

	/** Models, Map<alias, tableModels> */
	private Map<String, TableModel> models = new LinkedHashMap<String, TableModel>();

	private List<String[]> givesList = new ArrayList<String[]>();

	/**
	 * The row Data loaded from database, List<Map<colName, colValue>> or <"u",
	 * entity> or <"#u",entityId>
	 */
	// private List<Map<String, Object>> rowData = new ArrayList<Map<String,
	// Object>>();

	/** The body of entity net, Map<alias, Map<entityId, entity>> */
	private Map<Class<?>, LinkedHashMap<Object, Object>> body = new HashMap<Class<?>, LinkedHashMap<Object, Object>>();

	protected void core__________________________() {// NOSONAR
	}

	/** Config, parameters can be entity or entity class or TableModel */
	public EntityNet configFromPreparedSQL(PreparedSQL ps) {
		DbException.assureNotNull(ps.getModels(), "No tableModel setting found.");
		for (int i = 0; i < ps.getModels().length; i++)
			models.put(ps.getAliases()[i], (TableModel) ps.getModels()[i]);
		addGivesList(ps.getGivesList());
		return this;
	}

	/** Give a's value to b's aField */
	public EntityNet giveBoth(String a, String b) {
		give(a, b);
		give(b, a);
		return this;
	}

	/** Give a's value to b's aField */
	public EntityNet give(String a, String b) {
		TableModel aModel = models.get(a);
		DbException.assureNotNull(aModel, "Not found config for alias '" + a + "'");
		DbException.assureNotNull(aModel.getEntityClass(), "'entityClass' property not set for model " + aModel);
		String fieldName = StrUtils.toLowerCaseFirstOne(aModel.getEntityClass().getSimpleName());

		TableModel bModel = models.get(b);
		DbException.assureNotNull(bModel, "Not found config for alias '" + a + "'");
		DbException.assureNotNull(bModel.getEntityClass(), "'entityClass' property not set for model " + bModel);

		Method readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName);
		if (readMethod != null)
			give(a, b, StrUtils.toLowerCaseFirstOne(fieldName));

		readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName + "List");
		if (readMethod != null)
			give(a, b, fieldName + "List");

		readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName + "Set");
		if (readMethod != null)
			give(a, b, fieldName + "Set");

		readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName + "Map");
		if (readMethod != null)
			give(a, b, fieldName + "Map");
		return this;
	}

	/** Give a's value to b's someField */
	public EntityNet give(String a, String b, String someField) {
		DbException.assureNotEmpty(someField, "give field parameter can not be empty for '" + b + "'");
		givesList.add(new String[] { a, b, someField });
		return this;
	}

	/**
	 * Join a titleArrayList into current EntityNet, titleArrayList is a
	 * List<Object[]> structure, first row is titles, not data
	 */
	public EntityNet joinTitleArrayList(List<Object[]> titleArrayList) {
		String[] titles = (String[]) titleArrayList.get(0);
		int i = 0;
		for (Object[] oneRow : titleArrayList)
			if (i++ != 0)
				translateAndGive(titles, oneRow);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> pickEntityList(String alias) {
		return (List<T>) pickEntityList(models.get(alias).getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> pickEntitySet(String alias) {
		return (Set<T>) pickEntitySet(models.get(alias).getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public <T> Map<Object, T> pickEntityMap(String alias) {
		return (Map<Object, T>) pickEntityMap(models.get(alias).getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public <T> T pickOneEntity(String alias, Object entityId) {
		return (T) pickOneEntity(models.get(alias).getEntityClass(), entityId);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> pickEntityList(Class<T> claz) {
		Map<Object, Object> map = body.get(claz);
		if (map == null)
			return new ArrayList<T>();
		return (List<T>) new ArrayList<Object>(map.values());
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> pickEntitySet(Class<T> claz) {
		Map<Object, Object> map = body.get(claz);
		if (map == null)
			return new LinkedHashSet<T>();
		return (Set<T>) new LinkedHashSet<Object>(map.values());
	}

	@SuppressWarnings({ "unchecked" })
	public <T> Map<Object, T> pickEntityMap(Class<T> claz) {
		return (Map<Object, T>) body.get(claz);
	}

	@SuppressWarnings("unchecked")
	public <T> T pickOneEntity(Class<T> claz, Object entityId) {
		TableModel model = null;
		for (Entry<String, TableModel> entry : models.entrySet()) {
			if (claz.equals(entry.getValue().getEntityClass())) {
				model = entry.getValue();
				break;
			}
		}
		Object realEntityId = EntityIdUtils.buildEntityIdFromUnknow(entityId, model);
		if (realEntityId == null)
			throw new DbException("Can not build entityId for '" + entityId + "'");
		Map<Object, Object> map = body.get(claz);
		if (map == null)
			return null;
		return (T) map.get(realEntityId);
	}

	/** Translate one row of map list to entity objects, put into entity net body */
	private void translateAndGive(String[] titles, Object[] oneRow) {
		Map<String, Object> oneRowEntities = new HashMap<String, Object>();
		for (Entry<String, TableModel> config : this.models.entrySet()) {
			TableModel model = config.getValue();
			String alias = config.getKey();

			// find and build entityID
			Object entityId = EntityIdUtils.buildEntityIdFromOneRow(titles, oneRow, model, alias);
			if (entityId == null)
				continue;// not found entity ID columns
			DbException.assureNotNull(model.getEntityClass());
			Object entity = getOneEntity(model.getEntityClass(), entityId);
			// create new Entity
			if (entity == null) {
				entity = createEntity(titles, oneRow, model, alias);
				this.putOneEntity(model.getEntityClass(), entityId, entity);
			} else {
				updateEntity(titles, entity, oneRow, model, alias);
			}
			oneRowEntities.put(alias, entity);
			oneRowEntities.put("#" + alias, entity);
		}
		doGive(oneRowEntities);
	}

	private static Object createEntity(String[] titles, Object[] oneRow, TableModel model, String alias) {
		Object entity;
		entity = ClassCacheUtils.createNewEntity(model.getEntityClass());
		return updateEntity(titles, entity, oneRow, model, alias);
	}

	private static Object updateEntity(String[] titles, Object entity, Object[] oneRow, TableModel model,
			String alias) {
		for (int i = 0; i < titles.length; i++) {
			String titleAlias = StrUtils.substringBefore(titles[i], "_");
			if (!alias.equalsIgnoreCase(titleAlias))
				continue;
			String colName = StrUtils.substringAfter(titles[i], "_");
			ColumnModel col = model.getColumnByColName(colName);
			if (col != null && col.getTransientable())
				continue;
			if (col == null) {
				if (entity instanceof TailType)
					((TailType) entity).tails().put(colName, oneRow[i]);
			} else
				DbContextUtils.writeValueToBeanFieldOrTail(col, entity, oneRow[i]);
		}
		return entity;
	}

	/** Give values according gives setting for oneRow */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doGive(Map<String, Object> oneRow) {// NOSONAR
		for (String[] gives : givesList) {
			String fromAlias = gives[0];
			String toAlias = gives[1];
			Object from = oneRow.get(fromAlias);
			Object to = oneRow.get(toAlias);
			String tofield = gives[2];
			DbException.assureNotEmpty(tofield);
			if (from != null && to != null) {
				TableModel toModel = models.get(toAlias);
				ColumnModel col = toModel.getColumnByFieldName(tofield);
				Method readMethod = ClassCacheUtils.getClassFieldReadMethod(toModel.getEntityClass(),
						col.getEntityField());
				Class<?> fieldType = readMethod.getReturnType();
				if (fieldType.isAssignableFrom(List.class)) {
					List list = (List) ClassCacheUtils.readValueFromBeanField(to, tofield);
					if (list == null) {
						list = new ArrayList<Object>();
						ClassCacheUtils.writeValueToBeanField(to, tofield, list);
					}
					if (!list.contains(from))
						list.add(from);
				} else if (fieldType.isAssignableFrom(Set.class)) {
					Set set = (Set) ClassCacheUtils.readValueFromBeanField(to, tofield);
					if (set == null) {
						set = new HashSet<Object>();
						ClassCacheUtils.writeValueToBeanField(to, tofield, set);
					}
					if (!set.contains(from))
						set.add(from);
				} else if (fieldType.isAssignableFrom(Map.class)) {
					Map map = (Map) ClassCacheUtils.readValueFromBeanField(to, tofield);
					if (map == null) {
						map = new HashMap();
						ClassCacheUtils.writeValueToBeanField(to, tofield, map);
					}
					Object entityId = oneRow.get("#" + fromAlias);
					DbException.assureNotNull(entityId, "Can not find entityId for '" + fromAlias + "'");
					if (!map.containsKey(entityId))
						map.put(entityId, from);
				} else {// No matter what type, give "from" value to "to"
					ClassCacheUtils.writeValueToBeanField(to, tofield, from);
				}
			}
		}
	}

	public void putOneEntity(Class<?> claz, Object entityId, Object entity) {
		LinkedHashMap<Object, Object> entityMap = body.get(claz);
		if (entityMap == null) {
			entityMap = new LinkedHashMap<Object, Object>();
			body.put(claz, entityMap);
		}
		entityMap.put(entityId, entity);
	}

	public Object getOneEntity(Class<?> claz, Object entityId) {
		Map<Object, Object> entityMap = body.get(claz);
		if (entityMap == null)
			return null;
		return entityMap.get(entityId);
	}

	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n=========givesList=========\r\n");
		for (String[] gives : givesList) {
			for (String str : gives) {
				sb.append(str + " ");
			}
			sb.append("\r\n");
		}
		sb.append("\r\n=========configs=========\r\n");
		for (TableModel tb : models.values()) {
			sb.append(tb.getDebugInfo());
		}

		sb.append("\r\n=========body=========\r\n");
		for (LinkedHashMap<Object, Object> row : body.values()) {
			sb.append(row.toString()).append("\r\n");
		}
		return sb.toString();
	}

	/** Search related entity list inside of current EntityNet */
	public <E> List<E> findRelatedList(DbContext ctx, Object entity, Object... sqlItems) {
		Set<E> resultSet = findRelatedSet(ctx, entity, sqlItems);
		return new ArrayList<E>(resultSet);
	}

	/** Search related entity set inside of current EntityNet */
	@SuppressWarnings("unchecked")
	public <E> Set<E> findRelatedSet(DbContext ctx, Object entity, Object... sqlItems) {
		TableModel[] tbModels = DbContextUtils.findAllModels(sqlItems);
		// first model is entity self, last is target model
		DbException.assureTrue(tbModels.length > 1);
		DbException.assureTrue(entity.getClass().equals(tbModels[0].getEntityClass()));
		return (Set<E>) doFindRelatedSet(0, entity, tbModels);
	}

	/** Inside of current EntityNet, search related entity Map */
	public <E> Map<Object, E> findRelatedMap(DbContext ctx, Object entity, Object... sqlItems) {
		TableModel[] tbModels = DbContextUtils.findAllModels(sqlItems);
		Set<E> resultSet = findRelatedSet(ctx, entity, sqlItems);
		Map<Object, E> resultMap = new HashMap<Object, E>();
		for (E ent : resultSet) {
			Object entityId = EntityIdUtils.buildEntityIdFromEntity(ent, tbModels[tbModels.length - 1]);
			resultMap.put(entityId, ent);
		}
		return resultMap;
	}

	private static boolean hasRelationShip(Object e1, Object e2, TableModel m1, TableModel m2) {
		List<FKeyModel> fkeys = m1.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m2.getTableName())) {// m2 is parent
				return realDoRelationCheck(e1, e2, m1, m2, fkey);
			}
		}
		fkeys = m2.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m1.getTableName())) {// m1 is parent
				return realDoRelationCheck(e2, e1, m2, m1, fkey);
			}
		}
		throw new DbException("Not found relationship(foreign key) setting between '" + m1.getEntityClass() + "' and '"
				+ m2.getEntityClass() + "'");
	}

	/**
	 * Check if 2 entities have relationShip, e1's fkey value should equal e2's ID
	 */
	private static boolean realDoRelationCheck(Object e1, Object e2, TableModel m1, TableModel m2, FKeyModel fkey) {
		int i = 0;
		for (String col : fkey.getColumnNames()) {
			String refCol = fkey.getRefTableAndColumns()[i + 1];
			ColumnModel c1 = m1.getColumnByColName(col);
			ColumnModel c2 = m2.getColumnByColName(refCol);
			Object value1 = ClassCacheUtils.readValueFromBeanField(e1, c1.getEntityField());
			Object value2 = ClassCacheUtils.readValueFromBeanField(e2, c2.getEntityField());
			if (value1 == null || value2 == null || !value1.equals(value2))
				return false;
			i++;
		}
		return true;
	}

	public Set<Object> doFindRelatedSet(int index, Object entity, TableModel[] tbModels) {
		Set<Object> result = new HashSet<Object>();
		TableModel m1 = tbModels[index]; // User or UserRole or RolePrivilege...
		TableModel m2 = tbModels[index + 1]; // Privilege
		if ((index + 2) >= tbModels.length) {
			for (Entry<Object, Object> e : body.get(m2.getEntityClass()).entrySet()) {
				if (hasRelationShip(entity, e.getValue(), m1, m2))
					result.add(e.getValue());
			}
		} else {
			Set<Object> middleEntitis = doFindRelatedSet(0, entity, new TableModel[] { m1, m2 });
			for (Object mid : middleEntitis) {
				Set<Object> targets = doFindRelatedSet(index + 1, mid, tbModels);// NOSONAR
				result.addAll(targets);
			}
		}
		return result;
	}

	protected void getterSetter__________________________() {// NOSONAR
	}

	public Map<String, TableModel> getConfigs() {
		return models;
	}

	public EntityNet setConfigs(Map<String, TableModel> configs) {
		this.models = configs;
		return this;
	}

	public List<String[]> getGivesList() {
		return givesList;
	}

	/**
	 * Add a bunch of gives, give can be 2 or 3 items String[]
	 */
	public EntityNet addGivesList(List<String[]> givesList) {
		if (givesList == null)
			return this;
		for (String[] strings : givesList) {
			if (strings == null || strings.length < 2 || strings.length > 3)
				throw new DbException("gives should have 2 or 3 parameters");
			if (strings.length == 2)
				give(strings[0], strings[1]);
			else
				give(strings[0], strings[1], strings[2]);
		}
		return this;
	}

	public EntityNet setGivesList(List<String[]> givesList) {
		this.givesList = givesList;
		return this;
	}

	public Map<Class<?>, LinkedHashMap<Object, Object>> getBody() {
		return body;
	}

	public void setBody(Map<Class<?>, LinkedHashMap<Object, Object>> body) {
		this.body = body;
	}

}
