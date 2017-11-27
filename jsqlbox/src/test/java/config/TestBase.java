/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package config;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

import config.DataSourceConfig.DataSourceBox;

/**
 * Base class of unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TestBase {
	protected HikariDataSource dataSource;
	protected Dialect dialect;
	protected SqlBoxContext ctx;

	@Before
	public void init() {
		dataSource = BeanBox.getBean(DataSourceBox.class);
		// dataSource = new HikariDataSource();
		// dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		// dataSource.setDriverClassName("org.h2.Driver");
		// dataSource.setUsername("sa");
		// dataSource.setPassword("");
		// dataSource.setMaximumPoolSize(8);
		// dataSource.setConnectionTimeout(2000);
		dialect = Dialect.guessDialect(dataSource);
		ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setDefaultContext(ctx);
	}

	@After
	public void cleanUp() {
		// dataSource.close();
		BeanBox.defaultContext.close();
		SqlBoxContext.setDefaultContext(null);
	}

	public void executeDDLs(String[] ddls) {
		for (String sql : ddls)
			ctx.nExecute(sql);
	}

	public void quietExecuteDDLs(String[] ddls) {
		for (String sql : ddls) {
			try {
				ctx.nExecute(sql);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Drop and create database according given tableModels
	 */
	public void dropAndCreateDatabase(TableModel... tableModels) {
		String[] ddls = dialect.toDropDDL(tableModels);
		quietExecuteDDLs(ddls);

		ddls = dialect.toCreateDDL(tableModels);
		executeDDLs(ddls);
	}

	public static void printTimeUsed(long startTimeMillis, String msg) {
		System.out
				.println(String.format("%50s: %7s s", msg,  (System.currentTimeMillis() - startTimeMillis) /1000.0));
	}
}