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
package com.github.drinkjava2.jdbpro.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * SimpleCacheHandler is a simple memory cache used to cache SQL query result .
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
@SuppressWarnings("rawtypes")
public class SimpleCacheHandler implements ResultSetHandler, CacheSqlHandler {
	private static final Map<String, Object> cache = Collections.synchronizedMap(new LRULinkedHashMap(500));
	private int aliveSeconds;

	public SimpleCacheHandler() {
		aliveSeconds = 1000;
	}

	public SimpleCacheHandler(int aliveSeconds) {
		if (aliveSeconds <= 1)
			this.aliveSeconds = 1;
		else if (aliveSeconds <= 10)
			this.aliveSeconds = 10;
		else if (aliveSeconds <= 100)
			this.aliveSeconds = 100;
		else if (aliveSeconds <= 1000)
			this.aliveSeconds = 1000;
		else if (aliveSeconds <= 10000)
			this.aliveSeconds = 10000;
		else
			this.aliveSeconds = 100000;
	}

	@Override
	public Object handle(ResultSet result) throws SQLException {
		return result;
	}

	@Override
	public void writeToCache(String key, Object value) {
		if ((key == null || key.length() == 0) || value == null)
			return;
		cache.put(Long.toString(System.currentTimeMillis() / 1000 / aliveSeconds) + key, value);
	}

	@Override
	public Object readFromCache(String key) {
		if ((key == null || key.length() == 0))
			return null;
		return cache.get(Long.toString(System.currentTimeMillis() / 1000 / aliveSeconds) + key);
	}

	public static void clearCache() {
		cache.clear();
	}

	/**
	 * A Simple thread-safe LRU Cache
	 */
	@SuppressWarnings("serial")
	public static class LRULinkedHashMap extends LinkedHashMap<String, Object> {// NOSONAR
		private int capacity;

		LRULinkedHashMap(int capacity) {
			super(16, 0.75f, true);
			this.capacity = capacity;
		}

		@Override
		public boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
			return size() > capacity;
		}
	}

}
