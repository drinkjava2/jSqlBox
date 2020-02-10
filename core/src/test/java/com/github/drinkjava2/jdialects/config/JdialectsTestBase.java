/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.config;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.TinyJdbc;

/**
 * This base test class in charge of configure and close data sources.
 * 
 * @author Yong Z.
 * @since 1.0.2
 *
 */
public class JdialectsTestBase {
	protected DataSource ds = JBEANBOX.getBean(DataSourceBox.class);
	protected TinyJdbc dbPro = new TinyJdbc(ds);
	protected Dialect guessedDialect = Dialect.guessDialect(ds);

	@Before
	public void initDao() {
		Systemout.println("Current guessedDialect=" + guessedDialect);
		Dialect.setGlobalAllowReservedWords(false);
		Dialect.setGlobalAllowShowSql(false);
		Dialect.setGlobalSqlFunctionPrefix(null);
	}

	@After
	public void closeDataSource() {
		JBEANBOX.close();// close dataSource
	}

	protected static void printDDLs(String[] ddl) {
		for (String str : ddl) {
			Systemout.println(str);
		}
	}

	protected void quietExecuteDDLs(String... ddls) {
		for (String sql : ddls) {
			try {
				dbPro.nExecute(sql);
			} catch (Exception e) {
			}
		}
	}

	protected void executeDDLs(String... sqls) {
		for (String sql : sqls)
			dbPro.nExecute(sql);
	}

	public void reBuildDB(TableModel... tables) {
		String[] ddls = guessedDialect.toDropDDL(tables);
		quietExecuteDDLs(ddls);

		ddls = guessedDialect.toCreateDDL(tables);
		executeDDLs(ddls);
	}

	public void dropDB(TableModel... tables) {
		String[] ddls = guessedDialect.toDropDDL(tables);
		executeDDLs(ddls);
	}

	protected void testCreateAndDropDatabase(TableModel... tables) {
		String[] ddls = guessedDialect.toCreateDDL(tables);
		executeDDLs(ddls);

		ddls = guessedDialect.toDropAndCreateDDL(tables);
		executeDDLs(ddls);

		ddls = guessedDialect.toDropDDL(tables);
		executeDDLs(ddls);
	}

	protected void testOnCurrentRealDatabase(TableModel... tables) {
		Systemout.println("======Test on real Database of dialect: " + guessedDialect + "=====");

		String[] ddls = guessedDialect.toDropDDL(tables);

		quietExecuteDDLs(ddls);

		ddls = guessedDialect.toCreateDDL(tables);
		executeDDLs(ddls);

		ddls = guessedDialect.toDropAndCreateDDL(tables);
		executeDDLs(ddls);

		ddls = guessedDialect.toDropDDL(tables);
		executeDDLs(ddls);
	}

	protected static void printOneDialectsDDLs(Dialect dialect, TableModel... tables) {
		Systemout.println("======" + dialect + "=====");
		try {
			String[] ddls = dialect.toDropAndCreateDDL(tables);
			printDDLs(ddls);
			// printDDLs(DDLFormatter.format(ddls));
		} catch (Exception e) {
			Systemout.println("Exception found: " + e.getMessage());
		}
	}

	protected static void printAllDialectsDDLs(TableModel... tables) { 
		for (Dialect dialect : Dialect.dialects) {
			Systemout.println("======" + dialect + "=====");
			try {
				String[] ddls = dialect.toDropAndCreateDDL(tables);
				printDDLs(ddls);
				// printDDLs(DDLFormatter.format(ddls));
			} catch (Exception e) {
				Systemout.println("Exception found: " + e.getMessage());
			}
		}
	}
}
