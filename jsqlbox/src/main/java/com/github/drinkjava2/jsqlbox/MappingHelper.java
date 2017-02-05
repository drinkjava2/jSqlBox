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

import java.util.ArrayList;

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
 * note: c.ORDER(), c.ORDERLIST is for bind the node or node list to a field <br/>
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class MappingHelper {
	private static ThreadLocal<MappingType> inMapping = new ThreadLocal<MappingType>() {
		@Override
		protected MappingType initialValue() {
			return null;
		}
	};

	private static ThreadLocal<ArrayList<Mapping>> mappingCache = new ThreadLocal<ArrayList<Mapping>>() {
		@Override
		protected ArrayList<Mapping> initialValue() {
			return new ArrayList<>();
		}
	};

	private static ThreadLocal<ArrayList<Object>> entityCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<>();
		}
	};

	private MappingHelper() {// Disable default public constructor
	}

	public static Boolean isInMapping() {
		return inMapping.get() != null;
	}

	public static ArrayList<Object> getEntityCache() {
		return entityCache.get();
	}

	public static String mapping(String... args) {
		try {
			Mapping mapping = new Mapping();
			mapping.setMappingType(inMapping.get());
			mapping.setThisEntity(null);

			mappingCache.get().add(mapping);

			StringBuilder sb = new StringBuilder(" ");
			for (String string : args) {
				sb.append(string);
			}
			sb.append(" ");
			return sb.toString();
		} finally {
			inMapping.set(null);
			entityCache.get().clear();
		}
	}

	public static String oneToOne(String... fieldID) {
		inMapping.set(MappingType.ONETOONE);
		return "";
	}

	public static String oneToMany(String... fieldID) {
		inMapping.set(MappingType.ONETOMANY);
		return "";
	}

	public static String manyToMany(String... fieldID) {
		inMapping.set(MappingType.MANYTOMANY);
		return "";
	}

	public static String tree(String... fieldID) {
		inMapping.set(MappingType.TREE);
		return "";
	}
}
