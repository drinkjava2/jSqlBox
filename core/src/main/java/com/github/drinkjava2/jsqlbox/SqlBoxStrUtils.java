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
package com.github.drinkjava2.jsqlbox;

/**
 * This utility class store public static String methods for jSqlBox project
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class SqlBoxStrUtils { // NOSONAR

	/**
	 * Build "?,?,?....?" String according given howmanyQuestions
	 * 
	 * @param howmanyQuestions
	 * @return "?,?,?....?" String
	 */
	public static String getQuestionsStr(int howmanyQuestions) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < howmanyQuestions; i++) {
			sb.append("?,");
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
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

}
