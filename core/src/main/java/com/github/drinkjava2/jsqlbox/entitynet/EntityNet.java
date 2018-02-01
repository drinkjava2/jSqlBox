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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNet represents a graph structure entity net, use a No-SQL style query
 * browse between entity nodes
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public interface EntityNet {
	/** Used to combine compound key column names into a single String */
	public static final String COMPOUND_COLUMNNAME_SEPARATOR = "_CmPdIdSpr_";

	/** Used to combine compound key column values into a single String */
	public static final String COMPOUND_VALUE_SEPARATOR = "_CmPdValSpr_";
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

	/**
	 * Find entity set map in TinyNet by given path and entity input array
	 */
	public Map<Class<?>, Set<Object>> findEntityMap(Path path, Object... entities);

	/**
	 * Find node set in TinyNet by given path and entity input array
	 */
	public Map<Class<?>, Set<Node>> findNodeMapByEntities(Path path, Object... entities);

	/**
	 * Find node set in TinyNet by given path and entity input collection
	 */
	public Map<Class<?>, Set<Node>> findNodeMapByEntityCollection(Path path, Collection<Object> entityCollection);

	/**
	 * Find Node Map by Node collection
	 */
	public Map<Class<?>, Set<Node>> findNodeMapByNodeCollection(Path path, Collection<Node> input);

	/**
	 * Find entity set in TinyNet by given path and entity input collection
	 */
	public <T> Set<T> findEntitySet(Class<T> targetEntityClass, Path path, Collection<Object> entityCollection);

	/**
	 * Find entity set in TinyNet by given path and entity input array
	 */
	public <T> Set<T> findEntitySet(Class<T> targetEntityClass, Path path, Object... entities);

	/** Get Config models */
	Map<Class<?>, TableModel> getConfigModels();
}
