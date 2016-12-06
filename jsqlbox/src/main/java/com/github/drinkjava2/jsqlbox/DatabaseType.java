package com.github.drinkjava2.jsqlbox;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */

public enum DatabaseType {
	MYSQL, ORACLE, MS_SQLSERVER, DB2, INFORMIX, SYBASE;

	public static Object getType(String jdbcDriverName) {// NOSONAR
		String drv = jdbcDriverName.toUpperCase();
		if (drv.contains("MYSQL"))
			return DatabaseType.MYSQL;
		else if (drv.contains("ORACLE"))
			return DatabaseType.ORACLE;
		else if (drv.contains("SQL") && drv.contains("SERVER"))
			return DatabaseType.MS_SQLSERVER;
		else if (drv.contains("DB2"))
			return DatabaseType.DB2;
		else if (drv.contains("INFORMIX"))
			return DatabaseType.INFORMIX;
		else if (drv.contains("SYBASE"))
			return DatabaseType.SYBASE;
		else
			return null;
	}
}
