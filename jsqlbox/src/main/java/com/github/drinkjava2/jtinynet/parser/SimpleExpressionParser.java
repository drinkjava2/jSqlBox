/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jtinynet.parser;

import java.util.List;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * SimpleExpressionParser parse a String, return true or false
 * 
 * In SimpleExpressionParser, the parse method only support below keywords:
 * 
 * <pre>
 * ==========Math======== 
 * >
 * <
 * =
 * >=
 * <=
 * +
 * -
 * *
 * / 
 * 
 * ==========String========
 * equals 
 * equalsIgnoreCase 
 * contains
 * containsIgnoreCase 
 * startWith
 * startWithIgnoreCase
 * endWith
 * endWithIgnoreCase
 * '
 * 
 * ==========Logic========
 * or
 * and 
 * not 
 * (
 * )
 * if(condition, doTrue, doFalse)
 * 
 * ==========Parameter=========
 * ?
 * 
 * ========= Other ==========
 * &#64;selectedSize 
 * &#64;level
 * 
 * </pre>
 * 
 * For example: "userName startWith ? and not(-age*10 -age>?) or address
 * contains if(age>?,'FOO','BAR')
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class SimpleExpressionParser {

	/**
	 * Parse a expression String, return true if pass validate, false if failed for
	 * validate
	 * 
	 * @param bean The bean be validated
	 * @param level Current search level
	 * @param selectedSize Current selected Node QTY
	 * @param expression The expression
	 * @param params The expression parameter array
	 * @return true if pass validate, false if failed for validate
	 */
	public static boolean parse(Object bean, int level, int selectedSize, String expression, Object... params) {
		if (StrUtils.isEmpty(expression))
			return true;
		return false;
	}
	private static class Item{
		private char type;//O:operator, V:value, I:items
		private int operatorPriority;
		private Object value;
		private char valueType; //S:String, B:Boolean, L: Long, D:Double 
		private List<Item> subItem;
	}

}