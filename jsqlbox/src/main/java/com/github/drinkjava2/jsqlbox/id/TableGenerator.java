/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jsqlbox.id;

/**
 * Defines a table primary key generator, copied from JPA but deleted some unused fields
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TableGenerator implements IdGenerator {

	/**
	 * Name of table that stores the generated id values. Defaults to a name chosen by persistence provider.
	 */
	private String table = "";

	/**
	 * Name of the primary key column in the table. Defaults to a provider-chosen name.
	 */
	private String pkColumnName = "";

	/**
	 * Name of the column that stores the last value generated. Defaults to a provider-chosen name.
	 */
	private String valueColumnName = "";

	/**
	 * The primary key value in the generator table that distinguishes this set of generated values from others that may
	 * be stored in the table.
	 */
	private String pkColumnValue = "";

	/**
	 * The initial value to be used to initialize the column that stores the last value generated.
	 */
	private int initialValue = 0;

	/**
	 * The amount to increment by when allocating id numbers from the generator.
	 */
	private int allocationSize = 50;

	public TableGenerator() {
		// default constructor
	}

	public TableGenerator(String table) {

	}

	public TableGenerator(String table, String pkColumnName, String pkColumnValue, String valueColumnName) {
		this.table = table;
		this.pkColumnName = pkColumnName;
		this.valueColumnName = valueColumnName;
		this.pkColumnValue = pkColumnValue;
	}

	@Override
	public String getNextID() {
		//TODO return Dao.dao().queryForString("select ", valueColumnName, "+1", " from ", table, " where pkColumnName =",
		// pkColumnValue);
		return null;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}

	public String getPkColumnValue() {
		return pkColumnValue;
	}

	public void setPkColumnValue(String pkColumnValue) {
		this.pkColumnValue = pkColumnValue;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}

}
