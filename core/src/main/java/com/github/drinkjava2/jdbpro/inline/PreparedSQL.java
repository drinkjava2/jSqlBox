/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jdbpro.inline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.handler.Wrap;

/**
 * PreparedSQL is a POJO used for store SQL、parameter array、handler array
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class PreparedSQL {
	private String sql;

	private Object[] params;

	private Class<?>[] handlerClasses;

	private ResultSetHandler[] handlers;

	public PreparedSQL() {
		// default Constructor
	}

	public PreparedSQL(String sql, Object[] parameters) {
		this.sql = sql;
		this.params = parameters;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParams() {
		if (params == null)
			return new Object[] {};
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public Class<?>[] getHandlerClasses() {
		return handlerClasses;
	}

	public void setHandlerClasses(Class<?>[] handlerClasses) {
		this.handlerClasses = handlerClasses;
	}

	public ResultSetHandler[] getHandlers() {
		return handlers;
	}

	public void setHandlers(ResultSetHandler<?>[] handlers) {
		this.handlers = handlers;
	}

	public ResultSetHandler getWrappedHandler() {
		List<ResultSetHandler> l = new ArrayList<ResultSetHandler>();
		if (handlerClasses != null && handlerClasses.length != 0)
			try {
				for (int i = 0; i < handlerClasses.length; i++)
					l.add((ResultSetHandler) handlerClasses[i].newInstance());
			} catch (Exception e) {
				throw new DbProRuntimeException(e);
			}
		if (handlers != null && handlers.length != 0)
			try {
				for (int i = 0; i < handlers.length; i++)
					l.add(handlers[i]);
			} catch (Exception e) {
				throw new DbProRuntimeException(e);
			}
		if (l.isEmpty())
			return null;// I don't in charge of check null
		if (l.size() == 1)
			return l.get(0);
		return new Wrap(l.toArray(new ResultSetHandler[l.size()]));
	}

	public String getDebugInfo() {
		return new StringBuffer("SQL: ").append(this.getSql()).append("\nParameters: ")
				.append(Arrays.deepToString(this.getParams())).append("\nHandler Class:")
				.append(Arrays.deepToString(this.getHandlerClasses())).append("\nHandlers:")
				.append(Arrays.deepToString(this.getHandlers())).append("\n").toString();
	}

}
