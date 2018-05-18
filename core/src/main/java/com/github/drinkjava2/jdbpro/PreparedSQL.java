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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

/**
 * PreparedSQL is a POJO used for store SQL, parameter, ResultSetHandlers,
 * SqlHandlers, Connection and templateEngine..., this is a temporary object
 * prepared for lower layer JDBC tool to access database
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class PreparedSQL {

	/** SQL Operation Type */
	private SqlType type;

	/** Choose use master or slave options */
	private SqlOption masterSlaveOption;

	/** Switch to use another dbPro */
	private DbPro switchTo;

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

	/** Store template parameter map, for #{someName} format */
	private Map<String, Object> templateParamMap;

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

	/** Clone self to get a new PreparedSQL copy, this is a shallow clone */
	public PreparedSQL newCopy() {
		PreparedSQL ps = new PreparedSQL();
		ps.setType(this.type);
		ps.setConnection(this.connection);
		ps.setSql(this.sql);
		ps.setParams(this.params);
		ps.setUseTemplate(this.useTemplate);
		ps.setTemplateEngine(this.templateEngine);
		ps.setTemplateParamMap(this.templateParamMap);
		ps.setSqlHandlers(this.sqlHandlers);
		ps.setResultSetHandler(this.resultSetHandler);
		return ps;
	}

	/**
	 * This is used for debug only
	 */
	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("\ntype=").append(type);
		sb.append("\nsql=").append(sql);
		sb.append("\nparams=").append(Arrays.deepToString(params));
		sb.append("\nmasterSlaveSelect=").append(masterSlaveOption);
		sb.append("\nconnection=").append(connection);
		sb.append("\nresultSetHandler=").append(resultSetHandler);
		sb.append("\nsqlHandlers=").append(sqlHandlers);
		sb.append("\nuseTemplate=").append(useTemplate);
		sb.append("\ntemplateEngine=").append(templateEngine);
		sb.append("\ntemplateParams=").append(templateParamMap);
		sb.append("\n");
		return sb.toString();
	}

	public void addParam(Object param) {
		if (params == null)
			params = new Object[1];
		else {
			Object[] newParams = new Object[params.length + 1];
			System.arraycopy(params, 0, newParams, 0, params.length);
			params = newParams;
		}
		params[params.length - 1] = param;
	}

	/**
	 * Add map content to current template map, if keys already exist will use
	 * new value replace
	 */
	public void addTemplateMap(Map<String, Object> map) {
		if (map == null)
			return;
		if (templateParamMap == null)
			templateParamMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : map.entrySet())
			templateParamMap.put(entry.getKey(), entry.getValue());
	}

	public void addTemplateParam(SqlItem sp) {
		if (sp.getParameters() == null || ((sp.getParameters().length % 2) != 0))
			throw new DbProRuntimeException(
					"Put type template parameter should be key1, value1, key2,value2... format");
		if (templateParamMap == null)
			templateParamMap = new HashMap<String, Object>();
		for (int i = 1; i <= sp.getParameters().length / 2; i++)
			templateParamMap.put((String) sp.getParameters()[(i - 1) * 2], sp.getParameters()[(i - 1) * 2 + 1]);
	}

	public void addSqlHandler(SqlHandler sqlHandler) {
		if (sqlHandlers == null)
			sqlHandlers = new ArrayList<SqlHandler>();
		for (int i = sqlHandlers.size() - 1; i >= 0; i--) {
			if (sqlHandlers.get(i).getOrder() <= sqlHandler.getOrder()) {
				sqlHandlers.add(i + 1, sqlHandler);
				return;
			}
		}
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
	 *            a SqlHandler or ResultSetHandler instance or class
	 * @return true if added
	 */
	@SuppressWarnings("rawtypes")
	public void addHandler(Object handlerOrHandlerClass, IocTool iocTool) {
		if (handlerOrHandlerClass == null)
			throw new DbProRuntimeException("Handler Or Handler class can not be null");
		if (handlerOrHandlerClass instanceof ResultSetHandler)
			setResultSetHandler(((ResultSetHandler) handlerOrHandlerClass));
		else if (handlerOrHandlerClass instanceof SqlHandler)
			addSqlHandler((SqlHandler) handlerOrHandlerClass);
		else if (handlerOrHandlerClass instanceof Class) {
			Class itemClass = (Class) handlerOrHandlerClass;
			try {
				if (ResultSetHandler.class.isAssignableFrom(itemClass))
					setResultSetHandler((ResultSetHandler) itemClass.newInstance());
				else if (SqlHandler.class.isAssignableFrom(itemClass))
					addSqlHandler((SqlHandler) itemClass.newInstance());
				else {
					Object handler = iocTool.getBean((Class<?>) handlerOrHandlerClass);
					addHandler(handler, null);
				}
			} catch (Exception e) {
				throw new DbProRuntimeException(e);
			}
		} else
			throw new DbProRuntimeException("Can not create handler instance for '" + handlerOrHandlerClass + "'");
	}

	public Object[] getParams() {
		if (params == null)
			return new Object[0];
		return params;
	}

	public void addGlobalAndThreadedHandlers(DbPro dbPro) {
		if (dbPro.getSqlHandlers() != null)
			for (SqlHandler handler : dbPro.getSqlHandlers())
				addSqlHandler(handler);
		if (ImprovedQueryRunner.getThreadLocalSqlHandlers() != null) {
			try {
				for (SqlHandler handler : ImprovedQueryRunner.getThreadLocalSqlHandlers())
					addSqlHandler(handler);
			} finally {
				ImprovedQueryRunner.setThreadLocalSqlHandlers((SqlHandler[]) null);
			}
		}
	}

	protected void GetterSetters_________________________() {// NOSONAR
		// === below this line are normal getter && setter======
	}

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

	public Map<String, Object> getTemplateParamMap() {
		return templateParamMap;
	}

	public void setTemplateParamMap(Map<String, Object> templateParamMap) {
		this.templateParamMap = templateParamMap;
	}

	public List<SqlHandler> getSqlHandlers() {
		return sqlHandlers;
	}

	public void setSqlHandlers(List<SqlHandler> sqlHandlers) {
		this.sqlHandlers = sqlHandlers;
	}

	public ResultSetHandler<?> getResultSetHandler() {// NOSONAR
		return resultSetHandler;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public SqlOption getMasterSlaveSelect() {
		return masterSlaveOption;
	}

	public void setMasterSlaveSelect(SqlOption masterSlaveSelect) {
		this.masterSlaveOption = masterSlaveSelect;
	}

	public DbPro getSwitchTo() {
		return switchTo;
	}

	public void setSwitchTo(DbPro switchTo) {
		this.switchTo = switchTo;
	}

}
