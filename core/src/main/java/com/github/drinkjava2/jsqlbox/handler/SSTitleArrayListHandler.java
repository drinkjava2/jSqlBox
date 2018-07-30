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
import com.github.drinkjava2.jdbpro.SingleTonHandlers;

/**
 * SSTitleArrayListHandler is a SqlHandler used to explain alias.** to real columns in
 * SQL and return a List<Object[]>, first row is titles, SS means star-star, example:
 * 
 * select u.** from users u ==> select u.name as u_name, u.address as u_address
 * from users u
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class SSTitleArrayListHandler extends SSHandler {

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		ps.setResultSetHandler(SingleTonHandlers.titleArrayListHandler);
		return super.handle(runner, ps);
	}

}
