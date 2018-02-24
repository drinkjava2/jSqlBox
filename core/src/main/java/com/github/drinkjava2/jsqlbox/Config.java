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
package com.github.drinkjava2.jsqlbox;

import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * Config class is used to store constructor parameters for build SqlBoxContext
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
@SuppressWarnings("rawtypes")
public class Config {
	private SqlTemplateEngine templateEngine = SqlBoxContext.getGlobalTemplateEngine();
	private DbProLogger logger = SqlBoxContext.getGlobalLogger();
	private Boolean allowSqlSql = SqlBoxContext.getGlobalAllowShowSql();
	private Dialect dialect = SqlBoxContext.getGlobalDialect();
	private Integer batchSize = SqlBoxContext.getGlobalBatchSize();
	private ConnectionManager connectionManager = SqlBoxContext.getGlobalConnectionManager();
	private List<ResultSetHandler> resultSetHandlers = SqlBoxContext.getGlobalInterceptors();

	public SqlTemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public Config setTemplateEngine(SqlTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
		return this;
	}

	public DbProLogger getLogger() {
		return logger;
	}

	public Config setLogger(DbProLogger logger) {
		this.logger = logger;
		return this;
	}

	public Boolean getAllowSqlSql() {
		return allowSqlSql;
	}

	public Config setAllowSqlSql(Boolean allowSqlSql) {
		this.allowSqlSql = allowSqlSql;
		return this;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public Config setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public Config setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public Config setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		return this;
	}

	public List<ResultSetHandler> getResultSetHandlers() {
		return resultSetHandlers;
	}

	public Config setInterceptors(List<ResultSetHandler> interceptors) {
		this.resultSetHandlers = interceptors;
		return this;
	}

}