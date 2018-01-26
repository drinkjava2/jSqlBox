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
package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.improve.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * Config class is a Java bean store constructor parameters for SqlBoxContext
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class Config {
	private SqlTemplateEngine templateEngine = BasicSqlTemplate.instance();
	private DbProLogger logger = ImprovedQueryRunner.defaultlogger;
	private Boolean allowSqlSql = false;
	private Dialect dialect = null;
	private Integer batchSize = 100;
	private ConnectionManager connectionManager = null;

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

}