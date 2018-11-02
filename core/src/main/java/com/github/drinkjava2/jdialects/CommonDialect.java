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
 * Interface for Dialect, usually used to eliminate POM dependency for other
 * projects
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public interface CommonDialect {

	/**
	 * Create a pagination SQL by given pageNumber, pageSize and SQL<br/>
	 * 
	 * @param pageNumber The page number, start from 1
	 * @param pageSize The page item size
	 * @param sql The original SQL
	 * @return The paginated SQL
	 */
	public String pagin(int pageNumber, int pageSize, String sql);

	/**
	 * Translate a SQL to native SQL, i.e., all functions written by universal
	 * function format will be transfer to a native SQL function, for example,
	 * concat('a','b') in some dialects will be changed to 'a'+'b'
	 * 
	 * @param sql array
	 * @return the translated SQL
	 */
	public String trans(String... sql);

	/**
	 * Paginate and Translate a SQL
	 * 
	 * @param The page number, start from 1
	 * @param pageSize The page item size
	 * @param sql The original SQL
	 * @return The paginated and translated SQL
	 */
	public String paginAndTrans(int pageNumber, int pageSize, String... sql);

}
