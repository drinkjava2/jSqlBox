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

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * IActiveRecord designed for Java8, in Java8 if a entity class implements
 * IActiveRecord interface, it will have CRUD Method. To release
 * jSqlBox-x.x.x-java8.jar, need change source code of this class
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public interface IActiveRecord {

	// =========For release to Java6==========
	/** @return SqlBox instance binded in this entity */
	public SqlBox box();

	/** @return TableModel instance binded in this entity */
	public TableModel tableModel();

	/** @return table name */
	public String table();

	/** @return SqlBoxContext instance */
	public SqlBoxContext context();

	/** Insert entity to database */
	public void insert();

	/** Update entity in database */
	public int update();

	/** Delete entity in database */
	public void delete();

	/**
	 * Load entity from database by primary key, key can be single value or Map
	 */
	public <T> T load(Object pkey);

	// =========For release to Java8 need use below codes==========
	// TODO: default methods for Java8

}