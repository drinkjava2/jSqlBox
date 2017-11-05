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

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * If a entity class implements ActiveRecordSupport interface, it will have CRUD
 * Method.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public interface ActiveRecordSupport {

	/**
	 * @return the binded SqlBox instance
	 */
	public SqlBox bindedBox();

	/**
	 * @return the binded SqlBox instance, if no, create a new one and bind to
	 *         entity
	 */
	public SqlBox box();

	/**
	 * Bind a SqlBox instance to entity
	 */
	public void bindBox(SqlBox box);

	/**
	 * Unbind SqlBox instance of entity
	 */
	public void unbindBox();

	/** @return TableModel instance binded in this entity */
	public TableModel tableModel();
	
	/** set Entity's box's tableModel's alias name */
	public <T> T alias(String alias);

	/** @return binded TableModel's columnModel by given columnName */
	public ColumnModel columnModel(String columnName);

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

	/**
	 * Link style set values for entity field, format like:
	 * user.set("id","id1").set("name","Sam").set("address","Beijing","phone","12345",
	 * "email","abc@123.com")
	 */
	public ActiveRecordSupport put(Object... fieldAndValues);

	/** Cache a field array in ThreadLocal for putValues method use */
	public ActiveRecordSupport putFields(String... fieldNames);

	/**
	 * Put values for entity fields, field names should be cached by call
	 * putFields method first
	 */
	public ActiveRecordSupport putValues(Object... values);

}