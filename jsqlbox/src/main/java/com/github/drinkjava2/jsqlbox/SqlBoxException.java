
package com.github.drinkjava2.jsqlbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlBoxException extends RuntimeException {
	private static final Log log = LogFactory.getLog(SqlBoxException.class);

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
	public static void throwEX(Exception e, String errorMsg) {
		log.error(errorMsg, e);
		throw new SqlBoxException(errorMsg);
	}
}
