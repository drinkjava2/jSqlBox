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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jtinynet.TinyNetException;

/**
 * SimpleExpressionParser parse a String, return true or false
 * 
 * In SimpleExpressionParser, the parse method only support below keywords:
 * 
 * <pre>
>
<
=
>=
<=
+
-
*
/ 
equals 
equalsIgnoreCase 
contains
containsIgnoreCase 
startWith
startWithIgnoreCase
endWith
endWithIgnoreCase
or
and
not 


' 
()
? 
0~9

beanFields
selectedSize 
level
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
	public static SimpleExpressionParser instance = new SimpleExpressionParser();

	private Map<String, Func> FUNCTIONMAP = new HashMap<String, Func>();
	{
		FUNCTIONMAP.put("*", new Func(3, 2));
		FUNCTIONMAP.put("/", new Func(3, 2));
		FUNCTIONMAP.put("+", new Func(4, 2));
		FUNCTIONMAP.put("-", new Func(4, 2));
		FUNCTIONMAP.put("not", new Func(5, 1));
		FUNCTIONMAP.put("equals", new Func(6, 2));
		FUNCTIONMAP.put("equalsignorecase", new Func(6, 2));
		FUNCTIONMAP.put("contains", new Func(6, 2));
		FUNCTIONMAP.put("containsignorecase", new Func(6, 2));
		FUNCTIONMAP.put("startwith", new Func(6, 2));
		FUNCTIONMAP.put("startwithignorecase", new Func(6, 2));
		FUNCTIONMAP.put("endwith", new Func(6, 2));
		FUNCTIONMAP.put("endwithignorecase", new Func(6, 2));
		FUNCTIONMAP.put(">", new Func(8, 2));
		FUNCTIONMAP.put("<", new Func(8, 2));
		FUNCTIONMAP.put("=", new Func(8, 2));
		FUNCTIONMAP.put(">=", new Func(8, 2));
		FUNCTIONMAP.put("<=", new Func(8, 2));
		FUNCTIONMAP.put("<>", new Func(8, 2));
		FUNCTIONMAP.put("or", new Func(10, 2));
		FUNCTIONMAP.put("and", new Func(10, 2));
	}

	/**
	 * Parse a expression String, return true if pass validate, false if failed
	 * for validate
	 * 
	 * @param bean
	 *            The bean be validated
	 * @param level
	 *            Current search level
	 * @param selectedSize
	 *            Current selected Node QTY
	 * @param expression
	 *            The expression
	 * @param params
	 *            The expression parameter array
	 * @return true if pass validate, false if failed for validate
	 */
	public boolean parse(Object bean, int level, int selectedSize, String expression, Object... params) {
		if (StrUtils.isEmpty(expression))
			return true;
		return true;
	}

	public static class Func {
		public int priority;
		public int operatorQTY;

		public Func(int priority, int operatorQTY) {
			this.priority = priority;
			this.operatorQTY = operatorQTY;
		}
	}

	public static class Item {
		private int priority;
		private Object value;
		private char type; // S:String, B:Boolean, L: Long, D:Double,
							// P:parameter, I:items,
							// U:Unknow_Function_Or_Variant
		private List<Item> subItem;

		public String getDebugInfo() {
			String result = "\rItemValue=";
			if (value != null)
				result += value;
			if (subItem != null) {
				for (Item Item : subItem) {
					result += " " + Item.getDebugInfo();
				}
			}
			return result;
		}
	}

	public static class SearchResult {
		private Item item;
		private int leftStart;
		private int leftEnd;

		private SearchResult(Item item, int leftStart, int leftEnd) {
			this.item = item;
			this.leftStart = leftStart;
			this.leftEnd = leftEnd;
		}
	}

	private static boolean isLetterNumber(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.';
	}

	public SearchResult findFirstResult(char[] chars, int start, int end) {
		if (start > end)
			return null;
		boolean letters = false;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i <= end; i++) {
			if (!letters) {// no letters found
				if (chars[i] == '?') {
					Item item = new Item();
					item.type = 'V';
					item.value = "?";
					return new SearchResult(item, i + 1, end);
				} else if (chars[i] == '\'') {
					for (int j = i + 1; j <= end; j++) {
						if (chars[j] == '\'' && chars[j - 1] != '\\') {
							Item item = new Item();
							item.type = 'S';
							item.value = sb.toString();
							return new SearchResult(item, j + 1, end);
						} else
							sb.append(chars[j]);
					}
					throw new TinyNetException("Miss right ' charactor in expression.");
				} else if (chars[i] == '(') {
					int count = 1;
					boolean inString = false;
					for (int j = i + 1; j <= end; j++) {
						if (!inString) {
							if (chars[j] == '(')
								count++;
							else if (chars[j] == ')') {
								count--;
								if (count == 0) {
									List<Item> subItems = seperateCharsToItems(chars, i + 1, j - 1);
									Item item = new Item();
									item.type = 'I';
									item.subItem = subItems;
									return new SearchResult(item, j + 1, end);
								}
							} else if (chars[j] == '\'') {
								inString = true;
							}
						} else {
							if (chars[j] == '\'' && chars[j - 1] != '\\') {
								inString = false;
							}
						}
					}
					throw new TinyNetException("Miss right ) charactor in expression.");
				} else if (chars[i] > ' ') {
					letters = true;
					sb.append(chars[i]);
				}
			} else {// letters found
				if (chars[i] == '?' || chars[i] == '\'' || chars[i] == '(' || chars[i] <= ' '
						|| isLetterNumber(chars[i]) != isLetterNumber(chars[i - 1])) {
					Item item = new Item();
					item.type = 'U';
					item.value = sb.toString();
					return new SearchResult(item, i, end);
				} else {
					sb.append(chars[i]);
				}
			}
		}
		if (sb.length() > 0) {
			Item item = new Item();
			item.type = 'U';
			item.value = sb.toString();
			return new SearchResult(item, end + 1, end);
		} else
			return null;
	}

	public List<Item> seperateCharsToItems(char[] chars, int start, int end) {
		List<Item> items = new ArrayList<Item>();
		SearchResult result = findFirstResult(chars, start, end);
		while (result != null) {
			items.add(result.item);
			result = findFirstResult(chars, result.leftStart, result.leftEnd);
		}
		return items;
	}

	public Object doParse(String expression) {
		char[] chars = (" " + expression + " ").toCharArray();
		List<Item> items = seperateCharsToItems(chars, 1, chars.length - 2);
		for (Item item : items) {
			System.out.print(item.getDebugInfo());
		}
		return null;
	}

	public static void main(String[] args) {
		String s = "35*(24 -6*(2+ 56))+7/(4-6)";
		instance.doParse(s);
	}

}