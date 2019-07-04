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
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.List;

/**
 * Array Utilities usually used inside of framework
 * 
 * @author Yong Zhu
 * @since 2.0.1
 */
public class ArrayUtils {// NOSONAR

	/** Insert an Object at front of array */
	public static Object[] insertArray(Object obj, Object[] arr) {
		Object[] newArr = new Object[arr.length + 1];
		System.arraycopy(arr, 0, newArr, 1, arr.length);
		newArr[0] = obj;
		return newArr;
	}

	/** Append an Object at end of array */
	public static Object[] appendArray(Object[] arr, Object obj) {
		Object[] newArr = new Object[arr.length + 1];
		System.arraycopy(arr, 0, newArr, 0, arr.length);
		newArr[arr.length] = obj;
		return newArr;
	}

	/** Append a String at end of String array */
	public static String[] appendStrArray(String[] arr, String str) {
		String[] newArr = new String[arr.length + 1];
		System.arraycopy(arr, 0, newArr, 0, arr.length);
		newArr[arr.length] = str;
		return newArr;
	}

	/** Transfer a String array to String List */
	public static List<String> strArrayToList(String[] arr) {
		List<String> result = new ArrayList<String>();
		if (arr == null || arr.length == 0)
			return result;
		for (String str : arr)
			result.add(str);
		return result;
	}

	/** Transfer a String List to String array */
	public static String[] strListToArray(List<String> list) {
		if (list == null)
			return new String[0];
		return list.toArray(new String[list.size()]);
	}
}
