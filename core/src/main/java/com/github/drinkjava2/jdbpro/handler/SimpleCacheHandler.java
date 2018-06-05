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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;

/**
 * SimpleCacheHandler is a simple memory cache used to cache SQL query result .
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
public class SimpleCacheHandler extends DefaultOrderSqlHandler {

	/** A simple thread-safe LRU Cache with 500 items capacity */
	private Map<String, Object> cache;

	private int aliveSeconds = 1000;
	private int capacity = 500;

	public SimpleCacheHandler() {
		cache = Collections.synchronizedMap(new LRULinkedHashMap(capacity));
	}

	public int getAliveSeconds() {
		return aliveSeconds;
	}

	public int getCapacity() {
		return capacity;
	}

	public SimpleCacheHandler(int capacity, int aliveSeconds) {
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
		this.capacity = capacity;
		cache = Collections.synchronizedMap(new LRULinkedHashMap(capacity));
	}

	/** Call this method to manually clear cache */
	public void clearCache() {
		cache.clear();
	}

	private String createKey(PreparedSQL ps) {
		return new StringBuilder(Long.toString(System.currentTimeMillis() / 1000 / aliveSeconds)).append("SQL:")
				.append(ps.getSql()).append("  Params:").append(ps.getParams()).toString();
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		String key = createKey(ps);
		Object result = cache.get(key);
		if (result != null)
			return ((Object[]) result)[1];
		result = runner.runPreparedSQL(ps);
		cache.put(key, new Object[] { null, result });
		return result;
	}

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
