/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.jsqlboxdemo.controller;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.jsqlboxdemo.TestBase;
import com.jsqlboxdemo.controller.Controllers.home;
import com.jsqlboxdemo.controller.Controllers.team_add_post;
import com.jsqlboxdemo.controller.Controllers.team_listBiggerThan10;
import com.jsqlboxdemo.mock.MockPageContext;

import model.Team;

/**
 * This is unit test for Controller
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class ControllersTest extends TestBase {

	@Test
	public void test_team_add_post() {
		MockPageContext ptx = new MockPageContext();
		ptx.setRequestAttribute("name", "Tom");
		ptx.setRequestAttribute("rating", "123");
		team_add_post box = BeanBox.getBean(team_add_post.class);
		box.setPageContext(ptx);
		box.execute();
		Assert.assertEquals("Team was successfully added.", (String) ptx.getRequestAttribute("message"));
		Assert.assertTrue(box.getPage() instanceof home);
	}

	@Test
	public void test_team_listBiggerThan10() {
		MockPageContext ptx = new MockPageContext();
		team_listBiggerThan10 box = BeanBox.getBean(team_listBiggerThan10.class);
		box.setPageContext(ptx);
		box.execute();
		Assert.assertEquals(4, ((List<Team>) ptx.getRequestAttribute("teams")).size());
		Assert.assertEquals(box.getPage(), "/WEB-INF/pages/team_list.jsp"); 
	}
	

	@Test
	public void test_team_listBiggerThan102() {
		MockPageContext ptx = new MockPageContext();
		team_listBiggerThan10 box = new team_listBiggerThan10();
		box.setPageContext(ptx);
		box.execute();
		Assert.assertEquals(4, ((List<Team>) ptx.getRequestAttribute("teams")).size());
		Assert.assertEquals(box.getPage(), "/WEB-INF/pages/team_list.jsp"); 
	}

}
