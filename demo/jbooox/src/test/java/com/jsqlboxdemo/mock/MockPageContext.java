/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.jsqlboxdemo.mock;

import java.io.IOException;
import java.util.Enumeration;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * Mock class of PageContext for unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class MockPageContext extends PageContext {
	MockRequest request = new MockRequest();
	MockJspWriter out = new MockJspWriter(0, true);

	@Override
	public ServletRequest getRequest() { 
		return request;
	}

	public MockRequest getMockRequest() {
		return request;
	}

	public void setRequestURI(String requestURI) {
		request.setRequestURI(requestURI);
	}

	public void setRequestAttribute(String key, Object value) {
		request.setAttribute(key, value);
	}

	public Object getRequestAttribute(String key) {
		return request.getAttribute(key);
	}

	public void setRequestMethod(String method) {
		request.setMethod(method);
	}

	public void setPathParams(String... params) {
		request.setAttribute("pathParams", params);
	}

	@Override
	public JspWriter getOut() {
		return new MockJspWriter(0, true);
	}

	// ===============================================================
	// ====================garbage code below=========================
	// ===============================================================

	@Override
	public void forward(String arg0) throws ServletException, IOException {

	}

	@Override
	public Exception getException() {

		return null;
	}

	@Override
	public Object getPage() {

		return null;
	}

	@Override
	public ServletResponse getResponse() {

		return null;
	}

	@Override
	public ServletConfig getServletConfig() {

		return null;
	}

	@Override
	public ServletContext getServletContext() {

		return null;
	}

	@Override
	public HttpSession getSession() {

		return null;
	}

	@Override
	public void handlePageException(Exception arg0) throws ServletException, IOException {

	}

	@Override
	public void handlePageException(Throwable arg0) throws ServletException, IOException {

	}

	@Override
	public void include(String arg0) throws ServletException, IOException {

	}

	@Override
	public void include(String arg0, boolean arg1) throws ServletException, IOException {

	}

	@Override
	public void initialize(Servlet arg0, ServletRequest arg1, ServletResponse arg2, String arg3, boolean arg4, int arg5,
			boolean arg6) throws IOException, IllegalStateException, IllegalArgumentException {

	}

	@Override
	public void release() {

	}

	@Override
	public Object findAttribute(String arg0) {

		return null;
	}

	@Override
	public Object getAttribute(String arg0) {

		return null;
	}

	@Override
	public Object getAttribute(String arg0, int arg1) {

		return null;
	}

	@Override
	public Enumeration<String> getAttributeNamesInScope(int arg0) {

		return null;
	}

	@Override
	public int getAttributesScope(String arg0) {

		return 0;
	}

	@Override
	public ELContext getELContext() {

		return null;
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {

		return null;
	}

	@Override
	public VariableResolver getVariableResolver() {

		return null;
	}

	@Override
	public void removeAttribute(String arg0) {

	}

	@Override
	public void removeAttribute(String arg0, int arg1) {

	}

	@Override
	public void setAttribute(String arg0, Object arg1) {

	}

	@Override
	public void setAttribute(String arg0, Object arg1, int arg2) {

	}
}