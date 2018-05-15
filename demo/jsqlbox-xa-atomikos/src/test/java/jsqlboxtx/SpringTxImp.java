/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package jsqlboxtx;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class SpringTxImp extends TransactionInterceptor {
	private static final long serialVersionUID = 1L;

	public static final int ISOLATION_DEFAULT = -1;
	public static final int ISOLATION_READ_UNCOMMITTED = 1;
	public static final int ISOLATION_READ_COMMITTED = 2;
	public static final int ISOLATION_REPEATABLE_READ = 4;
	public static final int ISOLATION_SERIALIZABLE = 8;

	private static Properties simplePros(Integer transactionIsolation) {
		String isolation = "";
		switch (transactionIsolation) {
		case -1:
			isolation = "ISOLATION_DEFAULT";
			break;
		case 1:
			isolation = "ISOLATION_READ_UNCOMMITTED";
			break;
		case 2:
			isolation = "ISOLATION_READ_COMMITTED";
			break;
		case 4:
			isolation = "ISOLATION_REPEATABLE_READ";
			break;
		case 8:
			isolation = "ISOLATION_SERIALIZABLE";
			break;
		default:
			throw new RuntimeException("Isolation value can only be -1, 1, 2, 4, 8, but set to" + transactionIsolation);
		}
		Properties props = new Properties();
		props.put("*", "PROPAGATION_REQUIRED, " + isolation);
		return props;
	}

	public SpringTxImp(DataSource ds, Integer transactionIsolation) {
		super(new DataSourceTransactionManager(ds), simplePros(transactionIsolation));
	}

	public SpringTxImp(PlatformTransactionManager pm, Properties pros) {
		super(pm, pros);
	}

}