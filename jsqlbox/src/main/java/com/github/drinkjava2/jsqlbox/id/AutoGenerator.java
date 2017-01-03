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

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * AutoGenerator will depends database's id generator mechanism like MySql's Identity, Oracle's Sequence...
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class AutoGenerator implements IdGenerator {
	public static final AutoGenerator INSTANCE = new AutoGenerator();

	@Override
	public Object getNextID(SqlBoxContext ctx) {
		// will not use this getNextID
		return null;
	}

}
