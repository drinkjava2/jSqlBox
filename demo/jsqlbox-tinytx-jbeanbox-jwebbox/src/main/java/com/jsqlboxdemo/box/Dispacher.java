package com.jsqlboxdemo.box;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.github.drinkjava2.jwebbox.WebBox;

@SuppressWarnings("all")
public class Dispacher {
	public static void dispach(PageContext pageContext) throws Exception {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String uri = StringUtils.substringBefore(request.getRequestURI(), ".");
		String contextPath = request.getContextPath();
		// System.out.println("uri=" + uri);
		// System.out.println("contextPath=" + contextPath);

		if (!StringUtils.isEmpty(contextPath))
			uri = StringUtils.substringAfter(uri, contextPath);
		if (StringUtils.isEmpty(uri) || "/".equals(uri))
			uri = "/home";

		// Now uri can be /foo or /foo/bar or /foo/bar/xx/xxx
		String[] names = StringUtils.split(uri, "/");
		String boxClassName;
		if (names.length > 1)
			boxClassName = names[0] + "_" + names[1];
		else
			boxClassName = names[0];
		// System.out.println("boxClassName=" + boxClassName);
		WebBox box = (WebBox) Class.forName("com.jsqlboxdemo.box.Boxes$" + boxClassName).newInstance();
		box.show(pageContext);
	}

}
