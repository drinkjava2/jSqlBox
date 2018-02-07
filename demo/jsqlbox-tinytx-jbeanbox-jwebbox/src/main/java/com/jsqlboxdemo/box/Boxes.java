package com.jsqlboxdemo.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jwebbox.WebBox;
import com.jsqlboxdemo.service.TeamService;

@SuppressWarnings("all")
public class Boxes {

	static class SameNamePageBox extends WebBox {
		{
			setPage("/WEB-INF/pages/" + this.getClass().getSimpleName() + ".jsp");
		}
	}

	public static class home extends SameNamePageBox {
	}

	public static class team_add extends SameNamePageBox {
	}

	public static class team_edit extends SameNamePageBox {
	}

	public static class team_list extends SameNamePageBox {
	}

}
