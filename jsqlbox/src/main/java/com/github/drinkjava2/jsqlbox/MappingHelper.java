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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private static ThreadLocal<ArrayList<Mapping>> mappingListCache = new ThreadLocal<ArrayList<Mapping>>() {
		@Override
		protected ArrayList<Mapping> initialValue() {
			return new ArrayList<>();
		}
	};

	private static ThreadLocal<ArrayList<Object>> entityPairCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<>();
		}
	};

	private static ThreadLocal<ArrayList<String>> idPairCache = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	private static ThreadLocal<ArrayList<String>> propertyPairCache = new ThreadLocal<ArrayList<String>>() {
		@Override
		protected ArrayList<String> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * For store entity classes in tThreadlocal, these entities indicate how many classes will be created when do O-R
	 * mapping
	 */
	private static ThreadLocal<HashSet<Entity>> entityTemplates = new ThreadLocal<HashSet<Entity>>() {
		@Override
		protected HashSet<Entity> initialValue() {
			return new HashSet<>();
		}
	};

	private MappingHelper() {// Disable default public constructor
	}

	/**
	 * Clear all threadLocal variants
	 */
	public static void clearAllMappingCached() {
		inMapping.set(null);
		mappingListCache.get().clear();
		entityPairCache.get().clear();
		idPairCache.get().clear();
		propertyPairCache.get().clear();
		entityTemplates.get().clear();
	}

	/**
	 * Check inMapping in Threadlocal not null
	 */
	public static Boolean isInMapping() {
		return inMapping.get() != null;
	}

	/**
	 * Get entityPairCache in Threadlocal
	 */
	public static List<Object> getEntityPairCache() {
		return entityPairCache.get();
	}

	/**
	 * Get idPairCache in Threadlocal
	 */
	public static List<String> getIdPairCache() {
		return idPairCache.get();
	}

	/**
	 * Get propertyPairCache in Threadlocal
	 */
	public static List<String> getPropertyPairCache() {
		return propertyPairCache.get();
	}

	/**
	 * Get mappingListCache in Threadlocal
	 */
	public static List<Mapping> getMappingListCache() {
		return mappingListCache.get();
	}

	/**
	 * Get entityClassForQuery in Threadlocal
	 */
	public static Set<Entity> getEntityTemplates() {
		return entityTemplates.get();
	}

	/**
	 * Bind entity property with parent or child entity or entities <br/>
	 * Read cached mapping info from ThreadLocal and re-cache a Mapping instance in ThreadLocal, if propertyFieldName
	 * exist, at the query result tree, entity will be binded to the given property field
	 */
	public static String bind(String... propertyFieldName) {// NOSONAR propertyFieldName is useful, can not remove
		try {
			Mapping mapping = new Mapping();
			mapping.setMappingType(inMapping.get());

			mapping.setThisEntity(entityPairCache.get().get(0));
			mapping.setThisField(idPairCache.get().get(0));

			mapping.setOtherEntity(entityPairCache.get().get(1));
			mapping.setOtherfield(idPairCache.get().get(1));

			if (propertyFieldName.length == 1) {
				if (SqlBoxUtils.isEmptyStr(propertyFieldName[0]))
					SqlBoxException.throwEX("MappingHelper bind() can not set empty propertyFieldNames parameter");
				else
					mapping.setThisPropertyName(getPropertyPairCache().get(0));
			}

			if (propertyFieldName.length == 2) {
				if (SqlBoxUtils.isEmptyStr(propertyFieldName[0]) && SqlBoxUtils.isEmptyStr(propertyFieldName[1]))
					SqlBoxException
							.throwEX("MappingHelper bind() can not set both empty propertyFieldNames parameters");
				else if (SqlBoxUtils.isEmptyStr(propertyFieldName[0])) {
					mapping.setOtherPropertyName(getPropertyPairCache().get(0));
				} else if (SqlBoxUtils.isEmptyStr(propertyFieldName[1])) {
					mapping.setThisPropertyName(getPropertyPairCache().get(0));
				} else {
					mapping.setThisPropertyName(getPropertyPairCache().get(0));
					mapping.setOtherPropertyName(getPropertyPairCache().get(1));
				}
			}
			if (propertyFieldName.length > 2)
				SqlBoxException.throwEX("MappingHelper bind() can not have more than 2 parameters");

			mappingListCache.get().add(mapping);
			return "";
		} finally {
			inMapping.set(null);
			entityPairCache.get().clear();
			idPairCache.get().clear();
			propertyPairCache.get().clear();
		}
	}

	public static String oneToOne() {
		inMapping.set(MappingType.ONETOONE);
		return "";
	}

	public static String oneToMany() {
		inMapping.set(MappingType.ONETOMANY);
		return "";
	}

	public static String tree() {
		inMapping.set(MappingType.TREE);
		return "";
	}
}
