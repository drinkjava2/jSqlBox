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
package com.github.drinkjava2.jdialects.model;

import com.github.drinkjava2.jdialects.DialectException;

/**
 * The platform-independent Index model
 * 
 * <pre>
 * Usage:
 * Table t=new Table('Customer');
 * ...  
 *  t.index().forColumn("column1");
 *  or
 *  t.index("indexName").forColumn("column1","column2");
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public class IndexModel {
	/** (Optional) The names of the index */
	private String name;

	/** The names of the columns to be included in the index */
	private String[] columnList;

	/** Whether the index is unique. */
	private Boolean unique = false;

	private TableModel tableModel; // belong to which tableModel

	public IndexModel() {

	}

	public IndexModel(String name) {
		this.name = name;
	}

	public IndexModel newCopy() {
		IndexModel result = new IndexModel();
		result.name = name;
		result.columnList = columnList;
		result.unique = unique;
		return result;
	}

	public IndexModel columns(String... columns) {
		checkReadOnly();
		this.columnList = columns;
		return this;
	}

	public IndexModel unique() {
		checkReadOnly();
		this.unique = true;
		return this;
	}

	public void checkReadOnly() {
		if (tableModel != null && tableModel.getReadOnly())
			throw new DialectException(
					"TableModel '" + tableModel.getTableName() + "' is readOnly, can not be modified.");
	}

	// getter & setter =========
	public String getName() {
		return name;
	}

	public void setName(String name) {
		checkReadOnly();
		this.name = name;
	}

	public String[] getColumnList() {
		return columnList;
	}

	public void setColumnList(String[] columnList) {
		checkReadOnly();
		this.columnList = columnList;
	}

	public Boolean getUnique() {
		return unique;
	}

	public void setUnique(Boolean unique) {
		checkReadOnly();
		this.unique = unique;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		checkReadOnly();
		this.tableModel = tableModel;
	}

}
