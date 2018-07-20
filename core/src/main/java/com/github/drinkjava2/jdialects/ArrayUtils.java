/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * Array Utilities usually used inside of framework
 * 
 * @author Yong Zhu
 * @since 2.0.1
 */
public class ArrayUtils {//NOSONAR
	
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
}
