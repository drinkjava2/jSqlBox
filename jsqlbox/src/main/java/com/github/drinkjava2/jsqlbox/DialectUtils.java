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
package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jdialects.Dialect;

/**
 * Dialect Utils to deal with jDialects project's Dialects
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */

public class DialectUtils {

	private DialectUtils() {
		// Hide default constructor
	}

	public static boolean isMySql(Dialect dialect) {
		return dialect.toString().startsWith("MySQL");
	}

	public static boolean isOracle(Dialect dialect) {
		return dialect.toString().startsWith("Oracle");
	}

	public static boolean isH2(Dialect dialect) {
		return Dialect.H2Dialect.equals(dialect);
	}

	public static boolean isMsSQLSERVER(Dialect dialect) {
		return dialect.toString().startsWith("SQLServer");
	}
}
