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

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxException extends RuntimeException {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(SqlBoxException.class);

	private static final long serialVersionUID = 1L;

	public SqlBoxException() {
		// Default public constructor
	}

	public SqlBoxException(String message) {
		super(message);
	}

	/**
	 * Transfer all Exceptions to RuntimeException SqlBoxException. The only place throw Exception in this project
	 */
	public static Object throwEX(Exception e, String errorMsg) {
		if (e != null)
			log.error(errorMsg + "\r\n" + e.getMessage());
		else
			log.error(errorMsg);
		throw new SqlBoxException(errorMsg);
	}

	/**
	 * Eat exception to avoid SONAR warning
	 */
	public static void eatException(Exception e) {
		// do nothing here
	}
}
