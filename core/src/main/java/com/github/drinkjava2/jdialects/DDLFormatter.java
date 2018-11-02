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

/**
 * @author Yong Zhu
 * @since 2.0.4
 */
public class DDLFormatter {

	private static boolean isSpace(char c) {
		return c <= 32 || c == '\t' || c == '\r' || c == '\n';
	}

	public static String format(String template) {
		StringBuilder sb = new StringBuilder();
		char[] chars = ("  " + template + "  ").toCharArray();
		int inBrackets = 0;
		boolean justReturned = false;
		for (int i = 2; i < chars.length - 2; i++) {
			char c = chars[i];
			if (isSpace(c)) {
				if (!justReturned)
					sb.append(c);
			} else if (c == '(') {
				sb.append(c);
				if (inBrackets == 0) {
					sb.append("\n");
					justReturned = true;
				}
				inBrackets++;
			} else if (c == ')') {
				inBrackets--;
				if (inBrackets == 0) {
					sb.append("\n");
					justReturned = true;
				}
				sb.append(c);
			} else if (c == ',') {
				sb.append(c);
				if (inBrackets == 1) {
					sb.append("\n");
					justReturned = true;
				}
			} else {
				sb.append(c);
				justReturned = false;
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String ddl = "create table users("
				+ "id int(5), name varchar(10), \t \n age int, price float(10,3), address char)engine=innodb charset=utf8";
		System.out.println(format(ddl));
	}
}