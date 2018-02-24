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
import com.jsqlboxdemo.controller.team.team_add_post;
import com.jsqlboxdemo.controller.team.team_list_all;
import com.jsqlboxdemo.controller.team.team_list_bigger;
import com.jsqlboxdemo.dispatcher.Dispatcher;
import com.jsqlboxdemo.mock.MockPageContext;

import model.Team;

/**
 * This is unit test for Controller
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class TeamControllerTest extends TestBase {

	@Test // Test from execute method
	public void test_team_add_post() {
		MockPageContext mockPC = new MockPageContext();
		mockPC.setRequestAttribute("name", "Tom");
		mockPC.setRequestAttribute("rating", "123");
		team_add_post box = BeanBox.getPrototypeBean(team_add_post.class);
		box.setPageContext(mockPC);
		box.execute();
		Assert.assertEquals("Team was successfully added.", (String) mockPC.getRequestAttribute("message"));
		Assert.assertTrue(box.getPage() instanceof team_list_all);
	}

	@Test // Test from show method
	public void test_team_listBigger() {
		MockPageContext mockPC = new MockPageContext();
		mockPC.setPathParams("10");
		team_list_bigger box = BeanBox.getPrototypeBean(team_list_bigger.class);
		box.show(mockPC);
		Assert.assertEquals(3, ((List<Team>) mockPC.getRequestAttribute("teams")).size());
		Assert.assertEquals(box.getPage(), "/WEB-INF/pages/team_list.jsp");
	}

	@Test // Test from dispatcher
	public void test_team_equal() throws Exception {
		MockPageContext mockPC = new MockPageContext();
		mockPC.setRequestURI("/team/list_equal/10.html");
		Dispatcher.dispach(mockPC);
		Assert.assertEquals(1, ((List<Team>) mockPC.getRequestAttribute("teams")).size());
	}

}
