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
package com.github.drinkjava2.jtinynet;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * Condition store search condition <br/>
 * 
 * Sub class override check method can do a Java Native check for a Node to
 * determine if node can keep in result list
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class Condition {
	/**
	 * Can only be "C" or "P", C:Child P:Parent<br/>
	 */
	protected String type;

	/** The reference table name, if set table, then no need set entityClass */
	protected String table;

	/**
	 * The reference table corresponding entity class, if set entityClass , then
	 * no need set table
	 */
	protected Class<?> entityClass;

	/** The fkey column names */
	protected String columns;

	/** allowed keep in result list */
	protected Boolean keep = true;

	/** Next condition, used for build a linked conditions chain */
	protected Condition nextCondition;

	public Condition() {
	}

	public Condition(String type, String table, String columns) {
		this.type = type;
		this.table = table;
		this.columns = columns;
	}

	public Condition(String type, String table, String columns, Boolean keep) {
		this.type = type;
		this.table = table;
		this.columns = columns;
		this.keep = keep;
	}

	/** Shortcut link style method to set type to "C" Child */
	public Condition c() {
		this.type = "C";
		return this;
	}

	/** Shortcut link style method to set type to "P" Parent */
	public Condition p() {
		this.type = "p";
		return this;
	}

	/**
	 * Shortcut link style method to set target class
	 * 
	 * @param table
	 *            The table name
	 * @return self
	 */
	public Condition target(String table) {
		this.table = table;
		return this;
	}

	/**
	 * Shortcut link style method to set target class
	 * 
	 * @param entityClass
	 *            The target entity class
	 * @return self
	 */
	public Condition target(Class<?> entityClass) {
		this.entityClass = entityClass;
		return this;
	}

	/** Shortcut link style method to set FKey Column Names */
	public Condition col(String... fkeyColumnNames) {
		for (String colName : fkeyColumnNames) {
			if (!StrUtils.isEmpty(this.getColumns()))
				this.columns += TinyNet.COMPOUND_COLUMNNAME_SEPARATOR;
			else
				this.columns = colName;
			this.columns += colName;
		}
		return this;
	}

	/**
	 * Set the next condition, after this condition be executed, query will run
	 * based on next condition
	 */
	/**
	 * @param nextCondition
	 * @return nextCondition
	 */
	public Condition link(Condition nextCondition) {
		this.nextCondition = nextCondition;
		return nextCondition;
	}

	/**
	 * SubClass override this callback method to check if the node can keep in
	 * resultList
	 */
	public boolean check(Node node) {
		return true;
	}

	// getter & setter======

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public Boolean getKeep() {
		return keep;
	}

	public void setKeep(Boolean keep) {
		this.keep = keep;
	}

	public Condition getNextCondition() {
		return nextCondition;
	}

	public void setNextCondition(Condition nextCondition) {
		this.nextCondition = nextCondition;
	}
}
