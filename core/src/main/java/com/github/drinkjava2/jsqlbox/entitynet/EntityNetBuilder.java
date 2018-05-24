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

import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;
import com.github.drinkjava2.jsqlbox.handler.MapListWrap;

/**
 * EntityNetBuilder used to build EntityNet
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public class EntityNetBuilder {
	private SqlBoxContext ctx;

	public EntityNetBuilder(SqlBoxContext sqlBoxContext) {
		this.ctx = sqlBoxContext;
	}

	protected void entityNetAboutMethods__________________________() {// NOSONAR
	}

	/** Create a EntityNet by given configurations, load all columns */
	public EntityNet loadAll(Object... configObjects) {
		return EntityNet.createEntityNet(ctx, false, configObjects);
	}

	/** Create a EntityNet instance but only load PKey and FKeys columns */
	public EntityNet loadSketch(Object... configObjects) {
		return EntityNet.createEntityNet(ctx, true, configObjects);
	}

	/** Create a EntityNet by given list and netConfigs */
	public EntityNet create(List<Map<String, Object>> listMap, Object... configObjects) {
		TableModel[] result = SqlBoxContextUtils.objectConfigsToModels(ctx, configObjects);
		if (result == null || result.length == 0)
			throw new SqlBoxException("No entity class config found");
		return EntityNet.createEntityNet(listMap, result);
	}

	/** Create a EntityNet by given MapListWrap */
	public EntityNet create(MapListWrap mapListWrap) {
		if (mapListWrap.getConfig() == null || mapListWrap.getConfig().length == 0)
			throw new SqlBoxException("No entity class config found");
		return EntityNet.createEntityNet(mapListWrap.getMapList(), mapListWrap.getConfig());
	}

	/** Join list and netConfigs to existed EntityNet */
	@SuppressWarnings("unchecked")
	public <T> T join(EntityNet net, List<Map<String, Object>> listMap, Object... configObjects) {
		TableModel[] result = SqlBoxContextUtils.objectConfigsToModels(ctx, configObjects);
		return (T) net.addMapList(listMap, result);
	}

	/** Join MapListWrap to existed EntityNet */
	@SuppressWarnings("unchecked")
	public <T> T join(EntityNet net, MapListWrap mapListWrap) {
		return (T) net.addMapList(mapListWrap.getMapList(), mapListWrap.getConfig());
	}

	/** Add an entity to existed EntityNet */
	public void addEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entity);
		net.addEntity(entity, box.getTableModel());
	}

	/** Remove an entity from EntityNet */
	public void removeEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entity);
		net.removeEntity(entity, box.getTableModel());
	}

	/** Update an entity in EntityNet */
	public void updateEntity(EntityNet net, Object entity) {
		SqlBox box = SqlBoxUtils.findAndBindSqlBox(ctx, entity);
		net.updateEntity(entity, box.getTableModel());
	}

	/** Shortcut method, load all entities as list */
	public <T> List<T> loadAsEntityList(Class<T> entityClass) {
		return this.loadAll(entityClass).getAllEntityList(entityClass);
	}
}
