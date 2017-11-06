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
 * This is a helper class store public static methods concern to build EntityNet
 */
public interface EntityNetBuilder {

	/** Create a EntityNet instance */
	public <T> T createEntityNet();

	/** Create a EntityNet instance by given listMap and configs */
	public <T> T createEntityNet(List<Map<String, Object>> listMap, TableModel[] configs);

	/**
	 * Create a EntityNet instance, load data from database buy given loadKeyOnly
	 * and configObjects parameters
	 * 
	 * @param ctx A SqlBoxContext instance
	 * @param loadKeyOnly If true will only load PKey and FKeys field, otherwise
	 *            load all columns
	 * @param configObjects netConfigs array, can be entity class, entity, SqlBox or
	 *            TableModel instance
	 * @return The EntityNet
	 */
	public <T> T createEntityNet(SqlBoxContext ctx, boolean loadKeyOnly, Object... configObjects);
}
