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
package com.demo;

import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;

import com.demo.init.Initializer;
import com.demo.service.TeamService;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Base class of unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TestBase {
	@Autowired
	public TeamService teamService;

	protected TeamService teamServices;
	protected DataSource dataSource;
	protected Dialect dialect;
	protected SqlBoxContext ctx;
	private Initializer initializer;

	@Before
	public void init() {
		initializer = new Initializer();
		try {
			initializer.onStartup(new MockServletContext());
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

	@After
	public void cleanup() {

	}

}