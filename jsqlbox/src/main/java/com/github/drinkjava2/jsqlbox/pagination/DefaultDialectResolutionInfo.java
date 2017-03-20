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
package com.github.drinkjava2.jsqlbox.pagination;

import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * Default DialectResolutionInfo used for determine database dialect type
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DefaultDialectResolutionInfo implements DialectResolutionInfo {
	private final String databaseName;
	private final int databaseMajorVersion;
	private final int databaseMinorVersion;

	private final String driverName;
	private final int driverMajorVersion;
	private final int driverMinorVersion;

	public DefaultDialectResolutionInfo(String databaseName, int databaseMajorVersion, int databaseMinorVersion,
			String driverName, int driverMajorVersion, int driverMinorVersion) {
		this.databaseName = databaseName;
		this.databaseMajorVersion = databaseMajorVersion;
		this.databaseMinorVersion = databaseMinorVersion;
		this.driverName = driverName;
		this.driverMajorVersion = driverMajorVersion;
		this.driverMinorVersion = driverMinorVersion;
	}

	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public int getDatabaseMajorVersion() {
		return databaseMajorVersion;
	}

	@Override
	public int getDatabaseMinorVersion() {
		return databaseMinorVersion;
	}

	@Override
	public String getDriverName() {
		return driverName;
	}

	@Override
	public int getDriverMajorVersion() {
		return driverMajorVersion;
	}

	@Override
	public int getDriverMinorVersion() {
		return driverMinorVersion;
	}

	public static DefaultDialectResolutionInfo forDatabaseInfo(String name) {
		return forDatabaseInfo(name, NO_VERSION);
	}

	public static DefaultDialectResolutionInfo forDatabaseInfo(String name, int majorVersion) {
		return forDatabaseInfo(name, majorVersion, NO_VERSION);
	}

	public static DefaultDialectResolutionInfo forDatabaseInfo(String name, int majorVersion, int minorVersion) {
		return new DefaultDialectResolutionInfo(name, majorVersion, minorVersion, null, NO_VERSION, NO_VERSION);
	}

}
