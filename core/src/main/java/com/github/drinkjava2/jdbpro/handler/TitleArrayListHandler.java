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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * TitledArrayResultHandler transfer ResultSet to a List of Object[], first row
 * is titles, other rows are data
 * 
 * <pre>
 * A result set:
 *   name, age
 *   tom,  5
 *   sam,  6
 * 
 * be transfered to: 
 * 
 * First list item: object[]{"name", "age"}
 * 2nd list item:   object[]{"tom",  5}
 * 3rd list item:   object[]{"sam",  6}
 * 
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.7.0.2
 */
@SuppressWarnings("all")
public class TitleArrayListHandler implements ResultSetHandler<List<Object[]>> {

	@Override
	public List<Object[]> handle(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();

		String[] titles = new String[cols];
		for (int i = 1; i <= cols; i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (null == columnName || 0 == columnName.length())
				columnName = rsmd.getColumnName(i);
			titles[i - 1] = columnName;
		}
		List<Object[]> rows = new ArrayList<Object[]>();
		rows.add(titles);

		while (rs.next())
			rows.add(toArray(rs, cols));
		return rows;
	}

	public Object[] toArray(ResultSet rs, int cols) throws SQLException {
		Object[] result = new Object[cols];
		for (int i = 0; i < cols; i++)
			result[i] = rs.getObject(i + 1);
		return result;
	}

}
