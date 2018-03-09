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
package com.github.drinkjava2.jdbpro;

import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * SqlBoxContextConfig class is used to store constructor parameters for build
 * SqlBoxContext
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
@SuppressWarnings("rawtypes")
public class DbProConfig {
	private SqlTemplateEngine templateEngine = DbPro.getGlobalTemplateEngine();
	private DbProLogger logger = DbPro.getGlobalLogger();
	private Boolean allowSqlSql = DbPro.getGlobalAllowShowSql();
	private Integer batchSize = DbPro.getGlobalBatchSize();
	private ConnectionManager connectionManager = DbPro.getGlobalConnectionManager();
	private List<ResultSetHandler> handlers = DbPro.getGlobalHandlers();

	public SqlTemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public void setTemplateEngine(SqlTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public DbProLogger getLogger() {
		return logger;
	}

	public void setLogger(DbProLogger logger) {
		this.logger = logger;
	}

	public Boolean getAllowSqlSql() {
		return allowSqlSql;
	}

	public void setAllowSqlSql(Boolean allowSqlSql) {
		this.allowSqlSql = allowSqlSql;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public List<ResultSetHandler> getHandlers() {
		return handlers;
	}

	public void setHandlers(List<ResultSetHandler> handlers) {
		this.handlers = handlers;
	}

}