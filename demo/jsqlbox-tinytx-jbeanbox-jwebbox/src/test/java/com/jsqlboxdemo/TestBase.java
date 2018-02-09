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
package com.jsqlboxdemo;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.jsqlboxdemo.init.Initializer;
import com.jsqlboxdemo.service.TeamService;

/**
 * Base class of unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TestBase {
	protected TeamService teamServices;
	protected DataSource dataSource;
	protected Dialect dialect;
	protected SqlBoxContext ctx;
	private Initializer initializer;

	@Before
	public void init() {
		initializer = new Initializer();
		initializer.contextInitialized(null);
		teamServices = BeanBox.getBean(TeamService.class);
	}

	@After
	public void cleanup() {
		initializer.contextDestroyed(null);
	}

	public void executeDDLs(String[] ddls) {
		for (String sql : ddls)
			ctx.nExecute(sql);
	}

	public void quietExecuteDDLs(String[] ddls) {
		for (String sql : ddls)
			ctx.quiteExecute(sql);
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
		System.out.println(String.format("%50s: %7s s", msg, (System.currentTimeMillis() - startTimeMillis) / 1000.0));
	}
}