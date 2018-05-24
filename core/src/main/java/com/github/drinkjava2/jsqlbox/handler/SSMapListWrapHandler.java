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

/**
 * SSMapListWrapHandler is a SqlHandler used to explain alias.** to real columns
 * in SQL, example:
 * 
 * select u.** from users u ==> select u.name, u.address, u.age from users u
 * 
 * And return a MapListWrap object, with 2 properties: mapList and config
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class SSMapListWrapHandler extends SSMapListHandler {

	public SSMapListWrapHandler(Object... netConfigObjects) {
		super(netConfigObjects);
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		return new MapListWrap((List<Map<String, Object>>) super.handle(runner, ps), config);
	}

}
