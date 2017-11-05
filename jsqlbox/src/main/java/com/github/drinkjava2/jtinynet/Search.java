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

import java.util.ArrayList;
import java.util.List;

/**
 * Search is a pojo store search conditions
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class Search {
	/**
	 * Can only be "C" or "P" or "O" <br/>
	 * C:Child<br/>
	 * P:Parent<br/>
	 * O:Other<br/>
	 */
	String type;// C or P or O
	String table; //child or parent table name
	String columns; 
	private List<Search> subSearch = new ArrayList<Search>();

	public Search() {
		// default constructor
	}

	public Search(String type, String table, String columns) {
		this.type = type;
		this.table = table;
		this.columns = columns;
	}

	public Search(String type, String table, String columns, List<Search> subSearch) {
		this.type = type;
		this.table = table;
		this.columns = columns;
		this.subSearch = subSearch;
	}

	public Search link(Search search) {
		subSearch.add(search);
		return search;
	}

	/**
	 * SubClass override this callback method to check if the node can put into
	 * resultList
	 */
	public boolean checkNode(Node node) {// NOSONAR
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

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public List<Search> getSubSearch() {
		return subSearch;
	}

	public void setSubSearch(List<Search> subSearch) {
		this.subSearch = subSearch;
	}

}
