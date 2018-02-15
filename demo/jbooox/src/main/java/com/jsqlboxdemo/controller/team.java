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

import com.jsqlboxdemo.controller.home.home_default;
import com.jsqlboxdemo.init.Initializer.Transaction;

import model.Team;

/**
 * This is a Demo to show use jWebBox as controller, WebBox is not thread safe,
 * to use WebBox act as controller, always use a IOC/AOP tool to get a new
 * instance for each thread and some times will get a proxy instance
 * (child-Class of WebBox) to support transaction if put @Transactional or @Tx
 * or @TX or @Transaction annotation on controller's method.
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class team {
	public static class team_add extends home_default {
		{
			redirect("team_add.jsp");
		}
	}

	public static class team_add_post extends home_default {
		// Not recommend open transaction in view, usually should put on service methods
		@Transaction
		public void execute() {
			Team team = new Team();
			team.setName((String) this.getObject("name"));
			team.setRating(Integer.parseInt((String) this.getObject("rating")));
			team.insert();
			setRequestAttribute("message", "Team was successfully added.");
			redirect(team_list_all.class);
		}
	}

	public static class team_edit extends home_default {
		public void execute() {
			this.setRequestAttribute("team", new Team().load(pathParams[0]));
			redirect("team_edit.jsp");
		}
	}

	public static class team_edit_post extends team_edit {
		@Transaction // Not recommend open transaction in view
		public void execute() {
			super.execute();
			Team team = getObject("team");
			if (team == null)
				throw new NullPointerException("Team does not exist");
			team.setName((String) this.getObject("name"));
			team.setRating(Integer.parseInt((String) this.getObject("rating")));
			team.update();
			this.setRequestAttribute("message", "Team was successfully edited.");
			redirect(team_list_all.class);
		}
	}

	public static class team_delete extends team_edit {
		@Transaction // Not recommend open transaction in view
		public void execute() {
			super.execute();
			Team team = getObject("team");
			if (team == null)
				throw new NullPointerException("Team does not exist");
			team.delete();
			this.setRequestAttribute("message", "Team was successfully deleted.");
			redirect(team_list_all.class);
		}
	}

	public static class team_list extends home_default {
		{
			setPage("/WEB-INF/pages/team_list.jsp");
		}
	}

	public static class team_list_all extends team_list {
		public void execute() {
			this.setRequestAttribute("teams", teamService.listAll());
		}
	}

	public static class team_list_equal extends team_list {
		public void execute() {
			this.setRequestAttribute("teams", teamService.listEqual(Integer.parseInt(pathParams[0])));
		}
	}

	public static class team_list_notequal extends team_list {
		public void execute() {
			this.setRequestAttribute("teams", teamService.listNotEqual(Integer.parseInt(pathParams[0])));
		}
	}

	public static class team_list_bigger extends team_list {
		public void execute() {
			pathParams = (String[]) this.getObject("pathParams");
			this.setRequestAttribute("teams", teamService.listBigger(Integer.parseInt(pathParams[0])));
		}
	}

}
