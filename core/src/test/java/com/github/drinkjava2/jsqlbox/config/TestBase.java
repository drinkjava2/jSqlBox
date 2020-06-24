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
package com.github.drinkjava2.jsqlbox.config;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jlogs.ConsoleLog;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Base class of unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TestBase {
	protected DataSource dataSource;
	protected Dialect dialect;
	protected DbContext ctx;
	protected TableModel[] tablesForTest;

	@Before
	public void init() {
		System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
		DbContext.resetGlobalVariants();
		Systemout.setAllowPrint(false); // debug only, allow Systemout.print
		ConsoleLog.setLogHead(false);// print log head
		ConsoleLog.setLogLevel(ConsoleLog.INFO); // INFO/DEBUG/WARNING/OFF
		DbContext.setGlobalNextAllowShowSql(false); // disable sql log
		dataSource = JBEANBOX.getBean(DataSourceBox.class);
		dialect = Dialect.guessDialect(dataSource);
		Dialect.setGlobalAllowReservedWords(true);

		ctx = new DbContext(dataSource);
		DbContext.setGlobalDbContext(ctx);
		if (tablesForTest != null)
			quietDropTables(tablesForTest);
		if (tablesForTest != null)
			createAndRegTables(tablesForTest);
	}

	@After
	public void cleanUp() {
		if (tablesForTest != null)
			dropTables(tablesForTest);
		tablesForTest = null;
		JBEANBOX.close(); // IOC tool will close dataSource
		DbContext.resetGlobalVariants();
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
	 * Register tables, create and drop will done by TestBase
	 */
	public void regTables(TableModel... tableModels) {
		this.tablesForTest = tableModels;
	}

	/**
	 * Register tables, create and drop will done by TestBase
	 */
	public void regTables(Class<?>... classes) {
		this.tablesForTest = TableModelUtils.entity2ReadOnlyModels(classes);
	}

	public void createAndRegTables(TableModel... tableModels) {
		this.tablesForTest = tableModels;
		createTables(tableModels);
	}

	public void createAndRegTables(Class<?>... classes) {
		this.tablesForTest = TableModelUtils.entity2ReadOnlyModels(classes);
		createTables(tablesForTest);
	}

	public void quietCreateRegTables(Class<?>... classes) {
		ctx.quiteExecute(ctx.toDropDDL(classes));
		ctx.executeDDL(ctx.toCreateDDL(classes));
	}

	public void createTables(TableModel... tableModels) {
		String[] ddls = ctx.toCreateDDL(tableModels);
		ctx.executeDDL(ddls);
	}

	public void createTables(Class<?>... classes) {
		String[] ddls = ctx.toCreateDDL(classes);
		ctx.executeDDL(ddls);
	}

	public void dropTables(TableModel... tableModels) {
		String[] ddls = ctx.toDropDDL(tableModels);
		ctx.executeDDL(ddls);
	}

	public void quietDropTables(TableModel... tableModels) {
		String[] ddls = ctx.toDropDDL(tableModels);
		quietExecuteDDLs(ddls);
	}

	public void quietDropTables(Class<?>... classes) {
		String[] ddls = ctx.toDropDDL(classes);
		quietExecuteDDLs(ddls);
	}

	public void dropTables(Class<?>... classes) {
		String[] ddls = ctx.toDropDDL(classes);
		ctx.executeDDL(ddls);
	}

	public static void printTimeUsed(long startTimeMillis, String msg) {
		Systemout.println(String.format("%50s: %7s s", msg, (System.currentTimeMillis() - startTimeMillis) / 1000.0));
	}

	public static HikariDataSource createH2_HikariDataSource(String h2DbName) {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:" + h2DbName + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);
		return ds;
	}

	public static class Demo extends ActiveRecord<Demo> {
		@Id
		@UUID25
		private String id;
		private String name;
		private Integer age;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}
	}
}