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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * String Utilities usually used inside of framework
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class StrUtils {
	public static final String WHITESPACE = " \n\r\f\t";

	private StrUtils() {
		// default constructor
	}

	/**
	 * Check whether the given String is empty.
	 */
	public static boolean isEmpty(Object str) {
		return str == null || "".equals(str);
	}

	/**
	 * Check that the given CharSequence is neither {@code null} nor of length 0.
	 * Note: Will return {@code true} for a CharSequence that purely consists of
	 * whitespace.
	 * <p>
	 * 
	 * <pre class="code">
	 * StrUtils.hasLength(null) = false
	 * StrUtils.hasLength("") = false
	 * StrUtils.hasLength(" ") = true
	 * StrUtils.hasLength("Hello") = true
	 * </pre>
	 * 
	 * @param str
	 *            the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence is not null and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return str != null && str.length() > 0;
	}

	/**
	 * Check that the given String is neither {@code null} nor of length 0. Note:
	 * Will return {@code true} for a String that purely consists of whitespace.
	 * 
	 * @param str
	 *            the String to check (may be {@code null})
	 * @return {@code true} if the String is not null and has length
	 * @see #hasLength(CharSequence)
	 */
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * Check whether the given CharSequence contains any whitespace characters.
	 * 
	 * @param str
	 *            the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence is not empty and contains at least 1
	 *         whitespace character
	 * @see Character#isWhitespace
	 */
	public static boolean containsWhitespace(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given String contains any whitespace characters.
	 * 
	 * @param str
	 *            the String to check (may be {@code null})
	 * @return {@code true} if the String is not empty and contains at least 1
	 *         whitespace character
	 * @see #containsWhitespace(CharSequence)
	 */
	public static boolean containsWhitespace(String str) {
		return containsWhitespace((CharSequence) str);
	}

	/**
	 * Trim leading and trailing whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trim <i>all</i> whitespace from the given String: leading, trailing, and in
	 * between characters.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimAllWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		int index = 0;
		while (sb.length() > index) {
			if (Character.isWhitespace(sb.charAt(index))) {
				sb.deleteCharAt(index);
			} else {
				index++;
			}
		}
		return sb.toString();
	}

	/**
	 * Trim leading whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim trailing whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurrences of the supplied leading character from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @param leadingCharacter
	 *            the leading character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurrences of the supplied trailing character from the given
	 * String.
	 * 
	 * @param str
	 *            the String to check
	 * @param trailingCharacter
	 *            the trailing character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Test if the given String starts with the specified prefix, ignoring
	 * upper/lower case.
	 * 
	 * @param str
	 *            the String to check
	 * @param prefix
	 *            the prefix to look for
	 * @see java.lang.String#startsWith
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}

	/**
	 * Find if exist searchStr in str ignore case
	 */
	public static boolean containsIgnoreCase(String str, String searchStr) {
		if (str == null || searchStr == null)
			return false;
		final int length = searchStr.length();
		if (length == 0)
			return true;
		for (int i = str.length() - length; i >= 0; i--) {
			if (str.regionMatches(true, i, searchStr, 0, length))
				return true;
		}
		return false;
	}

	/**
	 * Replace all occurrences of a substring within a string with another string.
	 * 
	 * @param originString
	 *            The original String
	 * @param oldPattern
	 *            old String Pattern to replace
	 * @param newPattern
	 *            new String pattern to insert
	 * @return a String with the replacements
	 */
	public static String replace(String originString, String oldPattern, String newPattern) {
		if (!hasLength(originString) || !hasLength(oldPattern) || newPattern == null) {
			return originString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		int index = originString.indexOf(oldPattern);
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(originString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = originString.indexOf(oldPattern, pos);
		}
		sb.append(originString.substring(pos));
		return sb.toString();
	}

	/**
	 * Replace first occurrences of a substring within a string with another string.
	 * 
	 * @param originString
	 *            The original String
	 * @param oldPattern
	 *            old String Pattern to replace
	 * @param newPattern
	 *            new String pattern to insert
	 * @return a String with the replacements
	 */
	public static String replaceFirst(String originString, String oldPattern, String newPattern) {
		if (!hasLength(originString) || !hasLength(oldPattern) || newPattern == null) {
			return originString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		int index = originString.indexOf(oldPattern);
		int patLen = oldPattern.length();
		if (index >= 0) {
			sb.append(originString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
		}
		sb.append(originString.substring(pos));
		return sb.toString();
	}

	/**
	 * Replace all sub strings ignore case <br/>
	 * replaceIgnoreCase("AbcDECd", "Cd", "FF") = "AbFFEFF"
	 * 
	 */
	public static String replaceIgnoreCase(String text, String findtxt, String replacetxt) {
		if (text == null)
			return null;
		String str = text;
		if (findtxt == null || findtxt.length() == 0) {
			return str;
		}
		if (findtxt.length() > str.length()) {
			return str;
		}
		int counter = 0;
		String thesubstr;
		while ((counter < str.length()) && (str.substring(counter).length() >= findtxt.length())) {
			thesubstr = str.substring(counter, counter + findtxt.length());
			if (thesubstr.equalsIgnoreCase(findtxt)) {
				str = str.substring(0, counter) + replacetxt + str.substring(counter + findtxt.length());
				counter += replacetxt.length();
			} else {
				counter++;
			}
		}
		return str;
	}

	/**
	 * Return first postion ignore case, return -1 if not found
	 */
	public static int indexOfIgnoreCase(final String str, final String searchStr) {// NOSONAR
		if (searchStr.isEmpty() || str.isEmpty()) {
			return str.indexOf(searchStr);
		}
		for (int i = 0; i < str.length(); ++i) {
			if (i + searchStr.length() > str.length()) {
				return -1;
			}
			int j = 0;
			int ii = i;
			while (ii < str.length() && j < searchStr.length()) {
				char c = Character.toLowerCase(str.charAt(ii));
				char c2 = Character.toLowerCase(searchStr.charAt(j));
				if (c != c2) {
					break;
				}
				j++;
				ii++;
			}
			if (j == searchStr.length()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return last sub-String position ignore case, return -1 if not found
	 */
	public static int lastIndexOfIgnoreCase(String str, String searchStr) {
		if (searchStr.isEmpty() || str.isEmpty())
			return -1;
		return str.toLowerCase().lastIndexOf(searchStr.toLowerCase());
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separator specified. This is an
	 * alternative to using StringTokenizer.
	 * </p>
	 *
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator. For more control over the split use
	 * the StrTokenizer class.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.split(null, *)         = null
	 * StringUtils.split("", *)           = []
	 * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
	 * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
	 * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
	 * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
	 * </pre>
	 *
	 * @param str
	 *            the String to parse, may be null
	 * @param separatorChar
	 *            the character used as the delimiter
	 * @return an array of parsed Strings, {@code null} if null String input
	 * @since 2.0
	 */
	public static String[] split(final String str, final char separatorChar) {
		return splitWorker(str, separatorChar, false);
	}

	/**
	 * Performs the logic for the {@code split} and {@code splitPreserveAllTokens}
	 * methods that do not return a maximum array length.
	 *
	 * @param str
	 *            the String to parse, may be {@code null}
	 * @param separatorChar
	 *            the separate character
	 * @param preserveAllTokens
	 *            if {@code true}, adjacent separators are treated as empty token
	 *            separators; if {@code false}, adjacent separators are treated as
	 *            one separator.
	 * @return an array of parsed Strings, {@code null} if null String input
	 */
	private static String[] splitWorker(final String str, final char separatorChar, final boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return new String[0];
		}
		final int len = str.length();
		if (len == 0) {
			return new String[0];
		}
		final List<String> list = new ArrayList<String>();
		int i = 0;
		int start = 0;
		boolean match = false;
		boolean lastMatch = false;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match || preserveAllTokens) {
					list.add(str.substring(start, i));
					match = false;
					lastMatch = true;
				}
				start = ++i;
				continue;
			}
			lastMatch = false;
			match = true;
			i++;
		}
		if (match || preserveAllTokens && lastMatch) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}

	public static String[] split(String separators, String list) {
		return split(separators, list, false);
	}

	public static String[] split(String separators, String list, boolean include) {
		StringTokenizer tokens = new StringTokenizer(list, separators, include);
		String[] result = new String[tokens.countTokens()];
		int i = 0;
		while (tokens.hasMoreTokens()) {
			result[i++] = tokens.nextToken();
		}
		return result;
	}

	/**
	 * <p>
	 * Searches a String for substrings delimited by a start and end tag, returning
	 * all matching substrings in an array.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null} open/close
	 * returns {@code null} (no match). An empty ("") open/close returns
	 * {@code null} (no match).
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringsBetween("[a][b][c]", "[", "]") = ["a","b","c"]
	 * StringUtils.substringsBetween(null, *, *)            = null
	 * StringUtils.substringsBetween(*, null, *)            = null
	 * StringUtils.substringsBetween(*, *, null)            = null
	 * StringUtils.substringsBetween("", "[", "]")          = []
	 * </pre>
	 *
	 * @param str
	 *            the String containing the substrings, null returns null, empty
	 *            returns empty
	 * @param open
	 *            the String identifying the start of the substring, empty returns
	 *            null
	 * @param close
	 *            the String identifying the end of the substring, empty returns
	 *            null
	 * @return a String Array of substrings, or {@code null} if no match
	 * @since 2.3
	 */
	public static String[] substringsBetween(final String str, final String open, final String close) {
		if (str == null || isEmpty(open) || isEmpty(close)) {
			return new String[0];
		}
		final int strLen = str.length();
		if (strLen == 0) {
			return new String[0];
		}
		final int closeLen = close.length();
		final int openLen = open.length();
		final List<String> list = new ArrayList<String>();
		int pos = 0;
		while (pos < strLen - closeLen) {
			int start = str.indexOf(open, pos);
			if (start < 0) {
				break;
			}
			start += openLen;
			final int end = str.indexOf(close, start);
			if (end < 0) {
				break;
			}
			list.add(str.substring(start, end));
			pos = end + closeLen;
		}
		if (list.isEmpty()) {
			return new String[0];
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Gets the String that is nested in between two Strings. Only the first match
	 * is returned.
	 *
	 * A <code>null</code> input String returns <code>null</code>. A
	 * <code>null</code> open/close returns <code>null</code> (no match). An empty
	 * ("") open and close returns an empty string.
	 *
	 * <pre>
	 * StringUtils.substringBetween("wx[b]yz", "[", "]") = "b"
	 * StringUtils.substringBetween(null, *, *)          = null
	 * StringUtils.substringBetween(*, null, *)          = null
	 * StringUtils.substringBetween(*, *, null)          = null
	 * StringUtils.substringBetween("", "", "")          = ""
	 * StringUtils.substringBetween("", "", "]")         = null
	 * StringUtils.substringBetween("", "[", "]")        = null
	 * StringUtils.substringBetween("yabcz", "", "")     = ""
	 * StringUtils.substringBetween("yabcz", "y", "z")   = "abc"
	 * StringUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
	 * </pre>
	 *
	 * @param str
	 *            the String containing the substring, may be null
	 * @param open
	 *            the String before the substring, may be null
	 * @param close
	 *            the String after the substring, may be null
	 * @return the substring, <code>null</code> if no match
	 * @since 2.0
	 */
	public static String substringBetween(String str, String open, String close) {
		if (str == null || open == null || close == null) {
			return null;
		}
		int start = str.indexOf(open);
		if (start != -1) {
			int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}

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

	/**
	 * <p>
	 * Gets the substring after the last occurrence of a separator. The separator is
	 * not returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} string input will return {@code null}. An empty ("") string
	 * input will return the empty string. An empty or {@code null} separator will
	 * return the empty string if the input string is not {@code null}.
	 * </p>
	 *
	 * <p>
	 * If nothing is found, the empty string is returned.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringAfterLast(null, *)      = null
	 * StringUtils.substringAfterLast("", *)        = ""
	 * StringUtils.substringAfterLast(*, "")        = ""
	 * StringUtils.substringAfterLast(*, null)      = ""
	 * StringUtils.substringAfterLast("abc", "a")   = "bc"
	 * StringUtils.substringAfterLast("abcba", "b") = "a"
	 * StringUtils.substringAfterLast("abc", "c")   = ""
	 * StringUtils.substringAfterLast("a", "a")     = ""
	 * StringUtils.substringAfterLast("a", "z")     = ""
	 * </pre>
	 *
	 * @param str
	 *            the String to get a substring from, may be null
	 * @param separator
	 *            the String to search for, may be null
	 * @return the substring after the last occurrence of the separator,
	 *         {@code null} if null String input
	 * @since 2.0
	 */
	public static String substringAfterLast(final String str, final String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (isEmpty(separator)) {
			return "";
		}
		final int pos = str.lastIndexOf(separator);
		if (pos == -1 || pos == str.length() - separator.length()) {
			return "";
		}
		return str.substring(pos + separator.length());
	}

	/**
	 * <p>
	 * Gets the substring before the last occurrence of a separator. The separator
	 * is not returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} string input will return {@code null}. An empty ("") string
	 * input will return the empty string. An empty or {@code null} separator will
	 * return the input string.
	 * </p>
	 *
	 * <p>
	 * If nothing is found, the string input is returned.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringBeforeLast(null, *)      = null
	 * StringUtils.substringBeforeLast("", *)        = ""
	 * StringUtils.substringBeforeLast("abcba", "b") = "abc"
	 * StringUtils.substringBeforeLast("abc", "c")   = "ab"
	 * StringUtils.substringBeforeLast("a", "a")     = ""
	 * StringUtils.substringBeforeLast("a", "z")     = "a"
	 * StringUtils.substringBeforeLast("a", null)    = "a"
	 * StringUtils.substringBeforeLast("a", "")      = "a"
	 * </pre>
	 *
	 * @param str
	 *            the String to get a substring from, may be null
	 * @param separator
	 *            the String to search for, may be null
	 * @return the substring before the last occurrence of the separator,
	 *         {@code null} if null String input
	 * @since 2.0
	 */
	public static String substringBeforeLast(final String str, final String separator) {
		if (isEmpty(str) || isEmpty(separator))
			return str;
		final int pos = str.lastIndexOf(separator);
		if (pos == -1)
			return str;
		return str.substring(0, pos);
	}

	/**
	 * <p>
	 * Counts how many times the char appears in the given string.
	 * </p>
	 *
	 * <p>
	 * A {@code null} or empty ("") String input returns {@code 0}.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.countMatches(null, *)       = 0
	 * StringUtils.countMatches("", *)         = 0
	 * StringUtils.countMatches("abba", 0)  = 0
	 * StringUtils.countMatches("abba", 'a')   = 2
	 * StringUtils.countMatches("abba", 'b')  = 2
	 * StringUtils.countMatches("abba", 'x') = 0
	 * </pre>
	 *
	 * @param str
	 *            the CharSequence to check, may be null
	 * @param ch
	 *            the char to count
	 * @return the number of occurrences, 0 if the CharSequence is {@code null}
	 * @since 3.4
	 */
	public static int countMatches(final CharSequence str, final char ch) {
		if (isEmpty(str)) {
			return 0;
		}
		int count = 0;
		// We could also call str.toCharArray() for faster look ups but that
		// would generate more garbage.
		for (int i = 0; i < str.length(); i++) {
			if (ch == str.charAt(i)) {
				count++;
			}
		}
		return count;
	}

	private static final SecureRandom random = new SecureRandom();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
	
	public static char getRandomChar() {
		return ALPHABET[random.nextInt(32)];
	}

	public static String getRandomString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(ALPHABET[random.nextInt(32)]);
		}
		return sb.toString();
	}

	/**
	 * Compare 2 array
	 * 
	 * @return true if each item equal
	 */
	public static boolean arraysEqual(Object[] array1, Object[] array2) {
		if (array1 == null || array1.length == 0 || array2 == null || array2.length == 0)
			DialectException.throwEX("StrUtils arraysEqual() method can not compare empty arrays");
		for (int i = 0; array1 != null && array2 != null && i < array1.length; i++)
			if (!array1[i].equals(array2[i]))
				return false;
		return true;
	}

	/**
	 * Change a Object array to "obj1,obj2...,objn" String
	 */
	public static String arrayToStringButSkipFirst(Object[] array) {
		if (array == null)
			DialectException.throwEX("StrUtils arrayToString() method do not accept null parameter");
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for (Object object : array) {
			if (i++ != 1)
				sb.append("" + object + ",");

		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Change a Object array to "obj1,obj2...,objn" String
	 */
	public static String arrayToString(Object[] array) {
		if (array == null)
			DialectException.throwEX("StrUtils arrayToString() method do not accept null parameter");
		StringBuilder sb = new StringBuilder();
		for (Object object : array)
			sb.append("" + object + ",");
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Change a Object array to connected string by given seperateString
	 */
	public static String arrayToString(Object[] array, String seperateString) {
		if (array == null)
			DialectException.throwEX("StrUtils arrayToString() method do not accept null parameter");
		StringBuilder sb = new StringBuilder();
		for (Object object : array)
			sb.append("" + object + seperateString);
		if (sb.length() > 0)
			sb.setLength(sb.length() - seperateString.length());
		return sb.toString();
	}

	/**
	 * Change a Object List to "obj1,obj2...,objn" String
	 */
	public static String listToString(List<?> lst) {
		if (lst == null)
			DialectException.throwEX("StrUtils listToString() method do not accept null parameter");
		StringBuilder sb = new StringBuilder();
		for (Object object : lst)
			sb.append("" + object + ",");
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Join 2 String array into one
	 */
	public static String[] joinStringArray(String[] array1, String[] array2) {
		List<String> l = new ArrayList<String>();
		Collections.addAll(l, array1);
		Collections.addAll(l, array2);
		return l.toArray(new String[l.size()]);
	}

	/**
	 * Return true if first letter is Capitalised
	 */
	public static boolean isCapitalizedString(String str) {
		char c = str.substring(0, 1).toCharArray()[0];
		return c >= 'A' && c <= 'Z';
	}

	/**
	 * First letter change to lower
	 */
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * First letter change to capitalised
	 */
	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * Check if a String only have a-z,A-Z,0-9,"_" characters
	 */
	public static boolean isNormalLetters(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	/**
	 * Return true if is an invisible Char like space, tab, return... char
	 */
	public static boolean isInvisibleChar(char c) {
		return c <= ' ';
	}

	/**
	 * Format all " ", \t, \r... , to " ",
	 */
	public static String formatSQL(String sql) {
		if (sql == null || sql.length() == 0)
			return sql;
		StringBuilder sb = new StringBuilder();
		char[] chars = sql.toCharArray();
		boolean addedSpace = false;
		for (char c : chars) {
			if (isInvisibleChar(c)) {
				if (!addedSpace) {
					sb.append(" ");
					addedSpace = true;
				}
			} else {
				sb.append(c);
				addedSpace = false;
			}
		}
		sb.append(" ");
		return sb.toString();
	}

	/**
	 * Some column has quote chars, like `someCol` or "someCol" or [someCol] use
	 * this method to clear quote chars
	 */
	public static String clearQuote(String columnName) {
		if (StrUtils.isEmpty(columnName))
			return columnName;
		String s = StrUtils.replace(columnName, "`", "");
		s = StrUtils.replace(s, "\"", "");
		s = StrUtils.replace(s, "[", "");
		s = StrUtils.replace(s, "]", "");
		return s;
	}

	/** Simple replace danderous chars in String to avoid SQL injection attack */
	public static String simpleReplaceDangerous(String str) {
		str = str.replace(";", "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("'", "''").replace("--", "").replace("/", "").replace("%", "");
		return str;
	}

	/**
	 * Build a ('xxx',xxx,xxx, ..., xxx) format String based on a object array,
	 * usually used for an "in" condition SQL String, for example: <br/>
	 * 
	 * array("a","b",1,2) return String ('a','b',1,2)
	 * array() return String (null)
	 */
	public static String array(Object... arr) {
		if (arr==null || arr.length == 0) {
			return "(null)";
		}
		StringBuilder builder = new StringBuilder(200);
		builder.append("(");
		for (Object obj : arr) {
			if (obj == null)
				continue;
			Class<?> c = obj.getClass();
			if (String.class == c) {
				builder.append("'").append(simpleReplaceDangerous((String) obj)).append("',");
			} else {
				if (int.class == c || Integer.class == c || long.class == c || Long.class == c || short.class == c
						|| Short.class == c || byte.class == c || Byte.class == c)
					builder.append(obj).append(",");
			}
		}
		if (builder.length() > 1) {
			builder.setLength(builder.length() - 1);
			return builder.append(")").toString();
		}
		return "(null)";
	}

}
