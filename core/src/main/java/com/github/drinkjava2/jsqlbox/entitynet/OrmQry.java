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
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * OrmQuery
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class OrmQry {
	/** Used to combine compound key column values into a single String */
	public static final String COMPOUND_VALUE_SEPARATOR = "_CpdVlSpr_";

	/** NewNet depends on SqlBoxContext */
	SqlBoxContext ctx;

	/** Models, Map<alias, tableModels> */
	private Map<String, TableModel> configs = new LinkedHashMap<String, TableModel>();

	private List<String[]> givesList = new ArrayList<String[]>();

	/**
	 * The row Data loaded from database, List<Map<colName, colValue>> or <"u",
	 * entity> or <"#u",entityId>
	 */
	private List<Map<String, Object>> rowData = new ArrayList<Map<String, Object>>();

	/** The body of entity net, Map<alias, Map<entityId, entity>> */
	private Map<String, LinkedHashMap<Object, Object>> body = new HashMap<String, LinkedHashMap<Object, Object>>();

	protected void constructor__________________________() {// NOSONAR
	}

	public OrmQry(SqlBoxContext ctx) {
		this.ctx = ctx;
	}

	protected void core__________________________() {// NOSONAR
	}

	/** Config, parameters can be entity or entity class or TableModel */
	public OrmQry config(Object... entityOrModel) {
		for (Object object : entityOrModel) {
			TableModel t = SqlBoxContextUtils.objectConfigToModel(ctx, object);
			EntityNetException.assureNotNull(t.getEntityClass(), "'entityClass' property not set for model " + t);
			String alias = t.getAlias();
			if (StrUtils.isEmpty(alias)) {
				StringBuilder sb = new StringBuilder();
				char[] chars = t.getEntityClass().getSimpleName().toCharArray();
				for (char c : chars)
					if (c >= 'A' && c <= 'Z')
						sb.append(c);
				alias = sb.toString().toLowerCase();
			}
			EntityNetException.assureNotEmpty(alias, "Alias can not be empty for class '" + t.getEntityClass() + "'");
			if (configs.containsKey(alias)) {
				throw new EntityNetException("Duplicated alias '" + alias + "' for class '" + t.getEntityClass()
						+ "' found, need use configOne method manually set alias.");
			}
			t.setAlias(alias);
			configs.put(alias, t);
		}
		return this;
	}

	/** Config entity1, alias1, entity2, alias2... */
	public OrmQry configAlias(Object... args) {
		for (int i = 0; i < args.length / 2; i++)
			configOneAlias(args[i * 2], (String) args[i * 2 + 1]);
		return this;
	}

	/** Config one entity */
	private OrmQry configOneAlias(Object entityOrModel, String alias) {
		TableModel t = SqlBoxContextUtils.objectConfigToModel(ctx, entityOrModel);
		EntityNetException.assureNotNull(t.getEntityClass(), "'entityClass' property not set for model " + t);
		EntityNetException.assureNotEmpty(alias, "Alias can not be empty for class '" + t.getEntityClass() + "'");
		t.setAlias(alias);
		if (configs.containsKey(alias))
			throw new EntityNetException("Duplicated alias '" + alias + "' found, need manually set alias.");
		configs.put(alias, t);
		return this;
	}

	/** Give a's value to b's aField */
	public OrmQry giveBoth(String a, String b) {
		give(a, b);
		give(b, a);
		return this;
	}

	/** Give a's value to b's aField */
	public OrmQry give(String a, String b) {
		TableModel aModel = configs.get(a);
		EntityNetException.assureNotNull(aModel, "Not found config for alias '" + a + "'");
		EntityNetException.assureNotNull(aModel.getEntityClass(), "'entityClass' property not set for model " + aModel);
		String fieldName = StrUtils.toLowerCaseFirstOne(aModel.getEntityClass().getSimpleName());

		TableModel bModel = configs.get(b);
		EntityNetException.assureNotNull(bModel, "Not found config for alias '" + a + "'");
		EntityNetException.assureNotNull(bModel.getEntityClass(), "'entityClass' property not set for model " + bModel);

		boolean found = false;
		Method readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName);
		if (readMethod != null) {
			give(a, b, StrUtils.toLowerCaseFirstOne(fieldName));
			found = true;
		}

		readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName + "List");
		if (readMethod != null) {
			give(a, b, fieldName + "List");
			found = true;
		}

		readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName + "Set");
		if (readMethod != null) {
			give(a, b, fieldName + "Set");
			found = true;
		}
		readMethod = ClassCacheUtils.getClassFieldReadMethod(bModel.getEntityClass(), fieldName + "Map");
		if (readMethod != null) {
			give(a, b, fieldName + "Map");
			found = true;
		}
		if (!found)
			throw new EntityNetException("Not found field '" + fieldName + "' or '" + fieldName
					+ "List/Set/Map' in class /" + bModel.getEntityClass());
		return this;
	}

	/** Give a's value to b's someField */
	public OrmQry give(String a, String b, String someField) {
		EntityNetException.assureNotEmpty(someField, "give field parameter can not be empty for '" + b + "'");
		givesList.add(new String[] { a, b, someField });
		return this;
	}

	/** Execute a query and join result into current NewNet */
	@SuppressWarnings("unchecked")
	public OrmQry query(Object... sqlItems) {
		PreparedSQL ps = ctx.iPrepare(sqlItems);
		ps.addHandler(new SSMapListHandler(configs.values().toArray(new Object[configs.size()])));
		ps.setOperationType(SqlOption.QUERY);
		List<Map<String, Object>> result = (List<Map<String, Object>>) ctx.runPreparedSQL(ps);
		for (Map<String, Object> map : result) {
			rowData.add(map);
			translateToEntities(map);
			if (!givesList.isEmpty())
				doGive(map);
		}
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> pickEntityList(String alias) {
		return (List<T>) new ArrayList(body.get(alias).values());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Set<T> pickEntitySet(String alias) {
		return (Set<T>) new LinkedHashSet(body.get(alias).values());
	}

	@SuppressWarnings({ "unchecked" })
	public <T> Map<Object, T> pickEntityMap(String alias) {
		return (Map<Object, T>) body.get(alias);
	}

	/** Translate one row map list to entity objects, put into entity net body */
	private void translateToEntities(Map<String, Object> oneRow) {
		for (Entry<String, TableModel> config : this.configs.entrySet()) {
			TableModel model = config.getValue();
			String alias = config.getKey();

			// find and build entityID
			Object entityId = buildEntityId(oneRow, model, alias);
			if (entityId == null)
				continue;// not found entity ID columns
			Object entity = getOneEntity(alias, entityId);

			// create new Entity
			if (entity == null) {
				entity = createEntity(oneRow, model, alias);
				this.putOneEntity(alias, entityId, entity);
			}
			oneRow.put(alias, entity);// In this row, add entities directly
			oneRow.put("#" + alias, entityId); // In this row, add entityIds
		}
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
			EntityNetException.assureNotEmpty(tofield);
			if (from != null && to != null) {
				TableModel toModel = configs.get(toAlias);
				ColumnModel col = toModel.getColumn(tofield);
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
					EntityNetException.assureNotNull(entityId, "Can not find entityId for '" + fromAlias + "'");
					if (!map.containsKey(entityId))
						map.put(entityId, from);
				} else {// No matter what type, give "from" value to "to"
					ClassCacheUtils.writeValueToBeanField(to, tofield, from);
				}
			}
		}
	}

	private Object createEntity(Map<String, Object> oneRow, TableModel model, String alias) {
		Object entity;
		entity = ClassCacheUtils.createNewEntity(model.getEntityClass());
		ctx.getSqlBox(entity).setTableModel(model.newCopy());
		for (Entry<String, Object> row : oneRow.entrySet()) { // u_userName
			for (ColumnModel col : model.getColumns()) {
				if (col.getTransientable())
					continue;
				if (row.getKey().equalsIgnoreCase(alias + "_" + col.getColumnName())) {
					EntityNetException.assureNotEmpty(col.getEntityField(),
							"EntityField not set for column '" + col.getColumnName() + "'");
					ClassCacheUtils.writeValueToBeanField(entity, col.getEntityField(), row.getValue());
				}
			}
		}
		return entity;
	}

	private Object buildEntityId(Map<String, Object> oneRow, TableModel model, String alias) {
		Object entityId = null;
		for (Entry<String, Object> row : oneRow.entrySet()) { // u_userName
			for (ColumnModel col : model.getColumns()) {
				if (col.getTransientable() || !col.getPkey())
					continue;
				int pkCount = 0;

				if (row.getKey().equalsIgnoreCase(
						new StringBuilder(alias).append("_").append(col.getColumnName()).toString())) {
					pkCount++;
					if (pkCount == 1)
						entityId = row.getValue();
					else {
						entityId = new StringBuilder("").append(entityId).append(COMPOUND_VALUE_SEPARATOR)
								.append(row.getValue()).toString();
					}
				}
			}
		}
		return entityId;
	}

	public void putOneEntity(String alias, Object entityId, Object entity) {
		LinkedHashMap<Object, Object> entityMap = body.get(alias);
		if (entityMap == null) {
			entityMap = new LinkedHashMap<Object, Object>();
			body.put(alias, entityMap);
		}
		entityMap.put(entityId, entity);
	}

	public Object getOneEntity(String alias, Object entityId) {
		Map<Object, Object> entityMap = body.get(alias);
		if (entityMap == null)
			return null;
		return entityMap.get(entityId);
	}

	protected void getterSetter__________________________() {// NOSONAR
	}

	public SqlBoxContext getCtx() {
		return ctx;
	}

	public OrmQry setCtx(SqlBoxContext ctx) {
		this.ctx = ctx;
		return this;
	}

	public Map<String, TableModel> getConfigs() {
		return configs;
	}

	public OrmQry setConfigs(Map<String, TableModel> configs) {
		this.configs = configs;
		return this;
	}

	public List<Map<String, Object>> getRowData() {
		return rowData;
	}

	public OrmQry setRowData(List<Map<String, Object>> rowData) {
		this.rowData = rowData;
		return this;
	}

	public List<String[]> getGivesList() {
		return givesList;
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
		for (TableModel tb : configs.values()) {
			sb.append(tb.getDebugInfo());
		}
		sb.append("\r\n=========rowData=========\r\n");
		for (Map<String, Object> row : rowData) {
			sb.append(row.toString()).append("\r\n");
		}

		sb.append("\r\n=========body=========\r\n");
		for (LinkedHashMap<Object, Object> row : body.values()) {
			sb.append(row.toString()).append("\r\n");
		}
		return sb.toString();

	}

	public OrmQry setGivesList(List<String[]> givesList) {
		this.givesList = givesList;
		return this;
	}

	public Map<String, LinkedHashMap<Object, Object>> getBody() {
		return body;
	}

	public void setBody(Map<String, LinkedHashMap<Object, Object>> body) {
		this.body = body;
	}

}
