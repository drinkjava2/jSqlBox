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

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

/**
 * PreparedSQL2 is a POJO used for store SQL、parameter、ResultSetHandlers,
 * Connection, this is a temporary object only used for query method
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class PreparedSQL {

	/** SQL Operation Type */
	private SqlType type;

	/** Optional,Connection instance */
	private Connection connection;

	/** The SQL String */
	private String sql;

	/** Optional,The SQL parameters */
	private List<Object> params;

	/** If set true, will use templateEngine to render SQL */
	private Boolean useTemplate = false;

	/** Optional, store a return result */
	private SqlTemplateEngine templateEngine;

	/** Optional,SqlHandler instance list */
	private List<SqlHandler> sqlHandlers;

	/** Optional,ResultSetHandler instance */
	private ResultSetHandler<?> resultSetHandler;

	public void addParam(Object param) {
		if (params == null)
			params = new ArrayList<Object>();
		params.add(param);
	}

	public int getParamSize() {
		if (params == null)
			return 0;
		return params.size();
	}

	public void setResultSetHandler(ResultSetHandler<?> resultSetHandler) {
		if (this.resultSetHandler != null)
			throw new DbProRuntimeException(
					"ResultSetHandler already exist and can only set 1, please use changeResultSetHandler method");
		this.resultSetHandler = resultSetHandler;
	}

	public void changeResultSetHandler(ResultSetHandler<?> resultSetHandler) {
		this.resultSetHandler = resultSetHandler;
	}

	public void setParams(Object... args) {
		this.params = new ArrayList<Object>();
		for (Object obj : args)
			params.add(obj);
	}

	public Object[] getParamArray() {
		if (params == null)
			return new Object[0];
		return params.toArray();
	}

	// === Plan getter && setter======

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

	public void setListParams(List<Object> params) {
		this.params = params;
	}

	public List<Object> getListParams() {
		return params;
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

}
