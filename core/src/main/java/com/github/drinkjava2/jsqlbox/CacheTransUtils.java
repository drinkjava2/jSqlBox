/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.util.List;
import java.util.Map;

/**
 * Cache translate utils
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class CacheTransUtils {

	/**
	 * Do cache translate, usage: <br/>
	 * 
	 * <pre>
	 * Map<Integer, Map<String, Object>> users = ctx.iQuery("select id, * from users", new KeyedHandler<Integer>(1));
	 * Map<Integer, Map<String, Object>> groups = ctx.iQuery("select id, * from groups", new KeyedHandler<Integer>(1));
	 * List<Map<String, Object>> orders = ctx.iQueryForMapList("select * from orders");
	 * CacheTransUtils.translate(orders, users, "userID", "name", "userName", "age", "userAge", groups, "groupId",
	 * 		"groupName", "groupName");
	 * </pre>
	 * 
	 * @param list
	 * @param option
	 */
	public static <T> void translate(List<T> list, Object... option) {
		for (T row : list)
			translateRow(row, option);
	}

	/** Translate only one row record with given option */
	public static <T> void translateRow(T row, Object... option) {
		int i = 0;
		Map<Object, Object> rowMap = (Map) row;
		Map<Object, Map<String, Object>> cache = null;
		String idCol = null;
		String cacheCol;
		String dispCol;
		Object idColValue;

		while (i < option.length) {
			Object item = option[i];
			if (item instanceof String) {
				cacheCol = (String) option[i++];
				dispCol = (String) option[i++];
			} else {
				cache = (Map<Object, Map<String, Object>>) option[i++];
				idCol = (String) option[i++];
				cacheCol = (String) option[i++];
				dispCol = (String) option[i++];
			}
			idColValue = rowMap.get(idCol);
			if (idColValue != null) {
				Map<String, Object> cachRow = cache.get(idColValue);
				if (cachRow != null) {
					Object newValue = cachRow.get(cacheCol);
					if (newValue != null)
						rowMap.put(dispCol, newValue);
				}
			}
		}
	}
}