package com.jsqlboxdemo.jwebbox;

import javax.servlet.jsp.PageContext;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jwebbox.WebBox;
import com.jsqlboxdemo.service.TeamService;

@SuppressWarnings("all")
public class WebBoxes {

	private TeamService teamService = BeanBox.getBean(TeamService.class);

	public static class home extends WebBox {
		{
			setPage(sameNamePage(this.getClass()));
		}

		@Override
		public void show(PageContext pageContext) {
			super.show(pageContext);
		}
	}

	public static class add_team_form extends WebBox {
		{
			setPage(sameNamePage(this.getClass()));
		}
	}

	public static class edit_team_form extends WebBox {
		{
			setPage(sameNamePage(this.getClass()));
		}
	}

	public static class list_of_teams extends WebBox {
		{
			setPage(sameNamePage(this.getClass()));
		}
	}

	private static String sameNamePage(Class<?> clazz) {
		return "/WEB-INF/pages/" + clazz.getSimpleName() + ".jsp";
	}

}
