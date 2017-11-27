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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * TinyParser parse an expression, return type can be Boolean, String, Long,
 * Double, null
 * 
 * Here the parse method only support very few keywords: * > < = >= <= + - /
 * equals equalsIgnoreCase contains containsIgnoreCase startWith
 * startWithIgnoreCase endWith endWithIgnoreCase or and not ' () ? 0~9
 * SELECTLEVEL SELECTSIZE beanFields
 * 
 * 
 * For example: "userName startWith ? and not(age*(10+2) -age>?)"
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class TinyParser {
	public static final TinyParser instance = new TinyParser();

	/** Registered functions, key is function name, value is function priority */
	Map<String, Integer> functionMap = new HashMap<String, Integer>();
	{
		functionMap.put("*", 2);
		functionMap.put("/", 2);
		functionMap.put("+", 4);
		functionMap.put("-", 4);
		functionMap.put("EQUALS", 6);
		functionMap.put("EQUALSIGNORECASE", 6);
		functionMap.put("CONTAINS", 6);
		functionMap.put("CONTAINSIGNORECASE", 6);
		functionMap.put("STARTWITH", 6);
		functionMap.put("STARTWITHIGNORECASE", 6);
		functionMap.put("ENDWITH", 6);
		functionMap.put("ENDWITHIGNORECASE", 6);
		functionMap.put("IS", 6);
		functionMap.put(">", 8);
		functionMap.put("<", 8);
		functionMap.put("=", 8);
		functionMap.put(">=", 8);
		functionMap.put("<=", 8);
		functionMap.put("<>", 8);
		functionMap.put("NOT", 10);
		functionMap.put("AND", 12);
		functionMap.put("OR", 14);
	}

	/**
	 * Item type can be: <br/>
	 * S:String, B:Boolean, L: Long, D:Double, F:function, N:Null, (:sub items,
	 * U:Unknow(need correct), P:parameter(need correct)
	 * 
	 * @author Yong Zhu
	 * @since 1.7.0
	 */
	public static class Item {
		char type;
		int priority;
		Object value;

		List<Item> subItems;

		void setTypeAndValue(char type, Object value) {
			this.type = type;
			this.value = value;
		}

		void guess(Object obj) {
			value = obj;
			if (obj == null)
				type = 'N';
			else if (obj instanceof String)
				type = 'S';
			else if (obj instanceof Boolean)
				type = 'B';
			else if (obj instanceof Long)
				type = 'L';
			else if (obj instanceof Integer) {
				type = 'L';
				value = (long) (Integer) value;// NOSONAR
			} else if (obj instanceof Byte) {
				type = 'L';
				value = (long) (Byte) value;// NOSONAR
			} else if (obj instanceof Double)
				type = 'D';
			else if (obj instanceof Float) {
				type = 'D';
				value = (double) (Float) value;// NOSONAR
			} else
				throw new TinyParserException("Unrecognized expression datat type for '" + obj + "'");
		}

		String getDebugInfo(int include) {
			String result = "\r";
			for (int i = 0; i < include; i++) {
				result += "     ";
			}
			result += type + " ";
			if (value != null)
				result += value;
			if (subItems != null) {
				for (Item Item : subItems) {
					result += Item.getDebugInfo(include + 1);
				}
			}
			return result;
		}
	}

	static class SearchResult {
		Item item;
		int leftStart;
		int leftEnd;

		SearchResult(Item item, int leftStart, int leftEnd) {
			this.item = item;
			this.leftStart = leftStart;
			this.leftEnd = leftEnd;
		}
	}

	static class ParamPosition {
		int position = 0;
	}

	/**
	 * Parse a expression String, return an object result
	 * 
	 * @param bean Expression allow direct use only 1 bean's fields
	 * @param presetValues The preset values
	 * @param expression The expression
	 * @param params The expression parameter array
	 * @return an object result
	 */
	public Object doParse(Object bean, Map<String, Object> presetValues, String expression, Object... params) {
		if (StrUtils.isEmpty(expression))
			return null;
		char[] chars = (" " + expression + " ").toCharArray();
		List<Item> items = seperateCharsToItems(chars, 1, chars.length - 2);
		ParamPosition paramPosition = new ParamPosition();
		for (Item item : items) {
			correctType(item, bean, presetValues, paramPosition, params);
		}
		Item item = calculate(items);
		return item.value;
	}

	/** Separate chars to Items list */
	List<Item> seperateCharsToItems(char[] chars, int start, int end) {
		List<Item> items = new ArrayList<Item>();
		SearchResult result = findFirstResult(chars, start, end);
		while (result != null) {
			items.add(result.item);
			result = findFirstResult(chars, result.leftStart, result.leftEnd);
		}
		return items;
	}

	/** if is U type, use this method to correct type */
	void correctType(Item item, Object bean, Map<String, Object> presetValues, ParamPosition paramPostion,
			Object... params) {
		if (item.type == 'P') {
			item.guess(params[paramPostion.position++]);
		} else if (item.type == 'U') {// correct Unknown type to other type
			String valueStr = (String) item.value;
			String valueUpcase = valueStr.toUpperCase();
			// check is function
			if (functionMap.containsKey(valueUpcase)) {
				item.type = 'F';
				item.value = valueUpcase;
				item.priority = functionMap.get(valueUpcase);
			} else if (presetValues != null && presetValues.containsKey(valueUpcase)) {// is presetValues?
				item.guess(presetValues.get(valueUpcase));
			} else if (bean != null) {// check is bean fields
				Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(bean.getClass());
				if (readMethods.containsKey(valueStr))
					item.guess(ClassCacheUtils.readValueFromBeanField(bean, valueStr));
			}
			if (item.type == 'U')// still not found
				try { // is Long able?
					item.setTypeAndValue('L', Long.parseLong(valueUpcase));
				} catch (NumberFormatException e) {
					try {// is Double able?
						item.setTypeAndValue('D', Double.parseDouble(valueUpcase));
					} catch (NumberFormatException e1) { // is Boolean able?
						if ("TRUE".equalsIgnoreCase(valueUpcase)) {
							item.setTypeAndValue('B', true);
						} else if ("FALSE".equalsIgnoreCase(valueUpcase)) {
							item.setTypeAndValue('B', false);
						} else if ("NULL".equalsIgnoreCase(valueUpcase)) {
							item.setTypeAndValue('N', null);
						}
					}
				}
			if (item.type == 'U')// still not found
				throw new TinyParserException("Unrecognized expression near '" + valueStr + "'");
		}
		if (item.subItems != null)
			for (Item t : item.subItems)
				correctType(t, bean, presetValues, paramPostion, params);
	}

	/**
	 * Find first item and store left start and left end position in SearchResult
	 */
	SearchResult findFirstResult(char[] chars, int start, int end) {
		if (start > end)
			return null;
		boolean letters = false;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i <= end; i++) {
			if (!letters) {// no letters found
				if (chars[i] == '?') {
					Item item = new Item();
					item.type = 'P';
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
					throw new TinyParserException("Miss right ' charactor in expression.");
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
									item.type = '(';
									item.subItems = subItems;
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
					throw new TinyParserException("Miss right ) charactor in expression.");
				} else if (chars[i] > ' ') {
					letters = true;
					sb.append(chars[i]);
				}
			} else {// letters found
				if (chars[i] == '?' || chars[i] == '\'' || chars[i] == '(' || chars[i] <= ' '
						|| TinyParserUtils.isLetterNumber(chars[i]) != TinyParserUtils.isLetterNumber(chars[i - 1])) {
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

	/** Calculate items list into one item */
	Item calculate(List<Item> items) {
		for (Item item : items) {
			if (item.subItems != null) {
				Item newSubItem = calculate(item.subItems);
				item.type = newSubItem.type;
				item.value = newSubItem.value;
				item.subItems = null;
			}
		} // now there is no subItems

		// find highest priority function
		int functionPos;
		functionPos = -1;
		int priority = 100;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).type == 'F' && items.get(i).priority < priority) {
				functionPos = i;
				priority = items.get(i).priority;
			}
		}
		while (functionPos != -1) {
			doCalculate(items, functionPos);
			functionPos = -1;
			priority = 100;
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).type == 'F' && items.get(i).priority < priority) {
					functionPos = i;
					priority = items.get(i).priority;
				}
			}
		} // until all function be calculated

		int found = 0;
		Item result = null;
		for (Item item : items) {
			if (item.type != '0') {
				result = item;
				if (++found > 1)
					throw new TinyParserException("More than 1  calculated result found");
			}
		}
		if (result == null)
			throw new TinyParserException("No calculated result found");
		return result;

	}

	/** Execute the function and mark some items to '0' means deleted it */
	void doCalculate(List<Item> items, int functionPos) {
		Item lastItem = null;
		Item nextItem = null;
		for (int i = functionPos - 1; i >= 0; i--) {
			if (items.get(i).type != '0') {
				lastItem = items.get(i);
				break;
			}
		}
		for (int i = functionPos + 1; i < items.size(); i++) {
			if (items.get(i).type != '0') {
				nextItem = items.get(i);
				break;
			}
		}
		TinyParserUtils.doTheMath(items.get(functionPos), lastItem, nextItem);
	}

}