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
package com.github.drinkjava2.jdbpro;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

/**
 * PreparedSQL2 is a POJO used for store SQL、parameter、ResultSetHandlers,
 * Connection, this is a temporary object only used for query method.
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public class PreparedSQL {

	/** SQL Operation Type */
	private SqlType type;

	/** Optional,Connection instance */
	private Connection connection;

	/** The SQL String */
	private String sql;

	/** Optional,The SQL parameters */
	private Object[] params;

	/** If set true, will use templateEngine to render SQL */
	private Boolean useTemplate = false;

	/** Optional, store a SqlTemplateEngine used only for this PreparedSQL */
	private SqlTemplateEngine templateEngine;

	private Map<String, Object> templateParams;

	/** Optional,SqlHandler instance list */
	private List<SqlHandler> sqlHandlers;

	/** Optional,ResultSetHandler instance */
	private ResultSetHandler<?> resultSetHandler;

	public PreparedSQL() {// default constructor
	}

	public PreparedSQL(SqlType type, Connection conn, ResultSetHandler<?> rsh, String sql, Object... params) {
		this.type = type;
		this.connection = conn;
		this.resultSetHandler = rsh;
		this.sql = sql;
		this.params = params;
	}

	/** Clone self to get a new PreparedSQL copy */
	public PreparedSQL newCopy() {
		PreparedSQL ps = new PreparedSQL();
		ps.setType(this.type);
		ps.setConnection(this.connection);
		ps.setSql(this.sql);
		ps.setParams(this.params);
		ps.setUseTemplate(this.useTemplate);
		ps.setTemplateEngine(this.templateEngine);
		ps.setTemplateParams(this.templateParams);
		ps.setSqlHandlers(this.sqlHandlers);
		ps.setResultSetHandler(this.resultSetHandler);
		return ps;
	}

	public void addParam(Object param) {
		if (params == null)
			params = new Object[1];
		else
			params = new Object[params.length + 1];
		params[params.length - 1] = param;
	}

	public void addSqlHandler(SqlHandler sqlHandler) {
		if (sqlHandlers == null)
			sqlHandlers = new ArrayList<SqlHandler>();
		sqlHandlers.add(sqlHandler);
	}

	public int getParamSize() {
		if (params == null)
			return 0;
		return params.length;
	}

	public void setResultSetHandler(ResultSetHandler<?> rsh) {
		if (this.resultSetHandler != null)
			throw new DbProRuntimeException(
					"ResultSetHandler already exist and can only set 1, need use changeResultSetHandler method.");
		this.resultSetHandler = rsh;
	}

	public void changeResultSetHandler(ResultSetHandler<?> rsh) {
		this.resultSetHandler = rsh;
	}

	/**
	 * @param handlerOrHandlerClass
	 *            a SqlHandler or ResultHandler or a class of them
	 * @return true if added
	 */
	@SuppressWarnings("rawtypes")
	public boolean addHandler(Object handlerOrHandlerClass) {
		if (handlerOrHandlerClass == null)
			throw new DbProRuntimeException("Handler Or Handler class can not be null");
		if (handlerOrHandlerClass instanceof ResultSetHandler)
			setResultSetHandler(((ResultSetHandler) handlerOrHandlerClass));
		else if (handlerOrHandlerClass instanceof SqlHandler)
			addSqlHandler((SqlHandler) handlerOrHandlerClass);
		else if (handlerOrHandlerClass instanceof Class) {
			Class itemClass = (Class) handlerOrHandlerClass;
			try {
				if (ResultSetHandler.class.isAssignableFrom(itemClass)) {
					setResultSetHandler((ResultSetHandler) itemClass.newInstance());
					return true;
				}
				if (SqlHandler.class.isAssignableFrom(itemClass)) {
					addSqlHandler((SqlHandler) itemClass.newInstance());
					return true;
				}
			} catch (Exception e) {
				throw new DbProRuntimeException(e);
			}
			return false;
		} else
			return false;
		return true;
	}

	// === Normal getter && setter======

	public SqlType getType() {
		return type;
	}

	public void setType(SqlType type) {
		this.type = type;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<SqlHandler> getSqlHandlers() {
		return sqlHandlers;
	}

	public void setSqlHandlers(List<SqlHandler> sqlHandlers) {
		this.sqlHandlers = sqlHandlers;
	}

	public ResultSetHandler<?> getResultSetHandler() {
		return resultSetHandler;
	}

	public Boolean getUseTemplate() {
		return useTemplate;
	}

	public void setUseTemplate(Boolean useTemplate) {
		this.useTemplate = useTemplate;
	}

	public SqlTemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public void setTemplateEngine(SqlTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public Object[] getParams() {
		if (params == null)
			return new Object[0];
		return params;
	}

	public void setParams(Object... params) {
		this.params = params;
	}

	public Map<String, Object> getTemplateParams() {
		return templateParams;
	}

	public void setTemplateParams(Map<String, Object> templateParams) {
		this.templateParams = templateParams;
	}

}
