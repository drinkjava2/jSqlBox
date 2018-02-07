/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jwebbox;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * JWebBox is a small layout tool used in java server pages(JSP) projects,
 * playing the same role like Apache Tiles and SiteMesh, no XML file, no Tags,
 * simple(only 1 java file), can be used to support whole site's layout or only
 * few page components.
 * 
 * @author Yong Zhu(yong9981@gmail.com)
 * @version 2.1
 */
public class WebBox {
	public static final String JWEBBOX_ID = "jwebbox";

	/** Optional, you can give a name to WebBox instance */
	protected String name;

	/** A static method to prepare data, first be called if have */
	protected String prepareStaticMethod;

	/** A Bean has a "prepare" methods to prepare data, 2nd called */
	protected Object prepareBean;

	/** If Bean has prepareBeanMethod, use this one instead of "prepare" */
	protected String prepareBeanMethod;

	/** A URL, 3rd called, if have */
	protected String prepareURL;

	/** A text , 4th output, if not empty */
	protected String text;

	/** A JSP or FTL page, 5th output, if not empty */
	protected Object page;

	/** Inside use hashmap store attributes */
	protected Map<String, Object> attributeMap = new HashMap<String, Object>();

	/** Point to father WebBox instance if have */
	protected WebBox fatherWebBox;

	protected PageContext pageContext;

	public WebBox() {
		// default constructor
	}

	/**
	 * Create a WebBox
	 * 
	 * @param page
	 *            The JSP or FTL or any URL, for example: "/template/abc.jsp"
	 */
	public WebBox(String page) {
		this.setPage(page);
	}

	/** Check if String null or empty */
	public static boolean isEmptyStr(String str) {
		return (str == null || str.length() == 0);
	}

	/** For subclasses override this method to do something */
	public Object execute() {// NOSONAR
		return page;
	}

	/** For subclasses override this method to do something */
	public void afterExecute() {// NOSONAR
	}

	/** Prepare data and out put text include page if have */
	public void show(PageContext pageContext) {
		execute();
		this.pageContext = pageContext;
		prepareOnly(pageContext);
		if (text != null && text.length() > 0)
			try {
				pageContext.getOut().write(text);
			} catch (IOException e) {
				throw new WebBoxException(e);
			}
		showPageOrUrl(pageContext, this.page, this);
		afterExecute();
		this.pageContext = null;
	}

	/** Prepare data, only but do not output text and do not show page */
	public void prepareOnly(PageContext pageContext) {
		if (!isEmptyStr(prepareStaticMethod)) {
			int index = prepareStaticMethod.lastIndexOf('.');
			String className = prepareStaticMethod.substring(0, index);
			String methodName = prepareStaticMethod.substring(index + 1, prepareStaticMethod.length());
			if (isEmptyStr(className) || isEmptyStr(methodName))
				throw new WebBoxException("Error#001: Can not call method: " + prepareStaticMethod);
			try {
				Class<?> c = Class.forName(className);
				Method m = c.getMethod(methodName, PageContext.class, WebBox.class);
				m.invoke(c, pageContext, this); // Call a static method
			} catch (Exception e) {
				throw new WebBoxException(e);
			}
		}
		if (prepareBean != null)
			executeBeanMethod(pageContext);
		showPageOrUrl(pageContext, this.prepareURL, this);
	}

	/** Execute Bean Method */
	private void executeBeanMethod(PageContext pageContext) {
		try {
			Class<?> c = prepareBean.getClass();
			String methodName = isEmptyStr(prepareBeanMethod) ? "prepare" : prepareBeanMethod;
			Method m = c.getMethod(methodName, PageContext.class, WebBox.class);
			m.invoke(prepareBean, pageContext, this); // Call a bean method
		} catch (Exception e) {
			throw new WebBoxException(e);
		}
	}

	/** Show page only, do not call prepareStaticMethod and URL */
	public void showPageOnly(PageContext pageContext) {
		showPageOrUrl(pageContext, this.page, this);
	}

	/** Private method, use RequestDispatcher to show a URL or JSP page */
	private static void showPageOrUrl(PageContext pageContext, Object page, WebBox currentBox) {
		if (page instanceof WebBox) {
			((WebBox) page).show(pageContext);
			return;
		}
		String pageOrUrl = (String) page;
		if (isEmptyStr(pageOrUrl))
			return;
		WebBox fatherWebBox = (WebBox) pageContext.getRequest().getAttribute(JWEBBOX_ID);
		if (fatherWebBox != null)
			currentBox.setFatherWebBox(fatherWebBox);
		pageContext.getRequest().setAttribute(JWEBBOX_ID, currentBox);
		try {
			pageContext.getOut().flush();
			pageContext.getRequest().getRequestDispatcher(pageOrUrl).include(pageContext.getRequest(),
					pageContext.getResponse());
		} catch (Exception e) {
			throw new WebBoxException(e);
		} finally {
			pageContext.getRequest().setAttribute(JWEBBOX_ID, fatherWebBox);
			currentBox.setFatherWebBox(null);
		}
	}

	/** Get current pageContext's WebBox instance */
	public static WebBox getBox(PageContext pageContext) {
		WebBox currentBox = (WebBox) pageContext.getRequest().getAttribute(JWEBBOX_ID);
		if (currentBox == null)
			throw new WebBoxException("Error#003: Can not find WebBox instance in pageContext");
		return currentBox;
	}

