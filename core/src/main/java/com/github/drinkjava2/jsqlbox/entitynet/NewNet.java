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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.handler.SSMapListHandler;

/**
 * NewNet
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class NewNet {

	/** NewNet depends on SqlBoxContext */
	SqlBoxContext ctx;

	/** Models, Map<alias, tableModels> */
	private Map<String, TableModel> configs = new LinkedHashMap<String, TableModel>();

	private List<String[]> gives = new ArrayList<String[]>();

	/** The row Data loaded from database, List<Map<colName, colValue>> */
	private List<Map<String, Object>> rowData = new ArrayList<Map<String, Object>>();

	/**
	 * The row entity loaded from database, List<Map<alias, entity>>, repeated
	 * entity may found
	 */
	private List<Map<String, Object>> rowEntity = new ArrayList<Map<String, Object>>();

	/**
	 * The body of the NewNet Map<alias, LinkedHashMap<entityId, entity>>, no
	 * repeated entity
	 */
	private Map<Class<?>, LinkedHashMap<Object, Node>> body;

	protected void constructor__________________________() {// NOSONAR
	}

	public NewNet(SqlBoxContext ctx) {
		this.ctx = ctx;
	}

	protected void core__________________________() {// NOSONAR
	}

	/** Config, parameters can be entity or entity class or TableModel */
	public NewNet config(Object... entityOrModel) {
		for (Object object : entityOrModel) {
			TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, object);
			EntityNetException.assureNotNull(t.getEntityClass(), "'entityClass' property not set for model " + t);
			String alias = t.getAlias();
			if (StrUtils.isEmpty(alias))
				alias = t.getEntityClass().getSimpleName().substring(0, 1).toLowerCase();
			if (configs.containsKey(alias)) {
				StringBuilder sb = new StringBuilder();
				char[] chars = t.getEntityClass().getSimpleName().toCharArray();
				for (char c : chars)
					if (c >= 'A' && c <= 'Z')
						sb.append(c);
				alias = sb.toString().toLowerCase();
				if (configs.containsKey(alias)) {
					throw new EntityNetException("Duplicated alias '" + alias + "' for class '" + t.getEntityClass()
							+ "' found, need use configOne method manually set alias.");
				}
			}
			EntityNetException.assureNotEmpty(alias, "Alias can not be empty for class '" + t.getEntityClass() + "'");
			configs.put(alias, t);
		}
		return this;
	}

	/** Config one entity */
	public NewNet configOne(Object entityOrModel, String alias) {
		TableModel t = SqlBoxContextUtils.getTableModelFromEntityOrClass(ctx, entityOrModel);
		EntityNetException.assureNotNull(t.getEntityClass(), "'entityClass' property not set for model " + t);
		EntityNetException.assureNotEmpty(alias, "Alias can not be empty for class '" + t.getEntityClass() + "'");
		t.setAlias(alias);
		if (configs.containsKey(alias))
			throw new EntityNetException("Duplicated alias '" + alias + "' found, need manually set alias.");
		configs.put(alias, t);
		return this;
	}

	/** Give a's value to b's aField */
	public NewNet give(String a, String b) {
		TableModel t = configs.get(a);
		EntityNetException.assureNotNull(t, "Not found config for alias '" + a + "'");
		EntityNetException.assureNotNull(t.getEntityClass(), "'entityClass' property not set for model " + t);
		give(a, b, StrUtils.toLowerCaseFirstOne(t.getEntityClass().getSimpleName()));
		return this;
	}

	/** Give a's value to b's someField */
	public NewNet give(String a, String b, String someField) {
		gives.add(new String[] { a, b, someField });
		return this;
	}
	

	/** Execute a query and join result into current NewNet */
	@SuppressWarnings("unchecked")
	public NewNet joinQuery(Object... sqlItems) {
		PreparedSQL ps = ctx.iPrepare(sqlItems);
		ps.addHandler(new SSMapListHandler(configs.values().toArray(new Object[configs.size()])));
		ps.setType(SqlOption.QUERY);
		List<Map<String, Object>> result = (List<Map<String, Object>>) ctx.runPreparedSQL(ps);
		for (Map<String, Object> map : result)
			rowData.add(map);
		if (gives != null && !gives.isEmpty()) {
			doGive();
		}
		return this;
	}

	private void doGive() {
		// TODO
	}

	protected void getterSetter__________________________() {// NOSONAR
	}

	public SqlBoxContext getCtx() {
		return ctx;
	}

	public NewNet setCtx(SqlBoxContext ctx) {
		this.ctx = ctx;
		return this;
	}

	public Map<String, TableModel> getConfigs() {
		return configs;
	}

	public NewNet setConfigs(Map<String, TableModel> configs) {
		this.configs = configs;
		return this;
	}

	public List<Map<String, Object>> getRowData() {
		return rowData;
	}

	public NewNet setRowData(List<Map<String, Object>> rowData) {
		this.rowData = rowData;
		return this;
	}

	public List<Map<String, Object>> getRowEntity() {
		return rowEntity;
	}

	public NewNet setRowEntity(List<Map<String, Object>> rowEntity) {
		this.rowEntity = rowEntity;
		return this;
	}

	public Map<Class<?>, LinkedHashMap<Object, Node>> getBody() {
		return body;
	}

	public NewNet setBody(Map<Class<?>, LinkedHashMap<Object, Node>> body) {
		this.body = body;
		return this;
	}

	public List<String[]> getGives() {
		return gives;
	}

	public NewNet setGives(List<String[]> gives) {
		this.gives = gives;
		return this;
	}

}
