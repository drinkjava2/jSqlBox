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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TranslateUtil parse a Sql, translate all universal functions like fn_sin() to
 * native SQL functions like sin()
 * 
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class DialectFunctionTranslator {
	public static final DialectFunctionTranslator instance = new DialectFunctionTranslator();
	private Map<String, Integer> functionMap = new HashMap<String, Integer>();

	/**
	 * Register functions names need translated, values is percentage of dialects
	 * support this function, value not used for this project
	 */
	public DialectFunctionTranslator() {
		functionMap.put("ABS", 100);// Not necessary, all dialects are same
		functionMap.put("AVG", 100);
		functionMap.put("BIT_LENGTH", 100);
		functionMap.put("CAST", 100);// Not necessary
		functionMap.put("COALESCE", 100);
		functionMap.put("COUNT", 100);// Not necessary
		functionMap.put("DAY", 100);
		functionMap.put("EXTRACT", 100);
		functionMap.put("HOUR", 100);
		functionMap.put("LENGTH", 100);
		functionMap.put("LOCATE", 100);
		functionMap.put("LOWER", 100);// Not necessary
		functionMap.put("MAX", 100);// Not necessary
		functionMap.put("MIN", 100);// Not necessary
		functionMap.put("MINUTE", 100);
		functionMap.put("MOD", 100);
		functionMap.put("MONTH", 100);
		functionMap.put("NULLIF", 100);
		functionMap.put("SECOND", 100);
		functionMap.put("SQRT", 100);
		functionMap.put("STR", 100);
		functionMap.put("SUBSTRING", 100);
		functionMap.put("SUM", 100);// Not necessary
		functionMap.put("TRIM", 100);
		functionMap.put("UPPER", 100);// Not necessary
		functionMap.put("YEAR", 100);
		functionMap.put("CONCAT", 93);
		functionMap.put("COS", 83);
		functionMap.put("EXP", 83);
		functionMap.put("SIN", 83);
		functionMap.put("LOG", 81);
		functionMap.put("ROUND", 81);
		functionMap.put("ATAN", 80);
		functionMap.put("SIGN", 80);
		functionMap.put("ACOS", 79);
		functionMap.put("ASIN", 79);
		functionMap.put("CURRENT_DATE", 79);
		functionMap.put("FLOOR", 79);
		functionMap.put("TAN", 79);
		functionMap.put("CURRENT_TIMESTAMP", 76);
		functionMap.put("CURRENT_TIME", 73);
		functionMap.put("COT", 69);
		functionMap.put("ASCII", 68);
		functionMap.put("RTRIM", 67);
		functionMap.put("LN", 65);
		functionMap.put("LTRIM", 65);
		functionMap.put("DEGREES", 64);
		functionMap.put("RADIANS", 63);
		functionMap.put("RAND", 63);
		functionMap.put("CEIL", 61);
		functionMap.put("SOUNDEX", 56);
		functionMap.put("USER", 56);
		functionMap.put("LOG10", 52);
		functionMap.put("SUBSTR", 51);
		functionMap.put("CEILING", 49);
		functionMap.put("STDDEV", 49);
		functionMap.put("NOW", 45);
		functionMap.put("CHAR_LENGTH", 44);
		functionMap.put("CHR", 44);
		functionMap.put("DAYOFYEAR", 44);
		functionMap.put("OCTET_LENGTH", 43);
		functionMap.put("PI", 43);
		functionMap.put("WEEK", 43);
		functionMap.put("DAYNAME", 41);
		functionMap.put("DAYOFWEEK", 41);
		functionMap.put("LCASE", 41);
		functionMap.put("MONTHNAME", 41);
		functionMap.put("QUARTER", 41);
		functionMap.put("SPACE", 41);
		functionMap.put("SYSDATE", 41);
		functionMap.put("UCASE", 41);
		functionMap.put("CHAR", 39);
		functionMap.put("REVERSE", 39);
		functionMap.put("HEX", 37);
		functionMap.put("LAST_DAY", 37);
		functionMap.put("MD5", 37);
		functionMap.put("TIME", 37);
		functionMap.put("TIMESTAMP", 37);
		functionMap.put("DATE", 36);
		functionMap.put("TRUNC", 36);
		functionMap.put("VARIANCE", 36);
		functionMap.put("INITCAP", 35);
		functionMap.put("POWER", 35);
		functionMap.put("DAYOFMONTH", 33);
		functionMap.put("ATAN2", 31);
		functionMap.put("CHARACTER_LENGTH", 31);
		functionMap.put("CURDATE", 29);
		functionMap.put("CURTIME", 29);
		functionMap.put("DATEDIFF", 29);
		functionMap.put("REPLACE", 29);
		functionMap.put("TO_DATE", 29);
		functionMap.put("LOCALTIME", 28);
		functionMap.put("LOCALTIMESTAMP", 28);
		functionMap.put("NVL", 28);
		functionMap.put("TO_CHAR", 28);
		functionMap.put("LPAD", 27);
		functionMap.put("RPAD", 27);
		functionMap.put("BIN", 24);
		functionMap.put("ENCRYPT", 24);
		functionMap.put("FROM_DAYS", 24);
		functionMap.put("LOG2", 24);
		functionMap.put("TIMEDIFF", 24);
		functionMap.put("TO_DAYS", 24);
		functionMap.put("WEEKOFYEAR", 24);
		functionMap.put("CRC32", 23);
		functionMap.put("INSTR", 23);
		functionMap.put("ISNULL", 23);
		functionMap.put("LEN", 23);
		functionMap.put("OCT", 23);
		functionMap.put("TRANSLATE", 23);
		functionMap.put("MICROSECOND", 21);
		functionMap.put("RIGHT", 21);
		functionMap.put("CURRENT_USER", 20);
		functionMap.put("RANDOM", 20);
		functionMap.put("SESSION_USER", 20);
		functionMap.put("LEFT", 19);
		functionMap.put("DATE_TRUNC", 17);
		functionMap.put("UNHEX", 17);
		functionMap.put("WEEKDAY", 17);
		functionMap.put("CURRENT_SCHEMA", 16);
		functionMap.put("POSITION", 16);
		functionMap.put("TO_TIMESTAMP", 16);
		functionMap.put("ADD_MONTHS", 15);
		functionMap.put("BIT_COUNT", 15);
		functionMap.put("COSH", 15);
		functionMap.put("FROM_UNIXTIME", 15);
		functionMap.put("GETDATE", 15);
		functionMap.put("QUOTE", 15);
		functionMap.put("SEC_TO_TIME", 15);
		functionMap.put("SINH", 15);
		functionMap.put("TANH", 15);
		functionMap.put("TIME_TO_SEC", 15);
		functionMap.put("TO_NUMBER", 15);
		functionMap.put("UNIX_TIMESTAMP", 15);
		functionMap.put("UTC_DATE", 15);
		functionMap.put("UTC_TIME", 15);
		functionMap.put("AGE", 13);
		functionMap.put("CBRT", 13);
		functionMap.put("CURRENT_DATABASE", 13);
		functionMap.put("DATABASE", 13);
		functionMap.put("DATENAME", 13);
		functionMap.put("DATE_FORMAT", 13);
		functionMap.put("DIFFERENCE", 13);
		functionMap.put("DOW", 13);
		functionMap.put("IFNULL", 13);
		functionMap.put("MICROSECONDS", 13);
		functionMap.put("NEXT_DAY", 13);
		functionMap.put("ORD", 13);
		functionMap.put("QUOTE_IDENT", 13);
		functionMap.put("QUOTE_LITERAL", 13);
		functionMap.put("REPLICATE", 13);
		functionMap.put("SHA", 13);
		functionMap.put("SHA1", 13);
		functionMap.put("TIMEOFDAY", 13);
		functionMap.put("TO_ASCII", 13);
		functionMap.put("TRUNCATE", 13);
		functionMap.put("UTC_TIMESTAMP", 13);
		functionMap.put("YEARWEEK", 13);
		functionMap.put("DATETIME", 12);
		functionMap.put("GETUTCDATE", 12);
		functionMap.put("MONTHS_BETWEEN", 12);
		functionMap.put("NVL2", 12);
		functionMap.put("REPEAT", 12);
		functionMap.put("ROWNUM", 12);
		functionMap.put("SQUARE", 12);
		functionMap.put("STUFF", 12);
		functionMap.put("BIGINT", 11);
		functionMap.put("COMPRESS", 11);
		functionMap.put("DAYS", 11);
		functionMap.put("DECRYPT", 11);
		functionMap.put("INSTRB", 11);
		functionMap.put("INTEGER", 11);
		functionMap.put("REAL", 11);
		functionMap.put("ROWID", 11);
		functionMap.put("SMALLINT", 11);
		functionMap.put("SYSTIMESTAMP", 11);
		functionMap.put("TIMESTAMPADD", 11);
		functionMap.put("TIMESTAMPDIFF", 11);
		functionMap.put("VARCHAR", 11);
	}

	public Map<String, Integer> getFunctionMap() {
		return functionMap;
	}

	public void setFunctionMap(Map<String, Integer> functionMap) {
		this.functionMap = functionMap;
	}

	/**
	 * DialectSqlItem type can be: <br/>
	 * S:String, F:function, U:Unknow(need correct), ",":","
	 * 
	 * @author Yong Zhu
	 * @since 1.7.0
	 */
	static class DialectSqlItem {
		public char type;// NOSONAR
		public Object value;// NOSONAR

		DialectSqlItem[] subItems;

		void setTypeAndValue(char type, Object value) {
			this.type = type;
			this.value = value;
		}

		/**
		 * Only for debug purpose, show detail info of DialectSqlItem
		 */
		String getDebugInfo(int include) {
			String result = "\r";
			for (int i = 0; i < include; i++) {
				result += "     ";//NOSONAR
			}
			result += type + " ";
			if (value != null)
				result += value;
			if (subItems != null) {
				for (DialectSqlItem Item : subItems) {
					result += Item.getDebugInfo(include + 1);// NOSONAR
				}
			}
			return result;
		}

	}

	static class SearchResult {
		DialectSqlItem item;
		int leftStart;
		int leftEnd;

		SearchResult(DialectSqlItem item, int leftStart, int leftEnd) {
			this.item = item;
			this.leftStart = leftStart;
			this.leftEnd = leftEnd;
		}
	}

	static class ParamPosition {
		int position = 0;
	}

	/**
	 * Translate universal SQL to native SQL, all #xxx() format universal SQL
	 * functions will be translate to xxx() native SQL functions
	 */
	public String doTranslate(Dialect d, String sql) {
		if (StrUtils.isEmpty(sql))
			return sql;
		// if prefix not empty and SQL not include prefix, directly return
		if (!StrUtils.isEmpty(Dialect.getGlobalSqlFunctionPrefix())
				&& !StrUtils.containsIgnoreCase(sql, Dialect.getGlobalSqlFunctionPrefix()))
			return sql;
		char[] chars = (" " + sql + " ").toCharArray();
		DialectSqlItem[] items = seperateCharsToItems(chars, 1, chars.length - 2);
		for (DialectSqlItem item : items) {
			correctType(item);
		}
		String result = join(d, true, null, items);
		if (Dialect.getGlobalAllowShowSql())
			Dialect.logger.info("Translated sql: " + result);
		return result;
	}

	/** Separate chars to Items list */
	DialectSqlItem[] seperateCharsToItems(char[] chars, int start, int end) {
		List<DialectSqlItem> items = new ArrayList<DialectSqlItem>();
		SearchResult result = findFirstResult(chars, start, end);
		while (result != null) {
			items.add(result.item);
			result = findFirstResult(chars, result.leftStart, result.leftEnd);
		}
		return items.toArray(new DialectSqlItem[items.size()]);
	}

	/** if is U type, use this method to correct type */
	void correctType(DialectSqlItem item) {//NOSONAR
		if (item.type == 'U') {// correct Unknown type to other type
			String valueStr = (String) item.value;
			String valueUpcase = valueStr.toUpperCase();
			// check is function
			String funPrefix = Dialect.getGlobalSqlFunctionPrefix();
			if (!StrUtils.isEmpty(valueUpcase)) {
				if (!StrUtils.isEmpty(funPrefix) && StrUtils.startsWithIgnoreCase(valueUpcase, funPrefix)
						&& functionMap.containsKey(valueUpcase.substring(funPrefix.length()))) {
					item.type = 'F';
					item.value = valueStr.substring(funPrefix.length());
				}

				if ((StrUtils.isEmpty(funPrefix) && functionMap.containsKey(valueUpcase))) {
					item.type = 'F';
					item.value = valueStr;
				}
			}
			if (item.type == 'U')// still not found
				if (",".equals(valueStr))
					// is Long able?
					item.setTypeAndValue(',', valueStr);
				else
					item.setTypeAndValue('S', valueStr);
		}
		if (item.subItems != null)
			for (DialectSqlItem t : item.subItems)
				correctType(t);
	}

	/**
	 * Find first item and store left start and left end position in SearchResult
	 */
	SearchResult findFirstResult(char[] chars, int start, int end) {//NOSONAR
		if (start > end)
			return null;
		boolean letters = false;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i <= end; i++) {
			if (!letters) {// no letters found

				if (chars[i] == ' ') {
					DialectSqlItem item = new DialectSqlItem();
					item.type = 'S';
					item.value = " ";
					return new SearchResult(item, i + 1, end);
				}

				if (chars[i] == '?') {
					DialectSqlItem item = new DialectSqlItem();
					item.type = 'S';
					item.value = "?";
					return new SearchResult(item, i + 1, end);
				}

				if (chars[i] == '\'') {
					for (int j = i + 1; j <= end; j++) {
						if (chars[j] == '\'' && chars[j - 1] != '\\') {
							DialectSqlItem item = new DialectSqlItem();
							item.type = 'S';
							item.value = sb.insert(0, '\'').append('\'').toString();
							return new SearchResult(item, j + 1, end);
						} else
							sb.append(chars[j]);
					}
					throw new DialectException("Miss right ' charactor in SQL.");
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
									DialectSqlItem[] subItems = seperateCharsToItems(chars, i + 1, j - 1);
									DialectSqlItem item = new DialectSqlItem();
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
					throw new DialectException("Miss right ) charactor in SQL.");
				} else if (chars[i] > ' ') {
					letters = true;
					sb.append(chars[i]);
				}
			} else {// letters found
				if (chars[i] == '?' || chars[i] == '\'' || chars[i] == '(' || chars[i] <= ' '
						|| isLetterNumber(chars[i]) != isLetterNumber(chars[i - 1])) {
					DialectSqlItem item = new DialectSqlItem();
					item.type = 'U';
					item.value = sb.toString();
					return new SearchResult(item, i, end);
				} else {
					sb.append(chars[i]);
				}
			}
		}
		if (sb.length() > 0) {
			DialectSqlItem item = new DialectSqlItem();
			item.type = 'U';
			item.value = sb.toString();
			return new SearchResult(item, end + 1, end);
		} else
			return null;
	}

	/**
	 * Join items list into one String, if function is null, join as String,
	 * otherwise treat as function parameters
	 */
	String join(Dialect d, boolean isTopLevel, DialectSqlItem function, DialectSqlItem[] items) {//NOSONAR
		int pos = 0;
		for (DialectSqlItem item : items) {
			if (item.subItems != null) {
				String value;
				if (pos > 0 && items[pos - 1] != null && items[pos - 1].type == 'F')
					// join as parameters
					value = join(d, false, items[pos - 1], item.subItems);
				else
					value = join(d, false, null, item.subItems); // join as
																	// string
				item.type = 'S';
				item.value = value;
				item.subItems = null;
			}
			pos++;
		} // now there is no subItems

		if (function != null) {
			List<String> l = new ArrayList<String>();
			for (DialectSqlItem item : items) {
				if (item.type != '0')
					l.add((String) item.value);
			}
			return renderFunction(d, function, l.toArray(new String[l.size()]));
		}

		StringBuilder sb = new StringBuilder();
		if (!isTopLevel)
			sb.append("(");
		for (DialectSqlItem item : items)
			if (item.type != '0') {
				sb.append(item.value);
			}
		if (!isTopLevel)
			sb.append(")");
		return sb.toString();
	}

	private static String renderFunction(Dialect d, DialectSqlItem function, String... params) {
		function.type = '0';
		List<String> l = new ArrayList<String>();
		String current = "";
		for (String param : params) {
			if (",".equals(param)) {
				l.add(current);
				current = "";
			} else
				current += param;// NOSONAR
		}
		String lastValue = current.trim();
		if (lastValue.length() > 0)
			l.add(current);
		return DialectFunctionUtils.render(d, (String) function.value, l.toArray(new String[l.size()]));
	}

	public static void deleteItem(DialectSqlItem item) {
		if (item != null)
			item.type = '0';
	}

	public static void deleteItem(DialectSqlItem lastItem, DialectSqlItem nextItem) {
		if (lastItem != null)
			lastItem.type = '0';
		if (nextItem != null)
			nextItem.type = '0';
	}

	// ==================String Utils below======================

	public static boolean isLetterNumber(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.'
				|| c == '@' || c == '#' || c == '$' || c == '+' || c == '-';
	}

}