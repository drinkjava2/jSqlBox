/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import com.github.drinkjava2.jdialects.DialectException;

/**
 * The platform-independent Unique Constraint model
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
public class UniqueModel {
	private String name;
	private String[] columnList;
	private TableModel tableModel; // belong to which tableModel

	public UniqueModel() {

	}

	public UniqueModel(String name) {
		this.name = name;
	}

	public UniqueModel newCopy() {
		UniqueModel result = new UniqueModel();
		result.name = name;
		result.columnList = columnList;
		return result;
	}

	public UniqueModel columns(String... columns) {
		checkReadOnly();
		this.columnList = columns;
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

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		checkReadOnly();
		this.tableModel = tableModel;
	}

}