	/** Get an attribute from current page's WebBox instance */
	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(PageContext pageContext, String attributeName) {
		T obj = getBox(pageContext).getAttribute(attributeName);
		if (obj == null) {
			obj = (T) pageContext.getRequest().getAttribute(attributeName);
			if (obj == null)
				obj = (T) pageContext.getRequest().getParameter(attributeName);
		}
		return obj;
	}

	/** Assume the value is String or WebBox instance, show it */
	public static void showAttribute(PageContext pageContext, String attributeName) {
		Object obj = WebBox.getAttribute(pageContext, attributeName);
		showTarget(pageContext, obj);
	}

	/**
	 * Show an target object, target can be: WebBox instance or String or List of
	 * WebBox instance or String
	 */
	public static void showTarget(PageContext pageContext, Object target) {
		if (target == null)
			return;
		if (target instanceof WebBox)
			((WebBox) target).show(pageContext);
		else if (target instanceof ArrayList<?>) {
			for (Object item : (ArrayList<?>) target)
				showTarget(pageContext, item);
		} else if (target instanceof String) {
			String str = (String) target;
			if (str.startsWith("/")) {
				showPageOrUrl(pageContext, str, getBox(pageContext));
			} else {
				try {
					pageContext.getOut().write(str);
				} catch (IOException e) {
					throw new WebBoxException(e);
				}
			}
		} else
			throw new WebBoxException("Can not show unknow type object " + target + " on page");
	}

	// Getter & Setters

	/** Set attribute for current WebBox instance */
	public WebBox setAttribute(String key, Object value) {
		attributeMap.put(key, value);
		return this;
	}

	/** Get attribute from current WebBox instance */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key) {
		return (T) attributeMap.get(key);
	}

	/** Get the prepare URL */
	public String getPrepareURL() {
		return prepareURL;
	}

	/**
	 * Set prepare URL, this URL be called after prepare methods but before show
	 * page
	 */
	public WebBox setPrepareURL(String prepareURL) {
		this.prepareURL = prepareURL;
		return this;
	}

	/** Get the page */
	public Object getPage() {
		return page;
	}

	/** Set a JSP page or URL */
	public WebBox setPage(Object page) {
		this.page = page;
		return this;
	}

	/** Get the Text */
	public String getText() {
		return text;
	}

	/** Set the text String */
	public WebBox setText(String text) {
		this.text = text;
		return this;
	}

	/** Set a prepare static method */
	public WebBox setPrepareStaticMethod(String prepareStaticMethod) {
		this.prepareStaticMethod = prepareStaticMethod;
		return this;
	}

	/** Get the Prepare static method name */
	public String getPrepareStaticMethod() {
		return prepareStaticMethod;
	}

	/** Get the prepare bean instance */
	public Object getPrepareBean() {
		return prepareBean;
	}

	/** Set a prepare bean which has a prepare method */
	public WebBox setPrepareBean(Object prepareBean) {
		this.prepareBean = prepareBean;
		return this;
	}

	/** Get the prepare bean method name */
	public String getPrepareBeanMethod() {
		return prepareBeanMethod;
	}

	/** Set the bean prepare method name */
	public WebBox setPrepareBeanMethod(String prepareBeanMethod) {
		this.prepareBeanMethod = prepareBeanMethod;
		return this;
	}

	/** Get the attribute map of WebBox instance */
	public Map<String, Object> getAttributeMap() {
		return attributeMap;
	}

	/** Set the attribute map for WebBox instance */
	public WebBox setAttributeMap(Map<String, Object> attributeMap) {
		this.attributeMap = attributeMap;
		return this;
	}

	/** get the name of the WebBox instance */
	public String getName() {
		return name;
	}

	/** Set the name of the WebBox instance */
	public WebBox setName(String name) {
		this.name = name;
		return this;
	}

	/** Set the father page's WebBox instance */
	public WebBox getFatherWebBox() {
		return fatherWebBox;
	}

	/** Get the father page's WebBox instance */
	public void setFatherWebBox(WebBox fatherWebBox) {
		this.fatherWebBox = fatherWebBox;
	}

	/**
	 * Get current pageContext if have
	 */
	public PageContext getPageContext() {
		return pageContext;
	}

	/**
	 * Set pageContext to it
	 */
	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	/** A runtime exception caused by WebBox */
	public static class WebBoxException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public WebBoxException(String msg) {
			super(msg);
		}

		public WebBoxException(Throwable e) {
			super(e);
		}
	}

	/** This is a custom TagLib for JSP and also can be used in FreeMaker */
	public static class Show extends SimpleTagSupport {
		private String attribute;

		public String getAttribute() {
			return attribute;
		}

		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}

		private Object target;

		public Object getTarget() {
			return target;
		}

		public void setTarget(Object target) {
			this.target = target;
		}

		public void doTag() throws JspException, IOException {
			if (attribute != null && attribute.length() != 0)
				WebBox.showAttribute((PageContext) getJspContext(), getAttribute());
			if (target != null)
				WebBox.showTarget((PageContext) getJspContext(), target);
		}
	}
}
