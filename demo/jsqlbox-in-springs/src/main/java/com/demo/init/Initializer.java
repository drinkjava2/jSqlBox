package com.demo.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

public class Initializer implements WebApplicationInitializer {

	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebAppConfig.class);
		servletContext.addListener(new ContextLoaderListener(ctx));

		ctx.setServletContext(servletContext);

		Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
		servlet.addMapping("/");
		servlet.setLoadOnStartup(1);
		System.out.println("======================================================= ");
//		SqlBoxContext globalSqlBoxCTX = ctx.getBean(SqlBoxContext.class);
//		System.out.println("globalSqlBoxCTX=" + globalSqlBoxCTX);
//		SqlBoxContext.setGlobalSqlBoxContext(globalSqlBoxCTX);
	}

}
