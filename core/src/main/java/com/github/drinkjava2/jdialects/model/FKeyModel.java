/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		result.ddl=ddl;
		for (String colName : columnNames) {
			result.columnNames.add(colName);
		}
		return result;
	}

	public FKeyModel columns(String... columnNames) {
		this.columnNames = Arrays.asList(columnNames);
		return this;
	}

	public FKeyModel fkeyName(String fkeyName) {
		this.fkeyName = fkeyName;
		return this;
	}

	public FKeyModel ddl(Boolean ddl) {
		this.ddl = ddl;
		return this;
	}

	public FKeyModel refs(String... refTableAndColumns) {
		this.refTableAndColumns = refTableAndColumns;
		return this;
	}

	/** Add a tail String at the end of Foreign key DDL */
	public FKeyModel fkeyTail(String fkeyTail) {
		this.fkeyTail = fkeyTail;
		return this;
	}

	// getter & setter=====
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public String[] getRefTableAndColumns() {
		return refTableAndColumns;
	}

	public void setRefTableAndColumns(String[] refTableAndColumns) {
		this.refTableAndColumns = refTableAndColumns;
	}

	public String getFkeyName() {
		return fkeyName;
	}

	public void setFkeyName(String fkeyName) {
		this.fkeyName = fkeyName;
	}

	public String getFkeyTail() {
		return fkeyTail;
	}

	public void setFkeyTail(String tail) {
		this.fkeyTail = tail;
	}

	public Boolean getDdl() {
		return ddl;
	}

	public void setDdl(Boolean ddl) {
		this.ddl = ddl;
	}

}
