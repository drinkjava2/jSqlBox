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
package com.jsqlboxdemo.box;

import org.apache.commons.lang.StringUtils;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jwebbox.WebBox;
import com.jsqlboxdemo.service.TeamService;

import model.Team;

/**
 * This is a Demo to show use jWebBox as controller
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class Boxes {
	public static class RestfulWebBox extends WebBox {
		{
			setPage("/WEB-INF/pages/" + this.getClass().getSimpleName() + ".jsp");
		}
	}

	public static class home extends RestfulWebBox {
	}

	public static class team_add extends RestfulWebBox {
		public Object execute() {
			if (StringUtils.isEmpty((String) getAttribute("name")))
				return page;

			TeamService teamService = BeanBox.getBean(TeamService.class);
			Team team = new Team();
			team.setName((String) this.getAttribute("name"));
			team.setRating((Integer) this.getAttribute("rating"));
			teamService.insert(team);
			this.getPageContext().getRequest().setAttribute("message", "Team was successfully added.");
			return new home();
		}
	}

	public static class team_edit extends RestfulWebBox {

	}

	public static class team_list extends RestfulWebBox {
	}

}
