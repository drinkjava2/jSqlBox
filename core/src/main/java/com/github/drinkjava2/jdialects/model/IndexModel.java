/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

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
		this.columnList = columns;
		return this;
	}

	public IndexModel unique() {
		this.unique = true;
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

	public Boolean getUnique() {
		return unique;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

}
