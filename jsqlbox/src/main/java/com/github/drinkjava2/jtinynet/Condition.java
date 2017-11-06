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
	 * Can only be "C" or "P" or "O" <br/>
	 * C:Child<br/>
	 * P:Parent<br/>
	 * O:Other<br/>
	 */
	protected String type;

	/** The reference table name */
	protected String table;

	/** The fkey column names */
	protected String columns;

	/** allowed keep in result list */
	protected Boolean keep = true;

	protected Condition[] subConditions;

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

	/**
	 * add a subCondition into existed condition's subConditions, and return
	 * subCondition just added, not the current condition
	 */
	public Condition link(Condition condition) {
		TinyNetException.assureNotNull(condition, "condition can not be null");
		if (subConditions == null) {
			subConditions = new Condition[] { condition };
		} else {
			int length = subConditions.length;
			Condition[] newArray = new Condition[length + 1];
			System.arraycopy(subConditions, 0, newArray, 0, length);
			newArray[length] = condition;
			subConditions = newArray;
		}
		return condition;
	}

	/**
	 * add a subCondition into subConditions
	 */
	public void add(Condition condition) {
		TinyNetException.assureNotNull(condition, "condition can not be null");
		if (subConditions == null) {
			subConditions = new Condition[] { condition };
		} else {
			int length = subConditions.length;
			Condition[] newArray = new Condition[length + 1];
			System.arraycopy(subConditions, 0, newArray, 0, length);
			newArray[length] = condition;
			subConditions = newArray;
		}
	}

	/**
	 * SubClass override this callback method to check if the node can keep in
	 * resultList
	 */
	public boolean check(Node node) {
		return true;
	};

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

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public Condition[] getSubConditions() {
		return subConditions;
	}

	public void setSubConditions(Condition[] subConditions) {
		this.subConditions = subConditions;
	}

}
