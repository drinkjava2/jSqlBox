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

package com.github.drinkjava2.jsqlbox;

/**
 * This class is for transfer SQL query result List<Map<String, Object>> to object trees<br/>
 * There are 4 type mappings: oneToOne, oneToMany, manyToMany, tree <br/>
 * 
 * Here are some examples: <br/>
 * mapping(oneToOne(), c.ID(),o.CUSTOMERID()); <br/>
 * mapping(oneToMany(), c.ID(),o.CUSTOMERID()); <br/>
 * mapping(manyToMany(), c.ID(),o.CUSTOMERID()); <br/>
 * mapping(manyToMany(), c.ID(),o.CUSTOMERID()); <br/>
 * 
 * mapping(oneToOne(c.ORDER()), c.ID(),o.CUSTOMERID()); <br/>
 * mapping(oneToMany(c.ORDERLIST()), c.ID(),o.CUSTOMERID()); <br/>
 * mapping(manyToMany(c.ORDERLIST(), o.CUSTOMERLIST()), c.ID(),o.CUSTOMERID()); <br/>
 * mapping(tree(), c.ID(),c.PID); <br/>
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlMapping {
	private static final String EMPTY_STRING = "";

	private SqlMapping() {// Disable default public constructor
	}

	public static String mapping(String mappingType, String fieldID1, String fieldID2) {
		return new StringBuilder(" ").append(fieldID1).append(" = ").append(fieldID2).append(" ").toString();
	}

	public static String oneToOne(String... fieldID) {
		return EMPTY_STRING;
	}

	public static String oneToMany(String... fieldID) {
		return EMPTY_STRING;
	}

	public static String manyToMany(String... fieldID) {
		return EMPTY_STRING;
	}

	public static String tree(String... fieldID) {
		return EMPTY_STRING;
	}
}
