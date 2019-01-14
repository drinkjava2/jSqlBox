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
import java.util.Set;

/**
 * TwoKeyMap is a map use 2 keys 
 * @author Yong Zhu
 * @since 2.0.6
 */
public interface TwoKeyMap<K, P, V> {

	V get(K key1, P key2);

	V get(K key1, P key2, V defValue);

	V put(K key1, P key2, V value);
	
	void remove(K key1, P key2);

	V putIfAbsent(K key1, P key2, V value);

	Set<java.util.Map.Entry<TwoKey<K, P>, V>> entrySet();

	Set<TwoKey<K, P>> keys();

	Collection<V> values();

	int size();

	boolean isEmpty();

	public interface TwoKey<K, P> {
		K getKey1();

		P getkey2();
	}

}