/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

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
		this.columnList = columns;
		return this;
	}

	// getter & setter =========
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getColumnList() {
		return columnList;
	}

	public void setColumnList(String[] columnList) {
		this.columnList = columnList;
	}

}
