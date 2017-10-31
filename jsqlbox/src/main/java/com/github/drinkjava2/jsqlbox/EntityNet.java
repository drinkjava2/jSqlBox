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
 * Net represents a graph structure
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public interface EntityNet {

	/**
	 * Add a List<Map<String, Object>> list to current entity net, modelConfigs
	 * parameter is optional
	 *
	 * @param listMap
	 * @param modelConfigs
	 * @return EntityNet it self
	 */
	public EntityNet addMapList(List<Map<String, Object>> listMap, TableModel... modelConfigs);

	/** Return total how many nodes in entity net */
	public int size();

	/** Return entity list in EntityNet which type is entityClass */
	public <T> List<T> getEntityList(Class<T> entityClass);
}
