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
package com.github.drinkjava2.jsqlbox.converter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * The FieldConverter Utils, works like a small ioc tool, cache singleton
 * converter instance for use
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public abstract class FieldConverterUtils {
	private FieldConverterUtils() {
	}

	private static final Map<Object, FieldConverter> converterCache = new ConcurrentHashMap<Object, FieldConverter>();
	static {
		registerConverter("Version", new VersionFieldConverter());
		registerConverter("EnumType.ORDINAL", new EnumOrdinalFieldConverter());
		registerConverter("EnumType.STRING", new EnumStringFieldConverter());
	}

	/**
	 * Return the singleTon FieldConverter instance in cache, if not in cache, build
	 * a new one and cache it, if clazz is Version.class, then return
	 * VersionFieldConverter singleTon instance
	 */
	public static FieldConverter getFieldConverter(Object key) {
		FieldConverter converter = converterCache.get(key);
		if (converter == null) {
			try {
				converter = (FieldConverter) ((Class<?>) key).newInstance();
				converterCache.put(key, converter);
			} catch (Exception e) {
				throw new SqlBoxException(
						"Fail to use newInstance() method to build a singleTon instance for '" + key + "'", e);
			}
		}
		return converter;
	}

	public static void registerConverter(Object key, FieldConverter converter) {
		converterCache.put(key, converter);
	}

}