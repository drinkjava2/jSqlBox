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
		String boxClassName;

		if (paths.length > 1) {
			// /team/add/100...
			boxClassName = paths[0] + "_" + paths[1];
			pathParams = new String[paths.length - 2];
			for (int i = 2; i < paths.length; i++)
				pathParams[i - 2] = paths[i];
		} else {
			// /home
			boxClassName = paths[0];
			pathParams = new String[0];
		}
		if (request.getMethod().equals("POST"))
			boxClassName += "_post";
		boxClassName = "com.jsqlboxdemo.controller.Controllers$" + boxClassName;
		WebBox box;
		try {
			Class boxClass = Class.forName(boxClassName);
			box = BeanBox.getPrototypeBean(boxClass);
		} catch (Exception e) {
			throw new ClassNotFoundException("There is no WebBox classs '" + boxClassName + "' found.");
		}
		request.setAttribute("pathParamArray", pathParams);
		box.show(pageContext);
	}

}
