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

import javax.servlet.jsp.PageContext;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jwebbox.WebBox;
import com.jsqlboxdemo.service.TeamService;

import model.Team;

/**
 * This is a Demo to show use jWebBox as controller, WebBox is not thread safe,
 * to get a WebBox controller, always use a IOC/AOP tool to get a prototype bean
 * to make sure it's thread safe and some times get a proxy instance to support
 * transaction
 * 
 * For services usually make it singleton, default BeanBox's getBean() method
 * return a singleton except use getPrototypeBean() method.
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class Controllers {
	public static class RestfulWebBox extends WebBox {
		static TeamService teamService = BeanBox.getBean(TeamService.class);

		{
			String className = this.getClass().getSimpleName();
			if (className.indexOf("$$") > -1)// is proxy class
				className = StrUtils.substringBetween(className, "$", "$$");
			setPage("/WEB-INF/pages/" + className + ".jsp");
		}

		public void show(PageContext pageContext) {
			if (page instanceof Class) {
				try {
					page = BeanBox.getPrototypeBean((Class) page);
				} catch (Exception e) {
					throw new WebBoxException("Can not create WebBox instance for class '" + page + "'", e);
				}
			}
			super.show(pageContext);
		}
	}

	public static class home extends RestfulWebBox {
	}

	public static class team_add extends RestfulWebBox {
	}

	public static class team_add_post extends RestfulWebBox {
		public void execute() {
			Team team = new Team();
			team.setName((String) this.getAttribute("name"));
			team.setRating(Integer.parseInt((String) this.getAttribute("rating")));
			team.insert();
			this.getPageContext().getRequest().setAttribute("message", "Team was successfully added.");
			setPage(home.class);
		}
	}

	public static class team_list extends RestfulWebBox {
		public void execute() {
			this.getPageContext().getRequest().setAttribute("teams", teamService.queryAllTeams());
		}
	}

	public static class team_listBiggerThan10 extends RestfulWebBox {
		public void execute() {
			this.getPageContext().getRequest().setAttribute("teams", teamService.queryBeamsRatingBiggerThan(10));
			setPage("/WEB-INF/pages/team_list.jsp");
		}
	}

	public static class team_edit extends RestfulWebBox {
		public void execute() {
			Object[] pathParams = (String[]) this.getAttribute("pathParams");
			Team team = new Team().load(pathParams[0]);
			this.getPageContext().getRequest().setAttribute("team", team);
		}
	}

	public static class team_edit_post extends team_edit {
		public void execute() {
			super.execute();
			Team team = getAttribute("team");
			if (team == null)
				throw new NullPointerException("Team already be deleted");
			team.setName((String) this.getAttribute("name"));
			team.setRating(Integer.parseInt((String) this.getAttribute("rating")));
			team.update();
			this.getPageContext().getRequest().setAttribute("message", "Team was successfully edited.");
			setPage(team_list.class);
		}
	}

	public static class team_delete extends team_edit {
		public void execute() {
			super.execute();
			Team team = getAttribute("team");
			if (team == null)
				throw new NullPointerException("Team already be deleted");
			team.delete();
			this.getPageContext().getRequest().setAttribute("message", "Team was successfully deleted.");
			setPage(team_list.class);
		}
	}

}
