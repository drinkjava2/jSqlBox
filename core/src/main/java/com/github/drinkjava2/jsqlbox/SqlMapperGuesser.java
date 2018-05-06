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

import com.github.drinkjava2.jdbpro.PreparedSQL;

/**
 * Guess and execute the SQL for a annotated ActiveRecord entity's method
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public interface SqlMapperGuesser {// NOSONAR

	/**
	 * Execute operation to access database, based on current method @Sql annotated
	 * String or Text String and parameters, guess a best fit
	 * query/update/delete/execute method to run. SubClass can override this method
	 * to do different guess logic
	 * 
	 * @param entity
	 *            The ActiveRecord entity
	 * @param params
	 *            The SQL params
	 * @return T The Result object, a generic type
	 */
	public <T> T guess(SqlBoxContext ctx, Object entity, Object... params);

	/**
	 * Return the guessed SQL of a method which has @Sql annotation or Text in
	 * comment(need put Java in resources(class root) folder)
	 * 
	 * @param entity
	 *            The ActiveRecord entity
	 * @return The SQL String
	 */
	public String guessSQL(SqlBoxContext ctx, Object entity);

	/** Build a PreparedSQL instance of a method */
	/**
	 * @param entity
	 *            The ActiveRecord entity
	 * @param params
	 *            The Sql parameters
	 * @return The PreparedSQL instance
	 */
	public PreparedSQL doGuessPreparedSQL(SqlBoxContext ctx, Object entity, Object... params);

}