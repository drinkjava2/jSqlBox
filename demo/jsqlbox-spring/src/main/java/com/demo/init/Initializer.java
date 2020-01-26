package com.demo.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.demo.model.Team;
import com.github.drinkjava2.jsqlbox.DbContext;

public class Initializer implements WebApplicationInitializer {

	public void onStartup(ServletContext servletContext) throws ServletException {

		AnnotationConfigWebApplicationContext springCtx = new AnnotationConfigWebApplicationContext();
		springCtx.register(WebAppConfig.class);
		servletContext.addListener(new ContextLoaderListener(springCtx));

		springCtx.setServletContext(servletContext);

		Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(springCtx));
		servlet.addMapping("/");
		servlet.setLoadOnStartup(1);

		springCtx.refresh();// force refresh

		DbContext sbCtx = springCtx.getBean(DbContext.class);

		String[] ddls = sbCtx.toCreateDDL(Team.class);
		for (String ddl : ddls)
			sbCtx.nExecute(ddl);
		for (int i = 0; i < 5; i++)
			new Team().putField("name", "Team" + i, "rating", i * 10).insert();
		System.out.println("========== com.jsqlboxdemo.init.Initializer initialized=====");
	}

}
