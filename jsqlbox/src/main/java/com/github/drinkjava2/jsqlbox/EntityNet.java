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

import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNet represents a graph structure entity net, use a No-SQL style query
 * browse between entity nodes
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public interface EntityNet {
	/**
	 * Add a List<Map<String, Object>> list to entity net
	 *
	 * @param listMap
	 * @param modelConfigs
	 * @return EntityNet it self
	 */
	public EntityNet addMapList(List<Map<String, Object>> listMap, TableModel... modelConfigs);

	/**
	 * Add a Entity into entity net
	 */
	public void addEntity(Object entity, TableModel tableModel);

	/**
	 * Remove a Entity from entity net
	 */
	public void removeEntity(Object entity, TableModel tableModel);

	/**
	 * Update a Entity in entity net
	 */
	public void updateEntity(Object entity, TableModel tableModel);
}
