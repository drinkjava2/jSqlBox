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
package com.github.drinkjava2.jdbpro.handler;

import java.util.Arrays;

import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * PaginHandler is the AroundSqlHandler used to translate SQL to paginated SQL
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
@SuppressWarnings("all")
public class PrintSqlHandler extends DefaultOrderSqlHandler {
	private static boolean allowPrint = true;

	public static boolean isAllowPrint() {
		return allowPrint;
	}

	public static void setAllowPrint(boolean allowPrint) {
		PrintSqlHandler.allowPrint = allowPrint;
	}

	@Override
	public Object handle(ImprovedQueryRunner runner, PreparedSQL ps) {
		StringBuffer sb = new StringBuffer();

		sb.append("======PrintSqlHandler=========\n");
		sb.append("| SQL:       " + ps.getSql()).append("\n");
		sb.append("| Param:     " + Arrays.deepToString(ps.getParams())).append("\n");
		if (ps.getParams() != null && ps.getParams().length > 0) {
			sb.append("| FullSQL:   ");
			String s = ps.getSql();
			for (int i = 0; i < 1000; i++) {
				if (s.contains("?")) {
					Object o = ps.getParams()[i];
					if (o == null || o instanceof Number)
						s = StrUtils.replaceFirst(s, "?", "" + o);
					else
						s = StrUtils.replaceFirst(s, "?", "'" + o + "'");
				} else
					break;
			}
			sb.append(s).append("\n");
		}
		
		
		if (allowPrint)
			System.out.print(sb.toString());
		sb.setLength(0);
		long start = System.currentTimeMillis();		
		Object obj = runner.runPreparedSQL(ps);
		long end = System.currentTimeMillis();
		sb.append("| TimeUsed:  " + (end - start) + "ms\n");
		if (runner.getName() != null && runner.getName().length() > 0)
			sb.append("| DB:        " + runner.getName()).append("\n");
		sb.append("==============================");
		if (allowPrint)
			System.out.println(sb.toString());
		return obj;
	}

}
