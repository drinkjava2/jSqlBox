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
package com.github.drinkjava2.jdbpro.template;

import java.util.Map;

import com.github.drinkjava2.jdbpro.PreparedSQL;

/**
 * A SqlTemplateEngine render a SQL Template String and a Map<String, Object>
 * into a {@link PreparedSQL} instance
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public interface SqlTemplateEngine {

	/**
	 * Render a SQL Template String and a Map<String, Object> instance into a
	 * {@link PreparedSQL} instance
	 * 
	 * @param sqlTemplateOrSqlID
	 *            A SQL Template String, or a SqlId used to locate the real SQL
	 *            template String
	 * @param paramMap
	 *            A Map instance, key is String type, value is Object type
	 * @param unbindedParams
	 *            Optional, some time un-binded parameter can make template SQL
	 *            executed like normal SQL, i.e., use normal params to replace
	 *            placeholder according apperance order
	 * 
	 *            <pre>
	 *            For example: 
	 *            Template "delete from #{tb}", use put("tb","users") method,      will get "delete from ?"
	 *            Template "delete from ${tb}", use replace("tb","users") method,   will get "delete from users"
	 *            Template "delete from #{tb}", use replace("tb","users") method,   will cause an Exception
	 *            Template "delete from ${tb}", use put("tb","users") method,      will cause an Exception
	 * 
	 *            </pre>
	 * 
	 *            This design is to avoid typing mistake cause a SQL injection
	 *            security leak, when programmer use replace() or replace0() method,
	 *            he will aware this is a String direct replace method, not a SQL
	 *            parameter, SQL parameter always use "put" method.
	 * 
	 * @return PreparedSQL instance
	 */
	public PreparedSQL render(String sqlTemplateOrSqlID, Map<String, Object> paramMap, Object[] unbindedParams);

}
