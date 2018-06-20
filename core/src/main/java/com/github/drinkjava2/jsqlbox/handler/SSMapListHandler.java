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

import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;

/**
 * SSMapListHandler is a SqlHandler used to explain alias.** to real columns in
 * SQL, example:
 * 
 * select u.** from users u ==> select u.name, u.address, u.age from users u
 * 
 * And return a Map List
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class SSMapListHandler extends SSHandler {

	public SSMapListHandler() {

	}

	public SSMapListHandler(Object... configs) {
		super(configs);
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		ps.setResultSetHandler(SingleTonHandlers.mapListHandler);
		return super.handle(runner, ps);
	}

}
