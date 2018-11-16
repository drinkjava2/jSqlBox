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

import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdialects.ArrayUtils;
import com.github.drinkjava2.jdialects.DialectException;

/**
 * The platform-independent Foreign Key Constraint model
 * 
 * <pre>
 * Usage:
 * Table t=new Table('Customer');
 *    ...
 *  t.fkey("column1").ref("refTable", "refColumn1")
 *  
 *  or compound foreign key: *  
 *  t.fkey("column1","column2").ref("refTable", "refColumn1", "refColumn2");
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class FKeyModel {
	private String fkeyName;
	private String tableName;
	private List<String> columnNames = new ArrayList<String>();
	private String fkeyTail;
	private TableModel tableModel; // belong to which tableModel

	/*
	 * if ddl set to false, will not create DDL when call TableModelUtils's
	 * entity2Model() and oneEntity2Model methods
	 */
	private Boolean ddl = true;

	/** format: "reftable, refcol1, refcol2..." */
	private String[] refTableAndColumns;

	public FKeyModel() {
		// default constructor
	}

	public FKeyModel(String fkeyName) {
		this.fkeyName = fkeyName;
	}

	public FKeyModel newCopy() {
		FKeyModel result = new FKeyModel();
		result.fkeyName = fkeyName;
		result.tableName = tableName;
		result.fkeyTail = fkeyTail;
		result.refTableAndColumns = refTableAndColumns;
		result.ddl = ddl;
		for (String colName : columnNames) {
			result.columnNames.add(colName);
		}
		return result;
	}

	public FKeyModel columns(String... columnNames) {
		checkReadOnly();
		this.columnNames = ArrayUtils.strArrayToList(columnNames);
		return this;
	}

	public FKeyModel fkeyName(String fkeyName) {
		checkReadOnly();
		this.fkeyName = fkeyName;
		return this;
	}

	public FKeyModel ddl(Boolean ddl) {
		checkReadOnly();
		this.ddl = ddl;
		return this;
	}

	public FKeyModel refs(String... refTableAndColumns) {
		checkReadOnly();
		this.refTableAndColumns = refTableAndColumns;
		return this;
	}

	/** Add a tail String at the end of Foreign key DDL */
	public FKeyModel fkeyTail(String fkeyTail) {
		checkReadOnly();
		this.fkeyTail = fkeyTail;
		return this;
	}

	public void checkReadOnly() {
		if (tableModel != null && tableModel.getReadOnly())
			throw new DialectException(
					"TableModel '" + tableModel.getTableName() + "' is readOnly, can not be modified.");
	}

	// getter & setter=====
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		checkReadOnly();
		this.tableName = tableName;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		checkReadOnly();
		this.columnNames = columnNames;
	}

	public String[] getRefTableAndColumns() {
		return refTableAndColumns;
	}

	public void setRefTableAndColumns(String[] refTableAndColumns) {
		checkReadOnly();
		this.refTableAndColumns = refTableAndColumns;
	}

	public String getFkeyName() {
		return fkeyName;
	}

	public void setFkeyName(String fkeyName) {
		checkReadOnly();
		this.fkeyName = fkeyName;
	}

	public String getFkeyTail() {
		return fkeyTail;
	}

	public void setFkeyTail(String tail) {
		checkReadOnly();
		this.fkeyTail = tail;
	}

	public Boolean getDdl() {
		return ddl;
	}

	public void setDdl(Boolean ddl) {
		checkReadOnly();
		this.ddl = ddl;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		checkReadOnly();
		this.tableModel = tableModel;
	}

}
