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
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

/**
 * PreparedSQL is a temporary object used for store SQL, parameter,
 * ResultSetHandlers, SqlHandlers, Connection and templateEngine..., it's not
 * thread-safe
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("rawtypes")
public class PreparedSQL {

	private StringBuilder sqlBuilder = new StringBuilder();

	/** SQL Operation Type */
	private SqlOption operationType;

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
	private Boolean useTemplate = null;

	/** Optional, store a SqlTemplateEngine used only for this PreparedSQL */
	private SqlTemplateEngine templateEngine;

	/** Store template parameter map, for #{someName} format */
	private Map<String, Object> templateParamMap;

	/** Optional,SqlHandler instance list */
	private List<SqlHandler> sqlHandlers;

	/** Optional,ResultSetHandler instance, only allow have one */
	private ResultSetHandler<?> resultSetHandler;

	/** Handers in this list will disabled */
	private List<Class<?>> disabledHandlers;

	/** Store "SqlOption.Other" type SqlItem */
	private List<SqlItem> others = null;

	/**
	 * Designed for ORM program, if set true will ignore fields with null value in
	 * insert & update methods
	 */
	private Boolean ignoreNull = null;

	/**
	 * Designed for ORM program, if set true will ignore fields with null value or
	 * Empty String value in insert & update methods
	 */
	private Boolean ignoreEmpty = null;

	/** TableModels, this is designed for ORM program */
	private Object[] models;

	/** Alias of TableModels, this is designed for ORM Program */
	private String[] aliases;

	/** Give List, this is designed for ORM program's EntityNet */
	private List<String[]> givesList = null;

	/** EntityNet, this is designed for ORM program's EntityNet */
	private Object entityNet = null;

	public PreparedSQL() {// default constructor
	}

	public PreparedSQL(SqlOption type, Connection conn, ResultSetHandler<?> rsh, String sql, Object... params) {
		this.operationType = type;
		this.connection = conn;
		this.resultSetHandler = rsh;
		this.sql = sql;
		this.params = params;
	}

	/**
	 * This is used for debug only
	 */
	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("\ntype=").append(operationType);
		sb.append("\nsql=").append(sql);
		sb.append("\nparams=").append(Arrays.deepToString(params));
		sb.append("\nmasterSlaveSelect=").append(masterSlaveOption);
		sb.append("\nconnection=").append(connection);
		sb.append("\nresultSetHandler=").append(resultSetHandler);
		sb.append("\nsqlHandlers=").append(sqlHandlers);
		sb.append("\nuseTemplate=").append(useTemplate);
		sb.append("\ntemplateEngine=").append(templateEngine);
		sb.append("\ntemplateParams=").append(templateParamMap);
		sb.append("\ndisabledHandlers=").append(disabledHandlers);
		sb.append("\nmasterSlaveOption=").append(masterSlaveOption);
		sb.append("\n");
		return sb.toString();
	}

	public StringBuilder addSql(Object sqlPiece) {
		sqlBuilder.append(sqlPiece);
		return sqlBuilder;
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

	public void addModel(Object model) {
		if (models == null) {
			models = new Object[1];
			aliases = new String[1];
		} else {
			Object[] newModels = new Object[models.length + 1];
			System.arraycopy(models, 0, newModels, 0, models.length);
			models = newModels;
			String[] newAliases = new String[aliases.length + 1];
			System.arraycopy(aliases, 0, newAliases, 0, aliases.length);
			aliases = newAliases;
		}
		models[models.length - 1] = model;
	}

	public void setLastAliases(String... alias) {
		for (int i = 0; i < alias.length; i++) {
			aliases[models.length - alias.length + i] = alias[i];
		}
	}

	public void addGives(String[] gives) {
		if (givesList == null)
			givesList = new ArrayList<String[]>();
		if (gives == null || gives.length < 2)
			throw new DbProException("addGives at least need 2 alias parameters");
		givesList.add(gives);
	}

	public void giveBoth(String alias1, String alias2) {
		if (givesList == null)
			givesList = new ArrayList<String[]>();
		givesList.add(new String[] { alias1, alias2 });
		givesList.add(new String[] { alias2, alias1 });
	}

	/**
	 * Add map content to current template map, if keys already exist will use new
	 * value replace
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
			throw new DbProException("Put type template parameter should be key1, value1, key2,value2... format");
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

	public void disableHandlers(Object[] handlersClass) {
		if (handlersClass == null || handlersClass.length == 0)
			throw new DbProException("disableHandlers method need at least 1 parameter");
		if (disabledHandlers == null)
			disabledHandlers = new ArrayList<Class<?>>();
		for (Object obj : handlersClass)
			disabledHandlers.add((Class<?>) obj);
	}

	public boolean isDisabledHandler(Object handler) {
		if (disabledHandlers == null || disabledHandlers.isEmpty())
			return false;
		for (Class<?> disabled : disabledHandlers)
			if (disabled.equals(handler.getClass()))
				return true;
		return false;
	}

	public void enableAllHandlers() {
		disabledHandlers = null;
	}

	public int getParamSize() {
		if (params == null)
			return 0;
		return params.length;
	}

	public void setResultSetHandler(ResultSetHandler<?> rsh) {
		if (this.resultSetHandler != null)
			throw new DbProException(
					"ResultSetHandler already exist and can only set 1, need use changeResultSetHandler method.");
		this.resultSetHandler = rsh;
	}

	public void changeResultSetHandler(ResultSetHandler<?> rsh) {
		this.resultSetHandler = rsh;
	}

	public void addHandler(ResultSetHandler handler) {
		setResultSetHandler(handler);
	}

	public void addHandler(SqlHandler handler) {
		addSqlHandler(handler);
	}

	public void addNoParamHandlerByClass(Class handlerClass) {
		if (handlerClass == null)
			throw new DbProException("HandlerClass can not be null");
		try {
			if (ResultSetHandler.class.isAssignableFrom(handlerClass))
				setResultSetHandler((ResultSetHandler) handlerClass.newInstance());
			else if (SqlHandler.class.isAssignableFrom(handlerClass))
				addSqlHandler((SqlHandler) handlerClass.newInstance());
			else
				throw new DbProException("ResultSetHandler class or SqlHandler class required");
		} catch (Exception e) {
			throw new DbProException(e);
		}
	}

	public Object[] getParams() {
		if (params == null)
			return new Object[0];
		return params;
	}

	public void addGlobalAndThreadedHandlers(ImprovedQueryRunner dbPro) {
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

	/** If current type is null, set with new type value */
	public void ifNullSetType(SqlOption type) {
		if (this.operationType == null)
			this.operationType = type;
	}

	/** If current type is null, set with new type value */
	public void ifNullSetUseTemplate(Boolean useTemplate) {
		if (this.useTemplate == null)
			this.useTemplate = useTemplate;
	}

	public void addOther(SqlItem obj) {
		if (others == null)
			others = new ArrayList<SqlItem>();
		others.add(obj);
	}

	/** if InlineStyle=true or SQL is empty, add as SQL, else add as parameter */
	public void addSqlOrParam(boolean inlineStyle, String item) {
		if (inlineStyle)
			addSql(item);
		else if (getSqlBuilder().length() > 0)
			addParam(item);
		else
			addSql(item);
	}

	protected void GetterSetters_________________________() {// NOSONAR
		// === below this line are normal getter && setter======
	}

	public SqlOption getOperationType() {
		return operationType;
	}

	public void setOperationType(SqlOption operationType) {
		this.operationType = operationType;
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
		if (templateParamMap == null)
			return new HashMap<String, Object>();
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

	public void setMasterSlaveOption(SqlOption masterSlaveOption) {
		this.masterSlaveOption = masterSlaveOption;
	}

	public DbPro getSwitchTo() {
		return switchTo;
	}

	public void setSwitchTo(DbPro switchTo) {
		this.switchTo = switchTo;
	}

	public SqlOption getMasterSlaveOption() {
		return masterSlaveOption;
	}

	public List<Class<?>> getDisabledHandlers() {
		return disabledHandlers;
	}

	public void setDisabledHandlers(List<Class<?>> disabledHandlers) {
		this.disabledHandlers = disabledHandlers;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	public Object[] getModels() {
		return models;
	}

	public void setModels(Object[] models) {
		this.models = models;
	}

	public List<String[]> getGivesList() {
		return givesList;
	}

	public void setGivesList(List<String[]> givesList) {
		this.givesList = givesList;
	}

	public Object getEntityNet() {
		return entityNet;
	}

	public void setEntityNet(Object entityNet) {
		this.entityNet = entityNet;
	}

	public List<SqlItem> getOthers() {
		return others;
	}

	public void setOthers(List<SqlItem> others) {
		this.others = others;
	}

	public StringBuilder getSqlBuilder() {
		return sqlBuilder;
	}

	public void setSqlBuilder(StringBuilder sqlBuilder) {
		this.sqlBuilder = sqlBuilder;
	}

	public Boolean getIgnoreNull() {
		return ignoreNull;
	}

	public void setIgnoreNull(Boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

	public Boolean getIgnoreEmpty() {
		return ignoreEmpty;
	}

	public void setIgnoreEmpty(Boolean ignoreEmpty) {
		this.ignoreEmpty = ignoreEmpty;
	}

	public String wrapColumn(String col) {
		return isMysql() ? "`" + col + "`" : col;
	}
	private boolean isMysql() {
		try {
			DatabaseMetaData metaData            = connection.getMetaData();
			String           databaseProductName = metaData.getDatabaseProductName();
			return "MySQL".equals(databaseProductName);
		} catch (Throwable e) {
			return false;
		}
	}
}
