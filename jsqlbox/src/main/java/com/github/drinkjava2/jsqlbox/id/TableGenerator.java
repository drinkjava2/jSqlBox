/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jsqlbox.id;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Define a table ID generator, simlar like JPA TableGenerator but delete name field
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TableGenerator implements IdGenerator {

	/**
	 * Name of table that stores the generated id values. Defaults to a name chosen by persistence provider.
	 */
	private String table = "";

	/**
	 * Name of the primary key column in the table. Defaults to a provider-chosen name.
	 */
	private String pkColumnName = "";

	/**
	 * Name of the column that stores the last value generated. Defaults to a provider-chosen name.
	 */
	private String valueColumnName = "";

	/**
	 * The primary key value in the generator table that distinguishes this set of generated values from others that may
	 * be stored in the table.
	 */
	private String pkColumnValue = "";

	/**
	 * The initial value to be used to initialize the column that stores the last value generated.
	 */
	private Integer initialValue = 0;

	/**
	 * The amount to increment by when allocating id numbers from the generator.
	 */
	private Integer allocationSize = 50;

	/**
	 * Last ID Value
	 */
	private Integer lastValue = -1;

	public TableGenerator(String table, String pkColumnName, String pkColumnValue, String valueColumnName,
			Integer initialValue, Integer allocationSize) {
		this.table = table;
		this.pkColumnName = pkColumnName;
		this.valueColumnName = valueColumnName;
		this.pkColumnValue = pkColumnValue;
		this.initialValue = initialValue;
		this.allocationSize = allocationSize;
	}

	/**
	 * Get the next Table Generator ID
	 */
	@Override
	public Object getNextID(SqlBoxContext ctx) {
		JdbcTemplate jdbc = ctx.getJdbc();
		if (lastValue == -1) {
			int countOfRec = jdbc.queryForObject("select count(*) from " + table + " where " + pkColumnName + "=?",
					Integer.class, new Object[] { pkColumnValue });
			if (countOfRec == 0) {
				jdbc.update("insert into " + table + "( " + pkColumnName + "," + valueColumnName + " )  values(?,?)",
						new Object[] { pkColumnValue, initialValue });
				lastValue = initialValue;
				return lastValue;
			} else {
				int last = jdbc.queryForObject(
						"select " + valueColumnName + " from " + table + " where " + pkColumnName + "=?", Integer.class,
						new Object[] { pkColumnValue }); // 70 or 99 or 100 or 101
				last = calculateBucketFirstID(last, allocationSize);// 101 or 101 or 101 or 151
				jdbc.update("update " + table + " set " + valueColumnName + "=? where " + pkColumnName + " =?",
						new Object[] { calculateBucketFirstID(last + 1, allocationSize), pkColumnValue });// 151, 151,
																											// 151, 201
				lastValue = last;
				return lastValue;
			}
		} else {
			int last = lastValue;
			int nextBucketFirstID = calculateBucketFirstID(last, allocationSize);
			if (last + 1 >= nextBucketFirstID)
				jdbc.update("update " + table + " set " + valueColumnName + "=? where " + pkColumnName + " =?",
						new Object[] { calculateBucketFirstID(last + 1, allocationSize), pkColumnValue });
			lastValue = last + 1;
			return lastValue;
		}
	}

	private static int calculateBucketFirstID(Integer currentValue, Integer allocationSize) {
		return ((currentValue + allocationSize - 1) / allocationSize) * allocationSize + 1;
	}

}
