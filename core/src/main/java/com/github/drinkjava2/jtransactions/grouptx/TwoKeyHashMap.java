/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jtransactions.grouptx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Yong Zhu
 * @since 2.0.6
 */
public class TwoKeyHashMap<K, P, V> implements TwoKeyMap<K, P, V> {

	private Map<TwoKey<K, P>, V> map = new HashMap<TwoKey<K, P>, V>();

	public V get(K key1, P key2) {
		if (key1 == null || key2 == null)
			throw new IllegalArgumentException("key1 cannot be null");
		return map.get(newKey(key1, key2));
	}

	private TwoKeyMap.TwoKey<K, P> newKey(K key1, P key2) {
		return new TwoKeyImpl<K, P>(key1, key2);
	}

	public V get(K key1, P key2, V defValue) {
		if (key1 == null || key2 == null)
			throw new IllegalArgumentException("key1 cannot be null");

		V value = get(key1, key2);
		return value == null ? defValue : value;
	}

	public V put(K key1, P key2, V value) {
		return map.put(newKey(key1, key2), value);
	}

	public V putIfAbsent(K key1, P key2, V value) {
		V v = get(key1, key2);
		if (v == null)
			v = put(key1, key2, value);
		return v;
	}

	public Set<Entry<TwoKeyMap.TwoKey<K, P>, V>> entrySet() {
		return map.entrySet();
	}

	public Set<TwoKeyMap.TwoKey<K, P>> keys() {
		return map.keySet();
	}

	public Collection<V> values() {
		return map.values();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public void remove(K key1, P key2) {
		Set<TwoKey<K, P>> keys = this.keys();
		if (keys == null)
			return;
		TwoKey<K, P> toRemove = null;
		for (TwoKey<K, P> twoKey : keys) {
			if (key1 == twoKey.getKey1() && key2 == twoKey.getkey2()) {
				toRemove = twoKey;
				break;
			}
		}
		map.remove(toRemove);
	}

	static class TwoKeyImpl<K, P> implements TwoKey<K, P> {

		private K key1;

		private P key2;

		TwoKeyImpl(K key1, P key2) {
			super();
			this.key1 = key1;
			this.key2 = key2;
		}

		public K getKey1() {
			return key1;
		}

		public P getkey2() {
			return key2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key1 == null) ? 0 : key1.hashCode());
			result = prime * result + ((key2 == null) ? 0 : key2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TwoKeyImpl<?, ?> other = (TwoKeyImpl<?, ?>) obj;
			if (key1 == null) {
				if (other.key1 != null)
					return false;
			} else if (!key1.equals(other.key1))
				return false;
			if (key2 == null) {
				if (other.key2 != null)
					return false;
			} else if (!key2.equals(other.key2))
				return false;
			return true;
		}

	}

}