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
package com.github.drinkjava2.jsqlbox.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;

/**
 * EntityListHandler is the SqlHandler used explain the Entity query SQL (For
 * example 'select u.** from users u') and return a List<entityObject> instance
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class EntityListHandler extends DefaultOrderSqlHandler {
	protected final Object config;

	public EntityListHandler(Object config) {
		this.config = config;
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		ps.setResultSetHandler(SingleTonHandlers.mapListHandler);
		List<Map<String, Object>> maps = (List<Map<String, Object>>) runner.runPreparedSQL(ps);
		List<Object> entityList = new ArrayList<Object>();
		for (Map<String, Object> row : maps) {
			Object entity = SqlBoxContextUtils.mapToEntityBean((SqlBoxContext) runner, config, row);
			entityList.add(entity);
		}
		return entityList;
	}

}
