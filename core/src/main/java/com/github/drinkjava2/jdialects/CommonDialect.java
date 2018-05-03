/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
