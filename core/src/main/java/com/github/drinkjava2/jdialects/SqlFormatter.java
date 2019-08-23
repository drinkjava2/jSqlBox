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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Yong Zhu
 * @since 2.0.4
 */
public class SqlFormatter {
	private static final Set<String> keywords = new HashSet<String>();
	static {
		keywords.add("select");
		keywords.add("insert");
		keywords.add("update");
		keywords.add("delete");
		keywords.add("create");
		keywords.add("drop");
		keywords.add("alter");

		keywords.add("from");
		keywords.add("in");
		keywords.add("exists");
		keywords.add("where");

		keywords.add("order");
		keywords.add("group");
		keywords.add("union");

		keywords.add("inner");
		keywords.add("left");
		keywords.add("right");
		keywords.add("outer");
	}

	private static boolean isSpace(char c) {
		return c <= 32 || c == '\t' || c == '\r' || c == '\n';
	}

	public static String format(String template) {
		StringBuilder sb = new StringBuilder();
		char[] chars = ("  " + template + "  ").toCharArray();
		// 0 0 ' 1 1 1 ' 0 0 0
		boolean inStr = false;
		boolean justAddedSpace = true;
		StringBuilder lastWord = new StringBuilder();
		for (int i = 2; i < chars.length - 2; i++) {
			char c = chars[i];
			if (isSpace(c)) {
				if (inStr) {
					sb.append(c);
				} else if (!justAddedSpace) {
					addLast(sb, lastWord);
					sb.append(' ');
					justAddedSpace = true;
				}
			} else if (c == '\'') {
				if (!inStr)
					addLast(sb, lastWord);
				sb.append('\'');
				inStr = !inStr;
				justAddedSpace = false;
			} else {
				justAddedSpace = false;
				if (inStr)
					sb.append(c);
				else
					lastWord.append(c);
			}
		}
		addLast(sb, lastWord);
		return sb.toString();
	}

	private static void addLast(StringBuilder sb, StringBuilder lastWord) {
		if (lastWord.length() == 0)
			return;
		String last = lastWord.toString();
		if (sb.length() > 0 && keywords.contains(last.toLowerCase()))
			sb.append("\n");
		sb.append(last);
		lastWord.setLength(0);
	}

	public static void main(String[] args) {
		System.out.println(
				format("select  \t     * from users \t  \n  where '1 =  1 '   or       '  or 2=2'\r\n" + "or    3=3 order by    id"));
	}
}