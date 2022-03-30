/**
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
package com.github.drinkjava2.jdbpro;

import java.sql.Connection;

/**
 * Interface of Normal JDBC methods, this interface is a bridge between jDbPro
 * and jTransactions to eliminate jar dependency, other projects need copy this
 * interface into there source code folder but always use name
 * "com.github.drinkjava2.jdbpro.NormalJdbcTool"
 * 
 * @author Yong Zhu
 * @since 1.7.0.1
 */

public interface NormalJdbcTool {// NOSONAR

    
    /**
     * Query for an Object, only return the first row and first column's value if
     * more than one column or more than 1 rows returned, a null object may return
     * if no result found
     * 
     * @param con Connection
     * @param sql SQL string
     * @param params parameters
     * @return An Object or null, Object type determined by SQL content
     */
    public <T> T jdbcQueryForObject(Connection con, String sql, Object... params);
    
    
	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T jdbcQueryForObject(String sql, Object... params);


    /**
     * Executes the given INSERT, UPDATE, or DELETE SQL statement.
     * 
     * @param sql
     *            the SQL
     * @param params
     *            the parameters if have
     * @return The number of rows updated.
     */
	public int jdbcExecute(String sql, Object... params);
}
