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
package com.github.drinkjava2.jsqlbox.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.improve.CacheHandler;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * MemCacheHandler is a simple memory cache used to cache SQL query result .
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public class MemCacheHandler implements ResultSetHandler, CacheHandler {
	private static final LRULinkedHashMap cache = new LRULinkedHashMap(500);

	public MemCacheHandler() {
		// Default constructor
	}

	@Override
	public Object handle(ResultSet result) throws SQLException {
		return result;
	}

	@Override
	public void writeToCache(String key, Object value) {
		if (!StrUtils.isEmpty(key) && value != null)
			cache.putValue(key, value);
	}

	@Override
	public Object readFromCache(String key) {
		if (StrUtils.isEmpty(key) || !cache.haveKey(key))
			return null;
		return cache.getValue(key);
	}

	public static void clearCache() {
		cache.clearCache();
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

		public synchronized void putValue(String key, Object value) {
			this.put(key, value);
		}

		public synchronized Object getValue(String key) {
			return this.get(key);
		}

		public synchronized boolean haveKey(String key) {
			return this.containsKey(key);
		}

		public synchronized void clearCache() {
			this.clear();
		}

		@Override
		public boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
			return size() > capacity;
		}
	}

}
