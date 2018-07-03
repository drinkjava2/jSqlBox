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
import java.util.Map;

import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;

/**
 * EntityNetHandler used to convert SQL query result to EntityNet
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class EntityNetHandler extends SSMapListHandler {

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		EntityNet net = ps.getEntityNet() == null ? new EntityNet() : (EntityNet) ps.getEntityNet();
		net.configFromPreparedSQL(ps);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = (List<Map<String, Object>>) super.handle(runner, ps);
		net.joinMapList(result);
		return net;
	}

}
