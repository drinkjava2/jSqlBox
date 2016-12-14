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

/**
 * Defines the primary key generation strategies, copied from JPA
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0
 */
public enum GenerationType {

	/**
	 * Indicates that the persistence provider must assign primary keys for the entity using an underlying database
	 * table to ensure uniqueness.
	 */
	TABLE,

	/**
	 * Indicates that the persistence provider must assign primary keys for the entity using a database sequence.
	 */
	SEQUENCE,

	/**
	 * Indicates that the persistence provider must assign primary keys for the entity using a database identity column.
	 */
	IDENTITY,

	/**
	 * Indicates that the persistence provider should pick an appropriate strategy for the particular database. The
	 * <code>AUTO</code> generation strategy may expect a database resource to exist, or it may attempt to create one. A
	 * vendor may provide documentation on how to create such resources in the event that it does not support schema
	 * generation or cannot create the schema resource at runtime.
	 */
	AUTO
}
