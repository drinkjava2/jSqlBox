/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro.template;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.PreparedSQL;

/**
 * BasicSqlTemplate is a simple implementation of SqlTemplateEngine. It allow
 * use #{xxxx} format parameters in template, and replace ${xxxx} pieces
 * directly. This is a thread safe class.
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class BasicSqlTemplate implements SqlTemplateEngine {
	/**
	 * If set true, for ${placeHolder} in template, should use
	 * put("$placeHolder",value) instead of use put("placeHolder",value) <br/>
	 * Default is false
	 */
	protected Boolean dollarKeyForDollarPlaceHolder = false;

	/**
	 * If set true, ${placeHolder} can write as :placeHolder <br/>
	 * Default is true
	 */
	protected Boolean allowColonAsDelimiter = true;

	private String startDelimiter = "#{";
	private String endDelimiter = "}";

	private static final String DIRECT_REPLACE_START_DELIMITER = "${";
	private static final String DIRECT_REPLACE_END_DELIMITER = "}";

	/** A lazy initialization singleton pattern */
	private static class InnerBasicSqlTemplate {
		private InnerBasicSqlTemplate() {
		}

		private static final BasicSqlTemplate INSTANCE = new BasicSqlTemplate();
	}

	/** @return A singleton instance of BasicSqlTemplate */
	public static BasicSqlTemplate instance() {
		return InnerBasicSqlTemplate.INSTANCE;
	}

	/**
	 * Build a BasicSqlTemplate instance, default use #{} as delimiter,
	 * dollarKeyForDollarPlaceHolder is false, allow
	 */
	public BasicSqlTemplate() {
	}

	/**
	 * Build a BasicSqlTemplate instance by given startDelimiter and endDelimiter,
	 * startDelimiter should be 1 or 2 characters and endDelimiter should be 1
	 * character
	 * 
	 * @param startDelimiter
	 *            The start delimiter
	 * @param endDelimiter
	 *            The end delimiter
	 */
	/**
	 * @param startDelimiter
	 *            The start delimiter
	 * @param endDelimiter
	 *            The end delimiter
	 * @param allowColonAsDelimiter
	 *            If set true, write :placeHolder is equal to #{placeHolder}
	 * @param dollarKeyForDollarPlaceHolder
	 *            If set true, ${placeHolder} should use put("$placeHolder",value)
	 *            instead of use put("placeHolder",value)
	 */
	public BasicSqlTemplate(String startDelimiter, String endDelimiter, Boolean allowColonAsDelimiter,
			Boolean dollarKeyForDollarPlaceHolder) {
		if (isEmpty(startDelimiter) || isEmpty(endDelimiter) || startDelimiter.length() > 2
				|| endDelimiter.length() != 1)
			throw new BasicSqlTemplateException(
					"BasicSqlTemplate only support startDelimiter has 1 or 2 characters and endDelimiter has 1 character");
		this.startDelimiter = startDelimiter;
		this.endDelimiter = endDelimiter;
		this.allowColonAsDelimiter = allowColonAsDelimiter;
		this.dollarKeyForDollarPlaceHolder = dollarKeyForDollarPlaceHolder;
	}

	private static boolean isParamChars(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.';
	}

	@Override
	public PreparedSQL render(String sqlTemplate, Map<String, Object> paramMap, Object[] unbindParams) {
		if (paramMap == null)
			throw new BasicSqlTemplateException("In BasicSqlTemplate, paramMap can not be null");
		if (!paramMap.isEmpty() && unbindParams != null && unbindParams.length > 0)
			throw new BasicSqlTemplateException(
					"Can not use paramMap or unbindParams at same time in BasicSqlTemplate.");
		String newSql = sqlTemplate;
		if (allowColonAsDelimiter)
			newSql = translateColonToDelimiter(sqlTemplate);
		return doRender(newSql, paramMap, unbindParams);
	}

	/**
	 * Render a template with
	 * 
	 * @param template
	 *            A SQL Template String
	 * @param paramMap
	 *            A Map stored SQL parameters
	 * @param unbindedParams
	 *            Optional, unbinded params,
	 * @return A PreparedSQL instance which filled SQL and Params
	 */
	private PreparedSQL doRender(String template, Map<String, Object> paramMap, Object[] unbindParams) {
		if (template == null)
			throw new NullPointerException("Template can not be null");
		int unbindParamPos = 0;
		List<Object> realUnbindParam = null;// unbindParams has some direct replace String, need kick out
		boolean unbindMode = unbindParams != null && unbindParams.length > 0;
		if (unbindMode)
			realUnbindParam = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		StringBuilder keyNameSB = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		char[] chars = ("   " + template + "   ").toCharArray();

		int lg = startDelimiter.length();
		char start1 = startDelimiter.charAt(0);
		char start2 = '\u0000';
		if (lg == 2)
			start2 = startDelimiter.charAt(1);
		char e = endDelimiter.charAt(0);

		int drlg = DIRECT_REPLACE_START_DELIMITER.length();
		char drst1 = DIRECT_REPLACE_START_DELIMITER.charAt(0);
		char drst2 = '\u0000';
		if (drlg == 2)
			drst2 = DIRECT_REPLACE_START_DELIMITER.charAt(1);
		char eDirect = DIRECT_REPLACE_END_DELIMITER.charAt(0);

		// - - # { - - - } - - - $ { - - - } - -
		// - - @ [ - - - ] - - - $ { - - - } - -
		// 0 0 1 1 2 2 2 3 0 0 0 1 1 2 2 2 3 0 0
		// - - - - - - - - - - - D D D D D - - -
		int status = 0; // 0:normal 1:start-delimiter 2:inside 3: end-delimiter
		boolean directRep = false; // direct replace tag
		for (int i = 3; i < chars.length - 2; i++) {
			char c = chars[i];
			char c1 = chars[i + 1];
			char cm1 = chars[i - 1];
			char cm2 = chars[i - 2];
			if (status == 0 && ((lg == 1 && c == start1) || (lg == 2 && c == start1 && c1 == start2))) {
				status = 1;
				keyNameSB.setLength(0);
				directRep = false;
			} else if (status == 0 && ((drlg == 1 && c == drst1) || (drlg == 2 && c == drst1 && c1 == drst2))) {
				status = 1;
				keyNameSB.setLength(0);
				directRep = true;
			} else if (status == 1 && ((lg == 1 && cm1 == start1) || (lg == 2 && (cm2 == start1 && cm1 == start2)))) {
				status = 2;
			} else if (status == 1 && ((drlg == 1 && cm1 == drst1) || (drlg == 2 && (cm2 == drst1 && cm1 == drst2)))) {
				status = 2;
			} else if (status == 3 && (((cm1 == e) && !directRep) || ((cm1 == eDirect) && directRep))) {
				status = 0;
			} else if (status == 2 && (((c == e) && !directRep) || ((c == eDirect) && directRep))) {
				status = 3;
				if (keyNameSB.length() == 0)
					throwEX("Empty parameter name '" + startDelimiter + endDelimiter + "' found in template: "
							+ template);
				String key = keyNameSB.toString();
				if (key.indexOf('.') >= 0) {// JavaBean
					String beanName = substringBefore(key, ".");
					String propertyName = substringAfter(key, ".");
					if (isEmpty(beanName) || isEmpty(propertyName))
						throwEX("illegal parameter name '" + key + "' found in template: " + template);

					String paramKey = (directRep && dollarKeyForDollarPlaceHolder) ? "$" + beanName : beanName;
					boolean hasValue = paramMap.containsKey(paramKey);

					if (unbindMode) {// unbindMode
						if (directRep)
							sql.append(unbindParams[unbindParamPos++]);
						else {
							sql.append("?");
							realUnbindParam.add(unbindParams[unbindParamPos++]);
						}
					} else {// template mode
						if (!hasValue)
							throwEX("Not found bean '" + paramKey + "' when render template: " + template);
						Object bean = paramMap.get(paramKey);
						PropertyDescriptor pd = null;
						try {
							pd = new PropertyDescriptor(propertyName, bean.getClass());
						} catch (IntrospectionException e1) {
							throwEX("IntrospectionException happen when get bean property '" + key + "' in template: "
									+ template, e1);
						}
						Method method = pd.getReadMethod();
						Object beanProperty = null;
						try {
							beanProperty = method.invoke(bean);
						} catch (Exception e1) {
							throwEX("Exception happen when read bean property '" + key + "' in template: " + template,
									e1);
						}
						if (directRep) {
							sql.append(beanProperty);
						} else {
							sql.append("?");
							paramList.add(beanProperty);
						}
					}
				} else {
					if (unbindMode) {
						if (directRep)
							sql.append(unbindParams[unbindParamPos++]);
						else {
							sql.append("?");
							realUnbindParam.add(unbindParams[unbindParamPos++]);
						}
					} else { // template mode
						String paramKey = (directRep && dollarKeyForDollarPlaceHolder) ? "$" + key : key;
						if (!paramMap.containsKey(paramKey)) {
							throwEX("No parameter bind for '" + paramKey + "' in template: " + template);
						}
						if (directRep) {
							sql.append(paramMap.get(paramKey));
						} else {
							sql.append("?");
							paramList.add(paramMap.get(paramKey));
						}
					}
				}
				keyNameSB.setLength(0);
			}
			if (status == 0)
				sql.append(c);
			else if (status == 2)
				keyNameSB.append(c);
		}
		if (status != 0)
			throwEX("Missing end delimiter '" + endDelimiter + "' in template: " + template);
		PreparedSQL sp = new PreparedSQL();
		sql.setLength(sql.length() - 1);
		sp.setSql(sql.toString());
		if (unbindMode)
			sp.setParams(realUnbindParam.toArray(new Object[realUnbindParam.size()]));
		else
			sp.setParams(paramList.toArray());
		return sp;
	}

	/** Translate colon to delimiter, for example: user to #{user} */
	private String translateColonToDelimiter(String sql) {
		StringBuilder sb = new StringBuilder();
		int status = 0;// status 0:normal, 1:in parameter
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			if (status == 0 && c != ':')
				sb.append(c);
			else if (status == 0 && c == ':') {
				sb.append(startDelimiter);
				status = 1;
			} else if (status == 1 && isParamChars(c))
				sb.append(c);
			else {
				sb.append(endDelimiter).append(c);
				status = 0;
			}
		}
		if (status == 1)
			sb.append(endDelimiter);
		return sb.toString();
	}

	private static void throwEX(String message, Exception... cause) {
		if (cause != null && cause.length > 0)
			throw new BasicSqlTemplateException(message, cause[0]);
		else
			throw new BasicSqlTemplateException(message);
	}

	// Below 3 methods are copied from org.apache.commons.lang3.StringUtils
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if a CharSequence is empty ("") or null.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *
	 * <p>
	 * NOTE: This method changed in Lang version 2.0. It no longer trims the
	 * CharSequence. That functionality is available in isBlank().
	 * </p>
	 *
	 * @param cs
	 *            the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets the substring before the first occurrence of a separator. The separator
	 * is not returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} string input will return {@code null}. An empty ("") string
	 * input will return the empty string. A {@code null} separator will return the
	 * input string.
	 * </p>
	 *
	 * <p>
	 * If nothing is found, the string input is returned.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringBefore(null, *)      = null
	 * StringUtils.substringBefore("", *)        = ""
	 * StringUtils.substringBefore("abc", "a")   = ""
	 * StringUtils.substringBefore("abcba", "b") = "a"
	 * StringUtils.substringBefore("abc", "c")   = "ab"
	 * StringUtils.substringBefore("abc", "d")   = "abc"
	 * StringUtils.substringBefore("abc", "")    = ""
	 * StringUtils.substringBefore("abc", null)  = "abc"
	 * </pre>
	 *
	 * @param str
	 *            the String to get a substring from, may be null
	 * @param separator
	 *            the String to search for, may be null
	 * @return the substring before the first occurrence of the separator,
	 *         {@code null} if null String input
	 */
	public static String substringBefore(final String str, final String separator) {
		if (isEmpty(str) || separator == null) {
			return str;
		}
		if (separator.isEmpty()) {
			return "";
		}
		final int pos = str.indexOf(separator);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	/**
	 * <p>
	 * Gets the substring after the first occurrence of a separator. The separator
	 * is not returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} string input will return {@code null}. An empty ("") string
	 * input will return the empty string. A {@code null} separator will return the
	 * empty string if the input string is not {@code null}.
	 * </p>
	 *
	 * <p>
	 * If nothing is found, the empty string is returned.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringAfter(null, *)      = null
	 * StringUtils.substringAfter("", *)        = ""
	 * StringUtils.substringAfter(*, null)      = ""
	 * StringUtils.substringAfter("abc", "a")   = "bc"
	 * StringUtils.substringAfter("abcba", "b") = "cba"
	 * StringUtils.substringAfter("abc", "c")   = ""
	 * StringUtils.substringAfter("abc", "d")   = ""
	 * StringUtils.substringAfter("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str
	 *            the String to get a substring from, may be null
	 * @param separator
	 *            the String to search for, may be null
	 * @return the substring after the first occurrence of the separator,
	 *         {@code null} if null String input
	 */
	public static String substringAfter(final String str, final String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (separator == null) {
			return "";
		}
		final int pos = str.indexOf(separator);
		if (pos == -1) {
			return "";
		}
		return str.substring(pos + separator.length());
	}

	public static class BasicSqlTemplateException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public BasicSqlTemplateException(String msg) {
			super(msg);
		}

		public BasicSqlTemplateException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
}
