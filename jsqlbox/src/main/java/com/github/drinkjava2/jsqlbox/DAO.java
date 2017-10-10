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

/**
 * Collect some global CRUD methods, to use these methods, need set the global
 * default SqlBoxContext first, for example: </br>
 * "SqlBoxContext.setDefaultContext(new SqlBoxContext(someDataSource))";
 */
public abstract class DAO {// NOSONAR
	public static void insert(Object entity) {
		SqlBoxContext.defaultContext.insert(entity);
	}

	public static void update(Object entity) {
		SqlBoxContext.defaultContext.update(entity);
	}

	public static void delete(Object entity) {
		SqlBoxContext.defaultContext.delete(entity);
	}

	public static <T> T load(Object entity, Object pkey) {
		return SqlBoxContext.defaultContext.load(entity, pkey);
	}
}