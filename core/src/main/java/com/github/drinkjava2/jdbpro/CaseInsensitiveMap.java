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
package com.github.drinkjava2.jdbpro;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The case insensitive map
 * 
 * @author Yong Zhu
 * @since 2.0.2
 */
public class CaseInsensitiveMap<K, V> extends HashMap<K, V> {
	private final Map<String, Object> lowerCaseMap = new HashMap<String, Object>();

	private static final long serialVersionUID = -1L;

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(Object key) {
		Object realKey = lowerCaseMap.get(key.toString().toLowerCase(Locale.ENGLISH));
		return super.containsKey(realKey);
	}

	/** {@inheritDoc} */
	@Override
	public V get(Object key) {
		Object realKey = lowerCaseMap.get(key.toString().toLowerCase(Locale.ENGLISH));
		return super.get(realKey);
	}
 
	@Override
	public V put(K key, V value) { 
		Object oldKey = lowerCaseMap.put(key.toString().toLowerCase( ), key);
		V oldValue = super.remove(oldKey);
		super.put(key, value);
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends String, ?> m) {
		for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			this.put(key.toString(), value);
		}
	}

	@Override
	public Object remove(Object key) {
		Object realKey = lowerCaseMap.remove(key.toString().toLowerCase(Locale.ENGLISH));
		return super.remove(realKey);
	}
}
