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
package com.github.drinkjava2.jsqlbox.jpa;

import java.util.HashMap;

import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * Defines the value of primary key generation strategy, copied from JPA
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class GeneratedValue {
	public static HashMap<String, GeneratedValue> generatedValueCache = new HashMap<>();

	/**
	 * (Optional) The primary key generation strategy that the persistence provider must use to generate the annotated
	 * entity primary key.
	 */
	private GenerationType generationType = null;

	/**
	 * (Optional) The name of the primary key generator to use as specified in the {@link SequenceGenerator} or
	 */
	private String generator;

	public GenerationType getGenerationType() {
		return generationType;
	}

	public void setGenerationType(GenerationType generationType) {
		this.generationType = generationType;
	}

	public String getGenerator() {
		return generator;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	/**
	 * Find a singleton GeneratedValue instance from cache, if no, create a new one and cache it
	 */
	public static GeneratedValue getGeneratedValue(GenerationType generationType, String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(generationType);
		for (String arg : args) {
			sb.append("/").append(arg);
		}
		String key = sb.toString();
		GeneratedValue generatedValue;
		synchronized (generatedValueCache) {
			generatedValue = generatedValueCache.get(key);
		}
		if (generatedValue != null)
			return generatedValue;

		if (generationType == GenerationType.AUTO) {
			generatedValue = new GeneratedValue();
		} else if (generationType == GenerationType.TABLE) {

		}
		if (generatedValue == null)
			SqlBoxException.throwEX(null, "SqlBox GeneratedValue error, can not create GeneratedValue: " + key);
		synchronized (generatedValueCache) {
			generatedValueCache.put(key, generatedValue);
		}
		return generatedValue;
	}

}
