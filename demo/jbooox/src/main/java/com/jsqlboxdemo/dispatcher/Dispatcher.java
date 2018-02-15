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
package com.jsqlboxdemo.dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jwebbox.WebBox;

/**
 * This is a Dispatcher servlet, use jWebBox as controller
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class Dispatcher {
	public static void dispach(PageContext pageContext) throws Exception {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String uri = StringUtils.substringBefore(request.getRequestURI(), ".");
		String contextPath = request.getContextPath();

		if (!StringUtils.isEmpty(contextPath))
			uri = StringUtils.substringAfter(uri, contextPath);
		if (StringUtils.isEmpty(uri) || "/".equals(uri))
			uri = "/home";

		String[] paths = StringUtils.split(uri, "/");
		String[] pathParams;
		String resource = null;
		String operation = null;

		if (paths.length >= 2) {// /team/add/100...
			resource = paths[0];
			operation = paths[1];
			pathParams = new String[paths.length - 2];
			for (int i = 2; i < paths.length; i++)
				pathParams[i - 2] = paths[i];
		} else { // /home_default
			resource = paths[0];
			pathParams = new String[0];
		}

		if (operation == null)
			operation = "default";
		StringBuilder controller = new StringBuilder("com.jsqlboxdemo.controller.").append(resource).append("$")
				.append(resource).append("_").append(operation);
		if ("POST".equals(request.getMethod()))
			controller.append("_post");

		WebBox box;
		try {
			Class boxClass = Class.forName(controller.toString());
			box = BeanBox.getPrototypeBean(boxClass);
		} catch (Exception e) {
			throw new ClassNotFoundException("There is no WebBox classs '" + controller + "' found.");
		}
		request.setAttribute("pathParams", pathParams);
		box.show(pageContext);
	}

}
