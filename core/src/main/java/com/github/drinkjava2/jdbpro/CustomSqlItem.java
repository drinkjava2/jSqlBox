/*
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

/**
 * This interface is designed for users define customised SQL Items will be deal
 * with in DbPro's doPrepare method
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public interface CustomSqlItem {

	/**
	 * In this method to deal the item
	 * 
	 * @param ps
	 *            A PreparedSQL instance
	 * @param sql
	 *            the StringBuilder store SQL, this SQL String will be set to
	 *            PreparedSQL
	 */
	public void dealItem(PreparedSQL ps, StringBuilder sql);
}
