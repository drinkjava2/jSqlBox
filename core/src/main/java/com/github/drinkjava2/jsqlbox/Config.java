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

import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.improve.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.improve.SqlInterceptor;
import com.github.drinkjava2.jdbpro.template.NamedParamSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jtinynet.TinyEntityNetFactory;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * Config class is used to store constructor parameters for build SqlBoxContext,
 * please note, there are some setGlobalXxxxx methods should only be called 1
 * REPEAT_TIMES, becuase these methods are used to set global switch variants
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class Config {
	private static SqlTemplateEngine globalTemplateEngine = NamedParamSqlTemplate.instance();
	private static DbProLogger globalLogger = ImprovedQueryRunner.defaultlogger;
	private static Boolean globalAllowSqlSql = false;
	private static Dialect globalDialect = null;
	private static Integer globalBatchSize = 100;
	private static ConnectionManager globalConnectionManager = null;
	private static List<SqlInterceptor> globalInterceptors = null;
	private static EntityNetFactory globalEntityNetFactory = TinyEntityNetFactory.instance;
	private static SqlBoxContext globalSqlBoxContext = null;

	private SqlTemplateEngine templateEngine = globalTemplateEngine;
	private DbProLogger logger = globalLogger;
	private Boolean allowSqlSql = globalAllowSqlSql;
	private Dialect dialect = globalDialect;
	private Integer batchSize = globalBatchSize;
	private ConnectionManager connectionManager = globalConnectionManager;
	private List<SqlInterceptor> interceptors = globalInterceptors;
	private EntityNetFactory entityNetFactory = globalEntityNetFactory;

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

	public List<SqlInterceptor> getInterceptors() {
		return interceptors;
	}

	public Config setInterceptors(List<SqlInterceptor> interceptors) {
		this.interceptors = interceptors;
		return this;
	}

	public EntityNetFactory getEntityNetFactory() {
		return entityNetFactory;
	}

	public Config setEntityNetFactory(EntityNetFactory entityNetFactory) {
		this.entityNetFactory = entityNetFactory;
		return this;
	}

	// ===============below is for global switch===========
	public static SqlTemplateEngine getGlobalTemplateEngine() {
		return globalTemplateEngine;
	}

	public static void setGlobalTemplateEngine(SqlTemplateEngine globalTemplateEngine) {
		Config.globalTemplateEngine = globalTemplateEngine;
	}

	public static DbProLogger getGlobalLogger() {
		return globalLogger;
	}

	public static void setGlobalLogger(DbProLogger globalLogger) {
		Config.globalLogger = globalLogger;
	}

	public static Boolean getGlobalAllowSqlSql() {
		return globalAllowSqlSql;
	}

	public static void setGlobalAllowSqlSql(Boolean globalAllowSqlSql) {
		Config.globalAllowSqlSql = globalAllowSqlSql;
	}

	public static Dialect getGlobalDialect() {
		return globalDialect;
	}

	public static void setGlobalDialect(Dialect globalDialect) {
		Config.globalDialect = globalDialect;
	}

	public static Integer getGlobalBatchSize() {
		return globalBatchSize;
	}

	public static void setGlobalBatchSize(Integer globalBatchSize) {
		Config.globalBatchSize = globalBatchSize;
	}

	public static ConnectionManager getGlobalConnectionManager() {
		return globalConnectionManager;
	}

	public static void setGlobalConnectionManager(ConnectionManager globalConnectionManager) {
		Config.globalConnectionManager = globalConnectionManager;
	}

	public static List<SqlInterceptor> getGlobalInterceptors() {
		return globalInterceptors;
	}

	public static void setGlobalInterceptors(List<SqlInterceptor> globalInterceptors) {
		Config.globalInterceptors = globalInterceptors;
	}

	public static EntityNetFactory getGlobalEntityNetFactory() {
		return globalEntityNetFactory;
	}

	public static void setGlobalEntityNetFactory(EntityNetFactory globalEntityNetFactory) {
		Config.globalEntityNetFactory = globalEntityNetFactory;
	}

	/** Get the global SqlBoxContext */
	public static SqlBoxContext getGlobalSqlBoxContext() {
		return globalSqlBoxContext;
	}

	/** Get the global SqlBoxContext, equal to getGlobalSqlBoxContext() method */
	public static SqlBoxContext ctx() {// NOSONAR
		return globalSqlBoxContext;
	}

	/**
	 * Set a global SqlBoxContext instance, if an ActiveRecord object did not set
	 * SqlBoxContext property, it will use this global SqlBoxContext instance to do
	 * JDBC operation
	 */
	public static void setGlobalSqlBoxContext(SqlBoxContext globalSqlBoxContext) {
		Config.globalSqlBoxContext = globalSqlBoxContext;
	}

}