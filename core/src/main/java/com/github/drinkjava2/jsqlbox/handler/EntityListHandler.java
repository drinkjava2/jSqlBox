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

import java.util.List;

import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlHandler;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;

/**
 * EntityListHandler is the AroundSqlHandler used explain the Entity query sql
 * (For example 'select u.** from users u') and return a List<Entity> instance
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class EntityListHandler implements SqlHandler {
	protected final EntitySqlMapListHandler sqlMapListHandler;
	protected final Class<?> targetClass;

	public EntityListHandler(Class<?> targetClass, Object... netConfigObjects) {
		this.targetClass = targetClass;
		if (netConfigObjects == null || netConfigObjects.length == 0)
			this.sqlMapListHandler = new EntitySqlMapListHandler(targetClass);
		else
			this.sqlMapListHandler = new EntitySqlMapListHandler(netConfigObjects);
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		Object obj = sqlMapListHandler.handle(runner, ps);
		EntityNet net = ((SqlBoxContext) runner).netCreate((List) obj);
		return net.getAllEntityList(targetClass);
	}

}
