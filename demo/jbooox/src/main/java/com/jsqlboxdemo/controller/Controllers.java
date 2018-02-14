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

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jwebbox.WebBox;
import com.jsqlboxdemo.init.Initializer.Transaction;
import com.jsqlboxdemo.service.TeamService;

import model.Team;

/**
 * This is a Demo to show use jWebBox as controller, WebBox is not thread safe,
 * to use WebBox act as controller, always use a IOC/AOP tool to get a new
 * instance for each thread and some times will get a proxy instance
 * (child-Class of WebBox) to support transaction if put @Transactional or @Tx
 * or @TX or @Transaction annotation on controller's method.
 * 
 * Service classes usually is singleton, why? because the only purpose of
 * service class is to create proxy by AOP too to support transaction.
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class Controllers {
	public static class home extends WebBox {
		String[] pathParams;
		TeamService teamService = BeanBox.getBean(TeamService.class);// singleton
		{
			setPage("/WEB-INF/pages/home.jsp");
		}

		public void beforeExecute() {
			super.beforeExecute();
			System.out.println("======================================");
		}

		public void redirect(Object target) {
			if (target instanceof String) {
				String targetPage = (String) target;
				if (targetPage.indexOf("/WEB-INF/") == 0)
					setPage(targetPage);
				else
					setPage("/WEB-INF/pages/" + targetPage);
			} else if (target instanceof Class)
				setPage(BeanBox.getPrototypeBean((Class) target));// non-singleton
		}
	}

	public static class team_add extends home {
		{
			redirect("team_add.jsp");
		}
	}

	public static class team_add_post extends home {
		// Not recommend open transaction in view, usually should put on service methods
		@Transaction
		public void execute() {
			Team team = new Team();
			team.setName((String) this.getObject("name"));
			team.setRating(Integer.parseInt((String) this.getObject("rating")));
			team.insert();
			setRequestAttribute("message", "Team was successfully added.");
			redirect(team_list.class);
		}
	}

	public static class team_edit extends team_list {
		public void execute() {
			Team team = new Team().load(pathParams[0]);
			this.setRequestAttribute("team", team);
		}
	}

	public static class team_edit_post extends team_edit {
		@Transaction // Not recommend open transaction in view
		public void execute() {
			super.execute();
			Team team = getObject("team");
			if (team == null)
				throw new NullPointerException("Team does exist");
			team.setName((String) this.getObject("name"));
			team.setRating(Integer.parseInt((String) this.getObject("rating")));
			team.update();
			this.setRequestAttribute("message", "Team was successfully edited.");
		}
	}

	public static class team_delete extends team_edit {
		@Transaction // Not recommend open transaction in view
		public void execute() {
			super.execute();
			Team team = getObject("team");
			if (team == null)
				throw new NullPointerException("Team does exist");
			team.delete();
			this.setRequestAttribute("message", "Team was successfully deleted.");
			redirect(team_list.class);
		}
	}

	public static class team_list extends home {
		{
			setPage("/WEB-INF/pages/team_list.jsp");
		}
	}

	public static class team_listall extends team_list {
		public void execute() {
			this.setRequestAttribute("teams", teamService.listAll());
		}
	}

	public static class team_listequal extends team_list {
		public void execute() {
			this.setRequestAttribute("teams", teamService.listEqual(Integer.parseInt(pathParams[0])));
		}
	}

	public static class team_listnotequal extends team_list {
		public void execute() {
			this.setRequestAttribute("teams", teamService.listNotEqual(Integer.parseInt(pathParams[0])));
		}
	}

	public static class team_listbigger extends team_list {
		public void execute() {
			System.out.println(pathParams);
			pathParams = (String[]) this.getObject("pathParams");
			this.setRequestAttribute("teams", teamService.listBigger(Integer.parseInt(pathParams[0])));
		}
	}

}
