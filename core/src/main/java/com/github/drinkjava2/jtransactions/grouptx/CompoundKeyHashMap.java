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

public class CompoundKeyHashMap<K, P, V> implements CompoundKeyMap<K, P, V> {

	private Map<CompoundKey<K, P>, V> map = new HashMap<CompoundKey<K, P>, V>();

	public V get(K k, P p) {
		if (k == null || p == null)
			throw new IllegalArgumentException("key cannot be null");
		return map.get(newKey(k, p));
	}

	private CompoundKeyMap.CompoundKey<K, P> newKey(K key, P param) {
		return new CompoundKeyImpl<K, P>(key, param);
	}

	public V get(K k, P p, V defValue) {
		if (k == null || p == null)
			throw new IllegalArgumentException("key cannot be null");

		V value = get(k, p);
		return value == null ? defValue : value;
	}

	public V put(K key, P param, V value) {
		return map.put(newKey(key, param), value);
	}

	public V putIfAbsent(K key, P param, V value) {
		V v = get(key, param);
		if (v == null)
			v = put(key, param, value);
		return v;
	}

	public Set<Entry<CompoundKeyMap.CompoundKey<K, P>, V>> entrySet() {
		return map.entrySet();
	}

	public Set<CompoundKeyMap.CompoundKey<K, P>> keys() {
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

	static class CompoundKeyImpl<K, P> implements CompoundKey<K, P> {

		private K key;

		private P param;

		CompoundKeyImpl(K key, P param) {
			super();
			this.key = key;
			this.param = param;
		}

		public K getKey() {
			return key;
		}

		public P getParam() {
			return param;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((param == null) ? 0 : param.hashCode());
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
			CompoundKeyImpl<?, ?> other = (CompoundKeyImpl<?, ?>) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (param == null) {
				if (other.param != null)
					return false;
			} else if (!param.equals(other.param))
				return false;
			return true;
		}
	}

}