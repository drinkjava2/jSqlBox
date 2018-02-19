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

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdbpro.improve.SqlInterceptor;

/**
 * EntityNetMapListHandler used to explain alias.** to real columns in SQL,
 * example:
 * 
 * select u.** from users u ==> select u.name, u.address, u.age from users u
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNetMapListHandler extends MapListHandler implements SqlInterceptor {
	 

	private final EntityNetSqlExplainer entityNetSqlExplainer;

	public EntityNetMapListHandler(Object... configObjects) {
		entityNetSqlExplainer = new EntityNetSqlExplainer(configObjects);
	}

	@Override
	public String handleSql(QueryRunner query, String sql, int paramQtyType, Object paramOrParams) {
		return entityNetSqlExplainer.handleSql(query, sql, paramQtyType, paramOrParams);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T handleResult(Object originalResult) {
		return (T)entityNetSqlExplainer.handleResult(originalResult);
	}
}
