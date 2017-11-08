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
 * Path store search condition path, one path can link to another path to build
 * a path chain <br/>
 * 
 * Sub class override check method can do a Java Native check for a Node to
 * determine if node can keep in result list
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class Path {

	/** if id be set, each query based on this Path will be cached */
	protected String id;

	/** Can only be "C":Child or "P".:Parent */
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

	/** Next condition, used for build a linked path chain */
	protected Path nextPath;

	public Path(String table) {
		this.table = table;
	}

	public Path(Class<?> targetClass) {
	}
	
	public Path(String table,  String columns) {
		this.table = table;
	}

	public Path(Class<?> targetClass,  String columns) {
	}
	

	public Path(String type, String table, String columns) {
		this.type = type;
		this.table = table;
		this.columns = columns;
	}

	public Path(String type, String table, String columns, Boolean keep) {
		this.type = type;
		this.table = table;
		this.columns = columns;
		this.keep = keep;
	}

}
