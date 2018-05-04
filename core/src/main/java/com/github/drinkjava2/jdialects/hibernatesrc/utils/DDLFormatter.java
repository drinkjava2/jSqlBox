/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.hibernatesrc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Performs formatting of DDL SQL statements.
 *
 * @author Gavin King
 * @author Steve Ebersole
 * @author Yong Zhu(Modify)
 */
public class DDLFormatter {

	private static final String INITIAL_LINE = System.getProperty("line.separator") + "    ";
	private static final String OTHER_LINES = System.getProperty("line.separator") + "       ";
	/**
	 * Singleton access
	 */
	private static final DDLFormatter INSTANCE = new DDLFormatter();

	public static String format(String sql) {
		return INSTANCE.formatDDL(sql);
	}

	public static String[] format(String[] sql) {
		List<String> l = new ArrayList<String>();
		for (String string : sql) {
			l.add(format(string));
		}
		return l.toArray(new String[l.size()]);
	}

	private String formatDDL(String sql) {
		if (StringHelper.isEmpty(sql)) {
			return sql;
		}

		if (sql.toLowerCase(Locale.ROOT).startsWith("create table")) {
			return formatCreateTable(sql);
		} else if (sql.toLowerCase(Locale.ROOT).startsWith("create")) {
			return sql;
		} else if (sql.toLowerCase(Locale.ROOT).startsWith("alter table")) {
			return formatAlterTable(sql);
		} else if (sql.toLowerCase(Locale.ROOT).startsWith("comment on")) {
			return formatCommentOn(sql);
		} else {
			return INITIAL_LINE + sql;
		}
	}

	private String formatCommentOn(String sql) {
		final StringBuilder result = new StringBuilder(60).append(INITIAL_LINE);
		final StringTokenizer tokens = new StringTokenizer(sql, " '[]\"", true);

		boolean quoted = false;
		while (tokens.hasMoreTokens()) {
			final String token = tokens.nextToken();
			result.append(token);
			if (isQuote(token)) {
				quoted = !quoted;
			} else if (!quoted && "is".equals(token)) {
				result.append(OTHER_LINES);
			}
		}

		return result.toString();
	}

	private String formatAlterTable(String sql) {
		final StringBuilder result = new StringBuilder(60).append(INITIAL_LINE);
		final StringTokenizer tokens = new StringTokenizer(sql, " (,)'[]\"", true);

		boolean quoted = false;
		while (tokens.hasMoreTokens()) {
			final String token = tokens.nextToken();
			if (isQuote(token)) {
				quoted = !quoted;
			} else if (!quoted && isBreak(token)) {
					result.append(OTHER_LINES); 
			}
			result.append(token);
		}

		return result.toString();
	}

	private String formatCreateTable(String sql) {//NOSONAR
		final StringBuilder result = new StringBuilder(60).append(INITIAL_LINE);
		final StringTokenizer tokens = new StringTokenizer(sql, "(,)'[]\"", true);

		int depth = 0;
		boolean quoted = false;
		while (tokens.hasMoreTokens()) {
			final String token = tokens.nextToken();
			if (isQuote(token)) {
				quoted = !quoted;
				result.append(token);
			} else if (quoted) {
				result.append(token);
			} else {
				if (")".equals(token)) {
					depth--;
					if (depth == 0) {
						result.append(INITIAL_LINE);
					}
				}
				result.append(token);
				if (",".equals(token) && depth == 1) {
					result.append(OTHER_LINES);
				}
				if ("(".equals(token)) {
					depth++;
					if (depth == 1) {
						result.append(OTHER_LINES);
					}
				}
			}
		}

		return result.toString();
	}

	private static boolean isBreak(String token) {
		return "drop".equals(token) || "add".equals(token) || "references".equals(token) || "foreign".equals(token)
				|| "on".equals(token);
	}

	private static boolean isQuote(String tok) {
		return "\"".equals(tok) || "`".equals(tok) || "]".equals(tok) || "[".equals(tok) || "'".equals(tok);
	}

}
