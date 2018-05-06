/*
 * Copyright 2016 the original author or authors.
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
package com.github.drinkjava2.jdbpro;

/**
 * Special SQL Item Preparer
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public interface SpecialSqlItemPreparer {
	/**
	 * Prepare special SQL items
	 * 
	 * @param ps
	 *            PreparedSQL instance
	 * @param sql
	 *            The StringBuilder instance used to build SQL
	 * @param item
	 *            The SpecialSqlItem instance
	 * 
	 * @return True if this SpecialSqlItem can be handled, otherwise return false to
	 *         let system know to find other SpecialSqlItemPreparers to handle this
	 *         item
	 */
	public boolean doPrepare(PreparedSQL ps, StringBuilder sql, SpecialSqlItem item);
}